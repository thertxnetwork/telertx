# TeleRTX - Simple Telegram CLI Client

A simple Node.js CLI Telegram client with login and logout functionality, using TDLib through the [tglib](https://github.com/nodegin/tglib) library.

## Features

- **Login**: Phone number-based authentication with verification code
- **Logout**: Simple logout command with confirmation
- **Status Check**: View current login status and user information
- **CLI Interface**: Easy-to-use command-line interface
- **Session Management**: Automatic session persistence in `~/.telertx/`

## Based on tglib

This project uses [tglib](https://github.com/nodegin/tglib), a Node.js wrapper for Telegram's TDLib (Telegram Database Library), providing a simple and powerful way to interact with Telegram's API.

## Installation

### Prerequisites
- **Node.js** 10 or higher (Node.js 14+ recommended)
- **TDLib binary** - See [Requirements](#requirements) section below

### Setup

1. Clone the repository:
```bash
git clone https://github.com/thertxnetwork/telertx.git
cd telertx
```

2. Install dependencies:
```bash
npm install
```

3. Configure API credentials:
   
   Get your API ID and API Hash from https://my.telegram.org
   
   **Option A:** Create a `.env` file:
```bash
cp .env.example .env
# Edit .env and add your credentials:
# TELEGRAM_API_ID=your_api_id
# TELEGRAM_API_HASH=your_api_hash
```

   **Option B:** Set environment variables:
```bash
export TELEGRAM_API_ID=your_api_id
export TELEGRAM_API_HASH=your_api_hash
```

## Usage

### Login
```bash
npm run login
# or
node index.js login
```

You'll be prompted to:
1. Enter your phone number (with country code, e.g., +1234567890)
2. Enter the verification code sent to your Telegram app
3. Enter your 2FA password (if enabled)

### Check Status
```bash
npm run status
# or
node index.js status
```

Shows:
- Login status (logged in / not logged in)
- User information (name, phone, username)

### Logout
```bash
npm run logout
# or
node index.js logout
```

Confirms logout and removes session.

### Help
```bash
node index.js --help
```

## Requirements

### TDLib Binary

tglib requires the TDLib binary to be installed on your system. TDLib is Telegram's official library for building clients.

#### Installation by Platform:

**Windows:**
- Follow the guide: [Compile TDLib on Windows](https://github.com/c0re100/F9TelegramUtils#compile-tdlib-on-windows)

**macOS:**
```bash
brew install tdlib
```
Or build from source: [TDLib macOS Build Instructions](https://github.com/tdlib/td#macos)

**Linux (Ubuntu/Debian):**
```bash
# Install dependencies
sudo apt-get update
sudo apt-get install -y build-essential cmake gperf libssl-dev zlib1g-dev

# Clone and build TDLib
git clone https://github.com/tdlib/td.git
cd td
mkdir build && cd build
cmake -DCMAKE_BUILD_TYPE=Release ..
cmake --build .
sudo cmake --install .
```

**Linux (CentOS 7.5):**
- Follow this guide: [TDLib CentOS Build Guide](https://gist.github.com/nodegin/e3849aa1e5170c2e05942ffe86e4f8c9)

**Note:** Building TDLib requires at least 8GB of RAM. Building in Docker containers is recommended for consistent results.

## Project Structure

```
telertx/
├── index.js              # Main CLI application
├── lib/
│   └── client.js         # Telegram client wrapper
├── package.json          # Project configuration
├── .env.example          # Environment variables template
├── .gitignore            # Git ignore rules
└── README.md             # This file
```

## How It Works

The application uses TDLib (Telegram Database Library) through the `tglib` npm package:

1. **TDLib**: Telegram's official library providing a complete client implementation
2. **tglib**: Node.js wrapper that makes TDLib easy to use with JavaScript/Node.js
3. **Session Storage**: All data stored securely in `~/.telertx/` directory
4. **Authentication**: Handles phone number verification, 2FA, and new account creation

## Authentication Flow

```
1. User runs: npm run login
2. App prompts for phone number
3. Telegram sends verification code
4. User enters verification code
5. If 2FA enabled, user enters password
6. Session is saved in ~/.telertx/
7. User can check status or logout anytime
```

## Configuration

### API Credentials

The app needs Telegram API credentials (from https://my.telegram.org):

- Via environment variables:
  - `TELEGRAM_API_ID`
  - `TELEGRAM_API_HASH`
  
- Via `.env` file (recommended for development)

### Data Directory

All TDLib data is stored in: `~/.telertx/`
- Database files
- Downloaded files
- Session information

You can clear this directory to reset the app completely.

## Troubleshooting

### "TDLib not found" error
- Make sure TDLib is installed on your system
- Check that TDLib libraries are in your system's library path
- On Linux: `export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH`

### "PHONE_NUMBER_INVALID" error
- Ensure phone number includes country code (e.g., +1234567890)
- No spaces or special characters except +

### Authentication fails
- Verify API_ID and API_HASH are correct
- Check internet connection
- Make sure you're using the correct phone number

### Cannot build TDLib
- Ensure you have at least 8GB of RAM
- Consider using Docker for building
- Check TDLib's official build instructions for your platform

## Development

To contribute or modify:

```bash
# Clone the repository
git clone https://github.com/thertxnetwork/telertx.git
cd telertx

# Install dependencies
npm install

# Make changes to index.js or lib/client.js

# Test your changes
node index.js login
```

## License

See LICENSE file for details.

## Credits

- [tglib](https://github.com/nodegin/tglib) - Node.js wrapper for TDLib
- [TDLib](https://github.com/tdlib/td) - Telegram Database Library
- [Telegram](https://telegram.org) - The messaging platform
