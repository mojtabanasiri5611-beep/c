package com.example.presentation.ui.cheques.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VazirmatnFontFamily

@Composable
fun ChequeFinalConfirmationPreview(
    amountInWords: String, // مثلاً: یک میلیون و پانصد هزار
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "مبلغ چک به حروف:",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily)
        )
        // مبلغ چک به حروف به تومان با رنگ قرمز پررنگ (یا قرمز تیره)
        Text(
            text = "$amountInWords تومان",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = VazirmatnFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            ),
            color = Color(0xFFD32F2F) // قرمز پررنگ Material (Red 700)
        )
    }
}
