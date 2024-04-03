package ru.bestteam.virtualwear.feature.imageRecognition.data.classifier

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import ru.bestteam.virtualwear.core.response.alsoIfError
import ru.bestteam.virtualwear.core.response.alsoIfSuccess
import ru.bestteam.virtualwear.core.task.awaitResponse
import ru.bestteam.virtualwear.feature.camera.domain.ScreenSize
import ru.bestteam.virtualwear.feature.imageRecognition.domain.ImagePoseClassifier
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.BodyPart
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PointCoordinate
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint

class MlPoseClassifier : ImagePoseClassifier {

    private val poseDetector: PoseDetector

    init {
        poseDetector = initPoseDetector()
    }


    private fun initPoseDetector(): PoseDetector {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            //.setPreferredHardwareConfigs(1)
            .build()
        return PoseDetection.getClient(options)
    }


    @OptIn(ExperimentalGetImage::class)
    override suspend fun classify(
        inputImage: Bitmap,
        screenSize: ScreenSize,
        rotation: Int
    ): List<PosePoint> {

        val posesTask = poseDetector.process(inputImage, rotation)

        val scaleFactorX = screenSize.width / inputImage.width
        val scaleFactorY = screenSize.height / inputImage.height
        val scaleFactor = maxOf(scaleFactorX, scaleFactorY)

        posesTask.awaitResponse().alsoIfSuccess { pose ->
            val posePoints = mutableListOf<PosePoint>()
            pose.allPoseLandmarks.forEach { poseLandmark ->
                posePoints.add(
                    PosePoint(
                        BodyPart.fromMlInt(poseLandmark.landmarkType),
                        PointCoordinate(
                            poseLandmark.position3D.x * scaleFactor,
                            poseLandmark.position3D.y * scaleFactor,
                            poseLandmark.position3D.z
                        ),
                        0.6f
                    )
                )
            }
            return posePoints
        }.alsoIfError {
            return emptyList()
        }

        return emptyList()
    }
}