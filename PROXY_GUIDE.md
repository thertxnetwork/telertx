# TeleRTX Proxy Configuration Guide

Complete guide on how to configure and use SOCKS5/HTTP proxies in TeleRTX.

## ⚠️ IMPORTANT: How to Access Proxy Commands

**The proxy commands are used INSIDE the TeleRTX application, not in your terminal shell!**

### Step 0: Start TeleRTX First

Before you can use any proxy commands, you must first start the TeleRTX application:

```bash
# Navigate to the TeleRTX directory
cd /home/runner/work/telertx/telertx

# Run the application
java -jar target/telertx-1.0.0-jar-with-dependencies.jar
```

**OR if you built from source:**
```bash
java -jar target/telertx-*-jar-with-dependencies.jar
```

**After the application starts**, you'll see the `telertx>` prompt. This is where you type the proxy commands.

### Common Mistake ❌

**DON'T do this** (in your shell):
```bash
root@server:~# telertx> /proxy set
bash: telertx>: command not found
```

**DO this instead** ✅:
```bash
# 1. First, start the application
root@server:~/telertx# java -jar target/telertx-*-jar-with-dependencies.jar

# 2. Wait for it to load and show the prompt:
telertx>

# 3. NOW type the proxy command (without the shell prompt):
telertx> /proxy set
```

## Why Use a Proxy?

- **Telegram is blocked** in your region
- **Not receiving OTPs** (verification codes)
- Need to **bypass restrictions** or firewalls
- **Privacy** - hide your IP address

## Quick Start

### Step 1: Check Current Proxy Status

First, check if you have a proxy configured:

```bash
telertx> /proxy info
```

**Output if no proxy is configured:**
```
╔═══ Proxy Configuration ═══╗
║ Status: Disabled
║ Not configured
╚═══════════════════════════╝

Commands: /proxy set, /proxy enable, /proxy disable, /proxy test
```

### Step 2: Configure Your Proxy

Run the configuration command and follow the prompts:

```bash
telertx> /proxy set
```

**Interactive prompts:**

1. **Proxy type**: Choose SOCKS5 or HTTP
   ```
   Proxy type (SOCKS5/HTTP) [SOCKS5]: SOCKS5
   ```
   - Press Enter to accept default (SOCKS5)
   - Or type `HTTP` and press Enter

2. **Host**: Enter your proxy server address
   ```
   Proxy host: 127.0.0.1
   ```
   - For local proxy: `127.0.0.1` or `localhost`
   - For remote proxy: `proxy.example.com` or IP address

3. **Port**: Enter the proxy port
   ```
   Proxy port [1080]: 1080
   ```
   - Press Enter for default (1080 for SOCKS5, 8080 for HTTP)
   - Or type your custom port

4. **Authentication**: If your proxy requires username/password
   ```
   Use authentication? (yes/no) [no]: yes
   Username: myuser
   Password: ********
   ```
   - Type `yes` if your proxy needs authentication
   - Type `no` or press Enter to skip

**Success message:**
```
✓ Proxy configured successfully!

╔═══ Proxy Configuration ═══╗
║ Status: Enabled
║ Type: SOCKS5
║ Host: 127.0.0.1
║ Port: 1080
║ Auth: No
╚═══════════════════════════╝
```

### Step 3: Verify Configuration

Check that your proxy is configured correctly:

```bash
telertx> /proxy info
```

**Output:**
```
╔═══ Proxy Configuration ═══╗
║ Status: Enabled
║ Type: SOCKS5
║ Host: 127.0.0.1
║ Port: 1080
║ Auth: No
╚═══════════════════════════╝
```

### Step 4: Test Proxy (Optional)

Test if the proxy configuration is valid:

```bash
telertx> /proxy test
```

**Output:**
```
Testing proxy connection...
Proxy type: SOCKS5
Proxy address: 127.0.0.1:1080
✓ Proxy configuration is valid
Note: Full connectivity test requires actual Telegram API integration
```

## Common Use Cases

### Case 1: Local SOCKS5 Proxy (No Authentication)

**When to use:** Running your own proxy server locally (e.g., Shadowsocks, V2Ray)

```bash
telertx> /proxy set
Proxy type (SOCKS5/HTTP) [SOCKS5]: SOCKS5
Proxy host: 127.0.0.1
Proxy port [1080]: 1080
Use authentication? (yes/no) [no]: no
✓ Proxy configured successfully!
```

### Case 2: Remote SOCKS5 Proxy with Authentication

**When to use:** Using a paid proxy service or corporate proxy

```bash
telertx> /proxy set
Proxy type (SOCKS5/HTTP) [SOCKS5]: SOCKS5
Proxy host: proxy.example.com
Proxy port [1080]: 9050
Use authentication? (yes/no) [no]: yes
Username: john_doe
Password: ********
✓ Proxy configured successfully!
```

### Case 3: HTTP Proxy

**When to use:** Your network only supports HTTP proxies

```bash
telertx> /proxy set
Proxy type (SOCKS5/HTTP) [SOCKS5]: HTTP
Proxy host: httpproxy.example.com
Proxy port [1080]: 8080
Use authentication? (yes/no) [no]: no
✓ Proxy configured successfully!
```

## Managing Your Proxy

### Temporarily Disable Proxy

Keep configuration but use direct connection:

```bash
telertx> /proxy disable
✓ Proxy disabled
```

### Re-enable Proxy

Use previously configured proxy:

```bash
telertx> /proxy enable
✓ Proxy enabled
```

### Change Proxy Settings

Just run `/proxy set` again to reconfigure:

```bash
telertx> /proxy set
# Enter new settings...
```

### View Current Settings

```bash
telertx> /proxy info
```

## Troubleshooting

### Problem: "Proxy not configured"

**Solution:** You need to configure the proxy first using `/proxy set`

```bash
telertx> /proxy enable
Proxy not configured. Use '/proxy set' first.
```

### Problem: Can't receive OTP even with proxy

**Possible solutions:**
1. Check if proxy is actually enabled: `/proxy info`
2. Verify proxy is working: `/proxy test`
3. Try a different proxy server
4. Make sure proxy allows Telegram connections
5. Check if proxy credentials are correct (if using auth)

### Problem: Proxy authentication failed

**Solution:** Reconfigure with correct credentials

```bash
telertx> /proxy set
# Enter correct username and password
```

### Problem: Want to remove proxy completely

**Solution:** Disable it and delete config file

```bash
telertx> /proxy disable
✓ Proxy disabled

# Then manually delete config.properties file or just configure a new one
```

## Command Reference

| Command | What it does | When to use |
|---------|-------------|-------------|
| `/proxy set` | Configure proxy settings | First time setup or changing proxy |
| `/proxy info` | View current configuration | Check what proxy is configured |
| `/proxy enable` | Turn on proxy | After disabling, to reconnect via proxy |
| `/proxy disable` | Turn off proxy | Temporarily use direct connection |
| `/proxy test` | Validate configuration | After setup, to verify settings |

## Where Configuration is Stored

Your proxy settings are saved in:
```
config.properties
```

This file is:
- **Automatically created** when you configure a proxy
- **Loaded on startup** - you don't need to reconfigure every time
- **Not tracked by git** - your credentials stay private
- **Plain text** - be careful on shared computers

## Complete Example Session

Here's a full example of configuring and using a proxy:

```bash
# Start TeleRTX
$ java -jar telertx-1.0.0-jar-with-dependencies.jar

# Check if proxy is configured
telertx> /proxy info
╔═══ Proxy Configuration ═══╗
║ Status: Disabled
║ Not configured
╚═══════════════════════════╝

# Configure SOCKS5 proxy
telertx> /proxy set
Proxy type (SOCKS5/HTTP) [SOCKS5]: SOCKS5
Proxy host: 127.0.0.1
Proxy port [1080]: 1080
Use authentication? (yes/no) [no]: no
✓ Proxy configured successfully!

# Verify configuration
telertx> /proxy info
╔═══ Proxy Configuration ═══╗
║ Status: Enabled
║ Type: SOCKS5
║ Host: 127.0.0.1
║ Port: 1080
║ Auth: No
╚═══════════════════════════╝

# Test proxy
telertx> /proxy test
Testing proxy connection...
✓ Proxy configuration is valid

# Now use TeleRTX normally - all connections will use the proxy
telertx> /chats
# ... continue using the app ...
```

## Tips

1. **Save your proxy settings**: They persist across restarts - no need to reconfigure every time
2. **Test after configuration**: Always run `/proxy test` to verify
3. **Use `/proxy info` often**: To check current status
4. **Local proxies are fastest**: If possible, run a proxy locally (127.0.0.1)
5. **SOCKS5 is recommended**: Usually faster and more reliable than HTTP proxies

## Need Help?

If you're still having issues:
1. Check your proxy server is running
2. Verify the host and port are correct
3. Try without authentication first
4. Check firewall settings
5. Try a different proxy type (HTTP vs SOCKS5)

---

**Quick Command Recap:**
```bash
/proxy set      # Configure (follow prompts)
/proxy info     # View status
/proxy enable   # Turn on
/proxy disable  # Turn off
/proxy test     # Validate
```
