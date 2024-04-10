package ru.bestteam.virtualwear.app.main

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import ru.bestteam.virtualwear.app.base.BaseViewModel
import ru.bestteam.virtualwear.feature.camera.domain.ScreenSize
import ru.bestteam.virtualwear.feature.imageRecognition.data.classifier.MlPoseClassifier
import ru.bestteam.virtualwear.feature.imageRecognition.data.classifier.TensorPoseClassifier
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context
) : BaseViewModel() {

    private val _detectedPoints = MutableStateFlow<List<PosePoint>>(emptyList())
    val detectedPoints = _detectedPoints.asStateFlow()

    private val _inputMainProcessImage = MutableSharedFlow<MainProcessImage>(
        extraBufferCapacity = 3,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val inputMainProcessImage = _inputMainProcessImage.asSharedFlow()

    private val tensorImageClassifier = TensorPoseClassifier(context)
    private val mlPoseClassifier = MlPoseClassifier()

    init {
        safeLaunch(Dispatchers.Default) {
            inputMainProcessImage
                .debounce(IMAGE_DEBOUNCE)
                .collect { processItem ->
//                val points = tensorImageClassifier.classify(
//                    processItem.bitmap,
//                    processItem.screenSize,
//                    processItem.rotationDegrees
//                )

                    val points = mlPoseClassifier.classify(
                        processItem.bitmap,
                        processItem.screenSize,
                        processItem.rotationDegrees
                    )

                    _detectedPoints.update { points }
                }
        }
    }

    fun processImage(bitmap: Bitmap, screenSize: ScreenSize, rotationDegrees: Int) {
        safeLaunch {
            _inputMainProcessImage.emit(
                MainProcessImage(
                    bitmap,
                    screenSize,
                    rotationDegrees
                )
            )
        }
    }

    private companion object {
        private const val IMAGE_DEBOUNCE = 15L
    }
}

private data class MainProcessImage(
    val bitmap: Bitmap,
    val screenSize: ScreenSize,
    val rotationDegrees: Int
)
