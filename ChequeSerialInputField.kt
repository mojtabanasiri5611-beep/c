package com.example.presentation.components

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
import com.example.core.ui.PersianDigitsVisualTransformation
import com.example.core.ui.components.AppText
import com.example.core.utils.PersianNumberUtils
import com.example.ui.theme.VazirmatnFontFamily

@Composable
fun ChequeSerialInputField(
    suggestedFirstActiveSerial: String,
    serialRangeFrom: String = "",
    serialRangeTo: String = "",
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
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "شماره سریال چک",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = VazirmatnFontFamily,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 2.dp, start = 4.dp)
                .align(Alignment.Start)
        )

        OutlinedTextField(
            value = PersianNumberUtils.toPersianDigits(currentSerial),
            onValueChange = { input ->
                val clean = input.filter { it.isDigit() }
                onSerialChange(clean)
            },
            textStyle = TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center // کاملاً وسط‌نویس
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = PersianDigitsVisualTransformation(),
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp)
        )

        if (suggestedFirstActiveSerial.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))

            // زیرنویس با فرمت: پیشنهادی x (بازه a – b) و کلیک روی x جهت درج
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "پیشنهادی ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = VazirmatnFontFamily,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.outline
                )

                // کلیک‌پذیر x
                Surface(
                    onClick = { onSerialChange(suggestedFirstActiveSerial) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = " ${PersianNumberUtils.toPersianDigits(suggestedFirstActiveSerial)} ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                if (serialRangeFrom.isNotEmpty() && serialRangeTo.isNotEmpty()) {
                    Text(
                        text = " (بازه ${PersianNumberUtils.toPersianDigits(serialRangeFrom)} – ${PersianNumberUtils.toPersianDigits(serialRangeTo)})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun ChequeSerialInputField(
    suggestedFirstAvailableSerial: String,
    currentSerial: String,
    onSerialChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ChequeSerialInputField(
        suggestedFirstActiveSerial = suggestedFirstAvailableSerial,
        serialRangeFrom = "",
        serialRangeTo = "",
        currentSerial = currentSerial,
        onSerialChange = onSerialChange,
        modifier = modifier
    )
}
