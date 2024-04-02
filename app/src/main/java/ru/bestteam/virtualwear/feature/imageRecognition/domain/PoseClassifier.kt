package ru.bestteam.virtualwear.feature.imageRecognition.domain

import android.graphics.Bitmap
import ru.bestteam.virtualwear.feature.camera.domain.ScreenSize
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint

interface BitmapPoseClassifier {
    suspend fun classify(bitmap: Bitmap, screenSize: ScreenSize, rotation: Int): List<PosePoint>
}

interface ImagePoseClassifier {
    suspend fun classify(inputImage: Bitmap, screenSize: ScreenSize, rotation: Int): List<PosePoint>
}