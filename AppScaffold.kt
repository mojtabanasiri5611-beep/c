package com.example.presentation.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppMainScaffold(
    topBarContent: (@Composable () -> Unit)? = null,
    bottomBarContent: @Composable () -> Unit,
    snackbarHost: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { topBarContent?.invoke() },
        snackbarHost = snackbarHost,
        bottomBar = {
            // اعمال پدینگ اختصاصی برای جلوگیری از تداخل با دکمه‌های سیستم (Gesture Nav/Home)
            Surface(
                modifier = Modifier.navigationBarsPadding(),
                tonalElevation = 8.dp
            ) {
                bottomBarContent()
            }
        }
    ) { innerPadding ->
        content(Modifier.fillMaxSize().padding(innerPadding))
    }
}
