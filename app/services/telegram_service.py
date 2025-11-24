"""Telegram client service for async authentication and session management."""

import asyncio
import os
import json
from typing import Optional, Dict, Any
from datetime import datetime
import uuid
from asyncio import Event, wait_for, TimeoutError as AsyncTimeoutError

try:
    from telegram.client import Telegram
except ImportError as e:
    raise ImportError(
        "Failed to import telegram.client. "
        "Please ensure python-telegram is installed: pip install python-telegram"
    ) from e


class TelegramSession:
    """Represents a Telegram session with metadata."""
    
    def __init__(
        self,
        session_id: str,
        api_id: str,
        api_hash: str,
        phone: str,
        database_encryption_key: str,
        session_dir: str,
    ):
        self.session_id = session_id
        self.api_id = api_id
        self.api_hash = api_hash
        self.phone = phone
        self.database_encryption_key = database_encryption_key
        self.session_dir = session_dir
        self.created_at = datetime.now()
        self.last_used = datetime.now()
        self.is_authorized = False
        self._client: Optional[Telegram] = None
        self._authorization_state = None
        self._state_changed_event = Event()
        
    def get_session_path(self) -> str:
        """Get the path for this session's data."""
        return os.path.join(self.session_dir, self.session_id)
    
    def _update_handler(self, update):
        """Handle updates from TDLib."""
        if update.get("@type") == "updateAuthorizationState":
            self._authorization_state = update.get("authorization_state")
            self._state_changed_event.set()
    
    def initialize_client(self):
        """Initialize the Telegram client."""
        if self._client is None:
            self._client = Telegram(
                api_id=self.api_id,
                api_hash=self.api_hash,
                phone=self.phone,
                database_encryption_key=self.database_encryption_key,
                files_directory=self.get_session_path(),
            )
            # Register update handler for auth state changes
            self._client.add_update_handler("updateAuthorizationState", self._update_handler)
        return self._client
    
    async def wait_for_state_change(self, timeout: float = 5.0) -> Optional[Dict[str, Any]]:
        """Wait for authorization state to change."""
        try:
            await wait_for(self._state_changed_event.wait(), timeout=timeout)
            self._state_changed_event.clear()
            return self._authorization_state
        except AsyncTimeoutError:
            return None
    
    def get_client(self) -> Optional[Telegram]:
        """Get the Telegram client instance."""
        return self._client
        return self._client
    
    def update_last_used(self):
        """Update the last used timestamp."""
        self.last_used = datetime.now()
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert session to dictionary."""
        return {
            "session_id": self.session_id,
            "phone": self.phone,
            "is_authorized": self.is_authorized,
            "created_at": self.created_at.isoformat(),
            "last_used": self.last_used.isoformat(),
        }
    
    async def stop(self):
        """Stop the Telegram client."""
        if self._client:
            try:
                await self._client.stop()
            except Exception:
                pass
            self._client = None


class TelegramService:
    """Service for managing Telegram authentication and sessions."""
    
    def __init__(self, session_dir: str = "./sessions"):
        self.session_dir = session_dir
        self.sessions: Dict[str, TelegramSession] = {}
        self._ensure_session_dir()
    
    def _ensure_session_dir(self):
        """Ensure the session directory exists."""
        os.makedirs(self.session_dir, exist_ok=True)
    
    def _generate_session_id(self, session_name: Optional[str] = None) -> str:
        """Generate a unique session ID."""
        if session_name:
            return f"session_{session_name}"
        return f"session_{uuid.uuid4().hex[:12]}"
    
    async def create_session(
        self,
        api_id: str,
        api_hash: str,
        phone: str,
        database_encryption_key: str,
        session_name: Optional[str] = None,
    ) -> TelegramSession:
        """Create a new Telegram session."""
        session_id = self._generate_session_id(session_name)
        
        # Check if session already exists
        if session_id in self.sessions:
            return self.sessions[session_id]
        
        session = TelegramSession(
            session_id=session_id,
            api_id=api_id,
            api_hash=api_hash,
            phone=phone,
            database_encryption_key=database_encryption_key,
            session_dir=self.session_dir,
        )
        
        self.sessions[session_id] = session
        return session
    
    async def login(self, session: TelegramSession) -> Dict[str, Any]:
        """
        Initiate login process for a session.
        Returns the current authorization state.
        """
        client = session.initialize_client()
        
        # Get initial authorization state (returns AsyncResult, need to wait for it)
        result_obj = client.get_authorization_state()
        result_obj.wait(timeout=5)
        result = result_obj.update
        session._authorization_state = result
        
        # Handle authorizationStateWaitTdlibParameters - send TDLib configuration
        if result and result.get("@type") == "authorizationStateWaitTdlibParameters":
            # Set TDLib parameters
            params = {
                "database_directory": session.get_session_path(),
                "files_directory": session.get_session_path(),
                "use_test_dc": False,
                "use_file_database": True,
                "use_chat_info_database": True,
                "use_message_database": True,
                "use_secret_chats": False,
                "api_id": int(session.api_id),
                "api_hash": session.api_hash,
                "system_language_code": "en",
                "device_model": "Server",
                "system_version": "1.0",
                "application_version": "1.0",
            }
            
            client.call_method("setTdlibParameters", params=params, block=True)
            
            # Wait for state to change
            result = await session.wait_for_state_change(timeout=10.0)
            if result is None:
                result_obj = client.get_authorization_state()
                result_obj.wait(timeout=5)
                result = result_obj.update
            session._authorization_state = result
        
        # Handle authorizationStateWaitEncryptionKey - send database encryption key
        if result and result.get("@type") == "authorizationStateWaitEncryptionKey":
            # Check if database is encrypted
            is_encrypted = result.get("is_encrypted", False)
            
            # Send encryption key
            client.call_method(
                "checkDatabaseEncryptionKey",
                params={"encryption_key": session.database_encryption_key},
                block=True
            )
            
            # Wait for state to change
            result = await session.wait_for_state_change(timeout=10.0)
            if result is None:
                result_obj = client.get_authorization_state()
                result_obj.wait(timeout=5)
                result = result_obj.update
            session._authorization_state = result
        
        # If waiting for phone number, send it and wait for state change
        if result and result.get("@type") == "authorizationStateWaitPhoneNumber":
            # Send phone number using call_method
            client.call_method(
                "setAuthenticationPhoneNumber",
                params={"phone_number": session.phone},
                block=True
            )
            
            # Wait for state change event
            result = await session.wait_for_state_change(timeout=10.0)
            if result is None:
                # Fallback to polling if event wasn't received
                result_obj = client.get_authorization_state()
                result_obj.wait(timeout=5)
                result = result_obj.update
            session._authorization_state = result
        
        session.update_last_used()
        
        # Check current authorization state and return appropriate response
        if result and result.get("@type") == "authorizationStateWaitCode":
            return {
                "status": "awaiting_code",
                "message": "Authentication code has been sent to your Telegram app",
                "session_id": session.session_id,
            }
        elif result and result.get("@type") == "authorizationStateWaitPassword":
            return {
                "status": "awaiting_password",
                "message": "Two-factor authentication password required",
                "session_id": session.session_id,
            }
        elif result and result.get("@type") == "authorizationStateReady":
            session.is_authorized = True
            return {
                "status": "authorized",
                "message": "Successfully authorized",
                "session_id": session.session_id,
            }
        elif result and result.get("@type") == "authorizationStateWaitPhoneNumber":
            return {
                "status": "awaiting_phone",
                "message": "Phone number required for authentication",
                "session_id": session.session_id,
            }
        else:
            return {
                "status": "unknown",
                "message": f"Authorization state: {result.get('@type', 'unknown') if result else 'no_state'}",
                "session_id": session.session_id,
                "state": result,
            }
    
    async def submit_code(self, session_id: str, code: str) -> Dict[str, Any]:
        """Submit authentication code for a session."""
        session = self.sessions.get(session_id)
        if not session:
            raise ValueError(f"Session {session_id} not found")
        
        client = session.get_client()
        if not client:
            raise ValueError("Client not initialized")
        
        session.update_last_used()
        
        # Submit the code
        try:
            # Send the authentication code
            client.send_code(code)
            
            # Wait for state change event
            result = await session.wait_for_state_change(timeout=10.0)
            if result is None:
                # Fallback to polling if event wasn't received
                result_obj = client.get_authorization_state()
                result_obj.wait(timeout=5)
                result = result_obj.update
            
            session._authorization_state = result
            
            if result and result.get("@type") == "authorizationStateWaitPassword":
                return {
                    "status": "awaiting_password",
                    "message": "Two-factor authentication password required",
                }
            elif result and result.get("@type") == "authorizationStateReady":
                session.is_authorized = True
                return {
                    "status": "authorized",
                    "message": "Successfully authorized",
                }
            else:
                return {
                    "status": "unknown",
                    "message": f"Current state: {result.get('@type', 'unknown') if result else 'no_state'}",
                    "state": result,
                }
        except Exception as e:
            return {
                "status": "error",
                "message": f"Error submitting code: {str(e)}",
            }
    
    async def submit_password(self, session_id: str, password: str) -> Dict[str, Any]:
        """Submit 2FA password for a session."""
        session = self.sessions.get(session_id)
        if not session:
            raise ValueError(f"Session {session_id} not found")
        
        client = session.get_client()
        if not client:
            raise ValueError("Client not initialized")
        
        session.update_last_used()
        
        try:
            # Send the 2FA password
            client.send_password(password)
            
            # Wait for state change event
            result = await session.wait_for_state_change(timeout=10.0)
            if result is None:
                # Fallback to polling if event wasn't received
                result_obj = client.get_authorization_state()
                result_obj.wait(timeout=5)
                result = result_obj.update
            
            session._authorization_state = result
            
            if result and result.get("@type") == "authorizationStateReady":
                session.is_authorized = True
                return {
                    "status": "authorized",
                    "message": "Successfully authorized with 2FA",
                }
            else:
                return {
                    "status": "unknown",
                    "message": f"Current state: {result.get('@type', 'unknown') if result else 'no_state'}",
                    "state": result,
                }
        except Exception as e:
            return {
                "status": "error",
                "message": f"Error submitting password: {str(e)}",
            }
    
    def get_session(self, session_id: str) -> Optional[TelegramSession]:
        """Get a session by ID."""
        return self.sessions.get(session_id)
    
    def list_sessions(self) -> list[Dict[str, Any]]:
        """List all active sessions."""
        return [session.to_dict() for session in self.sessions.values()]
    
    async def close_session(self, session_id: str) -> bool:
        """Close and remove a session."""
        session = self.sessions.get(session_id)
        if session:
            await session.stop()
            del self.sessions[session_id]
            return True
        return False
    
    async def close_all_sessions(self):
        """Close all sessions."""
        for session in list(self.sessions.values()):
            await session.stop()
        self.sessions.clear()


# Global service instance
_telegram_service: Optional[TelegramService] = None


def get_telegram_service(session_dir: str = "./sessions") -> TelegramService:
    """Get or create the global Telegram service instance."""
    global _telegram_service
    if _telegram_service is None:
        _telegram_service = TelegramService(session_dir=session_dir)
    return _telegram_service
