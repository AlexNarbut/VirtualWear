package ru.bestteam.virtualwear.app.main

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import ru.bestteam.virtualwear.app.base.BaseViewModel
import ru.bestteam.virtualwear.feature.camera.domain.ScreenSize
import ru.bestteam.virtualwear.feature.imageRecognition.data.classifier.MlPoseClassifier
import ru.bestteam.virtualwear.feature.imageRecognition.data.classifier.TensorPoseClassifier
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val permissionsController: PermissionsController
) : BaseViewModel() {

    private val _mainState = MutableStateFlow<MainScreenState>(MainScreenState.Default)
    val mainState: StateFlow<MainScreenState> = _mainState

    private val _inputMainProcessImage = MutableSharedFlow<MainProcessImage>(
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val inputMainProcessImage = _inputMainProcessImage.asSharedFlow()

    private var listenImagesJob: Job? = null

    private val tensorImageClassifier = TensorPoseClassifier(context)
    private val mlPoseClassifier = MlPoseClassifier()

    init {
        prepareScan()
    }

    private fun startListenImages() {
        listenImagesJob?.cancel()
        listenImagesJob = safeLaunch(Dispatchers.Default) {
            _mainState.update { MainScreenState.CameraPreview() }
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

                    _mainState.update { MainScreenState.CameraPreview(points) }
                }
        }
    }

    fun prepareScan() {
        safeLaunch {
            val isAvailable = checkPermission()
            if (!isAvailable) return@safeLaunch
            startListenImages()
        }
    }

    private suspend fun checkPermission(): Boolean {
        _mainState.update { MainScreenState.PermissionCheck }
        PERMISSION_LIST.forEach { permission ->
            try {
                val state = permissionsController.getPermissionState(permission)
                if (state != PermissionState.Granted) {
                    permissionsController.providePermission(permission)
                }
            } catch (ex: DeniedAlwaysException) {
                _mainState.update { MainScreenState.PermissionError(permission.name, true) }
                return false
            } catch (ex: DeniedException) {
                _mainState.update { MainScreenState.PermissionError(permission.name, false) }
                return false
            }
        }
        return true
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
        private const val IMAGE_DEBOUNCE = 4L

        private val PERMISSION_LIST = listOf(
            Permission.CAMERA
        )
    }
}

private data class MainProcessImage(
    val bitmap: Bitmap,
    val screenSize: ScreenSize,
    val rotationDegrees: Int
)
