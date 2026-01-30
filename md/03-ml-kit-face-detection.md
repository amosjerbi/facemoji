# ML Kit Face Detection

## Dependency

```kotlin
implementation("com.google.mlkit:face-detection:16.1.6")
```

## Basic Setup

```kotlin
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetector {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.1f)  // 10% of image width minimum
        .build()

    private val detector = FaceDetection.getClient(options)

    fun close() {
        detector.close()
    }
}
```

## Performance Modes

```kotlin
// Fast - lower accuracy, faster processing
PERFORMANCE_MODE_FAST

// Accurate - higher accuracy, slower processing
PERFORMANCE_MODE_ACCURATE
```

## Landmark Detection

```kotlin
// Enable landmarks
.setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)

// Available landmarks
FaceLandmark.LEFT_EYE
FaceLandmark.RIGHT_EYE
FaceLandmark.NOSE_BASE
FaceLandmark.MOUTH_BOTTOM
FaceLandmark.MOUTH_LEFT
FaceLandmark.MOUTH_RIGHT
FaceLandmark.LEFT_EAR
FaceLandmark.RIGHT_EAR
FaceLandmark.LEFT_CHEEK
FaceLandmark.RIGHT_CHEEK

// Get landmark position
val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
// Returns PointF(x, y) or null
```

## Classification

```kotlin
// Enable classification
.setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)

// Available classifications (0.0 to 1.0 probability)
face.smilingProbability      // null if unavailable
face.leftEyeOpenProbability
face.rightEyeOpenProbability
```

## Coroutine Wrapper

```kotlin
suspend fun detectFaces(bitmap: Bitmap): List<DetectedFace> =
    suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)  // 0 = rotation

        detector.process(image)
            .addOnSuccessListener { faces ->
                val result = faces.map { face ->
                    DetectedFace(
                        boundingBox = face.boundingBox,
                        // Add any other data you need
                    )
                }
                continuation.resume(result)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }

        continuation.invokeOnCancellation {
            // ML Kit handles cancellation internally
        }
    }
```

## Face Bounding Box

```kotlin
data class DetectedFace(
    val boundingBox: Rect  // android.graphics.Rect
)

// Rect properties
boundingBox.left    // X coordinate of left edge
boundingBox.top     // Y coordinate of top edge
boundingBox.right   // X coordinate of right edge
boundingBox.bottom  // Y coordinate of bottom edge
boundingBox.width() // Width in pixels
boundingBox.height()// Height in pixels
boundingBox.centerX() // Center X coordinate
boundingBox.centerY() // Center Y coordinate
```

## InputImage Sources

```kotlin
// From Bitmap
val image = InputImage.fromBitmap(bitmap, rotationDegrees)

// From URI
val image = InputImage.fromFilePath(context, uri)

// From ByteBuffer (camera)
val image = InputImage.fromByteBuffer(
    byteBuffer,
    width, height,
    rotationDegrees,
    InputImage.IMAGE_FORMAT_NV21
)

// From MediaImage (CameraX)
val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
```

## On-Device vs Cloud

```
ML Kit Face Detection = ON-DEVICE
- No internet required
- No API key needed
- Model bundled with app (~3MB)
- Privacy: images never leave device
- Fast: ~50-200ms per image
```

## Optimization Tips

```kotlin
// 1. Reuse detector instance
private val detector = FaceDetection.getClient(options)

// 2. Close when done
override fun onCleared() {
    detector.close()
}

// 3. Scale down large images before detection
fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
    val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
    return if (scale < 1) {
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true
        )
    } else bitmap
}

// 4. Use PERFORMANCE_MODE_FAST for real-time
// 5. Use PERFORMANCE_MODE_ACCURATE for still images
```

## Error Handling

```kotlin
detector.process(image)
    .addOnSuccessListener { faces ->
        if (faces.isEmpty()) {
            // No faces detected - not an error
        }
    }
    .addOnFailureListener { e ->
        when (e) {
            is MlKitException -> {
                // ML Kit specific error
                Log.e("FaceDetector", "ML Kit error: ${e.errorCode}")
            }
            else -> {
                // General error (OOM, etc)
                Log.e("FaceDetector", "Error: ${e.message}")
            }
        }
    }
```
