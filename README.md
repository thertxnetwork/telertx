# TelertX - Asynchronous Telegram Login API

A fully asynchronous FastAPI application for Telegram authentication with reusable session management.

## Features

- ✨ Fully asynchronous API built with FastAPI
- 🔐 Secure Telegram authentication flow
- 💾 Reusable session management
- 📝 Complete API documentation with Swagger UI
- 🔄 Support for 2FA (Two-Factor Authentication)
- 🚀 Easy to deploy and scale

## Installation

1. Clone the repository:
```bash
git clone https://github.com/thertxnetwork/telertx.git
cd telertx
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. **Verify installation** (recommended):
```bash
python startup_check.py
```

This will check if all required dependencies, including TDLib, are properly installed.

4. Create a `.env` file (optional) to customize settings:
```env
SESSION_DIR=./sessions
HOST=0.0.0.0
PORT=8000
```

## Troubleshooting

### Import Errors on Startup

If you encounter import errors when starting the server (especially with uvicorn/uvloop), try:

1. **Run the startup check**:
   ```bash
   python startup_check.py
   ```

2. **Verify TDLib is installed**: The `python-telegram` library requires TDLib. On most systems:
   ```bash
   # Ubuntu/Debian
   sudo apt-get install libtdjson1.8.0

   # MacOS
   brew install tdlib

   # Or install from source: https://github.com/tdlib/td
   ```

3. **Test import directly**:
   ```bash
   python -c "from telegram.client import Telegram; print('OK')"
   ```

4. **Check Python version**: Ensure you're using Python 3.8+
   ```bash
   python --version
   ```

## Usage

### Starting the Server

Run the server using:

```bash
python run.py
```

Or using uvicorn directly:

```bash
uvicorn app.main:app --reload
```

The API will be available at `http://localhost:8000`

### API Documentation

Once the server is running, visit:
- Swagger UI: `http://localhost:8000/docs`
- ReDoc: `http://localhost:8000/redoc`

### Authentication Flow

#### 1. Initiate Login

Send a POST request to `/api/v1/telegram/login`:

```bash
curl -X POST "http://localhost:8000/api/v1/telegram/login" \
  -H "Content-Type: application/json" \
  -d '{
    "api_id": "11248032",
    "api_hash": "a34e92f7a98d7ed4136e4751eb93c1b8",
    "phone": "+85578174833",
    "database_encryption_key": "changeme1234",
    "session_name": "my_session"
  }'
```

Response:
```json
{
  "success": true,
  "message": "Authentication code has been sent to your Telegram app",
  "session_id": "session_my_session",
  "status": "awaiting_code"
}
```

#### 2. Submit Authentication Code

After receiving the code on your Telegram app, submit it:

```bash
curl -X POST "http://localhost:8000/api/v1/telegram/submit-code" \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "session_my_session",
    "code": "12345"
  }'
```

#### 3. Submit 2FA Password (if required)

If your account has 2FA enabled:

```bash
curl -X POST "http://localhost:8000/api/v1/telegram/submit-password" \
  -H "Content-Type: application/json" \
  -d '{
    "session_id": "session_my_session",
    "password": "your_2fa_password"
  }'
```

### Session Management

#### List All Sessions

```bash
curl -X GET "http://localhost:8000/api/v1/telegram/sessions"
```

#### Get Specific Session

```bash
curl -X GET "http://localhost:8000/api/v1/telegram/sessions/session_my_session"
```

#### Close a Session

```bash
curl -X DELETE "http://localhost:8000/api/v1/telegram/sessions/session_my_session"
```

#### Close All Sessions

```bash
curl -X DELETE "http://localhost:8000/api/v1/telegram/sessions"
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Root endpoint with API info |
| GET | `/health` | Health check endpoint |
| POST | `/api/v1/telegram/login` | Initiate Telegram login |
| POST | `/api/v1/telegram/submit-code` | Submit authentication code |
| POST | `/api/v1/telegram/submit-password` | Submit 2FA password |
| GET | `/api/v1/telegram/sessions` | List all sessions |
| GET | `/api/v1/telegram/sessions/{session_id}` | Get session details |
| DELETE | `/api/v1/telegram/sessions/{session_id}` | Close specific session |
| DELETE | `/api/v1/telegram/sessions` | Close all sessions |

## Project Structure

```
telertx/
├── app/
│   ├── __init__.py
│   ├── main.py              # FastAPI application
│   ├── config/
│   │   ├── __init__.py
│   │   └── settings.py      # Configuration settings
│   ├── models/
│   │   ├── __init__.py
│   │   └── schemas.py       # Pydantic models
│   ├── routes/
│   │   ├── __init__.py
│   │   └── telegram.py      # API routes
│   └── services/
│       ├── __init__.py
│       └── telegram_service.py  # Telegram client service
├── requirements.txt
├── run.py                   # Run script
└── README.md
```

## Configuration

The application can be configured using environment variables or a `.env` file:

- `SESSION_DIR`: Directory to store session data (default: `./sessions`)
- `HOST`: Host to bind the server (default: `0.0.0.0`)
- `PORT`: Port to bind the server (default: `8000`)

## Security Considerations

⚠️ **Important Security Notes:**

1. Never commit your API credentials to version control
2. Use environment variables or secure vaults for sensitive data
3. Configure CORS appropriately for production environments
4. Use HTTPS in production
5. Implement rate limiting for production deployments
6. Session data contains sensitive information - secure the `sessions/` directory

## Getting Telegram API Credentials

To use this API, you need Telegram API credentials:

1. Visit https://my.telegram.org
2. Log in with your phone number
3. Go to "API development tools"
4. Create a new application
5. Copy your `api_id` and `api_hash`

## Dependencies

- FastAPI - Modern web framework for building APIs
- python-telegram - Telegram client library
- Uvicorn - ASGI server
- Pydantic - Data validation
- aiofiles - Async file operations

## License

This project is licensed under the terms included in the LICENSE file.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.