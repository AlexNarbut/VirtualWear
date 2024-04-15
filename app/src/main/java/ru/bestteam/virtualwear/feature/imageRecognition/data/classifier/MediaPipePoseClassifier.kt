package ru.bestteam.virtualwear.feature.imageRecognition.data.classifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import ru.bestteam.virtualwear.feature.camera.domain.ScreenSize
import ru.bestteam.virtualwear.feature.imageRecognition.domain.ImagePoseClassifier
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.BodyPart
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PointCoordinate
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint

class MediaPipePoseClassifier(
    private val context: Context,
) : ImagePoseClassifier {

    val modelName = "pose_landmarker_full.task"

    private var poseLandmarker: PoseLandmarker

    init {
        poseLandmarker = initPoseDetector()
    }


    private fun initPoseDetector(): PoseLandmarker {
        val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath(modelName)
        baseOptionsBuilder.setDelegate(Delegate.GPU)

        val optionsBuilder =
            PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinPoseDetectionConfidence(DEFAULT_POSE_DETECTION_CONFIDENCE)
                .setMinTrackingConfidence(DEFAULT_POSE_TRACKING_CONFIDENCE)
                .setMinPosePresenceConfidence(DEFAULT_POSE_PRESENCE_CONFIDENCE)
                .setNumPoses(DEFAULT_NUM_POSES)
                .setRunningMode(RunningMode.IMAGE)

        val options = optionsBuilder.build()
        return PoseLandmarker.createFromOptions(context, options)
    }


    @OptIn(ExperimentalGetImage::class)
    override suspend fun classify(
        inputImage: Bitmap,
        screenSize: ScreenSize,
        rotation: Int
    ): List<PosePoint> {

        val matrix = Matrix().apply {
            // Rotate the frame received from the camera to be in the same direction as it'll be shown
            postRotate(rotation.toFloat())

            // flip image if user use front camera
            if (false) {
                postScale(
                    -1f,
                    1f,
                    inputImage.width.toFloat(),
                    inputImage.height.toFloat()
                )
            }
        }
        val rotatedBitmap = Bitmap.createBitmap(
            inputImage,
            0, 0,
            inputImage.width, inputImage.height,
            matrix, true
        )

        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        val result = poseLandmarker.detect(mpImage)

        val scaleFactorX = screenSize.width / rotatedBitmap.width
        val scaleFactorY = screenSize.height / rotatedBitmap.height
        val scaleFactor = maxOf(scaleFactorX, scaleFactorY)

        val posePoints = mutableListOf<PosePoint>()

        result.landmarks().forEach { landmarks ->
            landmarks.forEachIndexed { index, point ->

                posePoints.add(
                    PosePoint(
                        BodyPart.fromMlInt(index),
                        PointCoordinate(
                            point.x() * scaleFactor,
                            point.y() * scaleFactor,
                            point.z()
                        ),
                        0.6f
                    )
                )
            }
        }

        return posePoints
    }

    companion object {
//        const val DELEGATE_CPU = 0
//        const val DELEGATE_GPU = 1
        const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_POSES = 31
    }
}