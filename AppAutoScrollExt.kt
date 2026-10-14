package com.example.presentation.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.smoothAutoScroll(
    coroutineScope: CoroutineScope,
    bringIntoViewRequester: BringIntoViewRequester,
    extraPaddingDp: Int = 140
): Modifier = composed {
    val density = LocalDensity.current
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    this
        .bringIntoViewRequester(bringIntoViewRequester)
        .onGloballyPositioned { componentSize = it.size }
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                coroutineScope.launch {
                    delay(250) // تاخیر استاندارد برای انیمیشن نرم کیبورد
                    val extraPx = with(density) { extraPaddingDp.dp.toPx() }
                    val targetRect = Rect(
                        left = 0f,
                        top = 0f,
                        right = componentSize.width.toFloat(),
                        bottom = componentSize.height.toFloat() + extraPx
                    )
                    bringIntoViewRequester.bringIntoView(targetRect)
                }
            }
        }
}
