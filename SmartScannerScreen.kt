package com.example.presentation.ui.components

import android.content.Context
import android.content.ContextWrapper
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

private fun Context.findLifecycleOwner(): LifecycleOwner? {
    var curContext: Context? = this
    while (curContext != null) {
        if (curContext is LifecycleOwner) {
            return curContext
        }
        curContext = if (curContext is ContextWrapper) curContext.baseContext else null
    }
    return null
}

@Composable
fun SmartScannerScreen(
    modifier: Modifier = Modifier,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    val composeLifecycleOwner = LocalLifecycleOwner.current
    val effectiveLifecycleOwner = remember(context, composeLifecycleOwner) {
        context.findLifecycleOwner() ?: composeLifecycleOwner
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(effectiveLifecycleOwner) {
        onDispose {
            try {
                val providerFuture = ProcessCameraProvider.getInstance(context)
                if (providerFuture.isDone) {
                    providerFuture.get().unbindAll()
                }
            } catch (_: Exception) {}
            cameraExecutor.shutdown()
        }
    }

    // انیمیشن خط اسکنر
    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val scanLinePosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "line_anim"
    )

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                processImage(imageProxy, onResult)
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            effectiveLifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }
        )

        // رسم مربع جستجوگر و خط متحرک
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val boxWidth = canvasWidth * 0.85f
            val boxHeight = canvasHeight * 0.75f
            val left = (canvasWidth - boxWidth) / 2
            val top = (canvasHeight - boxHeight) / 2
            val right = left + boxWidth
            val bottom = top + boxHeight
            val cornerLength = 40f

            // رسم گوشه‌های کادر اسکن به رنگ سبز
            val path = Path().apply {
                // گوشه بالا چپ
                moveTo(left, top + cornerLength)
                lineTo(left, top)
                lineTo(left + cornerLength, top)

                // گوشه بالا راست
                moveTo(right - cornerLength, top)
                lineTo(right, top)
                lineTo(right, top + cornerLength)

                // گوشه پایین راست
                moveTo(right, bottom - cornerLength)
                lineTo(right, bottom)
                lineTo(right - cornerLength, bottom)

                // گوشه پایین چپ
                moveTo(left + cornerLength, bottom)
                lineTo(left, bottom)
                lineTo(left, bottom - cornerLength)
            }
            drawPath(path, color = Color(0xFF00E676), style = Stroke(width = 6f))

            // رسم خط اسکن متحرک به رنگ قرمز/قرمز روشن
            val lineY = top + (boxHeight * scanLinePosition)
            drawLine(
                color = Color(0xFFFF1744),
                start = Offset(left + 8f, lineY),
                end = Offset(right - 8f, lineY),
                strokeWidth = 3.5f
            )
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImage(imageProxy: ImageProxy, onResult: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // استخراج و نرمال‌سازی ارقام دست‌نویس و چاپی
                val rawText = visionText.text
                val normalizedDigits = extractAndNormalizeDigits(rawText)
                if (normalizedDigits.length >= 4) { // حداقل طول معتبر برای تشخیص
                    onResult(normalizedDigits)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}

// تابع تبدیل ارقام فارسی/عربی به انگلیسی و استخراج فقط اعداد
private fun extractAndNormalizeDigits(input: String): String {
    val persianDigits = listOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    val arabicDigits = listOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
    var normalized = input
    for (i in 0..9) {
        normalized = normalized.replace(persianDigits[i], i.toString())
        normalized = normalized.replace(arabicDigits[i], i.toString())
    }
    return normalized.filter { it.isDigit() }
}
