# Common Android Patterns

## Package Structure

```
com.yourapp/
├── MainActivity.kt          # Entry point
├── YourViewModel.kt         # State management
├── detection/
│   ├── FaceDetector.kt     # ML Kit wrapper
│   └── DetectedFace.kt     # Data class
├── utils/
│   └── ImageProcessor.kt   # Bitmap operations
└── ui/
    ├── YourScreen.kt       # Main composable
    └── Theme.kt            # Colors, styles
```

## StateFlow Pattern

```kotlin
// State class
data class AppState(
    val data: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel
class MyViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun updateState(transform: (AppState) -> AppState) {
        _state.update(transform)
    }
}

// Composable
val state by viewModel.state.collectAsState()
```

## Async Operation Pattern

```kotlin
fun loadData() {
    viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)

        try {
            val result = withContext(Dispatchers.IO) {
                // Blocking operation
                fetchData()
            }
            _state.value = _state.value.copy(
                data = result,
                isLoading = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message
            )
        }
    }
}
```

## Toggle Selection Pattern

```kotlin
// State
data class State(
    val selectedIndices: Set<Int> = emptySet()
)

// Toggle function
fun toggleSelection(index: Int) {
    _state.update { current ->
        val newSelection = if (index in current.selectedIndices) {
            current.selectedIndices - index
        } else {
            current.selectedIndices + index
        }
        current.copy(selectedIndices = newSelection)
    }
}

// UI check
val isSelected = index in state.selectedIndices
```

## Editable List Pattern

```kotlin
// State with list and editing index
data class State(
    val items: List<String> = listOf("A", "B", "C"),
    val showEditor: Boolean = false,
    val editingIndex: Int? = null
)

// Open editor for specific item
fun editItem(index: Int) {
    _state.value = _state.value.copy(
        showEditor = true,
        editingIndex = index
    )
}

// Update the item
fun updateItem(newValue: String) {
    val index = _state.value.editingIndex ?: return
    val updatedItems = _state.value.items.toMutableList()
    updatedItems[index] = newValue
    _state.value = _state.value.copy(
        items = updatedItems,
        showEditor = false,
        editingIndex = null
    )
}
```

## Resource Cleanup Pattern

```kotlin
class MyViewModel : ViewModel() {
    private val detector = FaceDetection.getClient(options)

    override fun onCleared() {
        super.onCleared()
        detector.close()  // Release ML Kit resources
    }
}
```

## Success Message Pattern

```kotlin
// State
data class State(
    val saveSuccess: Boolean = false
)

// Set success after operation
fun save() {
    viewModelScope.launch {
        val result = saveData()
        _state.value = _state.value.copy(saveSuccess = result != null)
    }
}

// Clear success flag
fun clearSaveSuccess() {
    _state.value = _state.value.copy(saveSuccess = false)
}

// Observe and show toast
LaunchedEffect(state.saveSuccess) {
    if (state.saveSuccess) {
        Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
        viewModel.clearSaveSuccess()
    }
}
```

## Auto-Launch Pattern

```kotlin
// Launch picker when screen appears
val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri ->
    uri?.let { viewModel.loadImage(context, it) }
}

LaunchedEffect(Unit) {
    launcher.launch(PickVisualMediaRequest(ImageOnly))
}
```

## Conditional Content Pattern

```kotlin
Box(contentAlignment = Alignment.Center) {
    when {
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> ErrorView(state.error)
        state.data != null -> ContentView(state.data)
        else -> EmptyView()
    }
}
```

## View Size Tracking Pattern

```kotlin
var viewSize by remember { mutableStateOf(IntSize.Zero) }

Box(
    modifier = Modifier
        .fillMaxSize()
        .onSizeChanged { viewSize = it }
) {
    // Use viewSize.width, viewSize.height
}
```

## Coordinate Transformation Pattern

```kotlin
// Transform tap coordinates from view to bitmap space
fun viewToBitmap(
    viewX: Float,
    viewY: Float,
    viewWidth: Float,
    viewHeight: Float,
    bitmapWidth: Int,
    bitmapHeight: Int
): Pair<Int, Int> {
    // Calculate ContentScale.Fit dimensions
    val bitmapAspect = bitmapWidth.toFloat() / bitmapHeight
    val viewAspect = viewWidth / viewHeight

    val displayedWidth: Float
    val displayedHeight: Float
    val offsetX: Float
    val offsetY: Float

    if (bitmapAspect > viewAspect) {
        displayedWidth = viewWidth
        displayedHeight = viewWidth / bitmapAspect
        offsetX = 0f
        offsetY = (viewHeight - displayedHeight) / 2
    } else {
        displayedHeight = viewHeight
        displayedWidth = viewHeight * bitmapAspect
        offsetX = (viewWidth - displayedWidth) / 2
        offsetY = 0f
    }

    val bitmapX = ((viewX - offsetX) / displayedWidth * bitmapWidth).toInt()
    val bitmapY = ((viewY - offsetY) / displayedHeight * bitmapHeight).toInt()

    return Pair(bitmapX, bitmapY)
}
```

## Intent Sharing Pattern

```kotlin
fun shareImage(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share"))
}
```

## FileProvider Pattern

```xml
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>

<!-- res/xml/file_paths.xml -->
<paths>
    <cache-path name="shared" path="shared/" />
</paths>
```

```kotlin
// Create shareable URI
val file = File(context.cacheDir, "shared/image.jpg")
val uri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.fileprovider",
    file
)
```

## Permission Check Pattern

```kotlin
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

## Error Handling Wrapper

```kotlin
suspend fun <T> safeCall(
    onError: (Exception) -> Unit = {},
    block: suspend () -> T
): T? {
    return try {
        block()
    } catch (e: Exception) {
        onError(e)
        null
    }
}

// Usage
val result = safeCall(onError = { _state.value = _state.value.copy(error = it.message) }) {
    loadData()
}
```
