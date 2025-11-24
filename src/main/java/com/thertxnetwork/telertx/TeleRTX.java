package com.thertxnetwork.telertx;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TeleRTX - Terminal-based Telegram Client
 * Inspired by Nagram, built for the terminal using Java.
 */
public class TeleRTX {
    private static final Logger logger = LoggerFactory.getLogger(TeleRTX.class);
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    
    private Client client;
    private TdApi.AuthorizationState authorizationState = null;
    private volatile boolean isRunning = true;
    private volatile boolean isAuthenticated = false;
    
    private final Lock authLock = new ReentrantLock();
    private final Condition authCondition = authLock.newCondition();
    
    private long currentChatId = 0;
    private String currentChatTitle = "";
    private final Map<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();
    private final Map<Integer, Long> chatNumberToId = new HashMap<>();
    
    private Terminal terminal;
    private LineReader lineReader;
    
    public TeleRTX() {
        // Set TDLib log level
        Client.execute(new TdApi.SetLogVerbosityLevel(1));
        Client.execute(new TdApi.SetLogStream(new TdApi.SetLogStreamFile("tdlib.log", 1 << 27, false)));
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
        
        // Create TDLib client
        client = Client.create(new UpdateHandler(), null, null);
        
        // Wait for authentication
        waitForAuthentication();
        
        if (isAuthenticated) {
            printSuccess("✓ Successfully authenticated!");
            System.out.println();
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
    
    private void waitForAuthentication() {
        authLock.lock();
        try {
            while (!isAuthenticated && isRunning) {
                try {
                    authCondition.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            authLock.unlock();
        }
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
        System.out.println("  " + ANSI_GREEN + "/quit, /q, /exit" + ANSI_RESET + "  - Exit TeleRTX");
        System.out.println();
        System.out.println(ANSI_YELLOW + "Tip: When a chat is open, just type your message (no command needed)" + ANSI_RESET);
        System.out.println();
    }
    
    private void listChats() {
        System.out.println(ANSI_CYAN + "Loading chats..." + ANSI_RESET);
        
        client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), 20), new ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if (object instanceof TdApi.Ok) {
                    client.send(new TdApi.GetChats(new TdApi.ChatListMain(), 20), new ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object obj) {
                            if (obj instanceof TdApi.Chats) {
                                TdApi.Chats chatList = (TdApi.Chats) obj;
                                displayChatList(chatList);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private void displayChatList(TdApi.Chats chatList) {
        System.out.println();
        System.out.println(ANSI_CYAN + "Recent Chats:" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "─".repeat(60) + ANSI_RESET);
        
        chatNumberToId.clear();
        int index = 1;
        
        for (long chatId : chatList.chatIds) {
            TdApi.Chat chat = chats.get(chatId);
            if (chat != null) {
                chatNumberToId.put(index, chatId);
                String chatType = getChatTypeIcon(chat.type);
                String unread = chat.unreadCount > 0 ? ANSI_RED + " [" + chat.unreadCount + "]" + ANSI_RESET : "";
                System.out.printf("  %s%2d.%s %s %s%s%n", 
                    ANSI_YELLOW, index, ANSI_RESET, chatType, chat.title, unread);
                index++;
            }
        }
        
        System.out.println(ANSI_CYAN + "─".repeat(60) + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "Use /open <number> to open a chat" + ANSI_RESET);
        System.out.println();
    }
    
    private String getChatTypeIcon(TdApi.ChatType type) {
        if (type instanceof TdApi.ChatTypePrivate || type instanceof TdApi.ChatTypeSecret) {
            return "👤";
        } else if (type instanceof TdApi.ChatTypeSupergroup) {
            TdApi.ChatTypeSupergroup supergroup = (TdApi.ChatTypeSupergroup) type;
            return supergroup.isChannel ? "📢" : "👥";
        } else if (type instanceof TdApi.ChatTypeBasicGroup) {
            return "👥";
        }
        return "❓";
    }
    
    private void openChat(String args) {
        if (args.isEmpty()) {
            printError("Usage: /open <number>");
            return;
        }
        
        try {
            int chatNum = Integer.parseInt(args);
            Long chatId = chatNumberToId.get(chatNum);
            
            if (chatId == null) {
                printError("Invalid chat number. Use /chats to see available chats.");
                return;
            }
            
            TdApi.Chat chat = chats.get(chatId);
            if (chat != null) {
                currentChatId = chatId;
                currentChatTitle = chat.title;
                printSuccess("✓ Opened chat: " + currentChatTitle);
                printWarning("Type messages to send, or /close to exit chat");
                showHistory("10");
            }
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
        
        System.out.println(ANSI_CYAN + "Loading messages..." + ANSI_RESET);
        
        TdApi.GetChatHistory request = new TdApi.GetChatHistory(currentChatId, 0, 0, limit, false);
        client.send(request, new ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if (object instanceof TdApi.Messages) {
                    TdApi.Messages messages = (TdApi.Messages) object;
                    displayMessages(messages);
                }
            }
        });
    }
    
    private void displayMessages(TdApi.Messages messages) {
        System.out.println(ANSI_CYAN + "─".repeat(60) + ANSI_RESET);
        
        // Display in chronological order (oldest first)
        for (int i = messages.messages.length - 1; i >= 0; i--) {
            displayMessage(messages.messages[i]);
        }
        
        System.out.println(ANSI_CYAN + "─".repeat(60) + ANSI_RESET);
    }
    
    private void displayMessage(TdApi.Message message) {
        String senderName = getSenderName(message);
        String timestamp = formatTimestamp(message.date);
        String content = getMessageContent(message.content);
        
        String color = message.isOutgoing ? ANSI_GREEN : ANSI_RESET;
        
        System.out.printf("%s[%s]%s %s%s:%s %s%n",
            ANSI_CYAN, timestamp, ANSI_RESET,
            color, senderName, ANSI_RESET,
            content);
    }
    
    private String getSenderName(TdApi.Message message) {
        // For now, return a simple indicator
        return message.isOutgoing ? "You" : "Contact";
    }
    
    private String formatTimestamp(int timestamp) {
        long millis = timestamp * 1000L;
        java.time.Instant instant = java.time.Instant.ofEpochMilli(millis);
        java.time.LocalDateTime dateTime = java.time.LocalDateTime.ofInstant(
            instant, java.time.ZoneId.systemDefault());
        return String.format("%02d:%02d:%02d", 
            dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
    }
    
    private String getMessageContent(TdApi.MessageContent content) {
        if (content instanceof TdApi.MessageText) {
            return ((TdApi.MessageText) content).text.text;
        } else if (content instanceof TdApi.MessagePhoto) {
            return "[Photo]";
        } else if (content instanceof TdApi.MessageVideo) {
            return "[Video]";
        } else if (content instanceof TdApi.MessageDocument) {
            return "[Document]";
        } else if (content instanceof TdApi.MessageAudio) {
            return "[Audio]";
        } else if (content instanceof TdApi.MessageVoiceNote) {
            return "[Voice]";
        } else if (content instanceof TdApi.MessageSticker) {
            return "[Sticker]";
        } else {
            return "[Media]";
        }
    }
    
    private void sendMessage(String text) {
        if (currentChatId == 0) {
            printError("No chat is open. Use /open <number> first.");
            return;
        }
        
        TdApi.InputMessageText content = new TdApi.InputMessageText();
        content.text = new TdApi.FormattedText(text, null);
        
        TdApi.SendMessage request = new TdApi.SendMessage();
        request.chatId = currentChatId;
        request.inputMessageContent = content;
        
        client.send(request, new ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if (object instanceof TdApi.Error) {
                    TdApi.Error error = (TdApi.Error) object;
                    printError("Failed to send message: " + error.message);
                }
            }
        });
    }
    
    private void quit() {
        System.out.println();
        System.out.println(ANSI_CYAN + "Goodbye! 👋" + ANSI_RESET);
        isRunning = false;
    }
    
    private void cleanup() {
        if (client != null) {
            client.send(new TdApi.Close(), null);
        }
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
    
    // Update handler class
    private class UpdateHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            if (object instanceof TdApi.UpdateAuthorizationState) {
                onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
            } else if (object instanceof TdApi.UpdateNewChat) {
                TdApi.UpdateNewChat update = (TdApi.UpdateNewChat) object;
                chats.put(update.chat.id, update.chat);
            } else if (object instanceof TdApi.UpdateChatTitle) {
                TdApi.UpdateChatTitle update = (TdApi.UpdateChatTitle) object;
                TdApi.Chat chat = chats.get(update.chatId);
                if (chat != null) {
                    chat.title = update.title;
                }
            } else if (object instanceof TdApi.UpdateNewMessage) {
                TdApi.UpdateNewMessage update = (TdApi.UpdateNewMessage) object;
                if (update.message.chatId == currentChatId && !update.message.isOutgoing) {
                    System.out.println(); // New line before message
                    displayMessage(update.message);
                }
            }
        }
    }
    
    private void onAuthorizationStateUpdated(TdApi.AuthorizationState authState) {
        if (authState != null) {
            this.authorizationState = authState;
        }
        
        if (authorizationState instanceof TdApi.AuthorizationStateWaitTdlibParameters) {
            handleWaitTdlibParameters();
        } else if (authorizationState instanceof TdApi.AuthorizationStateWaitPhoneNumber) {
            handleWaitPhoneNumber();
        } else if (authorizationState instanceof TdApi.AuthorizationStateWaitCode) {
            handleWaitCode();
        } else if (authorizationState instanceof TdApi.AuthorizationStateWaitPassword) {
            handleWaitPassword();
        } else if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            handleReady();
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
            System.out.println("Closing...");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            System.out.println("Closed");
            isRunning = false;
        }
    }
    
    private void handleWaitTdlibParameters() {
        TdApi.SetTdlibParameters parameters = new TdApi.SetTdlibParameters();
        parameters.databaseDirectory = "tdlib";
        parameters.useMessageDatabase = true;
        parameters.useSecretChats = true;
        parameters.apiId = 94575; // Default test API ID
        parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2"; // Default test API hash
        parameters.systemLanguageCode = "en";
        parameters.deviceModel = "Desktop";
        parameters.applicationVersion = "1.0";
        
        client.send(parameters, new AuthorizationHandler());
    }
    
    private void handleWaitPhoneNumber() {
        System.out.print("Enter phone number: ");
        Scanner scanner = new Scanner(System.in);
        String phone = scanner.nextLine();
        client.send(new TdApi.SetAuthenticationPhoneNumber(phone, null), new AuthorizationHandler());
    }
    
    private void handleWaitCode() {
        System.out.print("Enter verification code: ");
        Scanner scanner = new Scanner(System.in);
        String code = scanner.nextLine();
        client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationHandler());
    }
    
    private void handleWaitPassword() {
        System.out.print("Enter password: ");
        Scanner scanner = new Scanner(System.in);
        String password = scanner.nextLine();
        client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationHandler());
    }
    
    private void handleReady() {
        authLock.lock();
        try {
            isAuthenticated = true;
            authCondition.signalAll();
        } finally {
            authLock.unlock();
        }
    }
    
    // Authorization handler
    private class AuthorizationHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            if (object instanceof TdApi.Error) {
                TdApi.Error error = (TdApi.Error) object;
                System.err.println("Authentication error: " + error.message);
                authLock.lock();
                try {
                    authCondition.signalAll();
                } finally {
                    authLock.unlock();
                }
            }
        }
    }
    
    // Result handler interface
    private static abstract class ResultHandler implements Client.ResultHandler {
        @Override
        public abstract void onResult(TdApi.Object object);
    }
    
    public static void main(String[] args) {
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
