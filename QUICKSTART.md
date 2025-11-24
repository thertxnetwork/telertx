# TeleRTX Quick Start Guide

## ⚠️ Important Note

**All commands (like `/proxy`, `/chats`, `/help`) must be used INSIDE the TeleRTX application after you start it!**

Don't type `telertx> /proxy set` in your terminal shell - that won't work.

📖 **See [RUNNING.md](RUNNING.md) for detailed instructions on starting TeleRTX**

## Prerequisites
- Java 11 or higher: `java -version`
- Maven 3.6+: `mvn -version`

## Installation & First Run

### 1. Build the project
```bash
cd telertx
mvn clean package
```

This creates: `target/telertx-1.0.0-jar-with-dependencies.jar`

### 2. Run TeleRTX
```bash
java -jar target/telertx-1.0.0-jar-with-dependencies.jar
```

**You should see:**
```
╔═══════════════════════════════════════════════════╗
║       TeleRTX - Terminal Telegram Client          ║
╚═══════════════════════════════════════════════════╝

telertx>
```

**Now you can type commands inside the app!**

### 3. First-time authentication
You'll be prompted to enter:
- Phone number (with country code, e.g., `+1234567890`)
- Verification code (simulated - enter any 6 digits)
- 2FA password (optional - press Enter to skip)
- Username (optional - press Enter to skip)

**Note**: This is a demo version. The authentication is simulated for demonstration purposes.

### 4. Explore the interface
```
telertx> /help           # Show available commands
telertx> /chats          # List available chats (demo data)
telertx> /open 1         # Open first chat
[Chat Name] > Hello!     # Send a message (demo mode)
[Chat Name] > /history   # View message history
[Chat Name] > /close     # Close current chat
telertx> /account        # View account information
telertx> /quit           # Exit
```

## Common Commands

| Command | Description |
|---------|-------------|
| `/help`, `/h` | Show help message |
| `/chats`, `/c` | List recent chats |
| `/open <N>`, `/o <N>` | Open chat number N |
| `/close` | Close current chat |
| `/history [n]` | Show last n messages |
| `/account` | Show account info |
| `/proxy` | Configure SOCKS5/HTTP proxy |
| `/logout` | Logout current account |
| `/quit`, `/q` | Exit TeleRTX |

## Proxy Configuration (Important!)

If Telegram is blocked in your region or you're not receiving OTPs, configure a proxy.

📖 **See [PROXY_GUIDE.md](PROXY_GUIDE.md) for complete step-by-step instructions**

**Quick Setup:**
```bash
# Step 1: Configure proxy
telertx> /proxy set
Proxy type (SOCKS5/HTTP) [SOCKS5]: SOCKS5
Proxy host: 127.0.0.1
Proxy port [1080]: 1080
Use authentication? (yes/no) [no]: no
✓ Proxy configured successfully!

# Step 2: Verify it's enabled
telertx> /proxy info
╔═══ Proxy Configuration ═══╗
║ Status: Enabled
║ Type: SOCKS5
║ Host: 127.0.0.1
║ Port: 1080
║ Auth: No
╚═══════════════════════════╝

# Step 3 (Optional): Test connection
telertx> /proxy test

# Manage proxy
telertx> /proxy enable    # Turn on
telertx> /proxy disable   # Turn off
```

## Session Management

Sessions are saved in the `sessions/` directory. On subsequent runs, you'll automatically be logged in with your saved session.

To logout and remove a session:
```
telertx> /logout
Are you sure you want to logout? (yes/no): yes
```

## Building from Source

```bash
# Compile only
mvn compile

# Run without packaging
mvn exec:java -Dexec.mainClass="com.thertxnetwork.telertx.TeleRTX"

# Package without tests
mvn package -DskipTests

# Clean build
mvn clean package
```

## Troubleshooting

### Issue: "Unable to create a system terminal"
This is a warning - the app will work in "dumb" terminal mode. For full terminal features, ensure you're running in an actual terminal (not an IDE console).

### Issue: Build fails
```bash
# Ensure Java 11+ is installed
java -version

# Ensure Maven is installed
mvn -version

# Try clean build
mvn clean compile
```

### Issue: Character encoding problems (emoji not showing)
Your terminal needs UTF-8 support. On Linux/Mac, this is usually default. On Windows, use Windows Terminal or set UTF-8 encoding.

## Next Steps

### To add real Telegram functionality:

1. **Get Telegram API credentials**
   - Visit https://my.telegram.org/apps
   - Create an application
   - Note your API ID and API Hash

2. **Choose integration method**:
   - **TDLib** (recommended): Full Telegram client library
   - **Bot API**: For bot-based applications
   - **MTProto**: Direct protocol implementation (advanced)

3. **Refer to ARCHITECTURE.md** for detailed integration guidance

## Demo Mode Notice

TeleRTX is currently running in demonstration mode:
- ✅ Authentication flow (simulated)
- ✅ Session management (functional)
- ✅ Account operations (functional)  
- ✅ UI and commands (functional)
- ⚠️  Actual Telegram messaging (requires API integration)

The architecture and account management are fully functional and ready for Telegram API integration.

## Support

- **Documentation**: See README.md
- **Architecture**: See ARCHITECTURE.md
- **Issues**: https://github.com/thertxnetwork/telertx/issues

## Inspired By

- **Nagram**: https://github.com/NextAlone/Nagram
- Feature-rich Android Telegram client with advanced account management

---

**Ready to start?** Run `java -jar target/telertx-1.0.0-jar-with-dependencies.jar`
