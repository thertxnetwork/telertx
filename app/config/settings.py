"""Configuration settings for the Telegram API application."""

from pydantic_settings import BaseSettings
from typing import Optional
import os


class Settings(BaseSettings):
    """Application settings."""
    
    # API Settings
    api_title: str = "Telegram Login API"
    api_version: str = "1.0.0"
    api_description: str = "Asynchronous Telegram authentication API with session management"
    
    # Session Storage
    session_dir: str = "./sessions"
    
    # Server Settings
    host: str = "0.0.0.0"
    port: int = 8000
    
    class Config:
        env_file = ".env"
        case_sensitive = False


def get_settings() -> Settings:
    """Get application settings."""
    return Settings()
