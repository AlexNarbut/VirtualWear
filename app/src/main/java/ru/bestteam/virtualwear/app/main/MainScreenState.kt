package ru.bestteam.virtualwear.app.main

import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint

sealed class MainScreenState {
    data object Default : MainScreenState()
    data object PermissionCheck : MainScreenState()
    data class PermissionError(
        val permissionName: String,
        val needOpenAppSettings: Boolean
    ) : MainScreenState()

    data class ArState(
        val modelName: String? = null,
        val detectedPoints: List<PosePoint> = emptyList()
    ) : MainScreenState()
}