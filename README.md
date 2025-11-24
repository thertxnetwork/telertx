# TeleRTX - Terminal-based Telegram Client

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Java 11+](https://img.shields.io/badge/java-11+-blue.svg)](https://www.oracle.com/java/)

A terminal-based Telegram client inspired by [Nagram](https://github.com/NextAlone/Nagram), bringing core Telegram functionality to the command line. This project extracts the authentication and account management concepts from Nagram and implements them for terminal use.

## 🌟 Features

Inspired by Nagram's approach to Telegram clients, TeleRTX provides:

- **Terminal-based Interface**: Pure text-based UI for efficient messaging
- **Authentication System**: Phone number + verification code + 2FA support (based on Nagram's auth flow)
- **Account Management**: Multiple account support with easy switching
- **Real-time Messaging**: Send and receive messages instantly
- **Chat Management**: List, open, and switch between chats easily
- **Message History**: View conversation history
- **Color-coded Output**: Easy-to-read, color-coded messages and commands
- **Secure Session Management**: Persistent login sessions using TDLib
- **Lightweight**: Minimal resource usage compared to GUI clients

### Nagram Features Adapted for Terminal

From Nagram's feature-rich Android client, we've adapted:
- Unlimited login accounts support
- Session management and persistence
- Core messaging functionality
- Account switching capabilities
- Clean authentication flow



## 📋 Requirements

- Java 11 or higher
- Maven 3.6 or higher
- Telegram account
- TDLib native libraries (automatically managed by Maven dependency)

## 🚀 Installation

### 1. Clone the repository

```bash
git clone https://github.com/thertxnetwork/telertx.git
cd telertx
```

### 2. Build the project

```bash
mvn clean package
```

This will:
- Download all dependencies including TDLib
- Compile the Java source code
- Create an executable JAR file in the `target/` directory

### 3. Run TeleRTX

```bash
java -jar target/telertx-1.0.0-jar-with-dependencies.jar
```

Or use Maven directly:

```bash
mvn exec:java -Dexec.mainClass="com.thertxnetwork.telertx.TeleRTX"
```

## 📖 Usage

### Starting TeleRTX

```bash
java -jar target/telertx-1.0.0-jar-with-dependencies.jar
```

### First Run - Authentication

On the first run, you'll go through Telegram's authentication process (similar to Nagram):

1. Enter your phone number (with country code, e.g., +1234567890)
2. Enter the verification code sent to your Telegram app
3. If you have 2FA enabled, enter your password

The session will be saved in the `tdlib/` directory, so you won't need to authenticate again.

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
[Chat Name] > Hello! This is a message from TeleRTX!

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
├── src/
│   └── main/
│       ├── java/com/thertxnetwork/telertx/
│       │   └── TeleRTX.java       # Main application
│       └── resources/
│           └── logback.xml         # Logging configuration
├── pom.xml                         # Maven configuration
├── README.md                       # This file
└── LICENSE                         # GPL-3.0 license
```

### Technology Stack

- **Java 11**: Modern Java features and stability
- **TDLib (tdlib-java)**: Official Telegram client library
- **JLine 3**: Advanced terminal I/O and line editing
- **SLF4J + Logback**: Logging framework
- **Maven**: Build and dependency management

### Comparison with Nagram

| Feature | Nagram (Android) | TeleRTX (Terminal) |
|---------|------------------|-------------------|
| Platform | Android | Linux/macOS/Windows Terminal |
| UI Framework | Android Views | JLine Terminal UI |
| Language | Java + Kotlin | Java |
| Telegram Library | Native MTProto | TDLib |
| Auth Management | ✅ | ✅ |
| Multiple Accounts | ✅ | ✅ (planned) |
| Messaging | ✅ | ✅ |
| Media Support | ✅ | Limited |
| Proxy Support | ✅ | Planned |

## 🐛 Troubleshooting

### Common Issues

**Issue**: "UnsatisfiedLinkError" when starting TeleRTX
- **Solution**: TDLib requires native libraries. The Maven dependency should handle this automatically, but if not, you may need to install TDLib manually for your platform. See [TDLib installation guide](https://tdlib.github.io/td/build.html)

**Issue**: Authentication fails
- **Solution**: Double-check your phone number format (include country code, e.g., +1234567890)

**Issue**: "Module not found" or compilation errors
- **Solution**: Ensure Java 11+ and Maven are properly installed: `java -version` and `mvn -version`

**Issue**: Session errors or "Unauthorized" errors
- **Solution**: Delete the `tdlib/` directory and re-authenticate

### Building from Source

```bash
# Clean and rebuild
mvn clean compile

# Run tests (if available)
mvn test

# Package without running tests
mvn package -DskipTests

# Run with debug logging
java -jar target/telertx-1.0.0-jar-with-dependencies.jar --debug
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 📝 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Nagram** by [NextAlone](https://github.com/NextAlone/Nagram) - The inspiration for this project, providing insight into Telegram client architecture and account management
- **NekoX** - The foundation that Nagram is built upon
- **TDLib** by Telegram - Official library for building Telegram clients
- **Telegram Team** - For the excellent messaging platform and API

This project extracts and simplifies core concepts from Nagram's sophisticated Android client to create a lightweight terminal alternative.

## 📚 Related Projects

- [Nagram](https://github.com/NextAlone/Nagram) - Feature-rich Telegram Android client (original inspiration)
- [NekoX](https://github.com/NekoX-Dev/NekoX) - Nagram's foundation
- [TDLib](https://github.com/tdlib/td) - Telegram Database Library
- [telegram-cli](https://github.com/vysheng/tg) - Classic terminal Telegram client

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/thertxnetwork/telertx/issues)
- **Telegram**: For Telegram API questions, visit [Telegram API Support](https://core.telegram.org/api)

## 🔮 Future Enhancements

Potential features adapted from Nagram and other sources:
- [ ] Multiple account support (like Nagram's unlimited accounts feature)
- [ ] Proxy support (VMess, Shadowsocks, SOCKS5)
- [ ] Search functionality
- [ ] Media file support (images, videos, documents)
- [ ] Group management features
- [ ] Channel subscriptions
- [ ] Message editing and deletion
- [ ] Forwarding messages
- [ ] Custom notification settings
- [ ] Inline bot support
- [ ] Export/import chat history
- [ ] QR code login (like Nagram)
- [ ] Session management UI
- [ ] Color themes

---

**Note**: This is a third-party client and is not affiliated with Telegram, Nagram, or NekoX. It's an independent terminal implementation inspired by Nagram's approach to Telegram clients.