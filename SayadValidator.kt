package com.example.core.financial

import com.example.core.common.JalaliDate

object SayadValidator {

    fun cleanDigits(input: String?): String {
        if (input.isNullOrBlank()) return ""
        return JalaliDate.toEnglishDigits(input).filter { it.isDigit() }
    }

    /**
     * اعتبارسنجی ۱۶ رقمی شناسه صیاد طبق کنترل‌بیت استاندارد بانکی (Luhn Algorithm)
     */
    fun isValid(sayadId: String?): Boolean {
        val sanitized = cleanDigits(sayadId)
        
        // ۱. بررسی طول و فقط عدد بودن
        if (sanitized.length != 16 || !sanitized.all { it.isDigit() }) {
            return false
        }

        // ۲. اعتبارسنجی رقم کنترلی (Luhn Algorithm)
        var sum = 0
        var isAlternate = false
        
        for (i in sanitized.length - 1 downTo 0) {
            var n = sanitized[i].digitToInt()
            if (isAlternate) {
                n *= 2
                if (n > 9) {
                    n = (n % 10) + 1
                }
            }
            sum += n
            isAlternate = !isAlternate
        }

        return (sum % 10 == 0)
    }

    fun isValidSayadId(sayadId: String?): Boolean = isValid(sayadId)

    fun formatSayad(sayadId: String?, persian: Boolean = true): String {
        val cleaned = cleanDigits(sayadId)
        if (cleaned.length != 16) {
            return if (persian && sayadId != null) JalaliDate.toPersianDigits(sayadId) else (sayadId ?: "")
        }
        val formatted = "${cleaned.substring(0, 4)} - ${cleaned.substring(4, 8)} - ${cleaned.substring(8, 12)} - ${cleaned.substring(12, 16)}"
        return if (persian) JalaliDate.toPersianDigits(formatted) else formatted
    }
}
