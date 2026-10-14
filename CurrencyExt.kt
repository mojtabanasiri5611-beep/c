package com.example.core.common

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VazirmatnFontFamily

object CurrencyFormatter {
    fun format(amount: Long, isToman: Boolean = true, includeUnit: Boolean = true): String {
        return if (includeUnit) {
            com.example.core.utils.PersianNumberUtils.formatCurrency(amount, isToman)
        } else {
            val finalVal = if (isToman) amount / 10 else amount
            val absVal = kotlin.math.abs(finalVal)
            val symbols = java.text.DecimalFormatSymbols(java.util.Locale.US).apply { groupingSeparator = ',' }
            val formatted = com.example.core.utils.PersianNumberUtils.toPersianDigits(java.text.DecimalFormat("#,###", symbols).format(absVal))
            val sign = if (finalVal < 0) "-" else ""
            "$sign$formatted"
        }
    }

    val LtrNumberStyle = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        textDirection = TextDirection.Ltr // اجبار نمایش اعداد و علامت منفی از چپ به راست
    )
}
