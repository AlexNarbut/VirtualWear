package ru.bestteam.virtualwear.feature.camera.presentation

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {
            PreviewView(it).apply {
                scaleType = PreviewView.ScaleType.FILL_START
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        onRelease = {
            controller.unbind()
        },
        modifier = modifier
    )
}