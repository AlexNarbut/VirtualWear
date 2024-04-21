package ru.bestteam.virtualwear.app.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.ar.core.Frame
import com.google.ar.core.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.icerock.moko.permissions.PermissionsController
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import ru.bestteam.virtualwear.app.base.BaseViewModel
import ru.bestteam.virtualwear.core.switchIf
import ru.bestteam.virtualwear.core.switchIfInstance
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.BodyPart
import ru.bestteam.virtualwear.feature.permission.api.PermissionCheckResult
import ru.bestteam.virtualwear.feature.permission.api.PermissionChecker
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val permissionsController: PermissionsController,
    private val permissionChecker: PermissionChecker,
    private val arPoseClassifier: ArPoseClassifier,
) : BaseViewModel() {
    private val _arSessionFrames = MutableStateFlow<ArSessionFrame?>(null)

    private val _mainState = MutableStateFlow<MainScreenState>(MainScreenState.Default)
    val mainState = _mainState
        .onStart {
            detectPoints()
            anchorModel()
            debugLog()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, _mainState.value)

    val debugState = debugState()
        .stateIn(viewModelScope, SharingStarted.Lazily, MainScreenDebugState.PointsNotDetected)

    fun updateFrame(session: Session, frame: Frame) {
        _arSessionFrames.value = ArSessionFrame(session, frame)
    }

    fun checkPermissions() {
        _mainState.value = MainScreenState.PermissionCheck

        safeLaunch {
            _mainState.value = when (val result = permissionChecker.checkCamera()) {
                is PermissionCheckResult.Denied -> MainScreenState.PermissionError(result.permissionName, false)
                is PermissionCheckResult.DeniedAlways -> MainScreenState.PermissionError(result.permissionName, true)
                PermissionCheckResult.Granted -> MainScreenState.ArPointsDetecting
            }
        }
    }

    fun openAppSettings() {
        permissionsController.openAppSettings()
    }

    private fun detectPoints() {
        Log.d("MainScreen", "detectPoints")
        val frames = mainState
            .switchIf(_arSessionFrames) { state -> state is MainScreenState.ArPointsDetecting }
            .filterNotNull()

        arPoseClassifier.detectPoints(frames)
            .take(1)
            .onEach { points -> _mainState.value = MainScreenState.ArPointsDetected(points) }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private fun anchorModel() {
        Log.d("MainScreen", "anchorModel")
        mainState
            .switchIfInstance<MainScreenState.ArPointsDetected>()
            .mapNotNull { it.detectedPoints.find { it.bodyPart == BodyPart.LEFT_ANKLE } }
            .combine(_arSessionFrames.filterNotNull()) { point, arSessionFrame -> point to arSessionFrame.frame }
            .mapNotNull { (point, frame) ->
                frame.hitTestInstantPlacement(point.coordinate.x, point.coordinate.y, 0.5f)
                    .find {
                        it.isValid(
                            depthPoint = false,
                            point = false
                        )
                    }
            }
            .mapNotNull { hitResult -> hitResult.createAnchorOrNull() }
            .take(1)
            .onEach { _mainState.value = MainScreenState.ModelAnchored(it, MODEL_PATH) }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private fun debugState(): Flow<MainScreenDebugState> {
        val detectedPoints = arPoseClassifier.detectPoints(_arSessionFrames.filterNotNull())
            .map { points -> MainScreenDebugState.PointsDetected(points) }

        return _arSessionFrames
            .map { frame -> frame != null }
            .distinctUntilChanged()
            .flatMapLatest { hasFrames ->
                if (hasFrames) {
                    detectedPoints
                } else {
                    flowOf(MainScreenDebugState.PointsNotDetected)
                }
            }
    }

    private fun debugLog() {
        mainState
            .onEach { state -> Log.d("MainScreen", "currentState = $state") }
            .launchIn(viewModelScope)
    }

    private companion object {
        private const val MODEL_PATH = "models/female.glb"
    }
}





