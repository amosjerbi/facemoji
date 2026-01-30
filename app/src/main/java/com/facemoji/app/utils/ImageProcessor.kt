package com.facemoji.app.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextPaint
import com.facemoji.app.detection.DetectedFace
import java.io.File
import java.io.FileOutputStream

object ImageProcessor {

    fun createPreviewWithEmojis(
        originalBitmap: Bitmap,
        faces: List<DetectedFace>,
        emoji: String,
        coveredIndices: Set<Int>
    ): Bitmap {
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        faces.forEachIndexed { index, face ->
            if (index in coveredIndices) {
                drawEmojiOnFace(canvas, face.boundingBox, emoji)
            }
        }

        return mutableBitmap
    }

    private fun drawEmojiOnFace(canvas: Canvas, faceRect: Rect, emoji: String) {
        val paint = TextPaint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Make emoji slightly larger than face to fully cover it
        val emojiSize = (faceRect.width() * 1.3f).coerceAtLeast(100f)
        paint.textSize = emojiSize

        // Center emoji over face
        val centerX = faceRect.centerX().toFloat()
        val centerY = faceRect.centerY().toFloat()

        // Adjust Y position to center emoji vertically (text draws from baseline)
        val yOffset = (paint.descent() + paint.ascent()) / 2

        canvas.drawText(emoji, centerX, centerY - yOffset, paint)
    }

    fun saveToGallery(context: Context, bitmap: Bitmap): Uri? {
        val filename = "FaceMoji_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FaceMoji")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }

        return uri
    }

    fun saveToCache(context: Context, bitmap: Bitmap): Uri? {
        val sharedDir = File(context.cacheDir, "shared")
        if (!sharedDir.exists()) {
            sharedDir.mkdirs()
        }

        val file = File(sharedDir, "FaceMoji_share_${System.currentTimeMillis()}.jpg")

        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            }
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
