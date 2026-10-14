package com.example.presentation.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.presentation.settings.NotificationSettingsScreen
import com.example.presentation.settings.SecuritySettingsScreen
import com.example.ui.theme.VazirmatnFontFamily

@Composable
fun AppNavigationHandler(
    currentRoute: String,
    isInSubPage: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onExitApp: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        when {
            isInSubPage -> {
                // اگر در زیرصفحه بودیم، به صفحه قبل برگرد
                onNavigateBack()
            }
            currentRoute != "dashboard" -> {
                // اگر در تب‌ها بودیم، به داشبورد برگرد
                onNavigateToDashboard()
            }
            else -> {
                // اگر در داشبورد بودیم، استعلام خروج از برنامه بگیر
                showExitDialog = true
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("خروج از برنامه", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text("آیا می‌خواهید از برنامه خارج شوید؟", fontFamily = VazirmatnFontFamily) },
            confirmButton = {
                TextButton(onClick = { onExitApp() }) {
                    Text("بلی", fontFamily = VazirmatnFontFamily, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("خیر", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * هدایت و اتصال مستقیم روت‌های اختصاصی امنیت و اعلان به صفحات جدید
 */
@Composable
fun AppSettingsRouteContent(
    route: String,
    onNavigateBack: () -> Unit
) {
    when (route) {
        "notification_settings" -> {
            NotificationSettingsScreen(modifier = Modifier.fillMaxSize())
        }
        "security_settings" -> {
            SecuritySettingsScreen(onBackClick = onNavigateBack)
        }
    }
}
