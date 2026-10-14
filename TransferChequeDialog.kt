package com.example.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.ui.PersianDigitsVisualTransformation
import com.example.core.ui.components.AppText
import com.example.core.utils.PersianNumberUtils

@Composable
fun TransferChequeDialog(
    chequeAmount: Long,
    chequeSayadId: String,
    onDismissRequest: () -> Unit,
    onConfirmTransfer: (transfereeName: String, nationalId: String, trackingCode: String) -> Unit
) {
    var transfereeName by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var trackingCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp)
            ) {
                // هدر دیالوگ
                AppText(
                    text = "واگذاری و خرج کردن چک صیادی",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // نمایش مشخصات چک با فونت فارسی و واحد در سمت چپ
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        AppText(
                            text = "مبلغ: ${PersianNumberUtils.formatCurrencyOnLeft(chequeAmount, "ریال")}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        AppText(
                            text = "شناسه صیاد: $chequeSayadId",
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ۱. نام شخص جدید (طلبکار/تامین‌کننده)
                OutlinedTextField(
                    value = transfereeName,
                    onValueChange = { transfereeName = it },
                    label = { AppText("واگذار شده به (نام شخص یا شرکت)", fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ۲. کد ملی / شناسه ملی گیرنده
                OutlinedTextField(
                    value = nationalId,
                    onValueChange = { if (it.length <= 11) nationalId = it },
                    label = { AppText("کد ملی / شناسه ملی گیرنده جدید", fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PersianDigitsVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ۳. کد رهگیری صیاد
                OutlinedTextField(
                    value = trackingCode,
                    onValueChange = { trackingCode = it },
                    label = { AppText("کد رهگیری انتقال سامانه صیاد", fontSize = 14.sp) },
                    visualTransformation = PersianDigitsVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ۴. پیام هشدار قانونی و تعهد احتمالی (ماده ۲۴۹ قانون تجارت)
                Surface(
                    color = Color(0xFFFFF3CD),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppText(
                        text = "توجه: طبق قانون، شما در قبال وصول این چک مسئولیت تضامنی دارید و این سند تا زمان سررسید به عنوان «تعهد احتمالی» در سوابق شما ثبت خواهد بود.",
                        fontSize = 12.sp,
                        color = Color(0xFF856404),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    AppText(text = it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // دکمه‌های تایید و انصراف
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        AppText("انصراف", fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (transfereeName.isBlank()) {
                                errorMessage = "نام گیرنده نمی‌تواند خالی باشد"
                            } else if (nationalId.length < 10) {
                                errorMessage = "کد ملی یا شناسه ملی نامعتبر است"
                            } else {
                                onConfirmTransfer(transfereeName, nationalId, trackingCode)
                                onDismissRequest()
                            }
                        }
                    ) {
                        AppText("ثبت انتقال قطعی", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
