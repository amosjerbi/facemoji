package com.facemoji.app.detection

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.sqrt

data class DetectedFace(
    val boundingBox: Rect,
    val isChild: Boolean
)

class FaceDetector {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.1f)
        .build()

    private val detector = FaceDetection.getClient(options)

    suspend fun detectFaces(bitmap: Bitmap): List<DetectedFace> = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)

        detector.process(image)
            .addOnSuccessListener { faces ->
                val detectedFaces = faces.map { face ->
                    DetectedFace(
                        boundingBox = face.boundingBox,
                        isChild = isChildFace(face)
                    )
                }
                continuation.resume(detectedFaces)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }

        continuation.invokeOnCancellation {
            // ML Kit handles cancellation internally
        }
    }

    /**
     * Estimates if a face belongs to a child based on facial proportions.
     * Children typically have:
     * - Rounder faces (width/height ratio closer to 1)
     * - Eyes positioned lower on face (larger forehead)
     * - Larger eye distance relative to face width
     * - Smaller overall face size in group photos
     */
    private fun isChildFace(face: Face): Boolean {
        val boundingBox = face.boundingBox
        val faceWidth = boundingBox.width().toFloat()
        val faceHeight = boundingBox.height().toFloat()

        // Get landmarks
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
        val noseBase = face.getLandmark(FaceLandmark.NOSE_BASE)?.position
        val mouthBottom = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position

        var childScore = 0

        // 1. Face roundness - children have rounder faces
        val aspectRatio = faceWidth / faceHeight
        if (aspectRatio > 0.75f) {
            childScore += 2  // Rounder face suggests child
        }

        // 2. Eye position - children's eyes are in the lower half of the face (bigger forehead)
        if (leftEye != null && rightEye != null) {
            val eyeCenterY = (leftEye.y + rightEye.y) / 2
            val relativeEyePosition = (eyeCenterY - boundingBox.top) / faceHeight

            // Children's eyes are typically at 45-60% from top (larger forehead)
            // Adults' eyes are typically at 35-45% from top
            if (relativeEyePosition > 0.42f) {
                childScore += 2
            }

            // 3. Inter-eye distance relative to face width
            // Children have relatively larger eye spacing
            val eyeDistance = distance(leftEye, rightEye)
            val relativeEyeDistance = eyeDistance / faceWidth
            if (relativeEyeDistance > 0.32f) {
                childScore += 1
            }
        }

        // 4. Face proportions - forehead to chin ratio
        if (noseBase != null && mouthBottom != null && leftEye != null && rightEye != null) {
            val eyeCenterY = (leftEye.y + rightEye.y) / 2
            val foreheadHeight = eyeCenterY - boundingBox.top
            val lowerFaceHeight = boundingBox.bottom - eyeCenterY

            val foreheadRatio = foreheadHeight / lowerFaceHeight
            // Children have proportionally larger foreheads
            if (foreheadRatio > 0.65f) {
                childScore += 2
            }
        }

        // 5. Face size heuristic - in group photos, children's faces are often smaller
        val faceSizeRatio = (faceWidth * faceHeight) / 100000f
        if (faceSizeRatio < 0.8f) {
            childScore += 1
        }

        // Lower threshold: score >= 2 suggests child (more sensitive)
        return childScore >= 2
    }

    private fun distance(p1: PointF, p2: PointF): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }

    fun close() {
        detector.close()
    }
}
