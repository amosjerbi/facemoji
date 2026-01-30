# Architecture: MVVM with Compose

## Pattern Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        VIEW LAYER                           │
│  FaceMojiScreen.kt (Composable)                            │
│  - Observes StateFlow<FaceMojiState>                       │
│  - Emits events via ViewModel function calls               │
│  - Pure UI rendering, no business logic                    │
└──────────────────────┬──────────────────────────────────────┘
                       │ collectAsState()
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    VIEWMODEL LAYER                          │
│  FaceMojiViewModel.kt                                       │
│  - Holds MutableStateFlow<FaceMojiState>                   │
│  - Exposes immutable StateFlow to UI                       │
│  - Coordinates detection + processing                       │
│  - Survives configuration changes                          │
└──────────────────────┬──────────────────────────────────────┘
                       │ calls
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                             │
│  FaceDetector.kt - ML Kit operations                       │
│  ImageProcessor.kt - Bitmap manipulation                   │
│  - Stateless utility classes                               │
│  - Suspend functions for async work                        │
└─────────────────────────────────────────────────────────────┘
```

## State Design

```kotlin
data class FaceMojiState(
    // Image data
    val originalBitmap: Bitmap? = null,      // Immutable source
    val previewBitmap: Bitmap? = null,       // Modified preview

    // Detection results
    val detectedFaces: List<DetectedFace> = emptyList(),
    val coveredFaceIndices: Set<Int> = emptySet(),

    // User selections
    val selectedEmoji: String? = null,
    val customEmoji: String? = null,

    // UI state
    val showCustomPicker: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val saveSuccess: Boolean = false
)
```

## State Flow Pattern

```kotlin
class FaceMojiViewModel : ViewModel() {
    // Private mutable state
    private val _state = MutableStateFlow(FaceMojiState())

    // Public immutable exposure
    val state: StateFlow<FaceMojiState> = _state.asStateFlow()

    // State updates via copy()
    fun updateSomething() {
        _state.value = _state.value.copy(
            field = newValue
        )
    }
}
```

## Event Flow

```
User Action → Composable Event Handler → ViewModel Function → State Update → Recomposition

Example:
1. User taps emoji
2. onClick = { viewModel.selectEmoji(emoji) }
3. selectEmoji() updates _state.value
4. UI recomposes with new state
```

## Coroutine Scopes

```kotlin
// ViewModel-scoped coroutines (auto-cancelled on ViewModel clear)
viewModelScope.launch {
    // IO operations on IO dispatcher
    val bitmap = withContext(Dispatchers.IO) {
        loadBitmap(uri)
    }

    // CPU-intensive on Default dispatcher
    val faces = withContext(Dispatchers.Default) {
        faceDetector.detectFaces(bitmap)
    }

    // State update on Main (implicit)
    _state.value = _state.value.copy(...)
}
```

## Dependency Injection (Manual)

```kotlin
class FaceMojiViewModel : ViewModel() {
    // Dependencies instantiated in ViewModel
    private val faceDetector = FaceDetector()

    override fun onCleared() {
        super.onCleared()
        faceDetector.close()  // Cleanup
    }
}
```

## Key Principles

1. **Single Source of Truth**: All UI state in one StateFlow
2. **Unidirectional Data Flow**: Events up, state down
3. **Immutable State**: Copy on update, never mutate
4. **Side Effects in ViewModel**: UI is pure rendering
5. **Suspend for Async**: No callbacks, structured concurrency
