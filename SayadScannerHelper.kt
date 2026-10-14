package com.example.core.ocr

import com.example.core.common.JalaliDate

/**
 * Enhanced Offline OCR & Barcode/DataMatrix parser for Iranian Sayad Cheques.
 * Operates 100% offline with zero cloud dependency.
 * Designed to read printed numbers like an OCR scanner (extracting 16-digit Sayad IDs,
 * amounts, and Jalali due dates even from messy strings, SMS messages, or scanned raw text).
 */
object SayadScannerHelper {

    /**
     * Multi-stage regex extraction of 16-digit Sayad ID with full Persian & Arabic digit support:
     * 1. Labeled Sayad ID (e.g. شناسه صیاد: ۱۲۳۴۵۶۷۸۹۰۱۲۳۴۵۶, صیادی, sayad, id)
     * 2. Formatted 4x4 blocks separated by spaces, hyphens, dashes, slashes, or dots (e.g. 1234-5678-9012-3456)
     * 3. Direct 16 continuous digits bounded by non-digits or start/end of string
     * 4. Any continuous 16-digit substring in the normalized stream
     * 5. Fallback: Entire digit sequence if exactly 16 digits
     */
    fun extractSayadId(rawText: String): String? {
        if (rawText.isBlank()) return null
        val engText = JalaliDate.toEnglishDigits(rawText)

        // Stage 1: Check for labeled sayad id (e.g. شناسه صیاد:, صیادی:, کد صیاد:, sayad id:)
        val labeledRegex = Regex("""(?:شناسه\s*صیاد|شناسه\s*صیادی|کد\s*صیاد|کد\s*صیادی|صیادی|صیاد|sayad\s*id|sayad|id)[^\d]{0,12}(\d{16})""", RegexOption.IGNORE_CASE)
        labeledRegex.find(engText)?.let {
            return it.groupValues[1]
        }

        // Stage 2: Grouped 4-4-4-4 digits separated by spaces, hyphens, dashes, slashes, or dots
        val groupedRegex = Regex("""(\d{4})[\s\-_/.\u200C]+(\d{4})[\s\-_/.\u200C]+(\d{4})[\s\-_/.\u200C]+(\d{4})""")
        groupedRegex.find(engText)?.let {
            val combined = "${it.groupValues[1]}${it.groupValues[2]}${it.groupValues[3]}${it.groupValues[4]}"
            if (combined.length == 16) return combined
        }

        // Stage 3: Direct 16 continuous digits surrounded by non-digits or boundaries
        val digitRegex = Regex("""(?:^|[^\d])(\d{16})(?:[^\d]|$)""")
        digitRegex.find(engText)?.let {
            return it.groupValues[1]
        }

        // Stage 4: Any 16-digit sequence within the text
        val sequenceMatch = Regex("""\d{16}""").find(engText)
        if (sequenceMatch != null) {
            return sequenceMatch.value
        }

        // Stage 5: Fallback - Filter all digits if the entire string holds at least 16 digits
        val allDigits = engText.filter { it.isDigit() }
        if (allDigits.length == 16) {
            return allDigits
        } else if (allDigits.length > 16) {
            return allDigits.take(16)
        }

        return null
    }

    /**
     * Parses amount, Sayad ID, and date if present in a QR code payload, OCR text, or SMS.
     */
    data class ParsedChequeInfo(
        val sayadId: String? = null,
        val amount: Long? = null,
        val dueDateJalali: String? = null
    )

    fun parseScannedPayload(payload: String): ParsedChequeInfo {
        val sayad = extractSayadId(payload)
        val engText = JalaliDate.toEnglishDigits(payload)

        // Find potential amount (numbers with 6 to 13 digits not equal to sayad)
        var detectedAmount: Long? = null
        
        // Priority 1: Labeled amount (مبلغ: 50,000,000 ریال یا تومان)
        val labeledAmountRegex = Regex("""(?:مبلغ\s*چک|مبلغ|ریال|تومان|مبلغ\s*به\s*عدد|amount)[^\d]{0,12}([\d,]+)""", RegexOption.IGNORE_CASE)
        labeledAmountRegex.find(engText)?.let {
            val cleanNum = it.groupValues[1].replace(",", "").toLongOrNull()
            if (cleanNum != null && cleanNum > 10_000) {
                detectedAmount = cleanNum
            }
        }

        // Priority 2: Standalone numeric amount sequence
        if (detectedAmount == null) {
            val amountRegex = Regex("""\b(\d{6,13})\b""")
            val matches = amountRegex.findAll(engText)
            for (m in matches) {
                val numStr = m.groupValues[1]
                if (sayad != null && numStr == sayad) continue
                val candidate = numStr.toLongOrNull()
                if (candidate != null && candidate > 100_000) {
                    detectedAmount = candidate
                    break
                }
            }
        }

        // Find potential Jalali date (e.g. 1403/08/15 or 1403-08-15)
        var detectedDate: String? = null
        val dateRegex = Regex("""(1[34]\d{2})[/\-.](0?[1-9]|1[0-2])[/\-.](0?[1-9]|[12]\d|3[01])""")
        dateRegex.find(engText)?.let {
            val y = it.groupValues[1].toInt()
            val m = it.groupValues[2].toInt()
            val d = it.groupValues[3].toInt()
            detectedDate = JalaliDate(y, m, d).formattedString
        }

        return ParsedChequeInfo(
            sayadId = sayad,
            amount = detectedAmount,
            dueDateJalali = detectedDate
        )
    }
}
