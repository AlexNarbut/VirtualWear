package ru.bestteam.virtualwear.feature.imageRecognition.data.imageAnalyzer

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import ru.bestteam.virtualwear.feature.imageRecognition.data.classifier.TensorPoseClassifier
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint

class TensorPoseAnalyzer(
    private val classifier: TensorPoseClassifier,
    private val onResults: (List<PosePoint>) -> Unit
): ImageAnalysis.Analyzer {

    private var frameSkipCounter = 0
    private var frameSkipNumber = 20

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if(frameSkipCounter % frameSkipNumber == 0) {
            imageProxy.image

//            val results = classifier.classify(
//                imageProxy.toBitmap(),
//                imageProxy.imageInfo.rotationDegrees)
//
//            onResults(results)
            frameSkipCounter = 0
        }
        frameSkipCounter++

        imageProxy.close()
    }
}