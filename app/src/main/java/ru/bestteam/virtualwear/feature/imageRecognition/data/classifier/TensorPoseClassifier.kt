package ru.bestteam.virtualwear.feature.imageRecognition.data.classifier

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import ru.bestteam.virtualwear.feature.camera.domain.ScreenSize
import ru.bestteam.virtualwear.feature.imageRecognition.domain.BitmapPoseClassifier
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.BodyPart
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.BodyPart.Companion.KEY_POINT_NUMBER
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PointCoordinate
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint
import ru.bestteam.virtualwear.ml.AutoModel4

class TensorPoseClassifier(
    context: Context,
    private val minScore: Float = 0.4f
) : BitmapPoseClassifier {
    private val poseModel: AutoModel4

    init {
        poseModel = AutoModel4.newInstance(context)
    }

    override suspend fun classify(
        bitmap: Bitmap,
        screenSize: ScreenSize,
        rotation: Int,
    ): List<PosePoint> {

        val bitmapWidth: Int = bitmap.width
        val bitmapHeight: Int = bitmap.height

        val size = if (bitmapHeight > bitmapWidth) bitmapWidth else bitmapHeight

        val imageProcessor = ImageProcessor.Builder()
            .apply {
                add(Rot90Op(-rotation / 90)).build()
                add(ResizeWithCropOrPadOp(size, size))
                add(ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
            }
            .build()

        val tensorImage = imageProcessor.process(
            TensorImage.fromBitmap(bitmap)
        )

        val output = poseModel.process(tensorImage.tensorBuffer)

        val outputArray = output.outputFeature0AsTensorBuffer.floatArray

        val posePoints = mutableListOf<PosePoint>()

        val scaleFactorX = screenSize.width / bitmap.width
        val scaleFactorY = screenSize.height / bitmap.height
        val scaleFactor = maxOf(scaleFactorX, scaleFactorY)

        for (idx in 0 until KEY_POINT_NUMBER) {

            val x = outputArray[idx * 3 + 1] * bitmap.width * scaleFactor
            val y = outputArray[idx * 3 + 0] * bitmap.height * scaleFactor
            val score = outputArray[idx * 3 + 2]

            if (score >= minScore) {
                posePoints.add(
                    PosePoint(
                        BodyPart.fromTensorInt(idx),
                        PointCoordinate(
                            x,
                            y
                        ),
                        score
                    )
                )
            }
        }

        return posePoints
    }
}