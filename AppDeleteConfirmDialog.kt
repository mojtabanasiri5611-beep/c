package com.example.presentation.ui.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.VazirmatnFontFamily

@Composable
fun AppDeleteConfirmDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("تایید حذف", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
        },
        text = {
            Text("آیا از حذف $itemName مطمئن هستید؟", fontFamily = VazirmatnFontFamily)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("بلی", fontFamily = VazirmatnFontFamily, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            // پیش‌فرض روی خیر (تمرکز روی دکمه انصراف یا خیر)
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.focusRequester(focusRequester)
            ) {
                Text("خیر", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
            }
        }
    )
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
