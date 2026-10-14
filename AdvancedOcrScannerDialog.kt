package com.example.presentation.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.core.common.CurrencyFormatter
import com.example.core.common.JalaliDate
import com.example.core.ocr.SayadScannerHelper
import com.example.presentation.ui.components.ParsedChequeData
import com.example.ui.theme.VazirmatnFontFamily
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun AdvancedOcrScannerDialog(
    onDismiss: () -> Unit,
    onDataConfirmed: (ParsedChequeData) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) onDismiss()
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) return

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var detectedSayad by remember { mutableStateOf("") }
    var detectedSerial by remember { mutableStateOf("") }
    var detectedAmount by remember { mutableStateOf("") }
    var detectedDate by remember { mutableStateOf("") }
    var isDataExtracted by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val barcodeScanner = BarcodeScanning.getClient()
                        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(android.util.Size(1920, 1080))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !isDataExtracted) {
                                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                                // اولویت ۱: اسکن بارکد صیادی
                                barcodeScanner.process(inputImage)
                                    .addOnSuccessListener { barcodes ->
                                        barcodes.firstOrNull()?.rawValue?.let { raw ->
                                            val parsed = SayadScannerHelper.parseScannedPayload(raw)
                                            parsed.sayadId?.let { s ->
                                                if (s.length == 16) {
                                                    detectedSayad = s
                                                    parsed.amount?.let { detectedAmount = it.toString() }
                                                    parsed.dueDateJalali?.let { detectedDate = it }
                                                    isDataExtracted = true
                                                }
                                            }
                                        }
                                    }

                                // اولویت ۲: اسکن متنی OCR برای چک‌های افقی (شناسه صیاد، مبلغ، تاریخ و سریال)
                                textRecognizer.process(inputImage)
                                    .addOnSuccessListener { visionText ->
                                        val fullText = visionText.text
                                        if (fullText.isNotBlank()) {
                                            val parsed = SayadScannerHelper.parseScannedPayload(fullText)
                                            if (parsed.sayadId != null && parsed.sayadId.length == 16) {
                                                detectedSayad = parsed.sayadId
                                                parsed.amount?.let { detectedAmount = it.toString() }
                                                parsed.dueDateJalali?.let { detectedDate = it }

                                                // استخراج شماره سریال
                                                val serialRegex = Regex("""(?:سریال|سری|شماره\s*چک|no|serial)[^\d]{0,8}(\d{4,10})""", RegexOption.IGNORE_CASE)
                                                serialRegex.find(JalaliDate.toEnglishDigits(fullText))?.let { match ->
                                                    detectedSerial = match.groupValues[1]
                                                }

                                                isDataExtracted = true
                                            }
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("AdvancedOcr", "Camera bind error", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )

            // راهنمای افقی گرفتن گوشی در بالای صفحه
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.65f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "برای اسکن سریع تمام اطلاعات، گوشی را به صورت افقی روی چک بگیرید",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
                        color = Color.White
                    )
                }
            }

            // دکمه انصراف بالا
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "بستن",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // کارت پیش‌نمایش و تایید اطلاعات استخراج شده
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "اطلاعات استخراج شده توسط OCR",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = VazirmatnFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "انصراف",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // شناسه صیادی
                    OutlinedTextField(
                        value = detectedSayad,
                        onValueChange = { detectedSayad = it },
                        label = { Text("شناسه صیادی ۱۶ رقمی", fontFamily = VazirmatnFontFamily) },
                        placeholder = { Text("در حال اسکن...", fontFamily = VazirmatnFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // شماره سریال و تاریخ سررسید در یک سطر
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = detectedSerial,
                            onValueChange = { detectedSerial = it },
                            label = { Text("شماره سریال", fontFamily = VazirmatnFontFamily) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = detectedDate,
                            onValueChange = { detectedDate = it },
                            label = { Text("تاریخ سررسید", fontFamily = VazirmatnFontFamily) },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // مبلغ چک
                    OutlinedTextField(
                        value = detectedAmount,
                        onValueChange = { detectedAmount = it },
                        label = { Text("مبلغ چک (ریال)", fontFamily = VazirmatnFontFamily) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            onDataConfirmed(
                                ParsedChequeData(
                                    sayadId = detectedSayad.filter { it.isDigit() },
                                    serialNumber = detectedSerial.filter { it.isDigit() },
                                    amount = detectedAmount.filter { it.isDigit() },
                                    dueDate = detectedDate
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = detectedSayad.isNotBlank() || detectedSerial.isNotBlank() || detectedAmount.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تایید و درج در فرم چک",
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
