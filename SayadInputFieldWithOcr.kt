package com.example.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.ui.dialogs.AdvancedOcrScannerDialog
import com.example.ui.theme.VazirmatnFontFamily

@Composable
fun SayadInputFieldWithOcr(
    sayadIdValue: String,
    onSayadIdChange: (String) -> Unit,
    onOcrDataExtracted: (ParsedChequeData) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOcrScanner by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = sayadIdValue,
        onValueChange = { newValue ->
            val digits = newValue.filter { it.isDigit() }
            if (digits.length <= 16) {
                onSayadIdChange(digits)
            }
        },
        label = { Text("شناسه صیادی ۱۶ رقمی", fontFamily = VazirmatnFontFamily) },
        singleLine = true,
        visualTransformation = remember { SayadVisualTransformation() },
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = VazirmatnFontFamily,
            fontSize = 16.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            textDirection = androidx.compose.ui.text.style.TextDirection.Ltr
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            // دکمه OCR داخل فیلد شناسه صیادی
            IconButton(
                onClick = { showOcrScanner = true },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "OCR",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "OCR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )

    if (showOcrScanner) {
        AdvancedOcrScannerDialog(
            onDismiss = { showOcrScanner = false },
            onDataConfirmed = { parsedData ->
                onOcrDataExtracted(parsedData)
                showOcrScanner = false
            }
        )
    }
}

data class ParsedChequeData(
    val sayadId: String,
    val serialNumber: String,
    val amount: String,
    val dueDate: String
)
