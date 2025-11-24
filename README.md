# TeleRTX - Terminal-based Telegram Client

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Python 3.8+](https://img.shields.io/badge/python-3.8+-blue.svg)](https://www.python.org/downloads/)

A feature-rich terminal-based Telegram client inspired by [Nagram](https://github.com/NextAlone/Nagram). TeleRTX brings the power of Telegram messaging to your terminal with a clean, efficient interface.

## 🌟 Features

- **Terminal-based Interface**: Pure text-based UI for efficient messaging
- **Real-time Messaging**: Send and receive messages instantly
- **Chat Management**: List, open, and switch between chats easily
- **Message History**: View conversation history with customizable limits
- **Color-coded Output**: Easy-to-read, color-coded messages and commands
- **Secure Authentication**: Uses Telegram's official API with session persistence
- **Lightweight**: Minimal resource usage compared to GUI clients

## 📋 Requirements

- Python 3.8 or higher
- Telegram account
- API credentials from [https://my.telegram.org/apps](https://my.telegram.org/apps)

## 🚀 Installation

### 1. Clone the repository

```bash
git clone https://github.com/thertxnetwork/telertx.git
cd telertx
```

### 2. Install dependencies

```bash
pip install -r requirements.txt
```

Or using a virtual environment (recommended):

```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### 3. Get Telegram API credentials

1. Visit [https://my.telegram.org/apps](https://my.telegram.org/apps)
2. Log in with your phone number
3. Create a new application
4. Note down your `API_ID` and `API_HASH`

### 4. Configure the application

Create a `.env` file in the project root:

```bash
cp .env.example .env
```

Edit `.env` and add your credentials:

```
API_ID=your_api_id
API_HASH=your_api_hash
SESSION_NAME=telertx_session
PHONE_NUMBER=+1234567890
```

**Note**: `PHONE_NUMBER` is optional. If not provided, you'll be prompted when running the app.

## 📖 Usage

### Starting TeleRTX

```bash
python telertx.py
```

Or make it executable:

```bash
chmod +x telertx.py
./telertx.py
```

### First Run

On the first run, you'll need to authenticate:
1. Enter your phone number (if not in `.env`)
2. Enter the verification code sent to your Telegram app
3. If you have 2FA enabled, enter your password

The session will be saved, so you won't need to authenticate again.

## 🎯 Commands

| Command | Alias | Description |
|---------|-------|-------------|
| `/help` | `/h` | Show help message |
| `/chats` | `/c` | List recent chats (up to 20) |
| `/open <number>` | `/o <number>` | Open a chat by its number from the list |
| `/close` | - | Close the current chat |
| `/history [n]` | `/m [n]` | Show last n messages (default: 10) |
| `/quit` | `/q`, `/exit` | Exit TeleRTX |

### Usage Examples

```bash
# List your recent chats
telertx> /chats

# Open the 3rd chat from the list
telertx> /open 3

# Show last 20 messages in current chat
[Chat Name] > /history 20

# Send a message (when a chat is open)
[Chat Name] > Hello! This is a message.

# Close current chat
[Chat Name] > /close

# Exit the application
telertx> /quit
```

## 🎨 Interface Overview

- **Blue prompt** (`telertx>`) - Main command mode
- **Magenta prompt** (`[Chat Name] >`) - Chat mode (send messages directly)
- **Cyan text** - System messages and headers
- **Green text** - Your outgoing messages
- **White text** - Incoming messages
- **Yellow text** - Hints and warnings
- **Red text** - Errors

## 🔒 Security & Privacy

- **No data collection**: TeleRTX doesn't collect or store any personal data
- **Secure authentication**: Uses Telegram's official MTProto protocol
- **Local sessions**: Authentication sessions are stored locally (`.session` files)
- **Open source**: Full source code available for review

## 🛠️ Development

### Project Structure

```
telertx/
├── telertx.py          # Main application
├── requirements.txt    # Python dependencies
├── .env.example        # Environment variables template
├── .gitignore         # Git ignore rules
├── README.md          # This file
└── LICENSE            # GPL-3.0 license
```

### Dependencies

- **telethon**: Telegram API client library
- **python-dotenv**: Environment variable management
- **colorama**: Cross-platform colored terminal output
- **prompt-toolkit**: Advanced command-line interface

## 🐛 Troubleshooting

### Common Issues

**Issue**: "API_ID and API_HASH must be set in .env file"
- **Solution**: Make sure you've created a `.env` file with valid credentials from [my.telegram.org](https://my.telegram.org/apps)

**Issue**: Authentication fails
- **Solution**: Double-check your phone number format (include country code, e.g., +1234567890)

**Issue**: "Module not found" errors
- **Solution**: Ensure all dependencies are installed: `pip install -r requirements.txt`

**Issue**: Session errors after updating
- **Solution**: Delete the `.session` file and re-authenticate

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 📝 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by [Nagram](https://github.com/NextAlone/Nagram) - A feature-rich Telegram Android client
- Built with [Telethon](https://github.com/LonamiWebs/Telethon) - Python Telegram client library
- Thanks to the Telegram team for their excellent API documentation

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/thertxnetwork/telertx/issues)
- **Telegram**: For Telegram API questions, visit [Telegram API Support](https://core.telegram.org/api)

## 🔮 Future Enhancements

Potential features for future releases:
- [ ] Search functionality
- [ ] Media file support (images, videos, documents)
- [ ] Group management features
- [ ] Channel subscriptions
- [ ] Message editing and deletion
- [ ] Forwarding messages
- [ ] Sticker support
- [ ] Custom themes
- [ ] Multiple account support
- [ ] Inline bot support
- [ ] Voice message playback

---

**Note**: This is a third-party client and is not affiliated with Telegram or Nagram.