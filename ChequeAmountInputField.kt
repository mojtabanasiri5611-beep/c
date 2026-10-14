package com.example.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.example.core.common.JalaliDate
import com.example.core.ui.components.AppText
import com.example.core.util.MoneyToWordsConverter
import com.example.presentation.ui.components.NumericVisualTransformation
import com.example.ui.theme.VazirmatnFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChequeAmountInputField(
    amountRial: Long,
    onAmountChange: (Long) -> Unit,
    currencyUnit: String = "ریال",
    modifier: Modifier = Modifier
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val rawString = if (amountRial > 0) amountRial.toString() else ""

    val wordsSubtitle = remember(amountRial, currencyUnit) {
        if (amountRial <= 0L) ""
        else {
            val words = MoneyToWordsConverter.convert(amountRial, isToman = currencyUnit == "تومان")
            "$words $currencyUnit"
        }
    }

    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    var bringIntoViewJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onGloballyPositioned { coordinates -> fieldSize = coordinates.size }
            .padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "مبلغ چک ($currencyUnit)",
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
            value = rawString,
            onValueChange = { input ->
                val clean = JalaliDate.toEnglishDigits(input).filter { it.isDigit() }
                val parsed = clean.toLongOrNull() ?: 0L
                onAmountChange(parsed)
            },
            placeholder = {
                Text(
                    text = "مبلغ را به $currencyUnit وارد کنید",
                    style = TextStyle(
                        fontFamily = VazirmatnFontFamily,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    ),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            textStyle = TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center // کاملاً وسط‌نویس
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = NumericVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp)
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        bringIntoViewJob?.cancel()
                        bringIntoViewJob = coroutineScope.launch {
                            delay(180)
                            if (fieldSize.height > 0) {
                                val extraPx = with(density) { 50.dp.toPx() }
                                val bounds = androidx.compose.ui.geometry.Rect(
                                    left = 0f,
                                    top = 0f,
                                    right = fieldSize.width.toFloat(),
                                    bottom = fieldSize.height.toFloat() + extraPx
                                )
                                bringIntoViewRequester.bringIntoView(bounds)
                            }
                        }
                    } else {
                        bringIntoViewJob?.cancel()
                        bringIntoViewJob = null
                    }
                }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // زیرنویس به حروف کاملاً وسط‌نویس
        AppText(
            text = if (wordsSubtitle.isNotEmpty()) "به حروف: $wordsSubtitle" else "مبلغ را وارد کنید",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (wordsSubtitle.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
    }
}
