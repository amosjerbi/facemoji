package com.facemoji.app

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.facemoji.app.detection.DetectedFace
import com.facemoji.app.detection.FaceDetector
import com.facemoji.app.utils.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FaceMojiState(
    val originalBitmap: Bitmap? = null,
    val previewBitmap: Bitmap? = null,
    val detectedFaces: List<DetectedFace> = emptyList(),
    val coveredFaceIndices: Set<Int> = emptySet(),
    val selectedEmoji: String? = null,
    val emojiSlots: List<String> = emptyList(),
    val showEmojiPicker: Boolean = false,
    val editingSlotIndex: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val saveSuccess: Boolean = false
)

class FaceMojiViewModel(application: Application) : AndroidViewModel(application) {

    private val faceDetector = FaceDetector()

    // Default 6 emojis for the slots
    private val defaultEmojis = listOf(
        "\uD83D\uDE00", // 😀 Grinning face
        "\uD83D\uDE0E", // 😎 Smiling face with sunglasses
        "\uD83E\uDD73", // 🥳 Partying face
        "\uD83E\uDD20", // 🤠 Cowboy hat face
        "\uD83D\uDE08", // 😈 Smiling face with horns
        "\uD83D\uDC7B"  // 👻 Ghost
    )

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(FaceMojiState(emojiSlots = loadSavedEmojis()))
    val state: StateFlow<FaceMojiState> = _state.asStateFlow()

    private fun loadSavedEmojis(): List<String> {
        val saved = prefs.getString(KEY_EMOJI_SLOTS, null)
        return if (saved != null) {
            saved.split(DELIMITER).takeIf { it.size == 6 } ?: defaultEmojis
        } else {
            defaultEmojis
        }
    }

    private fun saveEmojis(emojis: List<String>) {
        prefs.edit().putString(KEY_EMOJI_SLOTS, emojis.joinToString(DELIMITER)).apply()
    }

    companion object {
        private const val PREFS_NAME = "facemoji_prefs"
        private const val KEY_EMOJI_SLOTS = "emoji_slots"
        private const val DELIMITER = "||"
    }

    // Popular emojis for custom picker
    val customPickerEmojis = listOf(
        // Smileys
        "\uD83D\uDE02", // 😂
        "\uD83E\uDD23", // 🤣
        "\uD83D\uDE0D", // 😍
        "\uD83E\uDD70", // 🥰
        "\uD83D\uDE18", // 😘
        "\uD83D\uDE07", // 😇
        "\uD83E\uDD13", // 🤓
        "\uD83E\uDD2F", // 🤯
        "\uD83E\uDD75", // 🥵
        "\uD83E\uDD76", // 🥶
        "\uD83D\uDE31", // 😱
        "\uD83D\uDC7B", // 👻
        // Animals
        "\uD83D\uDC36", // 🐶
        "\uD83D\uDC31", // 🐱
        "\uD83E\uDD81", // 🦁
        "\uD83D\uDC3B", // 🐻
        "\uD83D\uDC37", // 🐷
        "\uD83D\uDC35", // 🐵
        // Objects
        "\uD83C\uDF83", // 🎃
        "\uD83D\uDC80", // 💀
        "\uD83D\uDC7D", // 👽
        "\uD83E\uDD16", // 🤖
        "\u2B50",       // ⭐
        "\uD83C\uDF1F"  // 🌟
    )

    fun loadImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
                infoMessage = null,
                saveSuccess = false
            )

            try {
                val bitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }

                if (bitmap == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load image"
                    )
                    return@launch
                }

                val faces = withContext(Dispatchers.Default) {
                    faceDetector.detectFaces(bitmap)
                }

                val errorMsg = if (faces.isEmpty()) "No faces found in this photo" else null
                val infoMsg = if (faces.isNotEmpty()) {
                    "Found ${faces.size} face${if (faces.size > 1) "s" else ""} - tap emoji to cover, tap face to remove"
                } else null

                _state.value = _state.value.copy(
                    originalBitmap = bitmap,
                    previewBitmap = bitmap,
                    detectedFaces = faces,
                    coveredFaceIndices = emptySet(),
                    selectedEmoji = null,
                    isLoading = false,
                    errorMessage = errorMsg,
                    infoMessage = infoMsg
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error processing image: ${e.message}"
                )
            }
        }
    }

    fun selectEmoji(emoji: String) {
        val currentState = _state.value
        val originalBitmap = currentState.originalBitmap ?: return
        val faces = currentState.detectedFaces

        if (faces.isEmpty()) return

        val allFaceIndices = faces.indices.toSet()

        viewModelScope.launch {
            val previewBitmap = withContext(Dispatchers.Default) {
                ImageProcessor.createPreviewWithEmojis(originalBitmap, faces, emoji, allFaceIndices)
            }

            _state.value = _state.value.copy(
                selectedEmoji = emoji,
                coveredFaceIndices = allFaceIndices,
                previewBitmap = previewBitmap
            )
        }
    }

    fun showEmojiPickerForSlot(slotIndex: Int) {
        _state.value = _state.value.copy(
            showEmojiPicker = true,
            editingSlotIndex = slotIndex
        )
    }

    fun hideEmojiPicker() {
        _state.value = _state.value.copy(
            showEmojiPicker = false,
            editingSlotIndex = null
        )
    }

    fun updateSlotEmoji(emoji: String) {
        val slotIndex = _state.value.editingSlotIndex ?: return
        val updatedSlots = _state.value.emojiSlots.toMutableList()
        updatedSlots[slotIndex] = emoji

        _state.value = _state.value.copy(
            emojiSlots = updatedSlots,
            showEmojiPicker = false,
            editingSlotIndex = null
        )
        saveEmojis(updatedSlots)
        selectEmoji(emoji)
    }

    fun toggleFaceCoverage(faceIndex: Int) {
        val currentState = _state.value
        val originalBitmap = currentState.originalBitmap ?: return
        val faces = currentState.detectedFaces
        val emoji = currentState.selectedEmoji ?: return

        if (faceIndex !in faces.indices) return

        val newCoveredIndices = if (faceIndex in currentState.coveredFaceIndices) {
            currentState.coveredFaceIndices - faceIndex
        } else {
            currentState.coveredFaceIndices + faceIndex
        }

        viewModelScope.launch {
            val previewBitmap = withContext(Dispatchers.Default) {
                ImageProcessor.createPreviewWithEmojis(originalBitmap, faces, emoji, newCoveredIndices)
            }

            _state.value = _state.value.copy(
                coveredFaceIndices = newCoveredIndices,
                previewBitmap = previewBitmap
            )
        }
    }

    fun findFaceAtPosition(x: Float, y: Float, bitmapWidth: Int, bitmapHeight: Int, viewWidth: Float, viewHeight: Float): Int? {
        val faces = _state.value.detectedFaces
        val bitmap = _state.value.originalBitmap ?: return null

        val bitmapAspect = bitmap.width.toFloat() / bitmap.height
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

        val bitmapX = ((x - offsetX) / displayedWidth * bitmap.width).toInt()
        val bitmapY = ((y - offsetY) / displayedHeight * bitmap.height).toInt()

        faces.forEachIndexed { index, face ->
            val bounds = face.boundingBox
            val padding = (bounds.width() * 0.2f).toInt()
            val expandedBounds = android.graphics.Rect(
                bounds.left - padding,
                bounds.top - padding,
                bounds.right + padding,
                bounds.bottom + padding
            )
            if (expandedBounds.contains(bitmapX, bitmapY)) {
                return index
            }
        }
        return null
    }

    fun saveImage(context: Context) {
        val previewBitmap = _state.value.previewBitmap ?: return
        if (_state.value.coveredFaceIndices.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val uri = withContext(Dispatchers.IO) {
                ImageProcessor.saveToGallery(context, previewBitmap)
            }

            _state.value = _state.value.copy(
                isLoading = false,
                saveSuccess = uri != null,
                errorMessage = if (uri == null) "Failed to save image" else null
            )
        }
    }

    fun getShareUri(context: Context): Uri? {
        val previewBitmap = _state.value.previewBitmap ?: return null
        return ImageProcessor.saveToCache(context, previewBitmap)
    }

    fun clearSaveSuccess() {
        _state.value = _state.value.copy(saveSuccess = false)
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun reset() {
        _state.value = FaceMojiState(emojiSlots = loadSavedEmojis())
    }

    override fun onCleared() {
        super.onCleared()
        faceDetector.close()
    }
}
