package ru.bestteam.virtualwear.app.main

import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ru.bestteam.virtualwear.feature.camera.domain.ScreenSize
import ru.bestteam.virtualwear.feature.camera.presentation.CameraPreview
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val mainState by viewModel.mainState.collectAsState()
    when (mainState) {
        MainScreenState.Default -> Unit
        MainScreenState.PermissionCheck -> {
            Column {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Проверка разрешений",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(36.dp)
                                .padding(top = 12.dp)
                        )
                        Button(
                            modifier = Modifier
                                .padding(12.dp),
                            onClick = { viewModel.prepareScan() },
                        ) {
                            Text(
                                text = "Повторить",
                            )
                        }
                    }
                }
            }
        }

        is MainScreenState.PermissionError -> {
            val errorState = mainState as MainScreenState.PermissionError
            Column {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Нет доступа к  ${errorState.permissionName}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        if (errorState.needOpenAppSettings) {
                            Button(
                                modifier = Modifier
                                    .padding(12.dp),
                                onClick = { viewModel.permissionsController.openAppSettings() },
                            ) {
                                Text(
                                    text = "Настройки приложения",
                                )
                            }
                        } else {
                            Button(
                                modifier = Modifier
                                    .padding(12.dp),
                                onClick = { viewModel.prepareScan() },
                            ) {
                                Text(
                                    text = "Повторить",
                                )
                            }
                        }
                    }
                }
            }
        }

        is MainScreenState.CameraPreview -> {
            MainCameraPreview(
                (mainState as MainScreenState.CameraPreview).detectedPoints,
                viewModel
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun MainCameraPreview(
    detectedPoints: List<PosePoint>,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var previewSize by remember { mutableStateOf(ScreenSize()) }

    val analyzer = remember {
        ImageAnalysis.Analyzer { imageProxy ->
            if (imageProxy.image != null) {
                viewModel.processImage(
                    imageProxy.toBitmap(),
                    previewSize,
                    imageProxy.imageInfo.rotationDegrees
                )
            }
            imageProxy.close()
        }
    }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context), analyzer
            )
            imageAnalysisTargetSize = CameraController.OutputSize(Size(640, 480))
            previewTargetSize = CameraController.OutputSize(Size(640, 480))
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(Modifier.fillMaxWidth()) {
            CameraPreview(controller,
                Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        previewSize = ScreenSize(
                            height = it.height.toFloat(),
                            width = it.width.toFloat()
                        )
                    }
                    .drawWithContent {
                        drawContent()

                        detectedPoints.forEach { point ->
                            drawCircle(
                                color = Color.Yellow, radius = 2.dp.toPx(), center = Offset(
                                    point.coordinate.x, point.coordinate.y
                                )
                            )
                        }
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "",
//                text = points.joinToString(separator = ", ") {
//                    it.bodyPart.name + "(${it.score.format(2)})"
//                    //"(${it.coordinate.x.format(3)})" +
//                    //"(${it.coordinate.y.format(3)})"
//                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

    }
}