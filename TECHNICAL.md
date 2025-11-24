# TeleRTX Technical Documentation

## Architecture Overview

TeleRTX is a simple Android Telegram client built with Java, using the Telegram Database Library (TDLib) for authentication and API communication.

### Components

1. **MainActivity** - Main application screen
   - Displays login status
   - Shows user information when logged in
   - Provides login and logout buttons
   - Uses SharedPreferences for session persistence

2. **LoginActivity** - Authentication screen
   - Phone number input
   - Verification code input
   - Handles two-step authentication flow
   - Communicates with TelegramClient

3. **TelegramClient** - Telegram API wrapper (Singleton)
   - Manages TDLib instance
   - Handles authentication state
   - Provides callback-based API for login/logout
   - Processes Telegram updates

## Authentication Flow

### Login Process

```
User Opens App
    ↓
MainActivity (Check if logged in)
    ↓ (Not logged in)
User Clicks "Login"
    ↓
LoginActivity
    ↓
User Enters Phone Number
    ↓
TelegramClient.sendAuthenticationCode()
    ↓
TDLib → Telegram Server
    ↓
User Receives Code via Telegram
    ↓
User Enters Code
    ↓
TelegramClient.checkAuthenticationCode()
    ↓
TDLib verifies → Success
    ↓
Save session in SharedPreferences
    ↓
Return to MainActivity (Logged in)
```

### Logout Process

```
User Clicks "Logout"
    ↓
Show Confirmation Dialog
    ↓ (User confirms)
TelegramClient.logout()
    ↓
TDLib → Log out from Telegram
    ↓
Clear SharedPreferences
    ↓
Update UI (Not logged in state)
```

## Key Classes

### MainActivity.java

**Responsibilities:**
- Display current authentication status
- Show user information
- Handle navigation to LoginActivity
- Manage logout flow

**Key Methods:**
- `updateUI()` - Updates UI based on login state
- `showLogoutConfirmation()` - Shows logout dialog
- `performLogout()` - Executes logout and clears session

### LoginActivity.java

**Responsibilities:**
- Collect phone number from user
- Request authentication code from Telegram
- Collect verification code from user
- Complete authentication process

**Key Methods:**
- `sendVerificationCode()` - Sends phone number to Telegram
- `verifyCode()` - Verifies the authentication code
- `showProgress()` - Shows/hides loading indicator

### TelegramClient.java

**Responsibilities:**
- Initialize TDLib client
- Manage Telegram connection
- Handle authentication requests
- Process Telegram updates
- Manage client lifecycle

**Key Methods:**
- `getInstance(Context)` - Singleton instance accessor
- `initializeTelegramClient()` - Initializes TDLib with parameters
- `sendAuthenticationCode(phone, callback)` - Requests auth code
- `checkAuthenticationCode(code, callback)` - Verifies auth code
- `logout()` - Logs out from Telegram

**UpdatesHandler Inner Class:**
- Processes updates from TDLib
- Monitors authorization state changes
- Handles different authorization states

## TDLib Integration

### Initialization Parameters

```java
TdApi.SetTdlibParameters parameters = new TdApi.SetTdlibParameters();
parameters.databaseDirectory = context.getFilesDir().getAbsolutePath() + "/tdlib";
parameters.useMessageDatabase = true;
parameters.useSecretChats = true;
parameters.apiId = API_ID;              // From my.telegram.org
parameters.apiHash = API_HASH;          // From my.telegram.org
parameters.systemLanguageCode = "en";
parameters.deviceModel = Build.MODEL;
parameters.systemVersion = Build.VERSION.RELEASE;
parameters.applicationVersion = "1.0";
```

### Authorization States

TDLib manages authentication through state machine:

1. **AuthorizationStateWaitTdlibParameters** - Needs TDLib parameters
2. **AuthorizationStateWaitPhoneNumber** - Ready for phone number
3. **AuthorizationStateWaitCode** - Waiting for verification code
4. **AuthorizationStateReady** - Fully authenticated
5. **AuthorizationStateLoggingOut** - Logout in progress
6. **AuthorizationStateClosing** - Client closing
7. **AuthorizationStateClosed** - Client closed

## Data Persistence

### SharedPreferences Storage

The app uses SharedPreferences to store session information:

**Keys:**
- `isLoggedIn` (boolean) - Login state
- `userPhone` (String) - User's phone number
- `userName` (String) - User's display name

**File:** `TeleRTXPrefs`

This is a simple approach suitable for demo purposes. Production apps should use:
- Encrypted SharedPreferences
- Secure key storage
- Token-based session management

## UI Design

### Material Design Components

- **TextInputLayout/TextInputEditText** - Material text fields
- **Button** - Material buttons with theme colors
- **ConstraintLayout** - Responsive layouts
- **ProgressBar** - Loading indicators
- **AlertDialog** - Confirmation dialogs

### Theme

**Primary Color:** Telegram Blue (#0088CC)
- Consistent with Telegram branding
- Used for buttons, headers, and accents

**Layout Philosophy:**
- Clean, minimal design
- Clear call-to-action buttons
- Proper spacing and padding
- Responsive to different screen sizes

## Security Considerations

### Current Implementation

✅ **Implemented:**
- Secure communication via TDLib (encrypted)
- Phone-based authentication
- Session management
- Input validation

⚠️ **Not Implemented (Production Requirements):**
- Encrypted local storage
- API key obfuscation
- Certificate pinning
- ProGuard/R8 code obfuscation
- Root detection
- SSL/TLS verification

### Recommendations for Production

1. **Secure API Credentials:**
   - Store API_ID and API_HASH in native code or NDK
   - Use ProGuard/R8 to obfuscate code
   - Consider backend proxy for API calls

2. **Secure Local Storage:**
   - Use EncryptedSharedPreferences
   - Encrypt TDLib database
   - Implement secure key storage (Android Keystore)

3. **Network Security:**
   - Implement certificate pinning
   - Use network security configuration
   - Validate SSL certificates

4. **Session Security:**
   - Implement session timeout
   - Use refresh tokens
   - Add biometric authentication option

## Dependencies

### Core Dependencies

```gradle
// AndroidX
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

// Material Design
implementation 'com.google.android.material:material:1.11.0'

// Telegram TDLib
implementation 'org.drinkless:tdlib:1.8.0'
```

### TDLib Native Libraries

TDLib requires native libraries (.so files) for different architectures:
- armeabi-v7a
- arm64-v8a
- x86
- x86_64

These are included in the org.drinkless:tdlib dependency.

## Testing

### Manual Testing Checklist

- [ ] App launches successfully
- [ ] Login button navigates to LoginActivity
- [ ] Phone number validation works
- [ ] "Send Code" button triggers code request
- [ ] Verification code input appears after sending code
- [ ] Valid code completes login successfully
- [ ] Invalid code shows error message
- [ ] After login, MainActivity shows user info
- [ ] Logout button appears when logged in
- [ ] Logout confirmation dialog appears
- [ ] Logout clears session successfully
- [ ] App remembers login state after restart

### Automated Testing (Future Work)

Consider adding:
- Unit tests for TelegramClient logic
- UI tests with Espresso
- Integration tests for auth flow
- Mock TDLib for testing without network

## Known Limitations

1. **TDLib Dependency**: Requires working TDLib native libraries
2. **Network Required**: No offline mode
3. **Basic Error Handling**: Limited error recovery
4. **No 2FA Support**: Doesn't handle two-factor authentication
5. **Single Account**: No multi-account support
6. **Minimal Features**: Only login/logout, no messaging

## Future Enhancements

### Phase 1: Core Features
- [ ] User profile display
- [ ] Chat list
- [ ] Basic message viewing
- [ ] Contact list

### Phase 2: Messaging
- [ ] Send text messages
- [ ] Receive messages
- [ ] Message notifications
- [ ] Media sharing

### Phase 3: Advanced
- [ ] Group chats
- [ ] Channels
- [ ] Voice/Video calls
- [ ] Secret chats

## References

- [TDLib Documentation](https://core.telegram.org/tdlib)
- [TDLib Android Example](https://github.com/tdlib/td/tree/master/example/android)
- [Telegram API](https://core.telegram.org/api)
- [Android Development Guide](https://developer.android.com/guide)
