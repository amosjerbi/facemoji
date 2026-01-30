<p align="center">
  <img src="/app.png" alt="Wayve Logo" width="200"/>
</p>

# FaceMoji

Android app that automatically detects faces in photos and covers them with fun fruit emojis. Perfect for protecting children's privacy when sharing photos online.

## Features

- **Auto Face Detection** - ML Kit detects faces instantly, works offline
- **Emoji Selection** - Choose from 8 fruit emojis to cover faces
- **Batch Coverage** - Selected emoji applies to all detected faces
- **Save to Gallery** - Exports to Pictures/FaceMoji folder
- **Share Anywhere** - Native Android share sheet integration

## How It Works

1. Open app → Photo picker launches automatically
2. Select a photo → Faces are detected
3. Tap an emoji → All faces get covered
4. Save or Share the result

## Available Emojis

| | | |
|:---:|:---:|:---:|
| Lemon | Orange | Watermelon |
| Strawberry | Peach | Kiwi |
| Grapes | Cherries | |

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM
- **Face Detection**: Google ML Kit (on-device)
- **Image Loading**: Coil

## Requirements

- Android Studio (latest)
- Android device/emulator API 24+
- No API keys required - ML Kit runs on-device

## Setup

1. Clone the repository
```bash
git clone <repository-url>
cd emoji-app
```

2. Open in Android Studio

3. Sync Gradle and run

## Project Structure

```
app/src/main/java/com/facemoji/app/
├── MainActivity.kt           # Entry point
├── FaceMojiViewModel.kt      # State management
├── ui/
│   ├── FaceMojiScreen.kt     # Main UI
│   └── theme/Theme.kt        # Compose theme
├── detection/
│   └── FaceDetector.kt       # ML Kit wrapper
└── utils/
    └── ImageProcessor.kt     # Emoji overlay, save & share
```

## Privacy

- All processing happens on-device
- No photos are uploaded anywhere
- No internet connection required for face detection

## License

This project is for personal use.
