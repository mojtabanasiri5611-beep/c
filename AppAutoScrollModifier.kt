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
fun Modifier.autoScrollOnFocus(
    coroutineScope: CoroutineScope,
    bringIntoViewRequester: BringIntoViewRequester,
    extraBottomOffsetDp: Int = 120
): Modifier = composed {
    val density = LocalDensity.current
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }

    this
        .bringIntoViewRequester(bringIntoViewRequester)
        .onGloballyPositioned { fieldSize = it.size }
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                coroutineScope.launch {
                    delay(300)
                    val extraPx = with(density) { extraBottomOffsetDp.dp.toPx() }
                    val bounds = Rect(
                        left = 0f,
                        top = 0f,
                        right = fieldSize.width.toFloat(),
                        bottom = fieldSize.height.toFloat() + extraPx
                    )
                    bringIntoViewRequester.bringIntoView(bounds)
                }
            }
        }
}
