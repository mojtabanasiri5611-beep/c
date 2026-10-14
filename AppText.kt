package com.example.core.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VazirmatnFontFamily
import com.example.core.utils.PersianNumberUtils

/**
 * کامپوننت سراسری متن:
 * ۱. فونت استاندارد فارسی (Vazirmatn) را تضمین می‌کند.
 * ۲. تمام اعداد را به فارسی یونیکد تبدیل می‌کند.
 * ۳. فاصله خطوط (lineHeight) را برای جلوگیری از بریدگی فونت فارسی تنظیم می‌کند.
 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = (fontSize.value * 1.5).sp,
    textAlign: TextAlign? = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = LocalTextStyle.current
) {
    val persianText = PersianNumberUtils.toPersianDigits(text)

    Text(
        text = persianText,
        modifier = modifier,
        color = color,
        fontFamily = VazirmatnFontFamily,
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        style = style
    )
}
