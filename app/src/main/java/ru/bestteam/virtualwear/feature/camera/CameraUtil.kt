package ru.bestteam.virtualwear.feature.camera

import android.R.attr.viewportHeight
import android.R.attr.viewportWidth
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.view.Surface
import android.view.WindowManager

class CameraUtil(
    private val cameraManager: CameraManager,
    private val windowManager: WindowManager
) {
    fun getCameraSensorRelativeViewportAspectRatio(cameraId: String): Float {
        val aspectRatio: Float
        val cameraSensorToDisplayRotation: Int = getCameraSensorToDisplayRotation(cameraId)
        aspectRatio = when (cameraSensorToDisplayRotation) {
            90, 270 -> viewportHeight.toFloat() / viewportWidth.toFloat()
            0, 180 -> viewportWidth.toFloat() / viewportHeight.toFloat()
            else -> throw RuntimeException("Unhandled rotation: $cameraSensorToDisplayRotation")
        }
        return aspectRatio
    }

    @Suppress("DEPRECATION")
    fun getCameraSensorToDisplayRotation(cameraId: String): Int {
        val characteristics: CameraCharacteristics = try {
            cameraManager.getCameraCharacteristics(cameraId)
        } catch (e: CameraAccessException) {
            throw java.lang.RuntimeException("Unable to determine display orientation", e)
        }

        // Camera sensor orientation.
        val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!


        val displayOrientation: Int = toDegrees(windowManager.defaultDisplay.rotation)

        // Make sure we return 0, 90, 180, or 270 degrees.
        return (sensorOrientation - displayOrientation + 360) % 360
    }

    private fun toDegrees(rotation: Int): Int {
        return when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> throw java.lang.RuntimeException("Unknown rotation $rotation")
        }
    }
}
