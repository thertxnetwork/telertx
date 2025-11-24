# How to Start and Use TeleRTX

## Quick Start (3 Steps)

### Step 1: Navigate to TeleRTX Directory

```bash
cd /path/to/telertx
```

For example:
```bash
cd ~/telertx
# or
cd /root/telertx
```

### Step 2: Run the Application

```bash
java -jar target/telertx-1.0.0-jar-with-dependencies.jar
```

**Alternative (if exact version is unknown):**
```bash
java -jar target/telertx-*-jar-with-dependencies.jar
```

### Step 3: Use Commands Inside the Application

Once the application starts, you'll see:
```
╔═══════════════════════════════════════════════════╗
║       TeleRTX - Terminal Telegram Client          ║
║       Inspired by Nagram - Built with Java        ║
╚═══════════════════════════════════════════════════╝

telertx>
```

Now you can type commands like:
```
telertx> /help
telertx> /proxy set
telertx> /chats
```

## Complete Example

```bash
# In your Linux terminal/shell
root@server:~# cd ~/telertx
root@server:~/telertx# java -jar target/telertx-*-jar-with-dependencies.jar

# TeleRTX starts and shows its prompt:
╔═══════════════════════════════════════════════════╗
║       TeleRTX - Terminal Telegram Client          ║
╚═══════════════════════════════════════════════════╝

telertx> /proxy set
Proxy type (SOCKS5/HTTP) [SOCKS5]: SOCKS5
Proxy host: 127.0.0.1
Proxy port [1080]: 1080
Use authentication? (yes/no) [no]: no
✓ Proxy configured successfully!

telertx> /proxy info
╔═══ Proxy Configuration ═══╗
║ Status: Enabled
║ Type: SOCKS5
║ Host: 127.0.0.1
║ Port: 1080
╚═══════════════════════════╝

telertx> /quit
Goodbye! 👋

# Back to your shell prompt
root@server:~/telertx#
```

## Understanding the Two Different Prompts

### Shell Prompt (Your Terminal)
```bash
root@server:~/telertx#         ← This is your shell (bash/zsh)
user@hostname:~$                ← Another example
```
**What you run here:** System commands like `cd`, `ls`, `java`, etc.

### TeleRTX Prompt (Inside the Application)
```bash
telertx>                        ← This is INSIDE TeleRTX
[Chat Name] >                   ← When a chat is open
```
**What you run here:** TeleRTX commands like `/proxy`, `/chats`, `/help`, etc.

## Common Mistakes

### ❌ Mistake 1: Running TeleRTX commands in your shell
```bash
root@server:~# telertx> /proxy set
bash: telertx>: command not found
```
**Fix:** Start TeleRTX first, THEN use the commands.

### ❌ Mistake 2: Wrong directory
```bash
root@server:~# java -jar target/telertx-*.jar
Error: Unable to access jarfile
```
**Fix:** Navigate to the TeleRTX directory first:
```bash
cd ~/telertx
java -jar target/telertx-*.jar
```

### ❌ Mistake 3: JAR file doesn't exist
```bash
root@server:~/telertx# java -jar target/telertx-*.jar
Error: Unable to access jarfile
```
**Fix:** Build the project first:
```bash
mvn clean package
```

## Troubleshooting

### "java: command not found"
Install Java 11 or higher:
```bash
# Ubuntu/Debian
sudo apt install openjdk-11-jdk

# RHEL/CentOS/Fedora
sudo dnf install java-11-openjdk-devel
```

### "mvn: command not found"
Install Maven:
```bash
# Ubuntu/Debian
sudo apt install maven

# RHEL/CentOS/Fedora
sudo dnf install maven
```

### Can't find the JAR file
Build it first:
```bash
cd /path/to/telertx
mvn clean package
ls target/telertx-*.jar  # Verify it exists
```

### Application exits immediately
Check if you have an authentication session. First run requires:
1. Phone number
2. Verification code
3. Optional 2FA password

## Creating a Shortcut

### Option 1: Bash Alias
Add to your `~/.bashrc` or `~/.bash_profile`:
```bash
alias telertx='cd ~/telertx && java -jar target/telertx-*-jar-with-dependencies.jar'
```

Then reload:
```bash
source ~/.bashrc
```

Now you can just type:
```bash
telertx
```

### Option 2: Shell Script
Create `/usr/local/bin/telertx`:
```bash
#!/bin/bash
cd /root/telertx
java -jar target/telertx-*-jar-with-dependencies.jar
```

Make it executable:
```bash
chmod +x /usr/local/bin/telertx
```

Now you can run from anywhere:
```bash
telertx
```

### Option 3: Systemd Service (Advanced)
For running as a background service, create `/etc/systemd/system/telertx.service`:
```ini
[Unit]
Description=TeleRTX Telegram Client
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/root/telertx
ExecStart=/usr/bin/java -jar /root/telertx/target/telertx-1.0.0-jar-with-dependencies.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

Then:
```bash
systemctl daemon-reload
systemctl start telertx
systemctl enable telertx  # Start on boot
```

## Summary

**Remember:**
1. ✅ `java -jar target/telertx-*.jar` - Runs in your shell
2. ✅ `/proxy set` - Runs INSIDE TeleRTX (after starting it)
3. ❌ `telertx> /proxy set` - DON'T type this in your shell

**Simple workflow:**
```
Shell → Start TeleRTX → Use TeleRTX Commands → Exit → Back to Shell
```
