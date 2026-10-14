package com.mojtaba.chequemanager.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mojtaba.chequemanager.core.ui.components.AppText
import com.mojtaba.chequemanager.core.utils.PersianNumberUtils

@Composable
fun ChequeConfirmationDialog(
    sayadId: String,
    amountRial: Long,
    bankName: String,
    accountNumber: String,
    serialNumber: String,
    description: String,
    dueDate: String,
    issueDate: String,
    invoiceNumber: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppText("تایید نهایی مشخصات چک", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Divider(modifier = Modifier.padding(vertical = 10.dp))

                // نمایش مشخصات اصلی
                ConfirmationRow("شناسه صیاد:", sayadId)
                ConfirmationRow("مبلغ چک:", PersianNumberUtils.formatCurrency(amountRial, isToman = false))
                ConfirmationRow("بانک و شماره حساب:", "$bankName - ${PersianNumberUtils.toPersianDigits(accountNumber)}")
                ConfirmationRow("تاریخ سررسید:", dueDate)
                ConfirmationRow("تاریخ صدور:", issueDate)

                // نمایش شماره سریال در صورت پر بودن
                if (serialNumber.isNotBlank()) {
                    ConfirmationRow("شماره سریال برگه:", PersianNumberUtils.toPersianDigits(serialNumber))
                }

                // نمایش شماره فاکتور در صورت پر بودن
                if (invoiceNumber.isNotBlank()) {
                    ConfirmationRow("شماره فاکتور:", PersianNumberUtils.toPersianDigits(invoiceNumber))
                }

                // نمایش بابت / شرح سند در صورت پر بودن
                if (description.isNotBlank()) {
                    ConfirmationRow("شرح / بابت:", description)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismissRequest) {
                        AppText("ویرایش اطلاعات", fontSize = 14.sp)
                    }
                    Button(onClick = onConfirm) {
                        AppText("تایید و ثبت قطعی", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppText(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
        AppText(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
