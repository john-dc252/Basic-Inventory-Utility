package io.github.john_dc252.utils.basicinventory.composables

import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.coroutines.suspendCoroutine

///
/// CODE HERE IS MOSTLY JUST FROM https://proandroiddev.com/goodbye-androidview-camerax-goes-full-compose-4d21ca234c4e
///

const val SCAN_STATUS_STATE_KEY = "scanStatus"

sealed interface ScanStatus: Parcelable {
    @Parcelize
    object Requested : ScanStatus {}

    @Parcelize
    data class Success(val code: String) : ScanStatus
}

@Composable
fun CameraScanner(
    modifier: Modifier = Modifier,
    onCodeCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val asyncScope = rememberCoroutineScope()
    var isBusy by rememberSaveable { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val surfaceRequests = remember { MutableStateFlow<SurfaceRequest?>(null) }
    val surfaceRequest by surfaceRequests.collectAsState(initial = null)

    // remember current lens
    var useFront by rememberSaveable { mutableStateOf(false) }
    val selector =
        if (useFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

    // bind when camera selector changes (front/back camera)
    LaunchedEffect(selector) {
        isBusy = true
        val provider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { req -> surfaceRequests.value = req }
        }
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, selector, imageCapture, preview)
        isBusy = false
    }

    Box(Modifier.fillMaxSize()) {
        surfaceRequest?.let { req ->
            CameraXViewfinder(surfaceRequest = req, modifier = Modifier.fillMaxSize())
        }

        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        color = Color.Gray.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .width(128.dp)
                    .height(128.dp)
                    .padding(16.dp)
            )
            return@Box
        }

        FloatingActionButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) { Icon(Icons.Rounded.Cancel, contentDescription = "Cancel") }

        FloatingActionButton(
            onClick = {
                isBusy = true
                asyncScope.launch {
                    try {
                        val capturedImage = capturePhoto(context, imageCapture!!)
                        val capturedCode = scanForCode(capturedImage)

                        if (capturedCode != null) {
                            val capturedIsbn = capturedCode.rawValue!!
                            Toast.makeText(context, "ISBN: $capturedIsbn", Toast.LENGTH_SHORT)
                                .show()
                            onCodeCaptured(capturedIsbn)
                        } else {
                            Toast.makeText(context, "No barcode found", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }.invokeOnCompletion {
                    isBusy = false
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { Icon(Icons.Rounded.Camera, contentDescription = "Cancel") }

        FloatingActionButton(
            onClick = { useFront = !useFront },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) { Icon(Icons.Rounded.Cameraswitch, contentDescription = "Switch camera") }
    }
}

private suspend fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
): InputImage {
    return suspendCoroutine { continuation ->
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        image.use { image ->
                            val inputImage =
                                InputImage.fromBitmap(image.toBitmap(), image.imageInfo.rotationDegrees)

                            continuation.resumeWith(Result.success(inputImage))
                        }
                    } catch (e: Throwable) {
                        continuation.resumeWith(Result.failure(e))
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    // Handle error
                    continuation.resumeWith(Result.failure(exception))
                }
            }
        )
    }
}

private val scannerOptions = BarcodeScannerOptions.Builder()
    .setBarcodeFormats(
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_QR_CODE,
    )
    .build()

suspend fun scanForCode(inputImage: InputImage): Barcode? {
    val scanner = BarcodeScanning.getClient(scannerOptions)
    val res = scanner.process(inputImage).await()
    return res.getOrNull(0)
}

