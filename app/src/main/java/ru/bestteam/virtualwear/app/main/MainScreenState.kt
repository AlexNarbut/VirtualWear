package ru.bestteam.virtualwear.app.main

import com.google.ar.core.Anchor
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint

sealed interface MainScreenState {
    data object Default : MainScreenState
    data object PermissionCheck : MainScreenState
    data class PermissionError(
        val permissionName: String,
        val needOpenAppSettings: Boolean
    ) : MainScreenState

    sealed interface ArState : MainScreenState

    data object ArPointsDetecting : ArState

    data class ArPointsDetected(
        val detectedPoints: List<PosePoint>
    ) : ArState

    data class ModelAnchored(
        val anchor: Anchor,
        val modelPath: String
    ) : ArState
}

sealed interface MainScreenDebugState {
    data object PointsNotDetected : MainScreenDebugState

    data class PointsDetected(
        val detectedPoints: List<PosePoint>
    ) : MainScreenDebugState
}