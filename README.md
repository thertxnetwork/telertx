# TeleRTX - Simple Telegram Client

A simple Android Telegram client application with login and logout functionality, built using Java and the Telegram TDLib API.

## Features

- **Login**: Phone number-based authentication using Telegram's verification code system
- **Logout**: Secure logout with confirmation dialog
- **Simple UI**: Clean and intuitive Material Design interface

## Core Components

This app is based on the [Nagram](https://github.com/NextAlone/Nagram) Telegram client core, using the Telegram Database Library (TDLib) for authentication.

### Key Components:
- `MainActivity.java` - Main screen showing login status and logout option
- `LoginActivity.java` - Phone number and verification code input
- `TelegramClient.java` - Telegram API integration using TDLib

## Building the App

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 21 or higher
- JDK 11 or higher
- Internet access to download dependencies from Google Maven and Maven Central

### Build Instructions

1. Clone the repository:
```bash
git clone https://github.com/thertxnetwork/telertx.git
cd telertx
```

2. Get Telegram API credentials from https://my.telegram.org

3. Configure your API credentials in `local.properties`:
```bash
cp local.properties.template local.properties
# Edit local.properties and add:
# telegram.api.id=YOUR_API_ID
# telegram.api.hash=YOUR_API_HASH
```

4. Build the project:
```bash
./gradlew assembleDebug
```

5. Install on device:
```bash
./gradlew installDebug
```

### Building with Android Studio

It's recommended to open the project in Android Studio, which will automatically:
- Download required dependencies
- Configure the Android SDK
- Handle repository configuration

See [SETUP.md](SETUP.md) for detailed setup instructions.

## Usage

1. Launch the app
2. Tap "Login" button
3. Enter your phone number (with country code)
4. Tap "Send Code"
5. Enter the verification code received via Telegram
6. Tap "Verify"
7. You're logged in! Use the "Logout" button to sign out.

## License

See LICENSE file for details.