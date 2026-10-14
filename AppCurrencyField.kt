package com.example.presentation.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.JalaliDate
import com.example.core.util.MoneyToWordsConverter
import com.example.ui.theme.VazirmatnFontFamily
import com.example.ui.util.scrollOnFocusUpwards
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCurrencyField(
    rawValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "مبلغ (تومان)",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val formatter = remember { DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    var wasFocused by remember { mutableStateOf(false) }

    var textFieldValue by remember(rawValue) {
        val clean = JalaliDate.toEnglishDigits(rawValue).filter { it.isDigit() }
        val formatted = if (clean.isNotEmpty()) {
            try {
                formatter.format(clean.toLong())
            } catch (_: Exception) {
                clean
            }
        } else ""
        mutableStateOf(TextFieldValue(text = formatted, selection = TextRange(formatted.length)))
    }

    val amountWords = if (rawValue.isNotEmpty()) MoneyToWordsConverter.convertRialToTomanWords(rawValue) else ""

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scrollOnFocusUpwards(bringIntoViewRequester, coroutineScope)
            .onGloballyPositioned { fieldSize = it.size }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = VazirmatnFontFamily,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    val cleanDigits = JalaliDate.toEnglishDigits(newValue.text).filter { it.isDigit() }
                    if (cleanDigits.length <= 15) {
                        val formatted = if (cleanDigits.isNotEmpty()) {
                            try {
                                formatter.format(cleanDigits.toLong())
                            } catch (_: Exception) {
                                cleanDigits
                            }
                        } else ""
                        textFieldValue = newValue.copy(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )
                        onValueChange(cleanDigits)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp),
                textStyle = TextStyle(
                    fontFamily = VazirmatnFontFamily,
                    fontSize = 16.5.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    textDirection = TextDirection.Ltr,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = isError,
                shape = RoundedCornerShape(10.dp)
            )
        }
        if (amountWords.isNotEmpty() && !isError) {
            Text(
                text = amountWords,
                color = Color(0xFF8E24AA),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = VazirmatnFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .padding(top = 4.dp, start = 4.dp)
                    .align(Alignment.Start)
            )
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}
