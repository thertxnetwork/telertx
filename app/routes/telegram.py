"""FastAPI routes for Telegram authentication."""

from fastapi import APIRouter, HTTPException, Depends
from app.models import (
    TelegramLoginRequest,
    TelegramCodeRequest,
    TelegramPasswordRequest,
    LoginResponse,
    SessionInfo,
    SessionListResponse,
)
from app.services import get_telegram_service, TelegramService
from app.config import get_settings, Settings


router = APIRouter(prefix="/api/v1/telegram", tags=["telegram"])


def get_service(settings: Settings = Depends(get_settings)) -> TelegramService:
    """Dependency to get Telegram service."""
    return get_telegram_service(session_dir=settings.session_dir)


@router.post("/login", response_model=LoginResponse)
async def login(
    request: TelegramLoginRequest,
    service: TelegramService = Depends(get_service),
):
    """
    Initiate Telegram login process.
    
    This endpoint creates a new session and initiates the login process.
    Returns the session ID and status indicating what step comes next
    (e.g., awaiting_code, awaiting_password, or authorized).
    """
    try:
        # Create session
        session = await service.create_session(
            api_id=request.api_id,
            api_hash=request.api_hash,
            phone=request.phone,
            database_encryption_key=request.database_encryption_key,
            session_name=request.session_name,
        )
        
        # Initiate login
        result = await service.login(session)
        
        return LoginResponse(
            success=True,
            message=result.get("message", "Login initiated"),
            session_id=result.get("session_id"),
            status=result.get("status", "unknown"),
            data=result,
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to initiate login: {str(e)}"
        )


@router.post("/submit-code", response_model=LoginResponse)
async def submit_code(
    request: TelegramCodeRequest,
    service: TelegramService = Depends(get_service),
):
    """
    Submit authentication code received via Telegram.
    
    After initiating login, Telegram sends a code to your device.
    Use this endpoint to submit that code and continue the authentication process.
    """
    try:
        result = await service.submit_code(
            session_id=request.session_id,
            code=request.code,
        )
        
        return LoginResponse(
            success=True,
            message=result.get("message", "Code submitted"),
            session_id=request.session_id,
            status=result.get("status", "unknown"),
            data=result,
        )
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to submit code: {str(e)}"
        )


@router.post("/submit-password", response_model=LoginResponse)
async def submit_password(
    request: TelegramPasswordRequest,
    service: TelegramService = Depends(get_service),
):
    """
    Submit two-factor authentication password.
    
    If your account has 2FA enabled, you'll need to submit your password
    after the authentication code to complete the login process.
    """
    try:
        result = await service.submit_password(
            session_id=request.session_id,
            password=request.password,
        )
        
        return LoginResponse(
            success=True,
            message=result.get("message", "Password submitted"),
            session_id=request.session_id,
            status=result.get("status", "unknown"),
            data=result,
        )
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to submit password: {str(e)}"
        )


@router.get("/sessions", response_model=SessionListResponse)
async def list_sessions(
    service: TelegramService = Depends(get_service),
):
    """
    List all active Telegram sessions.
    
    Returns information about all currently active sessions including
    session IDs, phone numbers, authorization status, and timestamps.
    """
    try:
        sessions = service.list_sessions()
        return SessionListResponse(
            sessions=sessions,
            total=len(sessions),
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to list sessions: {str(e)}"
        )


@router.get("/sessions/{session_id}", response_model=SessionInfo)
async def get_session(
    session_id: str,
    service: TelegramService = Depends(get_service),
):
    """
    Get information about a specific session.
    
    Returns detailed information about a session including its
    authorization status and usage timestamps.
    """
    try:
        session = service.get_session(session_id)
        if not session:
            raise HTTPException(
                status_code=404,
                detail=f"Session {session_id} not found"
            )
        
        return SessionInfo(**session.to_dict())
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to get session: {str(e)}"
        )


@router.delete("/sessions/{session_id}")
async def close_session(
    session_id: str,
    service: TelegramService = Depends(get_service),
):
    """
    Close and remove a specific session.
    
    This stops the Telegram client and removes the session from memory.
    """
    try:
        success = await service.close_session(session_id)
        if not success:
            raise HTTPException(
                status_code=404,
                detail=f"Session {session_id} not found"
            )
        
        return {
            "success": True,
            "message": f"Session {session_id} closed successfully"
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to close session: {str(e)}"
        )


@router.delete("/sessions")
async def close_all_sessions(
    service: TelegramService = Depends(get_service),
):
    """
    Close all active sessions.
    
    This stops all Telegram clients and clears all sessions from memory.
    """
    try:
        await service.close_all_sessions()
        return {
            "success": True,
            "message": "All sessions closed successfully"
        }
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to close sessions: {str(e)}"
        )
