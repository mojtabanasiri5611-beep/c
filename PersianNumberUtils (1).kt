package com.mojtaba.chequemanager.core.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PersianNumberUtils {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(text: String): String {
        if (text.isEmpty()) return text
        val sb = StringBuilder(text.length)
        for (char in text) {
            if (char in '0'..'9') {
                sb.append(PERSIAN_DIGITS[char - '0'])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    fun toPersianDigits(number: Long): String = toPersianDigits(number.toString())
    fun toPersianDigits(number: Int): String = toPersianDigits(number.toString())

    /**
     * فرمت مبالغ مالی:
     * ۱. کاما دقیقاً در پایین عدد (پایین خط کرسی) قرار می‌گیرد.
     * ۲. واحد پول در سمت چپ عدد قرار می‌گیرد.
     * ۳. علامت منفی (-) در سمت چپ عدد قرار می‌گیرد: «ریال -۱۲,۵۰۰,۰۰۰»
     */
    fun formatCurrency(amountRial: Long, isToman: Boolean): String {
        val unit = if (isToman) "تومان" else "ریال"
        val value = if (isToman) amountRial / 10 else amountRial
        val isNegative = value < 0
        val absValue = kotlin.math.abs(value)

        // استفاده از کامای اسکی لاتین برای پایین‌نشین بودن قطعی
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val decimalFormat = DecimalFormat("#,###", symbols)
        val formattedNumber = toPersianDigits(decimalFormat.format(absValue))

        val sign = if (isNegative) "-" else ""
        // ساخت رشته با جهت چپ‌به‌راست قطعی: [واحد] [علامت منفی][عدد] در سمت چپ
        return "\u202A$unit $sign$formattedNumber\u202C"
    }
}
