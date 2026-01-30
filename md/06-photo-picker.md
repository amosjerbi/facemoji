# Android Photo Picker

## Modern Photo Picker (Android 11+)

```kotlin
@Composable
fun PhotoPickerScreen() {
    val context = LocalContext.current

    // Create launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { handleSelectedImage(it) }
    }

    // Launch picker
    Button(onClick = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }) {
        Text("Select Photo")
    }
}
```

## Media Types

```kotlin
// Images only
PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

// Videos only
PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)

// Images and Videos
PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)

// Specific MIME type
PickVisualMediaRequest(
    ActivityResultContracts.PickVisualMedia.SingleMimeType("image/png")
)
```

## Multiple Selection

```kotlin
val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
) { uris: List<Uri> ->
    uris.forEach { uri ->
        handleSelectedImage(uri)
    }
}

multiplePhotoPickerLauncher.launch(
    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
)
```

## Launch on Composition

```kotlin
@Composable
fun AutoLaunchPicker(viewModel: MyViewModel) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.loadImage(context, it) }
    }

    // Auto-launch once when screen appears
    LaunchedEffect(Unit) {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}
```

## Fallback for Older Devices

```kotlin
// Check if Photo Picker is available
val isPhotoPickerAvailable = ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable()

// Or use GetContent as fallback
val legacyPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let { handleSelectedImage(it) }
}

// Launch with MIME type
legacyPickerLauncher.launch("image/*")
```

## Open Document (SAF)

```kotlin
val documentPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri: Uri? ->
    uri?.let { handleDocument(it) }
}

// Launch with MIME types array
documentPickerLauncher.launch(arrayOf("image/*", "application/pdf"))
```

## Permissions

```kotlin
// Android 13+ (API 33+): No permission needed for Photo Picker!
// The picker handles its own permissions

// Android 12 and below: Need READ_EXTERNAL_STORAGE
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

// Android 13+ with direct file access (not picker):
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

## Request Permission (if needed)

```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted: Boolean ->
    if (isGranted) {
        // Permission granted, proceed
    } else {
        // Show rationale
    }
}

// Check and request
val permission = if (Build.VERSION.SDK_INT >= 33) {
    Manifest.permission.READ_MEDIA_IMAGES
} else {
    Manifest.permission.READ_EXTERNAL_STORAGE
}

when {
    ContextCompat.checkSelfPermission(context, permission) ==
        PackageManager.PERMISSION_GRANTED -> {
        // Already granted
    }
    else -> {
        permissionLauncher.launch(permission)
    }
}
```

## URI Handling

```kotlin
// Get persistent access (for documents)
context.contentResolver.takePersistableUriPermission(
    uri,
    Intent.FLAG_GRANT_READ_URI_PERMISSION
)

// Read file
context.contentResolver.openInputStream(uri)?.use { inputStream ->
    // Read data
}

// Get file name
fun getFileName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    }
}
```

## Camera Capture Alternative

```kotlin
// Take photo with camera
val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { success: Boolean ->
    if (success) {
        // Photo saved to provided URI
    }
}

// Create temp URI for photo
val photoUri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.fileprovider",
    File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
)

cameraLauncher.launch(photoUri)
```
