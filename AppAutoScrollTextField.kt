package com.example.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.JalaliDate
import com.example.core.util.MoneyToWordsConverter
import com.example.ui.theme.VazirmatnFontFamily
import java.text.DecimalFormat

// ترنسفورمیشن اختصاصی برای جلوگیری از معکوس شدن اعداد (چپ به راست اجباری با LRM)
class NumericVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = if (raw.isNotEmpty()) {
            val parsed = raw.toLongOrNull() ?: 0L
            val symbols = java.text.DecimalFormatSymbols(java.util.Locale.US).apply { groupingSeparator = ',' }
            val df = java.text.DecimalFormat("#,###", symbols)
            JalaliDate.toPersianDigits(df.format(parsed))
        } else {
            ""
        }
        
        // اضافه کردن کاراکتر LRM (\u200E) برای جلوگیری از معکوس شدن متن توسط موتور Bidi اندروید
        val transformedText = "\u200E$formatted"

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset <= 0) 1 else offset + (offset - 1) / 3 + 1
            }
            override fun transformedToOriginal(offset: Int): Int {
                val cleanOffset = (offset - 1).coerceAtLeast(0)
                return cleanOffset - (cleanOffset / 4)
            }
        }

        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}

@Composable
fun AppAutoScrollTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    // استخراج فقط اعداد خام انگلیسی
    val cleanDigits = JalaliDate.toEnglishDigits(value).replace(Regex("[^0-9]"), "")

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = cleanDigits,
            onValueChange = { newValue ->
                val filtered = JalaliDate.toEnglishDigits(newValue).replace(Regex("[^0-9]"), "")
                if (filtered != cleanDigits) {
                    onValueChange(filtered)
                }
            },
            label = { Text(label, fontFamily = VazirmatnFontFamily) },
            singleLine = true,
            isError = isError,
            visualTransformation = NumericVisualTransformation(),
            textStyle = TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                textDirection = TextDirection.Ltr
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )

        // زیرنویس بنفش رنگ مبلغ به حروف بدون کلمه معادل یا عدد
        val numericValue = cleanDigits.toLongOrNull() ?: 0L
        if (numericValue > 0) {
            val tomanVal = numericValue / 10
            val words = MoneyToWordsConverter.convertNumberToWords(tomanVal)

            Text(
                text = "$words تومان",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = VazirmatnFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E24AA) // رنگ بنفش
                ),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}
