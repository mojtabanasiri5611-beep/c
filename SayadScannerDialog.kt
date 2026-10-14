package com.example.presentation.ui.dialogs

import android.view.MotionEvent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.core.ocr.QrBarcodeAnalyzer
import com.example.ui.theme.VazirmatnFontFamily
import java.util.concurrent.Executors

@Composable
fun SayadScannerDialog(
    isOcrMode: Boolean = false,
    onCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var targetRect by remember { mutableStateOf<android.graphics.RectF?>(null) }
    var scannedPreviewValue by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = androidx.camera.view.PreviewView(ctx).apply { scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER }
                    val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(android.util.Size(1920, 1080))
                            .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(
                            cameraExecutor,
                            QrBarcodeAnalyzer(
                                isOcrMode = isOcrMode,
                                onTargetDetected = { rect -> targetRect = rect },
                                onScanned = { result ->
                                    if (scannedPreviewValue == null) {
                                        scannedPreviewValue = result 
                                    }
                                }
                            )
                        )

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                        } catch (e: Exception) { e.printStackTrace() }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )

            // کادر ردیاب متحرک
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = this.size.width
                val canvasH = this.size.height
                val targetLeft = targetRect?.left?.let { it * canvasW } ?: (canvasW * 0.15f)
                val targetTop = targetRect?.top?.let { it * canvasH } ?: (canvasH * 0.35f)
                val targetRight = targetRect?.right?.let { it * canvasW } ?: (canvasW * 0.85f)
                val targetBottom = targetRect?.bottom?.let { it * canvasH } ?: (canvasH * 0.65f)

                val cornerLength = 40f
                val path = Path().apply {
                    moveTo(targetLeft, targetTop + cornerLength); lineTo(targetLeft, targetTop); lineTo(targetLeft + cornerLength, targetTop)
                    moveTo(targetRight - cornerLength, targetTop); lineTo(targetRight, targetTop); lineTo(targetRight, targetTop + cornerLength)
                    moveTo(targetRight, targetBottom - cornerLength); lineTo(targetRight, targetBottom); lineTo(targetRight - cornerLength, targetBottom)
                    moveTo(targetLeft + cornerLength, targetBottom); lineTo(targetLeft, targetBottom); lineTo(targetLeft, targetBottom - cornerLength)
                }
                drawPath(path = path, color = Color.Green, style = Stroke(width = 8f))
            }

            // دکمه انصراف گوشه بالا هنگام اسکن
            if (scannedPreviewValue == null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "انصراف",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // باکس کنترل و پیش‌نمایش شناسه صیادی با دکمه انصراف اختصاصی درون باکس
            scannedPreviewValue?.let { rawCode ->
                SayadScannerOverlayControls(
                    scannedCode = rawCode,
                    onDismiss = onDismiss,
                    onConfirm = {
                        onCodeScanned(it)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun SayadScannerOverlayControls(
    scannedCode: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "شناسه صیادی اسکن شده",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // آیکون انصراف کوچک‌شده درون باکس
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "انصراف",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (scannedCode != null) {
                    val blocks = scannedCode.chunked(4)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            blocks.forEach { block ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(
                                        text = block,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = VazirmatnFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            textDirection = TextDirection.Ltr
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { onConfirm(scannedCode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "درج در فیلد",
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Text(
                        text = "در حال اسکن بارکد صیادی...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = VazirmatnFontFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
