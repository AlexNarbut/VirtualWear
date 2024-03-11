package ru.bestteam.virtualwear.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.bestteam.virtualwear.R
import ru.bestteam.virtualwear.databinding.AcitivtyMainBinding

class MainActivity : AppCompatActivity(R.layout.acitivty_main) {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                initCamera()
                openCamera()
            } else {
                Toast.makeText(this, "Not permission", Toast.LENGTH_LONG)
            }
        }

    private var cameraManager: CameraManager? = null

    private val paint = Paint().apply {
        color = Color.GREEN
    }

    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

//    lateinit var imageProcessor: ImageProcessor
//    lateinit var model: LiteModelMovenetSingleposeLightningTfliteFloat164
//    lateinit var bitmap: Bitmap

    private val binding by viewBinding(AcitivtyMainBinding::bind, R.id.container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.acitivty_main)

        binding.textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                checkPermission()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean = false

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                val bitmap = binding.textureView.bitmap
            }

        }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                initCamera()
                openCamera()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.CAMERA
            ) -> {
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    private fun initCamera() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val thread = HandlerThread("videoThread")
        handlerThread = thread
        handlerThread?.start()
        handler = Handler(thread.looper)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager?.let { manager ->
            val textureView = binding.textureView.surfaceTexture
            manager.openCamera(
                manager.cameraIdList[0], object : CameraDevice.StateCallback() {
                    override fun onOpened(p0: CameraDevice) {
                        val captureRequest = p0.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                        val surface = Surface(textureView)
                        captureRequest.addTarget(surface)
                        p0.createCaptureSession(
                            listOf(surface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(p0: CameraCaptureSession) {
                                    p0.setRepeatingRequest(captureRequest.build(), null, null)
                                }

                                override fun onConfigureFailed(p0: CameraCaptureSession) {
                                    val a = p0
                                }
                            },
                            handler
                        )
                    }

                    override fun onDisconnected(p0: CameraDevice) {
                        val camera = p0
                    }

                    override fun onError(p0: CameraDevice, p1: Int) {
                        val error = p0
                    }
                }, handler
            )
        }
    }


    companion object {
        private const val CAMERA_PERMISSION_CODE = 4234
    }
}