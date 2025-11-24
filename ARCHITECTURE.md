# Nagram to TeleRTX: Architecture Comparison

This document explains how TeleRTX extracts and adapts core concepts from Nagram for terminal use.

## Overview

**Nagram** is a feature-rich Android Telegram client with a sophisticated architecture for handling accounts, sessions, and messaging.

**TeleRTX** takes Nagram's architectural concepts and implements them for terminal environments, providing similar account management and session handling without Android dependencies.

## Architecture Mapping

### 1. Account Management

#### Nagram (Android)
```java
// org/telegram/messenger/AccountInstance.java
public class AccountInstance {
    private int currentAccount;
    private static SparseArray<AccountInstance> Instance = new SparseArray<>();
    
    public static AccountInstance getInstance(int num) {
        // Singleton pattern per account
        // Manages: MessagesController, ContactsController, etc.
    }
    
    public MessagesController getMessagesController() { ... }
    public UserConfig getUserConfig() { ... }
    // ... many more controllers
}
```

#### TeleRTX (Terminal)
```java
// Adapted for terminal - simpler but follows same pattern
static class AccountData {
    int accountId;
    String phoneNumber;
    String username;
    String sessionToken;
    boolean isActive;
}

private final Map<Integer, AccountData> accounts = new HashMap<>();
```

**Key Adaptation**: Instead of Android-specific controllers, TeleRTX uses a simplified AccountData structure that maintains the core concept of managing multiple accounts with unique identifiers.

### 2. Session Persistence

#### Nagram
- Uses Android's SharedPreferences and file system
- Stores authentication tokens, user data, settings
- Supports multiple simultaneous accounts

#### TeleRTX
```java
private void saveSession(AccountData account) {
    Properties props = new Properties();
    props.setProperty("accountId", String.valueOf(account.accountId));
    props.setProperty("phoneNumber", account.phoneNumber);
    // ... save to file
}

private void loadSessions() {
    // Load from .session files
}
```

**Key Adaptation**: Uses Java Properties files instead of Android SharedPreferences, maintaining the concept of persistent sessions across app restarts.

### 3. Authentication Flow

#### Nagram
- LoginActivity manages the UI
- Handles: phone → code → 2FA → username
- Integrates with Telegram's MTProto

#### TeleRTX
```java
private void performAuthentication() {
    // Step 1: Phone number
    String phone = lineReader.readLine("Enter phone number...");
    
    // Step 2: Verification code
    String code = lineReader.readLine("Enter verification code: ");
    
    // Step 3: Optional 2FA
    String password = lineReader.readLine("Enter 2FA password: ", '*');
    
    // Step 4: Username
    String username = lineReader.readLine("Enter username: ");
}
```

**Key Adaptation**: Same authentication flow, but using terminal input instead of Android Activities and Views.

### 4. Multi-Account Support

#### Nagram Feature
- "Unlimited login accounts" - one of Nagram's key features
- Easy switching between accounts
- Each account has independent state

#### TeleRTX Implementation
```java
private int currentAccount = 0;
private final Map<Integer, AccountData> accounts = new HashMap<>();

// Switch accounts
currentAccount = accounts.keySet().iterator().next();
AccountData account = accounts.get(currentAccount);
```

**Key Adaptation**: Structure is ready for multiple accounts, following Nagram's approach of using account IDs as keys.

## Feature Comparison

| Feature | Nagram (Android) | TeleRTX (Terminal) | Status |
|---------|------------------|-------------------|--------|
| **Core Features** |
| Account Management | ✅ Full | ✅ Basic | Implemented |
| Session Persistence | ✅ Full | ✅ Full | Implemented |
| Authentication Flow | ✅ MTProto | ⚠️  Simulated | Demo |
| Multi-Account | ✅ Unlimited | ✅ Structure Ready | Implemented |
| **Messaging** |
| Send/Receive Messages | ✅ Full | ⚠️  Demo | Demo Mode |
| Chat Management | ✅ Full | ✅ Basic | Implemented |
| Message History | ✅ Full | ⚠️  Demo | Demo Mode |
| **Advanced Features** |
| Media Support | ✅ Full | ❌ Not Implemented | Future |
| Stickers/Emoji | ✅ Custom | ❌ Not Implemented | Future |
| Proxy Support | ✅ VMess, SS, etc. | ❌ Not Implemented | Future |
| Groups/Channels | ✅ Full | ⚠️  Basic | Demo Mode |
| **UI** |
| Interface | Android Views | Terminal (JLine) | Implemented |
| Theming | Material Design | ANSI Colors | Implemented |
| Notifications | Android System | Terminal Output | Implemented |

## Code Examples

### Nagram: Opening a Chat
```java
// In Nagram (simplified)
public class ChatActivity extends BaseFragment {
    private long dialog_id;
    private TLRPC.Chat currentChat;
    
    public void openChat(long chatId) {
        dialog_id = chatId;
        currentChat = MessagesController.getInstance(currentAccount)
            .getChat(chatId);
        loadMessages();
    }
}
```

### TeleRTX: Opening a Chat
```java
private void openChat(String args) {
    int chatNum = Integer.parseInt(args);
    Chat chat = (Chat) chats.values().toArray()[chatNum - 1];
    currentChatId = chat.id;
    currentChatTitle = chat.title;
    showHistory("5");
}
```

## What's Missing for Full Functionality

TeleRTX is a demonstration of Nagram's architecture. For full Telegram functionality, you would need:

1. **Telegram API Integration**
   - TDLib (C++ library with Java bindings)
   - OR Telegram Bot API
   - OR Direct MTProto implementation (like Nagram uses)

2. **Native Dependencies**
   ```bash
   # TDLib requires native compilation
   # See: https://tdlib.github.io/td/build.html
   ```

3. **API Credentials**
   - API ID and API Hash from https://my.telegram.org/apps
   - Required for any real Telegram client

## Building on This Foundation

To extend TeleRTX into a full Telegram client:

### Option 1: Integrate TDLib
```xml
<!-- Add TDLib dependency (requires manual installation) -->
<dependency>
    <groupId>org.drinkless</groupId>
    <artifactId>tdlib-java</artifactId>
    <version>1.8.29</version>
</dependency>
```

### Option 2: Use Telegram Bot API
```xml
<dependency>
    <groupId>org.telegram</groupId>
    <artifactId>telegrambots</artifactId>
    <version>6.8.0</version>
</dependency>
```

### Option 3: Implement MTProto (Like Nagram)
- Study Nagram's `ConnectionsManager` class
- Implement MTProto 2.0 protocol
- Handle encryption, authorization, and API calls

## Conclusion

TeleRTX successfully demonstrates how Nagram's sophisticated Android architecture can be adapted for terminal use. The core concepts of account management, session persistence, and multi-account support translate well to a command-line environment.

While the current implementation uses simulated Telegram functionality for demonstration, the architecture is solid and ready for integration with actual Telegram APIs (TDLib, Bot API, or MTProto).

## References

- **Nagram Source**: https://github.com/NextAlone/Nagram
- **NekoX** (Nagram's base): https://github.com/NekoX-Dev/NekoX
- **TDLib**: https://core.telegram.org/tdlib
- **Telegram API**: https://core.telegram.org/api
- **MTProto Protocol**: https://core.telegram.org/mtproto
