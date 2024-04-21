package ru.bestteam.virtualwear.app.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView
import ru.bestteam.virtualwear.feature.camera.domain.ScreenSize
import ru.bestteam.virtualwear.feature.imageRecognition.domain.model.PosePoint

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val mainState by viewModel.mainState.collectAsState()
    val debugState by viewModel.debugState.collectAsState()

    when (mainState) {
        MainScreenState.Default -> viewModel.checkPermissions()

        MainScreenState.PermissionCheck -> CheckPermission(viewModel::checkPermissions)
        is MainScreenState.PermissionError -> PermissionError(
            errorState = mainState as MainScreenState.PermissionError,
            openSettings = viewModel::openAppSettings,
            check = viewModel::checkPermissions
        )

        is MainScreenState.ArState -> ARPreview(
            state = (mainState as MainScreenState.ArState),
            debugState = debugState,
            updateFrame = viewModel::updateFrame
        )
    }
}

@Composable
fun ARPreview(
    state: MainScreenState.ArState,
    debugState: MainScreenDebugState,
    updateFrame: (session: Session, frame: Frame) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {

        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val materialLoader = rememberMaterialLoader(engine)
        val cameraNode = rememberARCameraNode(engine)
        val childNodes = rememberNodes()
        val view = rememberView(engine)
        val collisionSystem = rememberCollisionSystem(view)

        var previewSize by remember { mutableStateOf(ScreenSize()) }

        val modelInstances = remember { mutableListOf<ModelInstance>() }
        var trackingFailureReason by remember {
            mutableStateOf<TrackingFailureReason?>(null)
        }

        LaunchedEffect(state is MainScreenState.ModelAnchored) {
            if (state is MainScreenState.ModelAnchored) {
                childNodes += createAnchorNode(
                    modelPath = state.modelPath,
                    engine = engine,
                    modelLoader = modelLoader,
                    materialLoader = materialLoader,
                    modelInstances = modelInstances,
                    anchor = state.anchor
                )
            }
        }

        ARScene(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    previewSize = ScreenSize(
                        height = it.height.toFloat(),
                        width = it.width.toFloat()
                    )
                }
                .drawDebugPoints(debugState),
            childNodes = childNodes,
            engine = engine,
            view = view,
            modelLoader = modelLoader,
            collisionSystem = collisionSystem,
            sessionConfiguration = { session, config ->
                config.depthMode =
                    when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                        true -> Config.DepthMode.AUTOMATIC
                        else -> Config.DepthMode.DISABLED
                    }
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                config.lightEstimationMode =
                    Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },
            cameraNode = cameraNode,
            planeRenderer = false,
            onTrackingFailureChanged = {
                trackingFailureReason = it
            },
            onSessionUpdated = updateFrame,
        )
    }
}

private fun Modifier.drawDebugPoints(state: MainScreenDebugState): Modifier {
    return if (state is MainScreenDebugState.PointsDetected) {
        drawPoints(state.detectedPoints)
    } else {
        this
    }
}

private fun Modifier.drawPoints(points: List<PosePoint>): Modifier {
    return drawWithContent {
        drawContent()
        points.forEach { point ->
            drawCircle(
                color = Color.Yellow, radius = 2.dp.toPx(), center = Offset(
                    point.coordinate.x, point.coordinate.y
                )
            )
        }
    }
}

@Composable
private fun CheckPermission(
    check: () -> Unit
) {
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
                    onClick = check,
                ) {
                    Text(
                        text = "Повторить",
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionError(
    errorState: MainScreenState.PermissionError,
    openSettings: () -> Unit,
    check: () -> Unit
) {
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
                        onClick = openSettings,
                    ) {
                        Text(
                            text = "Настройки приложения",
                        )
                    }
                } else {
                    Button(
                        modifier = Modifier
                            .padding(12.dp),
                        onClick = check,
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


// https://github.com/googlesamples/arcore-ml-sample/blob/main/app/src/main/java/com/google/ar/core/examples/java/ml/AppRenderer.kt
fun createAnchorNode(
    modelPath: String?,
    engine: Engine,
    modelLoader: ModelLoader,
    materialLoader: MaterialLoader,
    modelInstances: MutableList<ModelInstance>,
    anchor: Anchor
): AnchorNode {
    val anchorNode = AnchorNode(engine = engine, anchor = anchor)
    val modelNode = ModelNode(
        modelInstance = modelInstances.apply {
            if (isEmpty() && modelPath != null) {
                this += modelLoader.createInstancedModel(modelPath, 1)
            }
        }.removeLast(),
        // Scale to fit in a 0.5 meters cube
        scaleToUnits = 0.5f,
        centerOrigin = Position(x = 0.0f, y = -1.0f, z = 0.0f)
    ).apply {
        // Model Node needs to be editable for independent rotation from the anchor rotation
        isEditable = true
    }
    val boundingBoxNode = CubeNode(
        engine,
        size = modelNode.extents,
        center = modelNode.center,
        materialInstance = materialLoader.createColorInstance(Color.White.copy(alpha = 0.5f))
    ).apply {
        isVisible = false
    }
    modelNode.addChildNode(boundingBoxNode)
    anchorNode.addChildNode(modelNode)

    listOf(modelNode, anchorNode).forEach {
        it.onEditingChanged = { editingTransforms ->
            boundingBoxNode.isVisible = editingTransforms.isNotEmpty()
        }
    }
    return anchorNode
}

//@OptIn(ExperimentalGetImage::class)
//@Composable
//fun MainCameraPreview(
//    detectedPoints: List<PosePoint>,
//    viewModel: MainViewModel
//) {
//    val context = LocalContext.current
//    var previewSize by remember { mutableStateOf(ScreenSize()) }
//
//    val analyzer = remember {
//        ImageAnalysis.Analyzer { imageProxy ->
//            if (imageProxy.image != null) {
//                viewModel.processImage(
//                    imageProxy.toBitmap(),
//                    previewSize,
//                    imageProxy.imageInfo.rotationDegrees
//                )
//            }
//            imageProxy.close()
//        }
//    }
//
//    val controller = remember {
//        LifecycleCameraController(context).apply {
//            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
//            setImageAnalysisAnalyzer(
//                ContextCompat.getMainExecutor(context), analyzer
//            )
//            imageAnalysisTargetSize = CameraController.OutputSize(Size(640, 480))
//            previewTargetSize = CameraController.OutputSize(Size(640, 480))
//        }
//    }
//
//    BoxWithConstraints(
//        modifier = Modifier
//            .fillMaxSize()
//    ) {
//        Column(Modifier.fillMaxWidth()) {
//            CameraPreview(controller,
//                Modifier
//                    .fillMaxSize()
//                    .onSizeChanged {
//                        previewSize = ScreenSize(
//                            height = it.height.toFloat(),
//                            width = it.width.toFloat()
//                        )
//                    }
//                    .drawWithContent {
//                        drawContent()
//
//                        detectedPoints.forEach { point ->
//                            drawCircle(
//                                color = Color.Yellow, radius = 2.dp.toPx(), center = Offset(
//                                    point.coordinate.x, point.coordinate.y
//                                )
//                            )
//                        }
//                    }
//            )
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.TopCenter)
//        ) {
//            Text(
//                text = "",
////                text = points.joinToString(separator = ", ") {
////                    it.bodyPart.name + "(${it.score.format(2)})"
////                    //"(${it.coordinate.x.format(3)})" +
////                    //"(${it.coordinate.y.format(3)})"
////                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(MaterialTheme.colorScheme.primaryContainer)
//                    .padding(8.dp),
//                textAlign = TextAlign.Center,
//                fontSize = 8.sp,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//
//    }
//
//    val convertFloats = FloatArray(4)
//    val convertFloatsOut = FloatArray(4)
//
//    /** Create an anchor using (x, y) coordinates in the [Coordinates2d.IMAGE_PIXELS] coordinate space. */
//    fun createAnchor(xImage: Float, yImage: Float, frame: Frame): Anchor? {
//        convertFloats[0] = xImage
//        convertFloats[1] = yImage
//        frame.transformCoordinates2d(
//            Coordinates2d.IMAGE_PIXELS,
//            convertFloats,
//            Coordinates2d.VIEW,
//            convertFloatsOut
//        )
//
//        // Conduct a hit test using the VIEW coordinates
//        val hits = frame.hitTest(convertFloatsOut[0], convertFloatsOut[1])
//        val result = hits.getOrNull(0) ?: return null
//        return result.trackable.createAnchor(result.hitPose)
//    }
//}


