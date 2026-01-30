package com.facemoji.app.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facemoji.app.FaceMojiViewModel

val TealColor = Color(0xFF009688)
val CoralColor = Color(0xFFEF5350)

@Composable
fun FaceMojiScreen(
    modifier: Modifier = Modifier,
    viewModel: FaceMojiViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var imageViewSize by remember { mutableStateOf(IntSize.Zero) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.loadImage(context, it) }
    }

    fun launchPhotoPicker() {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    LaunchedEffect(Unit) {
        launchPhotoPicker()
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            Toast.makeText(context, "Saved to gallery", Toast.LENGTH_SHORT).show()
            viewModel.clearSaveSuccess()
        }
    }

    // Emoji picker dialog for any slot
    if (state.showEmojiPicker) {
        EmojiPickerDialog(
            emojis = viewModel.customPickerEmojis,
            onEmojiSelected = { viewModel.updateSlotEmoji(it) },
            onDismiss = { viewModel.hideEmojiPicker() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Emoji Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Emoji",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )

                state.infoMessage?.let { info ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = info,
                        fontSize = 12.sp,
                        color = TealColor,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2 rows x 3 columns grid - all slots support tap-and-hold to change
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // First 3 emojis
                    state.emojiSlots.take(3).forEachIndexed { index, emoji ->
                        EmojiItem(
                            emoji = emoji,
                            isSelected = state.selectedEmoji == emoji,
                            enabled = state.detectedFaces.isNotEmpty(),
                            onClick = { viewModel.selectEmoji(emoji) },
                            onLongClick = { viewModel.showEmojiPickerForSlot(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Last 3 emojis
                    state.emojiSlots.drop(3).forEachIndexed { index, emoji ->
                        EmojiItem(
                            emoji = emoji,
                            isSelected = state.selectedEmoji == emoji,
                            enabled = state.detectedFaces.isNotEmpty(),
                            onClick = { viewModel.selectEmoji(emoji) },
                            onLongClick = { viewModel.showEmojiPickerForSlot(index + 3) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .onSizeChanged { imageViewSize = it }
                .pointerInput(state.selectedEmoji, state.detectedFaces) {
                    detectTapGestures { offset ->
                        if (state.selectedEmoji != null && state.detectedFaces.isNotEmpty()) {
                            val bitmap = state.originalBitmap
                            if (bitmap != null) {
                                val faceIndex = viewModel.findFaceAtPosition(
                                    x = offset.x,
                                    y = offset.y,
                                    bitmapWidth = bitmap.width,
                                    bitmapHeight = bitmap.height,
                                    viewWidth = imageViewSize.width.toFloat(),
                                    viewHeight = imageViewSize.height.toFloat()
                                )
                                if (faceIndex != null) {
                                    viewModel.toggleFaceCoverage(faceIndex)
                                } else {
                                    launchPhotoPicker()
                                }
                            }
                        } else {
                            launchPhotoPicker()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(color = TealColor)
                }
                state.previewBitmap != null -> {
                    Image(
                        bitmap = state.previewBitmap!!.asImageBitmap(),
                        contentDescription = "Selected photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    state.errorMessage?.let { errorMsg ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { launchPhotoPicker() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$errorMsg\nTap to select another photo",
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        text = "Tap to select a photo",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable { launchPhotoPicker() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.saveImage(context) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                enabled = state.selectedEmoji != null && state.coveredFaceIndices.isNotEmpty()
            ) {
                Text(
                    text = "Save",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = {
                    val uri = viewModel.getShareUri(context)
                    uri?.let {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/jpeg"
                            putExtra(Intent.EXTRA_STREAM, it)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share image"))
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralColor),
                enabled = state.selectedEmoji != null && state.coveredFaceIndices.isNotEmpty()
            ) {
                Text(
                    text = "Share",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmojiItem(
    emoji: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    !enabled -> Color(0xFFE0E0E0)
                    isSelected -> Color(0xFFFFE4E6)
                    else -> Color(0xFFF8F8F8)
                }
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) CoralColor else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { onLongClick() }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 36.sp,
            color = if (enabled) Color.Unspecified else Color.Gray
        )
    }
}

@Composable
private fun EmojiPickerDialog(
    emojis: List<String>,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Emoji",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(emojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8F8F8))
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 24.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = CoralColor)
                }
            }
        }
    }
}
