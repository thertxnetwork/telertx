"""Pydantic models for API requests and responses."""

from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
from datetime import datetime


class TelegramLoginRequest(BaseModel):
    """Request model for Telegram login."""
    
    api_id: str = Field(..., description="Telegram API ID")
    api_hash: str = Field(..., description="Telegram API Hash")
    phone: str = Field(..., description="Phone number with country code")
    database_encryption_key: str = Field(
        default="changeme1234",
        description="Database encryption key for TDLib"
    )
    session_name: Optional[str] = Field(
        None,
        description="Optional session name for reusability"
    )
    
    class Config:
        json_schema_extra = {
            "example": {
                "api_id": "11248032",
                "api_hash": "a34e92f7a98d7ed4136e4751eb93c1b8",
                "phone": "+85578174833",
                "database_encryption_key": "changeme1234",
                "session_name": "my_session"
            }
        }


class TelegramCodeRequest(BaseModel):
    """Request model for submitting authentication code."""
    
    session_id: str = Field(..., description="Session ID from login request")
    code: str = Field(..., description="Authentication code from Telegram")
    
    class Config:
        json_schema_extra = {
            "example": {
                "session_id": "session_abc123",
                "code": "12345"
            }
        }


class TelegramPasswordRequest(BaseModel):
    """Request model for submitting 2FA password."""
    
    session_id: str = Field(..., description="Session ID from login request")
    password: str = Field(..., description="2FA password")
    
    class Config:
        json_schema_extra = {
            "example": {
                "session_id": "session_abc123",
                "password": "my_password"
            }
        }


class LoginResponse(BaseModel):
    """Response model for login operations."""
    
    success: bool
    message: str
    session_id: Optional[str] = None
    status: str
    data: Optional[Dict[str, Any]] = None
    
    class Config:
        json_schema_extra = {
            "example": {
                "success": True,
                "message": "Code sent to your Telegram app",
                "session_id": "session_abc123",
                "status": "awaiting_code",
                "data": None
            }
        }


class SessionInfo(BaseModel):
    """Response model for session information."""
    
    session_id: str
    phone: str
    is_authorized: bool
    created_at: datetime
    last_used: datetime
    
    class Config:
        json_schema_extra = {
            "example": {
                "session_id": "session_abc123",
                "phone": "+85578174833",
                "is_authorized": True,
                "created_at": "2025-11-24T18:00:00",
                "last_used": "2025-11-24T18:30:00"
            }
        }


class SessionListResponse(BaseModel):
    """Response model for listing sessions."""
    
    sessions: list[SessionInfo]
    total: int
    
    class Config:
        json_schema_extra = {
            "example": {
                "sessions": [],
                "total": 0
            }
        }
