package com.example.ui.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize

/**
 * ۱. محو کردن قطعی صفحه کلید با کلیک در هر نقطه از صفحه.
 */
fun Modifier.clearFocusOnTap(focusManager: FocusManager): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }
}

/**
 * ۲. اسکرول هوشمند فیلد به سمت بالا (۲ برابر ارتفاع خودش) جهت دید بهتر بالای کیبورد
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.scrollOnFocusUpwards(
    bringIntoViewRequester: BringIntoViewRequester,
    coroutineScope: CoroutineScope
): Modifier {
    var scrollJob: Job? = null
    var height = 0f
    var width = 0f

    return this
        .onGloballyPositioned {
            height = it.size.height.toFloat()
            width = it.size.width.toFloat()
        }
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusEvent { focusState ->
            if (focusState.isFocused) {
                scrollJob?.cancel()
                scrollJob = coroutineScope.launch {
                    try {
                        delay(300)
                        // ارسال یک Rect که ۳ برابر ارتفاع فیلد است (خود فیلد + ۲ برابر ارتفاع در پایین آن)
                        // این باعث می‌شود سیستم اسکرول کند تا کل این ناحیه ۳ برابر دیده شود، در نتیجه فیلد بالاتر می‌رود.
                        if (height > 0) {
                            bringIntoViewRequester.bringIntoView(Rect(0f, 0f, width, height * 3f))
                        } else {
                            bringIntoViewRequester.bringIntoView()
                        }
                    } catch (_: Exception) {
                    }
                }
            } else {
                scrollJob?.cancel()
            }
        }
}

/**
 * ۳. اسکرول هوشمند فیلد فوکوس‌شده به همراه پدینگ اضافی برای زیرنویس‌ها
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.scrollOnFocusWithOffset(
    bringIntoViewRequester: BringIntoViewRequester,
    coroutineScope: CoroutineScope,
    extraBottomPadding: Dp = 48.dp
): Modifier {
    var scrollJob: Job? = null

    return this
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusEvent { focusState ->
            if (focusState.isFocused) {
                scrollJob?.cancel()
                scrollJob = coroutineScope.launch {
                    try {
                        delay(250)
                        bringIntoViewRequester.bringIntoView()
                    } catch (_: Exception) {
                    }
                }
            } else {
                scrollJob?.cancel()
            }
        }
}
