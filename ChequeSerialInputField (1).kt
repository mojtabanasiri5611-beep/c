package com.mojtaba.chequemanager.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mojtaba.chequemanager.core.ui.PersianDigitsVisualTransformation
import com.mojtaba.chequemanager.core.ui.components.AppText
import com.mojtaba.chequemanager.core.ui.theme.BNazaninFontFamily
import com.mojtaba.chequemanager.core.utils.PersianNumberUtils

@Composable
fun ChequeSerialInputField(
    suggestedFirstActiveSerial: String, // سریال فعال محاسبه‌شده (x)
    serialRangeFrom: String,            // شروع بازه (a)
    serialRangeTo: String,              // پایان بازه (b)
    currentSerial: String,
    onSerialChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // هوشمند: در ابتدا اگر فیلد خالی بود، خود برنامه اولین سریال فعال را درج می‌کند
    LaunchedEffect(suggestedFirstActiveSerial) {
        if (currentSerial.isEmpty() && suggestedFirstActiveSerial.isNotEmpty()) {
            onSerialChange(suggestedFirstActiveSerial)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = PersianNumberUtils.toPersianDigits(currentSerial),
            onValueChange = { input ->
                val clean = input.filter { it.isDigit() }
                onSerialChange(clean)
            },
            label = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AppText("شماره سریال چک", fontSize = 14.sp)
                }
            },
            textStyle = TextStyle(
                fontFamily = BNazaninFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center // کاملاً وسط‌نویس
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = PersianDigitsVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // زیرنویس با قابلیت کلیک روی عدد پیشنهادی x
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText("پیشنهادی: ", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
            
            // بخش کلیک‌پذیر x
            AppText(
                text = PersianNumberUtils.toPersianDigits(suggestedFirstActiveSerial),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onSerialChange(suggestedFirstActiveSerial) }
                    .padding(horizontal = 4.dp)
            )

            AppText(
                text = "- بازه: (${PersianNumberUtils.toPersianDigits(serialRangeFrom)} تا ${PersianNumberUtils.toPersianDigits(serialRangeTo)})",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
