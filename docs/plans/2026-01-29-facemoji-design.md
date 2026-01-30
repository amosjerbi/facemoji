# FaceMoji App Design

## Overview
Android app that lets users select photos from gallery, automatically detects faces (especially children), and covers them with selected emojis for privacy protection.

## User Flow
1. App opens → Gallery picker launches immediately
2. User selects a photo → ML Kit detects faces
3. Main screen shows: emoji grid at top, photo preview below with faces highlighted
4. User taps an emoji → All detected faces get covered with that emoji
5. User taps Save → Image saved to gallery with emojis baked in
6. User taps Share → Standard Android share sheet opens

## Architecture (MVVM)

```
├── MainActivity.kt          # Single activity, hosts Compose UI
├── FaceMojiViewModel.kt     # Manages state: selected image, faces, chosen emoji
├── ui/
│   └── FaceMojiScreen.kt    # Main screen with emoji grid + image preview
├── detection/
│   └── FaceDetector.kt      # ML Kit wrapper for face detection
├── utils/
│   ├── ImageProcessor.kt    # Overlay emojis on bitmap, save to file
│   └── ShareUtils.kt        # Handle share intent
```

## Tech Stack
- Kotlin + Jetpack Compose + Material 3
- ML Kit Face Detection (on-device)
- Coil for image loading
- Photo Picker API for gallery access

## UI Layout

```
┌─────────────────────────────┐
│      "Choose Emoji"         │  ← Header text
├─────────────────────────────┤
│  🍋  🍊  🍉                 │
│  🍓  🍑  🥝                 │  ← 3x3 emoji grid (selectable)
│  🍇  🍒                     │
├─────────────────────────────┤
│                             │
│   ┌───────────────────────┐ │
│   │   Photo Preview       │ │  ← Selected photo with emoji overlays
│   │   (faces covered)     │ │
│   └───────────────────────┘ │
│                             │
├─────────────────────────────┤
│  [ Save ]     [ Share ]     │  ← Action buttons
└─────────────────────────────┘
```

## Component Details
- **Emoji Grid**: LazyVerticalGrid, 3 columns, each emoji ~64dp with rounded selection border
- **Photo Preview**: Fills available space, maintains aspect ratio
- **Buttons**: Rounded rectangles - teal (#009688) for Save, coral (#EF5350) for Share
- **Empty State**: "Tap to select photo" placeholder

## Face Detection (ML Kit)

```kotlin
// When photo is selected:
1. Load bitmap from URI
2. Create InputImage from bitmap
3. Run ML Kit FaceDetector:
   - Performance mode: FAST
   - Landmark detection: OFF
   - Classification: OFF
4. Returns List<Face> with boundingBox for each face
```

## Emoji Overlay Logic

```kotlin
// When emoji is selected:
1. For each detected face bounding box:
   - Emoji size = face width × 1.2 (slightly larger to cover)
   - Center emoji over face bounding box
   - Draw emoji text on canvas
2. Display preview with overlays (non-destructive)

// When Save is tapped:
1. Create mutable copy of original bitmap
2. Draw emojis at same positions (baked into image)
3. Save to MediaStore (Pictures/FaceMoji folder)
4. Show success toast
```

## Edge Cases
- No faces detected → Show message "No faces found in this photo"
- Very small faces → Minimum emoji size of 48dp
- Face at edge → Emoji clamped to image bounds

## Permissions

```xml
<!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- Android 12 and below -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

## Save & Share

**Save:**
1. Create bitmap with emojis baked in
2. Insert into MediaStore: `Pictures/FaceMoji/FaceMoji_timestamp.jpg`
3. Quality: 95% JPEG
4. Toast: "Saved to gallery"

**Share:**
1. Save temp file to app cache
2. Create URI via FileProvider
3. Launch ACTION_SEND with image/jpeg
4. Android share sheet appears

## Emojis
System Unicode emojis: 🍋 🍊 🍉 🍓 🍑 🥝 🍇 🍒

## App Details
- Name: FaceMoji
- Package: com.facemoji.app
- Min SDK: 24
- Target SDK: 35
