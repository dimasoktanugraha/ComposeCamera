package com.artera.composecamera.ui.screen.camera

import android.Manifest
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FlashOff
import androidx.compose.material.icons.sharp.FlashOn
import androidx.compose.material.icons.sharp.Timer3
import androidx.compose.material.icons.sharp.TimerOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artera.composecamera.R
import com.artera.composecamera.di.Injection
import com.artera.composecamera.ui.ViewModelFactory
import com.artera.composecamera.ui.common.CameraUIAction
import com.artera.composecamera.ui.component.CameraControl
import com.artera.composecamera.ui.component.CameraControls
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel(
        factory = ViewModelFactory(Injection.provideRepository())
    ),
    navigateBack: () -> Unit,
) {

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
        )
    )

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    PermissionsRequired(
        multiplePermissionsState = permissionState,
        permissionsNotGrantedContent = {
            navigateBack()
                                       },
        permissionsNotAvailableContent = {
            navigateBack()
        }
    ) {
        CameraView(onImageCaptured = { uri, fromGallery ->
            Log.d("CAMERATEST", "Image : $uri")
        }, onError = { imageCaptureException ->
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Error : $imageCaptureException")
            }
        })
    }
}

@Composable
fun CameraView(
    onImageCaptured: (Uri, Boolean) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val context = LocalContext.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var timer by remember { mutableStateOf(0) }
    var counterTimer by remember { mutableStateOf(0) }
    var startTimer by remember { mutableStateOf(false) }
    var isBlink by remember { mutableStateOf(false) }
    var isFlash by remember { mutableStateOf(false) }
    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder().build()
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onImageCaptured(uri, true)
    }

    CameraPreviewView(
        imageCapture,
        lensFacing,
        counterTimer,
        startTimer,
        isBlink,
        isFlash,
        cameraUIAction = { action ->
            when (action) {
                is CameraUIAction.OnCameraClick -> {
                    if (timer == 0 || counterTimer == 0) {
                        startTimer = false
                        isBlink = true
//                        imageCapture.takePicture(context, lensFacing, onImageCaptured, onError)
                        counterTimer = timer

                        Log.d("CAMERATEST", "capture clicked")
                    }else{
                        startTimer = true
                    }
                }
                is CameraUIAction.OnSwitchCameraClick -> {
                    lensFacing =
                        if (lensFacing == CameraSelector.LENS_FACING_BACK)
                            CameraSelector.LENS_FACING_FRONT
                        else
                            CameraSelector.LENS_FACING_BACK
                }
                is CameraUIAction.OnTimerCameraClick -> {
                    val time = if (timer == 0) 3 else 0
                    timer = time
                    counterTimer = timer
                }
                is CameraUIAction.OnFlashCameraClick -> {
                    isFlash = !isFlash
                    Log.d("CAMERATEST", "isFlash $isFlash")
                }
                is CameraUIAction.OnGalleryViewClick -> {
                    if (true == context.getOutputDirectory().listFiles()?.isNotEmpty()) {
                        galleryLauncher.launch("image/*")
                    }
                }
            }
        },
        outputTimer = { output ->
            counterTimer = output
        },
        blinkDone = { ->
            isBlink = false
        }
    )
}

@Composable
private fun CameraBlinkView(isBlink: Boolean = false, ){
    if (isBlink){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        )
    }
}

@Composable
private fun CameraPreviewView(
    imageCapture: ImageCapture,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    counterTimer: Int = 0,
    startTimer: Boolean = false,
    isBlink: Boolean = false,
    isFlash: Boolean = false,
    cameraUIAction: (CameraUIAction) -> Unit,
    outputTimer: (Int) -> Unit,
    blinkDone: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    val previewView = remember { PreviewView(context) }
    LaunchedEffect(lensFacing, isFlash) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        camera.cameraControl.enableTorch(isFlash)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    LaunchedEffect(startTimer, counterTimer) {
        if (startTimer){
            if (counterTimer == 0){
                cameraUIAction(CameraUIAction.OnCameraClick)
            }else{
                delay(1000L)
                var counter = counterTimer
                counter--
                outputTimer(counter)
            }
        }
    }

    LaunchedEffect(isBlink) {
        if (isBlink){
            delay(50L)
            blinkDone()
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .padding()
            .fillMaxSize(),
    ) {
        val (cameraView, controlButton, timerButton, flashButton, counterText) = createRefs()

        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(cameraView) {}
        )
        CameraControls(
            cameraUIAction,
            modifier = Modifier
                .constrainAs(controlButton){
                    bottom.linkTo(parent.bottom, margin = 20.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
        )
        CameraControl(
            if (counterTimer == 0) Icons.Sharp.TimerOff else Icons.Sharp.Timer3,
            R.string.camera_timer,
            modifier= Modifier
                .size(30.dp)
                .constrainAs(timerButton) {
                    top.linkTo(parent.top, margin = 40.dp)
                    end.linkTo(parent.end, margin = 40.dp)
                },
            onClick = { cameraUIAction(CameraUIAction.OnTimerCameraClick)  }
        )
        CameraControl(
            if (isFlash) Icons.Sharp.FlashOn else Icons.Sharp.FlashOff,
            R.string.camera_timer,
            modifier= Modifier
                .size(30.dp)
                .constrainAs(flashButton) {
                    top.linkTo(parent.top, margin = 40.dp)
                    start.linkTo(parent.start, margin = 40.dp)
                },
            onClick = { cameraUIAction(CameraUIAction.OnFlashCameraClick)  }
        )
        if (startTimer && counterTimer != 0){
            Text(
                text = counterTimer.toString(),
                fontSize = 150.sp,
                color = Color.White,
                modifier = Modifier
                    .alpha(0.7f)
                    .constrainAs(counterText) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
        }
        CameraBlinkView(isBlink)
    }
}

private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
private const val PHOTO_EXTENSION = ".jpg"

fun ImageCapture.takePicture(
    context: Context,
    lensFacing: Int,
    onImageCaptured: (Uri, Boolean) -> Unit,
    onError: (ImageCaptureException) -> Unit,
) {
    val outputDirectory = context.getOutputDirectory()
    // Create output file to hold the image
    val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
    val outputFileOptions = getOutputFileOptions(lensFacing, photoFile)

    this.takePicture(
        outputFileOptions,
        Executors.newSingleThreadExecutor(),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                // If the folder selected is an external media directory, this is
                // unnecessary but otherwise other apps will not be able to access our
                // images unless we scan them using [MediaScannerConnection]
                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(savedUri.toFile().extension)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(savedUri.toFile().absolutePath),
                    arrayOf(mimeType)
                ) { _, uri ->

                }
                onImageCaptured(savedUri, false)
            }
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        })
}

fun getOutputFileOptions(
    lensFacing: Int,
    photoFile: File,
): ImageCapture.OutputFileOptions {

    // Setup image capture metadata
    val metadata = ImageCapture.Metadata().apply {
        // Mirror image when using the front camera
        isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
    }
    // Create output options object which contains file + metadata
    return ImageCapture.OutputFileOptions.Builder(photoFile)
        .setMetadata(metadata)
        .build()
}

fun createFile(baseFolder: File, format: String, extension: String) =
    File(
        baseFolder, SimpleDateFormat(format, Locale.US)
            .format(System.currentTimeMillis()) + extension
    )


fun Context.getOutputDirectory(): File {
    val mediaDir = this.externalMediaDirs.firstOrNull()?.let {
        File(it, this.resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else this.filesDir
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

