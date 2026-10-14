package com.example.core.financial

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * تبدیلگر امن مبالغ پولی با جداکننده ۳ رقم ۳ رقم.
 * این پیاده‌سازی نگاشت دوطرفه را به شکل قطعی محاسبه می‌کند تا از کرش‌های کرسر جلوگیری شود.
 */
class CurrencyAmountVisualTransformation(private val separator: String = ",") : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatted = StringBuilder()
        val numDigits = originalText.length

        for (i in 0 until numDigits) {
            formatted.append(originalText[i])
            val digitsRemaining = numDigits - (i + 1)
            if (digitsRemaining > 0 && digitsRemaining % 3 == 0) {
                formatted.append(separator)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, numDigits)
                if (clamped == 0) return 0
                val totalCommas = if (numDigits > 0) (numDigits - 1) / 3 else 0
                val commasAhead = (numDigits - clamped) / 3
                val addedCommas = totalCommas - commasAhead
                return clamped + addedCommas
            }

            override fun transformedToOriginal(offset: Int): Int {
                val transformedLen = formatted.length
                val clamped = offset.coerceIn(0, transformedLen)
                var rawCount = 0
                var currentTransformed = 0

                while (currentTransformed < clamped && rawCount < numDigits) {
                    currentTransformed++
                    rawCount++
                    val digitsRemaining = numDigits - rawCount
                    if (digitsRemaining > 0 && digitsRemaining % 3 == 0) {
                        if (currentTransformed < clamped) {
                            currentTransformed++
                        }
                    }
                }
                return rawCount.coerceIn(0, numDigits)
            }
        }

        return TransformedText(AnnotatedString(formatted.toString()), offsetMapping)
    }
}
