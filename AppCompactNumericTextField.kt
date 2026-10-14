package com.example.presentation.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VazirmatnFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCompactNumericTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    maxLength: Int? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    subText: String? = null,
    subTextColor: Color = Color(0xFF8E24AA),
    isSayadId: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    var bringIntoViewJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val activeVisualTransformation = when {
        visualTransformation != null -> visualTransformation
        isSayadId || (maxLength == 16 && label.contains("صیاد")) -> remember { SayadVisualTransformation() }
        else -> androidx.compose.ui.text.input.VisualTransformation.None
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onGloballyPositioned { coordinates -> fieldSize = coordinates.size }
            .padding(bottom = 2.dp)
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
                value = value,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }
                    if (maxLength == null || filtered.length <= maxLength) onValueChange(filtered)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            bringIntoViewJob?.cancel()
                            bringIntoViewJob = coroutineScope.launch {
                                delay(180)
                                if (fieldSize.height > 0) {
                                    val nextFieldExpectedHeight = with(density) { 50.dp.toPx() }
                                    val expandedBounds = Rect(
                                        left = 0f,
                                        top = 0f,
                                        right = fieldSize.width.toFloat(),
                                        bottom = fieldSize.height.toFloat() + nextFieldExpectedHeight
                                    )
                                    bringIntoViewRequester.bringIntoView(expandedBounds)
                                }
                            }
                        } else {
                            bringIntoViewJob?.cancel()
                            bringIntoViewJob = null
                        }
                    },
                singleLine = true,
                isError = isError,
                visualTransformation = activeVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                textStyle = TextStyle(
                    fontFamily = VazirmatnFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    textDirection = TextDirection.Ltr,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon ?: {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = { onValueChange("") }) { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    }
                },
                shape = RoundedCornerShape(10.dp)
            )
        }

        if (subText != null && !isError) {
            Text(
                text = subText,
                color = subTextColor,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 6.dp)
            )
        }
    }
}
