package com.example.presentation.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.core.common.JalaliDate

/**
 * VisualTransformation to format 16-digit Sayad ID into 4-digit blocks (e.g. "1234 5678 9012 3456")
 * with Persian digits from left to right (using \u200E LRM prefix to prevent Bidi inversion).
 */
class SayadVisualTransformation(
    private val usePersianDigits: Boolean = true
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }
        val sb = StringBuilder()
        for (i in raw.indices) {
            sb.append(raw[i])
            if ((i + 1) % 4 == 0 && i + 1 < raw.length) {
                sb.append(' ')
            }
        }
        val formatted = if (usePersianDigits) {
            JalaliDate.toPersianDigits(sb.toString())
        } else {
            sb.toString()
        }

        // Prepend LRM (\u200E) to ensure numbers read cleanly from left to right
        val transformedText = "\u200E$formatted"

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                val spaces = if (clamped == 0) 0 else (clamped - 1) / 4
                return 1 + clamped + spaces
            }

            override fun transformedToOriginal(offset: Int): Int {
                val cleanOffset = (offset - 1).coerceAtLeast(0)
                // In transformed string, every group of 5 chars has 4 digits + 1 space
                val spaces = cleanOffset / 5
                val original = cleanOffset - spaces
                return original.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}
