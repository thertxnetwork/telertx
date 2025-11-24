package com.thertxnetwork.telertx;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * TeleRTX - Terminal-based Telegram Client
 * Inspired by Nagram (https://github.com/NextAlone/Nagram), built for the terminal using Java.
 * 
 * This implementation demonstrates the core structure extracted from Nagram's architecture:
 * - Account management (AccountInstance concept from Nagram)
 * - Session persistence
 * - Authentication flow
 * - Terminal-based UI for messaging
 * 
 * Note: This is a simplified version. For full Telegram functionality, you would need to:
 * 1. Integrate TDLib (Telegram Database Library) - requires native compilation
 * 2. Or use Telegram Bot API for bot-based clients
 * 3. Or implement MTProto protocol directly (as Nagram does)
 */
public class TeleRTX {
    private static final Logger logger = LoggerFactory.getLogger(TeleRTX.class);
    
    // ANSI color codes for terminal output (like Nagram's UI theming)
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    
    // UI constants
    private static final String SEPARATOR_LINE = "─".repeat(60);
    
    // Configuration
    private static final String SESSION_DIR = "sessions";
    private static final String CONFIG_FILE = "config.properties";
    
    // Proxy configuration
    private ProxyConfig proxyConfig;
    
    // Application state
    private volatile boolean isRunning = true;
    private boolean isAuthenticated = false;
    private String currentUser = null;
    private int currentAccount = 0; // Multi-account support (like Nagram)
    
    // Account management (inspired by Nagram's AccountInstance)
    private final Map<Integer, AccountData> accounts = new HashMap<>();
    
    // Chat state
    private long currentChatId = 0;
    private String currentChatTitle = "";
    private final Map<Long, Chat> chats = new LinkedHashMap<>();
    private final List<Chat> chatList = new ArrayList<>();
    
    // Terminal UI
    private Terminal terminal;
    private LineReader lineReader;
    
    /**
     * Proxy configuration class - inspired by Nagram's proxy support
     */
    static class ProxyConfig {
        boolean enabled;
        String type; // "SOCKS5", "HTTP", "DIRECT"
        String host;
        int port;
        String username;
        String password;
        
        ProxyConfig() {
            this.enabled = false;
            this.type = "DIRECT";
            this.host = "";
            this.port = 1080;
            this.username = "";
            this.password = "";
        }
        
        Proxy toProxy() {
            if (!enabled || "DIRECT".equals(type)) {
                return Proxy.NO_PROXY;
            }
            
            Proxy.Type proxyType = "SOCKS5".equalsIgnoreCase(type) || "SOCKS".equalsIgnoreCase(type)
                ? Proxy.Type.SOCKS
                : Proxy.Type.HTTP;
            
            return new Proxy(proxyType, new InetSocketAddress(host, port));
        }
        
        void setupAuthentication() {
            if (enabled && username != null && !username.isEmpty()) {
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        if (getRequestingHost().equalsIgnoreCase(host)) {
                            return new PasswordAuthentication(username, password.toCharArray());
                        }
                        return null;
                    }
                });
            }
        }
    }
    
    /**
     * Account data structure - inspired by Nagram's AccountInstance
     */
    static class AccountData {
        int accountId;
        String phoneNumber;
        String username;
        String sessionToken;
        boolean isActive;
        
        AccountData(int id) {
            this.accountId = id;
            this.isActive = false;
        }
    }
    
    /**
     * Chat data structure
     */
    static class Chat {
        long id;
        String title;
        String type; // private, group, channel
        int unreadCount;
        
        Chat(long id, String title, String type) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.unreadCount = 0;
        }
    }
    
    /**
     * Message data structure
     */
    static class Message {
        long id;
        long chatId;
        String senderName;
        String text;
        long timestamp;
        boolean isOutgoing;
        
        Message(long id, long chatId, String senderName, String text, boolean isOutgoing) {
            this.id = id;
            this.chatId = chatId;
            this.senderName = senderName;
            this.text = text;
            this.timestamp = System.currentTimeMillis();
            this.isOutgoing = isOutgoing;
        }
    }
    
    public TeleRTX() {
        // Initialize session directory
        try {
            Files.createDirectories(Paths.get(SESSION_DIR));
        } catch (IOException e) {
            logger.error("Failed to create session directory", e);
        }
        
        // Initialize proxy configuration
        proxyConfig = new ProxyConfig();
        loadProxyConfig();
    }
    
    public void start() throws IOException {
        printHeader();
        
        // Initialize terminal
        terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        
        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        
        // Load existing sessions
        loadSessions();
        
        // Check if proxy is configured - if not, offer to configure it first
        if (accounts.isEmpty() && !proxyConfig.enabled && proxyConfig.host.isEmpty()) {
            System.out.println();
            System.out.println(ANSI_YELLOW + "═══════════════════════════════════════════════════════════" + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "No existing sessions found." + ANSI_RESET);
            System.out.println();
            System.out.println(ANSI_CYAN + "Would you like to configure a proxy before authentication?" + ANSI_RESET);
            System.out.println(ANSI_CYAN + "(Recommended if Telegram is blocked in your region)" + ANSI_RESET);
            System.out.println();
            
            try {
                String response = lineReader.readLine(ANSI_YELLOW + "Configure proxy now? (yes/no) [no]: " + ANSI_RESET);
                if ("yes".equalsIgnoreCase(response) || "y".equalsIgnoreCase(response)) {
                    System.out.println();
                    configureProxy();
                    System.out.println();
                }
            } catch (Exception e) {
                logger.warn("Error prompting for proxy setup", e);
            }
            
            System.out.println(ANSI_YELLOW + "═══════════════════════════════════════════════════════════" + ANSI_RESET);
            System.out.println();
            System.out.println(ANSI_GREEN + "Starting authentication..." + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "(You can configure proxy later using /proxy set command)" + ANSI_RESET);
            System.out.println();
        }
        
        // Authenticate or load existing session
        if (accounts.isEmpty()) {
            performAuthentication();
        } else {
            System.out.println(ANSI_GREEN + "✓ Loaded " + accounts.size() + " existing session(s)" + ANSI_RESET);
            isAuthenticated = true;
            currentAccount = accounts.keySet().iterator().next();
            AccountData account = accounts.get(currentAccount);
            currentUser = account.username != null ? account.username : account.phoneNumber;
        }
        
        if (isAuthenticated) {
            printSuccess("✓ Successfully authenticated as: " + currentUser);
            System.out.println();
            
            // Load demo chats (in real implementation, this would fetch from Telegram)
            loadDemoChats();
            
            showHelp();
            mainLoop();
        } else {
            printError("Authentication failed.");
        }
        
        cleanup();
    }
    
    private void printHeader() {
        System.out.println(ANSI_CYAN + "╔═══════════════════════════════════════════════════╗");
        System.out.println("║       TeleRTX - Terminal Telegram Client          ║");
        System.out.println("║       Inspired by Nagram - Built with Java        ║");
        System.out.println("╚═══════════════════════════════════════════════════╝" + ANSI_RESET);
        System.out.println();
    }
    
    /**
     * Authentication flow - inspired by Nagram's LoginActivity
     * In a real implementation, this would use Telegram's auth API
     */
    private void performAuthentication() {
        try {
            System.out.println(ANSI_CYAN + "=== Telegram Authentication ===" + ANSI_RESET);
            System.out.println();
            
            // Step 1: Phone number
            String phone = lineReader.readLine("Enter phone number (with country code, e.g., +1234567890): ");
            if (phone == null || phone.trim().isEmpty()) {
                printError("Phone number is required");
                return;
            }
            
            System.out.println(ANSI_YELLOW + "→ Sending verification code to " + phone + "..." + ANSI_RESET);
            System.out.println();
            
            // Step 2: Verification code (simulated)
            String code = lineReader.readLine("Enter verification code: ");
            if (code == null || code.trim().isEmpty()) {
                printError("Verification code is required");
                return;
            }
            
            // Step 3: Optional 2FA password
            System.out.println();
            String password = lineReader.readLine("Enter 2FA password (press Enter to skip): ", '*');
            
            // Step 4: Username (optional)
            System.out.println();
            String username = lineReader.readLine("Enter username (optional, press Enter to skip): ");
            
            // Create account session
            currentAccount = accounts.size();
            AccountData account = new AccountData(currentAccount);
            account.phoneNumber = phone;
            account.username = username != null && !username.trim().isEmpty() ? username : null;
            account.sessionToken = UUID.randomUUID().toString(); // In real impl, from Telegram
            account.isActive = true;
            
            accounts.put(currentAccount, account);
            currentUser = account.username != null ? account.username : account.phoneNumber;
            
            // Save session
            saveSession(account);
            
            isAuthenticated = true;
            System.out.println();
            printSuccess("✓ Authentication successful!");
            System.out.println();
            
        } catch (Exception e) {
            logger.error("Authentication error", e);
            printError("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Load existing sessions from disk - inspired by Nagram's session persistence
     */
    private void loadSessions() {
        File sessionDir = new File(SESSION_DIR);
        File[] sessionFiles = sessionDir.listFiles((dir, name) -> name.endsWith(".session"));
        
        if (sessionFiles != null) {
            for (File sessionFile : sessionFiles) {
                try {
                    Properties props = new Properties();
                    props.load(new FileInputStream(sessionFile));
                    
                    int accountId = Integer.parseInt(props.getProperty("accountId", "0"));
                    AccountData account = new AccountData(accountId);
                    account.phoneNumber = props.getProperty("phoneNumber");
                    account.username = props.getProperty("username");
                    account.sessionToken = props.getProperty("sessionToken");
                    account.isActive = Boolean.parseBoolean(props.getProperty("isActive", "false"));
                    
                    accounts.put(accountId, account);
                    logger.info("Loaded session for account: {}", accountId);
                } catch (Exception e) {
                    logger.error("Failed to load session: " + sessionFile.getName(), e);
                }
            }
        }
    }
    
    /**
     * Save session to disk
     */
    private void saveSession(AccountData account) {
        try {
            Properties props = new Properties();
            props.setProperty("accountId", String.valueOf(account.accountId));
            props.setProperty("phoneNumber", account.phoneNumber);
            if (account.username != null) {
                props.setProperty("username", account.username);
            }
            props.setProperty("sessionToken", account.sessionToken);
            props.setProperty("isActive", String.valueOf(account.isActive));
            
            File sessionFile = new File(SESSION_DIR, "account_" + account.accountId + ".session");
            props.store(new FileOutputStream(sessionFile), "TeleRTX Session");
            logger.info("Saved session for account: {}", account.accountId);
        } catch (Exception e) {
            logger.error("Failed to save session", e);
        }
    }
    
    /**
     * Load demo chats for demonstration
     * In real implementation, this would fetch from Telegram API
     */
    private void loadDemoChats() {
        chats.put(1L, new Chat(1L, "Saved Messages", "private"));
        chats.put(2L, new Chat(2L, "John Doe", "private"));
        chats.put(3L, new Chat(3L, "Development Team", "group"));
        chats.put(4L, new Chat(4L, "Telegram News", "channel"));
        chats.get(2L).unreadCount = 3;
        chats.get(3L).unreadCount = 7;
        
        // Populate chatList for indexed access
        chatList.clear();
        chatList.addAll(chats.values());
    }
    
    private void mainLoop() {
        while (isRunning) {
            try {
                String prompt = currentChatId == 0 
                    ? ANSI_BLUE + "telertx> " + ANSI_RESET
                    : ANSI_MAGENTA + "[" + currentChatTitle + "] > " + ANSI_RESET;
                
                String input = lineReader.readLine(prompt);
                
                if (input != null && !input.trim().isEmpty()) {
                    handleCommand(input.trim());
                }
            } catch (Exception e) {
                if (isRunning) {
                    logger.error("Error in main loop", e);
                    printError("Error: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleCommand(String input) {
        if (!input.startsWith("/") && currentChatId != 0) {
            sendMessage(input);
            return;
        }
        
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        switch (command) {
            case "/help":
            case "/h":
                showHelp();
                break;
            case "/chats":
            case "/c":
                listChats();
                break;
            case "/open":
            case "/o":
                openChat(args);
                break;
            case "/close":
                closeChat();
                break;
            case "/history":
            case "/messages":
            case "/m":
                showHistory(args);
                break;
            case "/account":
            case "/acc":
                showAccountInfo();
                break;
            case "/proxy":
                handleProxyCommand(args);
                break;
            case "/logout":
                logout();
                break;
            case "/quit":
            case "/q":
            case "/exit":
                quit();
                break;
            default:
                printError("Unknown command: " + command);
                printWarning("Type /help for available commands");
        }
    }
    
    private void showHelp() {
        System.out.println(ANSI_CYAN + "Available Commands:" + ANSI_RESET);
        System.out.println("  " + ANSI_GREEN + "/help, /h" + ANSI_RESET + "          - Show this help message");
        System.out.println("  " + ANSI_GREEN + "/chats, /c" + ANSI_RESET + "         - List recent chats");
        System.out.println("  " + ANSI_GREEN + "/open <id>, /o" + ANSI_RESET + "    - Open chat by number from list");
        System.out.println("  " + ANSI_GREEN + "/close" + ANSI_RESET + "             - Close current chat");
        System.out.println("  " + ANSI_GREEN + "/history [n], /m" + ANSI_RESET + "  - Show last n messages (default: 10)");
        System.out.println("  " + ANSI_GREEN + "/account, /acc" + ANSI_RESET + "    - Show account information");
        System.out.println("  " + ANSI_GREEN + "/proxy" + ANSI_RESET + "             - Configure SOCKS5/HTTP proxy");
        System.out.println("  " + ANSI_GREEN + "/logout" + ANSI_RESET + "            - Logout current account");
        System.out.println("  " + ANSI_GREEN + "/quit, /q, /exit" + ANSI_RESET + "  - Exit TeleRTX");
        System.out.println();
        System.out.println(ANSI_YELLOW + "Tip: When a chat is open, just type your message (no command needed)" + ANSI_RESET);
        System.out.println();
    }
    
    private void listChats() {
        System.out.println();
        System.out.println(ANSI_CYAN + "Recent Chats:" + ANSI_RESET);
        System.out.println(ANSI_CYAN + SEPARATOR_LINE + ANSI_RESET);
        
        int index = 1;
        for (Chat chat : chats.values()) {
            String chatType = getChatTypeIcon(chat.type);
            String unread = chat.unreadCount > 0 ? ANSI_RED + " [" + chat.unreadCount + "]" + ANSI_RESET : "";
            System.out.printf("  %s%2d.%s %s %s%s%n", 
                ANSI_YELLOW, index, ANSI_RESET, chatType, chat.title, unread);
            index++;
        }
        
        System.out.println(ANSI_CYAN + SEPARATOR_LINE + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "Use /open <number> to open a chat" + ANSI_RESET);
        System.out.println();
    }
    
    private String getChatTypeIcon(String type) {
        switch (type) {
            case "private": return "👤";
            case "group": return "👥";
            case "channel": return "📢";
            default: return "❓";
        }
    }
    
    private void openChat(String args) {
        if (args.isEmpty()) {
            printError("Usage: /open <number>");
            return;
        }
        
        try {
            int chatNum = Integer.parseInt(args);
            
            if (chatNum < 1 || chatNum > chatList.size()) {
                printError("Invalid chat number. Use /chats to see available chats.");
                return;
            }
            
            // Get chat by index from list
            Chat chat = chatList.get(chatNum - 1);
            currentChatId = chat.id;
            currentChatTitle = chat.title;
            
            printSuccess("✓ Opened chat: " + currentChatTitle);
            printWarning("Type messages to send, or /close to exit chat");
            
            // Show recent messages
            showHistory("5");
            
        } catch (NumberFormatException e) {
            printError("Invalid number. Use /chats to see available chats.");
        }
    }
    
    private void closeChat() {
        if (currentChatId != 0) {
            printWarning("Closed chat: " + currentChatTitle);
            currentChatId = 0;
            currentChatTitle = "";
        } else {
            printWarning("No chat is currently open");
        }
    }
    
    private void showHistory(String args) {
        if (currentChatId == 0) {
            printError("No chat is open. Use /open <number> first.");
            return;
        }
        
        int limit = 10;
        if (!args.isEmpty()) {
            try {
                limit = Integer.parseInt(args);
            } catch (NumberFormatException e) {
                printWarning("Using default limit: 10");
            }
        }
        
        System.out.println(ANSI_CYAN + SEPARATOR_LINE + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "[Demo messages - in real implementation, these would be fetched from Telegram]" + ANSI_RESET);
        System.out.println(ANSI_CYAN + SEPARATOR_LINE + ANSI_RESET);
        
        // Demo messages
        displayMessage(new Message(1, currentChatId, "Contact", "Hello! How are you?", false));
        displayMessage(new Message(2, currentChatId, "You", "I'm doing great, thanks!", true));
        displayMessage(new Message(3, currentChatId, "Contact", "That's wonderful to hear!", false));
        
        System.out.println(ANSI_CYAN + SEPARATOR_LINE + ANSI_RESET);
    }
    
    private void displayMessage(Message message) {
        String timestamp = formatTimestamp(message.timestamp);
        String color = message.isOutgoing ? ANSI_GREEN : ANSI_RESET;
        
        System.out.printf("%s[%s]%s %s%s:%s %s%n",
            ANSI_CYAN, timestamp, ANSI_RESET,
            color, message.senderName, ANSI_RESET,
            message.text);
    }
    
    private String formatTimestamp(long timestamp) {
        java.time.Instant instant = java.time.Instant.ofEpochMilli(timestamp);
        java.time.LocalDateTime dateTime = java.time.LocalDateTime.ofInstant(
            instant, java.time.ZoneId.systemDefault());
        return String.format("%02d:%02d:%02d", 
            dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
    }
    
    private void sendMessage(String text) {
        if (currentChatId == 0) {
            printError("No chat is open. Use /open <number> first.");
            return;
        }
        
        // In real implementation, this would send via Telegram API
        Message message = new Message(System.currentTimeMillis(), currentChatId, "You", text, true);
        displayMessage(message);
        System.out.println(ANSI_YELLOW + "[Demo mode - message not actually sent]" + ANSI_RESET);
    }
    
    /**
     * Show account information - inspired by Nagram's account settings
     */
    private void showAccountInfo() {
        System.out.println();
        System.out.println(ANSI_CYAN + "╔═══ Account Information ═══╗" + ANSI_RESET);
        
        AccountData account = accounts.get(currentAccount);
        if (account != null) {
            System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Account ID: " + ANSI_GREEN + account.accountId + ANSI_RESET);
            System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Phone: " + ANSI_GREEN + account.phoneNumber + ANSI_RESET);
            if (account.username != null) {
                System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Username: " + ANSI_GREEN + "@" + account.username + ANSI_RESET);
            }
            System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Status: " + ANSI_GREEN + (account.isActive ? "Active" : "Inactive") + ANSI_RESET);
            System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Session: " + ANSI_GREEN + account.sessionToken.substring(0, 8) + "..." + ANSI_RESET);
        }
        
        System.out.println(ANSI_CYAN + "╚═══════════════════════════╝" + ANSI_RESET);
        System.out.println();
        System.out.println(ANSI_YELLOW + "Total accounts: " + accounts.size() + ANSI_RESET);
        System.out.println();
    }
    
    /**
     * Logout current account - inspired by Nagram's logout functionality
     */
    private void logout() {
        System.out.println();
        try {
            String confirm = lineReader.readLine(ANSI_YELLOW + "Are you sure you want to logout? (yes/no): " + ANSI_RESET);
            
            if ("yes".equalsIgnoreCase(confirm)) {
                AccountData account = accounts.get(currentAccount);
                if (account != null) {
                    // Delete session file
                    File sessionFile = new File(SESSION_DIR, "account_" + account.accountId + ".session");
                    if (sessionFile.exists()) {
                        sessionFile.delete();
                    }
                    
                    accounts.remove(currentAccount);
                    printSuccess("✓ Logged out successfully");
                    
                    if (accounts.isEmpty()) {
                        printWarning("No more active accounts. Exiting...");
                        isRunning = false;
                    } else {
                        currentAccount = accounts.keySet().iterator().next();
                        AccountData nextAccount = accounts.get(currentAccount);
                        currentUser = nextAccount.username != null ? nextAccount.username : nextAccount.phoneNumber;
                        printSuccess("Switched to account: " + currentUser);
                    }
                }
            } else {
                System.out.println(ANSI_YELLOW + "Logout cancelled" + ANSI_RESET);
            }
        } catch (Exception e) {
            logger.error("Error during logout", e);
            printError("Logout failed: " + e.getMessage());
        }
        System.out.println();
    }
    
    private void quit() {
        System.out.println();
        System.out.println(ANSI_CYAN + "Goodbye! 👋" + ANSI_RESET);
        isRunning = false;
    }
    
    /**
     * Handle proxy configuration command
     */
    private void handleProxyCommand(String args) {
        if (args.isEmpty()) {
            showProxyInfo();
            return;
        }
        
        String[] parts = args.split("\\s+");
        String subCommand = parts[0].toLowerCase();
        
        switch (subCommand) {
            case "set":
                configureProxy();
                break;
            case "enable":
                enableProxy();
                break;
            case "disable":
                disableProxy();
                break;
            case "test":
                testProxy();
                break;
            case "info":
                showProxyInfo();
                break;
            default:
                printError("Unknown proxy subcommand: " + subCommand);
                System.out.println(ANSI_YELLOW + "Usage: /proxy [set|enable|disable|test|info]" + ANSI_RESET);
        }
    }
    
    /**
     * Configure proxy settings interactively
     */
    private void configureProxy() {
        try {
            System.out.println();
            System.out.println(ANSI_CYAN + "=== Proxy Configuration ===" + ANSI_RESET);
            System.out.println();
            
            // Proxy type
            String type = lineReader.readLine("Proxy type (SOCKS5/HTTP) [SOCKS5]: ");
            if (type == null || type.trim().isEmpty()) {
                type = "SOCKS5";
            }
            proxyConfig.type = type.toUpperCase();
            
            // Host
            String host = lineReader.readLine("Proxy host: ");
            if (host == null || host.trim().isEmpty()) {
                printError("Host is required");
                return;
            }
            proxyConfig.host = host.trim();
            
            // Port
            String portStr = lineReader.readLine("Proxy port [1080]: ");
            int port = 1080;
            if (portStr != null && !portStr.trim().isEmpty()) {
                try {
                    port = Integer.parseInt(portStr.trim());
                } catch (NumberFormatException e) {
                    printWarning("Invalid port, using default: 1080");
                }
            }
            proxyConfig.port = port;
            
            // Authentication
            String useAuth = lineReader.readLine("Use authentication? (yes/no) [no]: ");
            if ("yes".equalsIgnoreCase(useAuth)) {
                String username = lineReader.readLine("Username: ");
                String password = lineReader.readLine("Password: ", '*');
                
                proxyConfig.username = username != null ? username.trim() : "";
                proxyConfig.password = password != null ? password : "";
            } else {
                proxyConfig.username = "";
                proxyConfig.password = "";
            }
            
            // Enable by default after configuration
            proxyConfig.enabled = true;
            
            // Save configuration
            saveProxyConfig();
            
            // Setup authentication if needed
            proxyConfig.setupAuthentication();
            
            System.out.println();
            printSuccess("✓ Proxy configured successfully!");
            showProxyInfo();
            System.out.println();
            printWarning("Note: Proxy will be used for future Telegram API connections.");
            
        } catch (Exception e) {
            logger.error("Error configuring proxy", e);
            printError("Failed to configure proxy: " + e.getMessage());
        }
    }
    
    /**
     * Enable proxy
     */
    private void enableProxy() {
        if (proxyConfig.host == null || proxyConfig.host.isEmpty()) {
            printError("Proxy not configured. Use '/proxy set' first.");
            return;
        }
        
        proxyConfig.enabled = true;
        proxyConfig.setupAuthentication();
        saveProxyConfig();
        printSuccess("✓ Proxy enabled");
    }
    
    /**
     * Disable proxy
     */
    private void disableProxy() {
        proxyConfig.enabled = false;
        saveProxyConfig();
        printSuccess("✓ Proxy disabled");
    }
    
    /**
     * Test proxy connection
     */
    private void testProxy() {
        System.out.println();
        System.out.println(ANSI_CYAN + "Testing proxy connection..." + ANSI_RESET);
        
        if (!proxyConfig.enabled) {
            printWarning("Proxy is currently disabled");
            return;
        }
        
        try {
            // Simple test - try to create a socket connection
            Proxy proxy = proxyConfig.toProxy();
            
            System.out.println(ANSI_YELLOW + "Proxy type: " + proxyConfig.type + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "Proxy address: " + proxyConfig.host + ":" + proxyConfig.port + ANSI_RESET);
            
            // In a real implementation, this would test actual connectivity
            printSuccess("✓ Proxy configuration is valid");
            printWarning("Note: Full connectivity test requires actual Telegram API integration");
            
        } catch (Exception e) {
            logger.error("Proxy test failed", e);
            printError("Proxy test failed: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Show current proxy configuration
     */
    private void showProxyInfo() {
        System.out.println();
        System.out.println(ANSI_CYAN + "╔═══ Proxy Configuration ═══╗" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Status: " + 
            (proxyConfig.enabled ? ANSI_GREEN + "Enabled" : ANSI_RED + "Disabled") + ANSI_RESET);
        
        if (proxyConfig.host != null && !proxyConfig.host.isEmpty()) {
            System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Type: " + ANSI_GREEN + proxyConfig.type + ANSI_RESET);
            System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Host: " + ANSI_GREEN + proxyConfig.host + ANSI_RESET);
            System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Port: " + ANSI_GREEN + proxyConfig.port + ANSI_RESET);
            
            if (proxyConfig.username != null && !proxyConfig.username.isEmpty()) {
                System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Auth: " + ANSI_GREEN + "Yes (user: " + proxyConfig.username + ")" + ANSI_RESET);
            } else {
                System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + "Auth: " + ANSI_YELLOW + "No" + ANSI_RESET);
            }
        } else {
            System.out.println(ANSI_CYAN + "║ " + ANSI_RESET + ANSI_YELLOW + "Not configured" + ANSI_RESET);
        }
        
        System.out.println(ANSI_CYAN + "╚═══════════════════════════╝" + ANSI_RESET);
        System.out.println();
        System.out.println(ANSI_YELLOW + "Commands: /proxy set, /proxy enable, /proxy disable, /proxy test" + ANSI_RESET);
        System.out.println();
    }
    
    /**
     * Load proxy configuration from file
     */
    private void loadProxyConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            return;
        }
        
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(configFile));
            
            proxyConfig.enabled = Boolean.parseBoolean(props.getProperty("proxy.enabled", "false"));
            proxyConfig.type = props.getProperty("proxy.type", "SOCKS5");
            proxyConfig.host = props.getProperty("proxy.host", "");
            proxyConfig.port = Integer.parseInt(props.getProperty("proxy.port", "1080"));
            proxyConfig.username = props.getProperty("proxy.username", "");
            proxyConfig.password = props.getProperty("proxy.password", "");
            
            if (proxyConfig.enabled) {
                proxyConfig.setupAuthentication();
                logger.info("Proxy loaded: {}:{}", proxyConfig.host, proxyConfig.port);
            }
        } catch (Exception e) {
            logger.error("Failed to load proxy configuration", e);
        }
    }
    
    /**
     * Save proxy configuration to file
     */
    private void saveProxyConfig() {
        try {
            Properties props = new Properties();
            
            // Load existing properties if file exists
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                props.load(new FileInputStream(configFile));
            }
            
            // Update proxy properties
            props.setProperty("proxy.enabled", String.valueOf(proxyConfig.enabled));
            props.setProperty("proxy.type", proxyConfig.type);
            props.setProperty("proxy.host", proxyConfig.host);
            props.setProperty("proxy.port", String.valueOf(proxyConfig.port));
            props.setProperty("proxy.username", proxyConfig.username);
            props.setProperty("proxy.password", proxyConfig.password);
            
            props.store(new FileOutputStream(configFile), "TeleRTX Configuration");
            logger.info("Proxy configuration saved");
        } catch (Exception e) {
            logger.error("Failed to save proxy configuration", e);
            printError("Failed to save proxy configuration");
        }
    }
    
    private void cleanup() {
        if (terminal != null) {
            try {
                terminal.close();
            } catch (IOException e) {
                logger.error("Error closing terminal", e);
            }
        }
    }
    
    private void printSuccess(String message) {
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }
    
    private void printWarning(String message) {
        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
    }
    
    private void printError(String message) {
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }
    
    public static void main(String[] args) {
        System.out.println(ANSI_CYAN + "╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  TeleRTX - Demo Terminal Telegram Client                  ║");
        System.out.println("║  Inspired by Nagram's architecture                         ║");
        System.out.println("║                                                            ║");
        System.out.println("║  NOTE: This is a demonstration version showing the        ║");
        System.out.println("║  structure and concepts from Nagram. For full Telegram    ║");
        System.out.println("║  functionality, you would need to integrate:               ║");
        System.out.println("║  - TDLib (requires native compilation)                     ║");
        System.out.println("║  - or Telegram Bot API                                     ║");
        System.out.println("║  - or implement MTProto protocol directly                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝" + ANSI_RESET);
        System.out.println();
        
        try {
            TeleRTX app = new TeleRTX();
            app.start();
        } catch (Exception e) {
            logger.error("Fatal error", e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
