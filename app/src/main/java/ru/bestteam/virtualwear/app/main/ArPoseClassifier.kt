package ru.bestteam.virtualwear.app.main

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import ru.bestteam.virtualwear.core.takeEach
import ru.bestteam.virtualwear.feature.camera.CameraUtil
import ru.bestteam.virtualwear.feature.camera.domain.ScreenSize
import ru.bestteam.virtualwear.feature.imageRecognition.convertYuv
import ru.bestteam.virtualwear.feature.imageRecognition.domain.ImagePoseClassifier
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint
import ru.bestteam.virtualwear.feature.imageRecognition.tryAcquireCameraImage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArPoseClassifier @Inject constructor(
    private val imagePoseClassifier: ImagePoseClassifier,
    private val cameraUtil: CameraUtil,
) {
    fun detectPoints(arSessionFrame: Flow<ArSessionFrame>): Flow<List<PosePoint>> {
        return arSessionFrame
            .filterNotNull()
            .takeEach(FRAME_RATE)
            .debounce(FRAME_UPDATE_INTERVAL)
            .mapNotNull(cameraUtil::getMainProcessImage)
            .mapLatest(imagePoseClassifier::classify)
            .filter { it.isNotEmpty() }
    }

    companion object {
        const val FRAME_UPDATE_INTERVAL = 0L
        const val FRAME_RATE = 30
    }
}

private data class MainProcessImage(
    val bitmap: Bitmap,
    val timeStamp: Long,
    val screenSize: ScreenSize,
    val rotationDegrees: Int
)

private fun CameraUtil.getMainProcessImage(arSessionFrame: ArSessionFrame): MainProcessImage? {
    val image = arSessionFrame.frame.tryAcquireCameraImage() ?: return null

    return MainProcessImage(
        bitmap = image.convertYuv(),
        timeStamp = arSessionFrame.frame.timestamp,
        screenSize = ScreenSize(height = image.height.toFloat(), width = image.width.toFloat()),
        rotationDegrees = getCameraSensorToDisplayRotation(arSessionFrame.session.cameraConfig.cameraId)
    ).also { image.close() }
}

private suspend fun ImagePoseClassifier.classify(image: MainProcessImage): List<PosePoint> {
    return classify(
        inputImage = image.bitmap,
        screenSize = image.screenSize,
        rotation = image.rotationDegrees
    )
}