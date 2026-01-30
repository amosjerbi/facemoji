# Image Processing

## Loading Bitmap from URI

```kotlin
suspend fun loadBitmap(context: Context, uri: Uri): Bitmap? =
    withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }
```

## Drawing Emoji on Bitmap

```kotlin
fun createPreviewWithEmojis(
    originalBitmap: Bitmap,
    faces: List<DetectedFace>,
    emoji: String,
    coveredIndices: Set<Int>
): Bitmap {
    // Create mutable copy
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

    // Size emoji to cover face (1.3x for full coverage)
    val emojiSize = (faceRect.width() * 1.3f).coerceAtLeast(100f)
    paint.textSize = emojiSize

    // Center position
    val centerX = faceRect.centerX().toFloat()
    val centerY = faceRect.centerY().toFloat()

    // Adjust Y for text baseline
    val yOffset = (paint.descent() + paint.ascent()) / 2

    canvas.drawText(emoji, centerX, centerY - yOffset, paint)
}
```

## Saving to Gallery (MediaStore)

```kotlin
fun saveToGallery(context: Context, bitmap: Bitmap): Uri? {
    val filename = "FaceMoji_${System.currentTimeMillis()}.jpg"

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        // Android 10+ uses relative path
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/FaceMoji")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        }

        // Mark as complete on Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
    }

    return uri
}
```

## Saving to Cache for Sharing

```kotlin
fun saveToCache(context: Context, bitmap: Bitmap): Uri? {
    val sharedDir = File(context.cacheDir, "shared")
    if (!sharedDir.exists()) {
        sharedDir.mkdirs()
    }

    val file = File(sharedDir, "share_${System.currentTimeMillis()}.jpg")

    return try {
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        }

        // Use FileProvider for secure sharing
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
```

## Sharing via Intent

```kotlin
fun shareImage(context: Context, uri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share image"))
}
```

## Bitmap Scaling

```kotlin
fun scaleBitmapToFit(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    val scaleWidth = maxWidth.toFloat() / width
    val scaleHeight = maxHeight.toFloat() / height
    val scale = minOf(scaleWidth, scaleHeight, 1f)

    if (scale >= 1f) return bitmap

    return Bitmap.createScaledBitmap(
        bitmap,
        (width * scale).toInt(),
        (height * scale).toInt(),
        true  // filter for smooth scaling
    )
}
```

## Memory Management

```kotlin
// Bitmap configs
Bitmap.Config.ARGB_8888  // 4 bytes/pixel, full color
Bitmap.Config.RGB_565    // 2 bytes/pixel, no alpha

// Memory calculation
val memoryBytes = bitmap.width * bitmap.height * 4  // ARGB_8888

// Recycle when done (not needed with modern GC, but good practice)
bitmap.recycle()

// Check if mutable
if (!bitmap.isMutable) {
    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
}
```

## Drawing Shapes on Canvas

```kotlin
val canvas = Canvas(bitmap)

// Circle
val circlePaint = Paint().apply {
    color = Color.RED
    style = Paint.Style.FILL
}
canvas.drawCircle(centerX, centerY, radius, circlePaint)

// Rectangle
canvas.drawRect(left, top, right, bottom, paint)

// Rounded rectangle
canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

// Text
val textPaint = TextPaint().apply {
    textSize = 48f
    color = Color.BLACK
    textAlign = Paint.Align.CENTER
}
canvas.drawText("Hello", x, y, textPaint)

// Path (custom shapes)
val path = Path().apply {
    moveTo(x1, y1)
    lineTo(x2, y2)
    quadTo(cx, cy, x3, y3)
    close()
}
canvas.drawPath(path, paint)
```

## EXIF Orientation Handling

```kotlin
fun rotateBitmapIfRequired(bitmap: Bitmap, uri: Uri, context: Context): Bitmap {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
    val exif = ExifInterface(inputStream)

    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
    }

    return Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
    )
}
```
