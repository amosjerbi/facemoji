# FaceMoji Android App - Build Documentation

## Document Index

| File | Purpose |
|------|---------|
| 01-architecture.md | MVVM pattern, state management, data flow |
| 02-project-setup.md | Gradle config, dependencies, manifest |
| 03-ml-kit-face-detection.md | Face detection implementation |
| 04-image-processing.md | Bitmap manipulation, emoji overlay, save/share |
| 05-compose-ui.md | Jetpack Compose UI patterns, state hoisting |
| 06-photo-picker.md | Android Photo Picker API integration |
| 07-touch-handling.md | Gesture detection, coordinate mapping |
| 08-custom-dialogs.md | Modal dialogs in Compose |
| 09-adaptive-icons.md | Vector drawable icons, splash screen |
| 10-patterns.md | Reusable patterns for similar apps |

## App Summary

```
INPUT: Gallery photo
PROCESS: ML Kit face detection → User selects emoji → Overlay on faces
OUTPUT: Modified image saved/shared
```

## Core Tech Stack

```kotlin
// build.gradle.kts essentials
plugins {
    id("com.android.application") version "8.2.0"
    id("org.jetbrains.kotlin.android") version "1.9.21"
}

dependencies {
    // UI
    implementation(platform("androidx.compose:compose-bom:2024.08.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // ML
    implementation("com.google.mlkit:face-detection:16.1.6")

    // Image
    implementation("io.coil-kt:coil-compose:2.4.0")
}
```

## File Structure

```
app/src/main/java/com/facemoji/app/
├── MainActivity.kt           # Entry, theme wrapper
├── FaceMojiViewModel.kt      # State container, business logic
├── ui/
│   ├── FaceMojiScreen.kt     # Main composable, UI events
│   └── theme/Theme.kt        # Material3 theming
├── detection/
│   └── FaceDetector.kt       # ML Kit wrapper
└── utils/
    └── ImageProcessor.kt     # Bitmap ops, MediaStore
```
