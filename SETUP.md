# TeleRTX Setup Guide

This guide will help you set up and build the TeleRTX Android application.

## Step 1: Install Prerequisites

### Android Studio
Download and install the latest version of Android Studio from:
https://developer.android.com/studio

### JDK 11 or higher
Android Studio usually comes with a JDK. If not, download from:
https://adoptium.net/

## Step 2: Get Telegram API Credentials

1. Visit https://my.telegram.org
2. Log in with your phone number
3. Go to "API development tools"
4. Create a new application
5. Note down your `api_id` and `api_hash`

## Step 3: Configure the Project

### Update API Credentials
Open `app/src/main/java/com/thertx/telertx/TelegramClient.java` and update:

```java
private static final int API_ID = YOUR_API_ID; // Replace with your API ID
private static final String API_HASH = "YOUR_API_HASH"; // Replace with your API Hash
```

### Configure Android SDK Path
Copy `local.properties.template` to `local.properties` and set your Android SDK path:

```properties
sdk.dir=/path/to/Android/sdk
```

Or set the ANDROID_HOME environment variable:
```bash
export ANDROID_HOME=/path/to/Android/sdk
```

### Update build.gradle for Google Repository

The project requires Google's Maven repository for Android dependencies. Update the root `build.gradle`:

```gradle
buildscript {
    repositories {
        google()  // Add this
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.2.0'
    }
}

allprojects {
    repositories {
        google()  // Add this
        mavenCentral()
    }
}
```

## Step 4: Build the Project

### Using Command Line

Build debug APK:
```bash
./gradlew assembleDebug
```

Build release APK:
```bash
./gradlew assembleRelease
```

Install on connected device:
```bash
./gradlew installDebug
```

### Using Android Studio

1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the telertx directory
4. Wait for Gradle sync to complete
5. Click "Run" (green play button) or press Shift+F10

## Step 5: Run the App

### On Physical Device
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Click Run in Android Studio or use `./gradlew installDebug`

### On Emulator
1. Open AVD Manager in Android Studio
2. Create a new Virtual Device (recommended: Pixel 5 with API 30+)
3. Start the emulator
4. Click Run in Android Studio

## Troubleshooting

### Build Fails: Cannot resolve dependencies
- Ensure you have added `google()` repository in build.gradle
- Check internet connection
- Try File > Invalidate Caches / Restart in Android Studio

### Build Fails: SDK not found
- Set ANDROID_HOME environment variable
- Or create local.properties with sdk.dir

### App Crashes: TDLib native library not loaded
- TDLib might need additional native libraries
- Check that TDLib dependency version is compatible
- Consider using TDLib's prebuilt native libraries

### Authentication Fails
- Verify API_ID and API_HASH are correct
- Ensure phone number includes country code (e.g., +1234567890)
- Check device has internet connection
- Verify Telegram servers are accessible

## Project Structure

```
telertx/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/thertx/telertx/
│   │       │   ├── MainActivity.java       # Main screen
│   │       │   ├── LoginActivity.java      # Login screen
│   │       │   └── TelegramClient.java     # Telegram API wrapper
│   │       ├── res/
│   │       │   ├── layout/                 # UI layouts
│   │       │   ├── values/                 # Strings, colors, themes
│   │       │   └── mipmap-*/               # App icons
│   │       └── AndroidManifest.xml
│   ├── build.gradle                        # App module config
│   └── proguard-rules.pro                  # ProGuard rules
├── gradle/                                 # Gradle wrapper
├── build.gradle                            # Root project config
├── settings.gradle                         # Gradle settings
└── README.md
```

## Next Steps

After successfully building and running the app:

1. Test login with your phone number
2. Verify you receive the authentication code
3. Complete login and test logout functionality
4. Consider adding more features like:
   - Viewing chat list
   - Sending messages
   - User profile display
   - Settings screen

## Additional Resources

- [Telegram TDLib Documentation](https://core.telegram.org/tdlib)
- [Android Developer Guide](https://developer.android.com/guide)
- [Material Design Guidelines](https://material.io/design)
