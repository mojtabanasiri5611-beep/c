package com.example.core.ocr

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.core.common.JalaliDate
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class QrBarcodeAnalyzer(
    private val isOcrMode: Boolean = false,
    private val onTargetDetected: (android.graphics.RectF?) -> Unit,
    private val onScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val barcodeOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val barcodeScanner = BarcodeScanning.getClient(barcodeOptions)
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @Volatile
    private var isProcessing = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || isProcessing) {
            imageProxy.close()
            return
        }

        isProcessing = true
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        // محاسبه ابعاد تصویر برای نرمالایز کردن مختصات (با در نظر گرفتن چرخش ۹۰ درجه دوربین)
        val imageWidth = if (imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270) imageProxy.height.toFloat() else imageProxy.width.toFloat()
        val imageHeight = if (imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270) imageProxy.width.toFloat() else imageProxy.height.toFloat()

        if (!isOcrMode) {
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val barcode = barcodes.firstOrNull()
                    if (barcode != null && barcode.boundingBox != null) {
                        val box = barcode.boundingBox!!
                        // نرمالایز کردن مختصات بین ۰ تا ۱
                        val normalizedRect = android.graphics.RectF(
                            box.left / imageWidth,
                            box.top / imageHeight,
                            box.right / imageWidth,
                            box.bottom / imageHeight
                        )
                        onTargetDetected(normalizedRect)

                        val rawValue = barcode.rawValue ?: return@addOnSuccessListener
                        val englishText = JalaliDate.toEnglishDigits(rawValue)
                        val match = Regex("\\b\\d{16}\\b").find(englishText)
                        if (match != null) {
                            onScanned(match.value)
                        } else if (englishText.length == 16 && englishText.all { it.isDigit() }) {
                            onScanned(englishText)
                        }
                    } else {
                        onTargetDetected(null)
                    }
                }
                .addOnFailureListener { Log.e("QrBarcodeAnalyzer", "خطا: ${it.message}") }
                .addOnCompleteListener {
                    isProcessing = false
                    imageProxy.close()
                }
        } else {
            textRecognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val detectedText = JalaliDate.toEnglishDigits(visionText.text)
                    if (detectedText.isNotBlank()) {
                        val numbers = Regex("\\d+").findAll(detectedText).map { it.value }.toList()
                        val selectedNumber = numbers.firstOrNull { it.length >= 10 } ?: numbers.firstOrNull()
                        if (selectedNumber != null) onScanned(selectedNumber)
                    }
                }
                .addOnFailureListener { Log.e("QrBarcodeAnalyzer", "خطا: ${it.message}") }
                .addOnCompleteListener {
                    isProcessing = false
                    imageProxy.close()
                }
        }
    }
}
