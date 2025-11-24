"""Main FastAPI application."""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
from app.config import get_settings
from app.routes import telegram_router
from app.services import get_telegram_service


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Lifespan context manager for startup and shutdown events."""
    # Startup
    settings = get_settings()
    telegram_service = get_telegram_service(session_dir=settings.session_dir)
    yield
    # Shutdown
    await telegram_service.close_all_sessions()


def create_app() -> FastAPI:
    """Create and configure the FastAPI application."""
    settings = get_settings()
    
    app = FastAPI(
        title=settings.api_title,
        version=settings.api_version,
        description=settings.api_description,
        lifespan=lifespan,
    )
    
    # Add CORS middleware
    # WARNING: For production, restrict allow_origins to specific trusted domains
    # Example: allow_origins=["https://your-frontend.com", "https://your-app.com"]
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],  # Allow all origins for development
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    
    # Include routers
    app.include_router(telegram_router)
    
    @app.get("/")
    async def root():
        """Root endpoint."""
        return {
            "message": "Telegram Login API",
            "version": settings.api_version,
            "docs": "/docs",
        }
    
    @app.get("/health")
    async def health():
        """Health check endpoint."""
        return {"status": "healthy"}
    
    return app


app = create_app()
