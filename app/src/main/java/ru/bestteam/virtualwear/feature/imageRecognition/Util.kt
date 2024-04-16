package ru.bestteam.virtualwear.feature.imageRecognition

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import com.google.ar.core.Frame
import java.io.ByteArrayOutputStream

fun Image.convertYuv(): Bitmap {
    val cameraPlaneY = planes[0].buffer
    val cameraPlaneU = planes[1].buffer
    val cameraPlaneV = planes[2].buffer

    val compositeByteArray =
        ByteArray(cameraPlaneY.capacity() + cameraPlaneU.capacity() + cameraPlaneV.capacity())

    cameraPlaneY.get(compositeByteArray, 0, cameraPlaneY.capacity())
    cameraPlaneU.get(compositeByteArray, cameraPlaneY.capacity(), cameraPlaneU.capacity())
    cameraPlaneV.get(
        compositeByteArray,
        cameraPlaneY.capacity() + cameraPlaneU.capacity(),
        cameraPlaneV.capacity()
    )

    val baOutputStream = ByteArrayOutputStream()
    val yuvImage = YuvImage(compositeByteArray, ImageFormat.NV21, width, height, null)
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, baOutputStream)
    val byteForBitmap = baOutputStream.toByteArray()
    return BitmapFactory.decodeByteArray(byteForBitmap, 0, byteForBitmap.size)
}

fun Frame.tryAcquireCameraImage(): Image? = try {
    this.acquireCameraImage()
} catch (ex: Exception) {
    null
}
