package com.example.presentation.ui.averagematurity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.presentation.ui.dialogs.AppDeleteConfirmDialog
import com.example.ui.theme.VazirmatnFontFamily

@Composable
fun AverageMaturityScreen(modifier: Modifier = Modifier) {
    var rows by remember { mutableStateOf(listOf(1, 2)) }
    var itemToDelete by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "راس‌گیری",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(rows, key = { it }) { rowId ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // فیلدهای تاریخ و مبلغ با چیدمان مناسب بدون کات شدن
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("مبلغ", fontFamily = VazirmatnFontFamily) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedButton(
                            onClick = { itemToDelete = rowId }, // درخواست تایید حذف ردیف
                            modifier = Modifier.alignByBaseline()
                        ) {
                            Text("حذف", fontFamily = VazirmatnFontFamily, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { rows = rows + (rows.size + 1) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("افزودن ردیف", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
        }
    }

    // دیالوگ اخطار و تایید حذف ردیف با پیش‌فرض روی خیر
    if (itemToDelete != null) {
        AppDeleteConfirmDialog(
            itemName = "این ردیف راس‌گیری",
            onConfirm = {
                rows = rows.filter { it != itemToDelete }
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}
