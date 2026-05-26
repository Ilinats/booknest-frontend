# BookNest (Android)

Android client for **BookNest** — discover books, apply for reader copies, manage author campaigns, connect with friends, and handle profiles and notifications.

Built with **Kotlin**, **Jetpack Compose**, and a clean architecture split (UI → ViewModel → Use cases → Data sources).

## Requirements

- [Android Studio](https://developer.android.com/studio) (Ladybug or newer recommended)
- **JDK 11**
- Android SDK (API **36** compile; **minSdk 24**)
- A running BookNest backend (or your own API on port `3000`)

Gradle wrapper is included (`./gradlew`).

## Getting started

1. **Clone the repository**

   ```bash
   git clone <repo-url>
   cd booknest-frontend
   ```

2. **SDK path** — Android Studio creates `local.properties` with `sdk.dir` on first open. If you open the project from the CLI only, copy the template comment from an existing machine or let Studio generate it.

3. **Firebase** — `app/google-services.json` is required for Google Sign-In and Firebase Cloud Messaging. Use your Firebase project file if you replace the bundled one.

4. **Run** — open the project in Android Studio, select a device or emulator, and run the `app` configuration.

## Build & test

From the project root:

```bash
# Debug APK
./gradlew assembleDebug

# Unit tests
./gradlew test

# Instrumented tests (device or emulator)
./gradlew connectedDebugAndroidTest
```

## Project structure

```
app/src/main/java/com/example/booknest/
├── data/          # API data sources, session, DTOs
├── domain/        # Use cases and domain models
├── di/            # Koin modules (network, data, domain, ViewModels)
├── presentation/  # Navigation types and events
├── ui/            # Compose screens and components
├── viewmodel/     # Screen ViewModels
└── services/      # Firebase messaging, etc.
```

## Tech stack

- Jetpack Compose + Material 3
- Navigation Compose
- Koin (dependency injection)
- Retrofit + OkHttp + Kotlin Serialization
- DataStore + Encrypted preferences (session)
- Firebase (Analytics, Cloud Messaging)
- Google Sign-In
- Coil (images)