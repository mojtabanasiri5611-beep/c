package com.example.core.util

object MoneyToWordsConverter {
    private val units = arrayOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val teens = arrayOf("ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده")
    private val tens = arrayOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val hundreds = arrayOf("", "صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد")
    private val thousands = arrayOf("", " هزار", " میلیون", " میلیارد", " تریلیون", " کوادریلیون", " کوئینتیلیون")

    fun convert(amount: Long, isToman: Boolean = false): String {
        val valInUnit = if (isToman) amount / 10 else amount
        return convertNumberToWords(kotlin.math.abs(valInUnit))
    }

    fun convertRialToTomanWords(rialAmountString: String): String {
        val clean = rialAmountString.filter { it.isDigit() }
        val rial = clean.toLongOrNull() ?: return ""
        if (rial == 0L) return "صفر تومان"

        // تبدیل ریال به تومان (تقسیم بر ۱۰)
        val toman = rial / 10
        if (toman == 0L) return "زیر یک تومان"

        return convertNumberToWords(toman) + " تومان"
    }

    fun convertNumberToWords(number: Long): String {
        if (number == 0L) return "صفر"
        var num = number
        val parts = mutableListOf<String>()
        var groupIndex = 0

        while (num > 0) {
            val remainder = (num % 1000).toInt()
            if (remainder > 0) {
                val threeDigitWord = convertThreeDigits(remainder)
                val suffix = thousands.getOrNull(groupIndex) ?: ""
                parts.add(0, threeDigitWord + suffix)
            }
            num /= 1000
            groupIndex++
            if (groupIndex >= thousands.size) break
        }

        return parts.joinToString(" و ")
    }

    private fun convertThreeDigits(number: Int): String {
        var n = number
        val result = mutableListOf<String>()

        val h = n / 100
        if (h in 1..9 && h < hundreds.size) {
            result.add(hundreds[h])
            n %= 100
        }

        if (n in 10..19) {
            val teenIndex = n - 10
            if (teenIndex in teens.indices) {
                result.add(teens[teenIndex])
            }
        } else {
            val t = n / 10
            if (t in 2..9 && t < tens.size) {
                result.add(tens[t])
            }
            val u = n % 10
            if (u in 1..9 && u < units.size) {
                result.add(units[u])
            }
        }

        return result.joinToString(" و ")
    }
}
