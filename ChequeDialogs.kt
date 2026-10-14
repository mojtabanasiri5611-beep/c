package com.example.presentation.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.JalaliDate
import com.example.core.financial.SayadValidator
import com.example.domain.model.Cheque
import com.example.domain.model.Counterparty
import com.example.domain.model.Endorsement
import com.example.presentation.ui.components.AppJalaliDatePickerDialog
import com.example.ui.theme.VazirmatnFontFamily

/**
 * دیالوگ خرج کردن (انتقال / ظهرنویسی) چک‌های دریافتی با مشخصات جدید:
 * ۱. عنوان کوچک و در یک خط: "خرج کردن چک"
 * ۲. شناسه صیادی ۴ رقمی و در خط پایین مبلغ به حروف برحسب تومان
 * ۳. تحویل‌گیرنده: امکان انتخاب از لیست طرف‌های حساب یا تایپ مستقیم نام جدید
 * ۴. تاریخ انتقال: حذف کلمه شمسی و استفاده از همان تقویم جلالی محبوب برنامه با لمس آیکون تقویم
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndorsementDialog(
    cheque: Cheque,
    counterparties: List<Counterparty> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Endorsement) -> Unit
) {
    var transfereeName by remember { mutableStateOf("") }
    var transfereeNationalCode by remember { mutableStateOf("") }
    var transferDateText by remember { mutableStateOf(JalaliDate.now().formattedString) }
    var description by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    var isCounterpartyDropdownExpanded by remember { mutableStateOf(false) }
    var isDatePickerOpen by remember { mutableStateOf(false) }

    val currentDensity = LocalDensity.current
    val fixedFontDensity = remember(currentDensity.density) {
        Density(density = currentDensity.density, fontScale = 1.0f)
    }

    // تاریخ انتخابی فعلی برای تقویم
    val parsedDate = remember(transferDateText) {
        JalaliDate.parse(transferDateText) ?: JalaliDate.now()
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalDensity provides fixedFontDensity
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "خرج کردن چک (انتقال به شخص ثالث)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = VazirmatnFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // شناسه صیادی ۴ رقم ۴ رقم
                    val blockedSayad = JalaliDate.toPersianDigits(
                        cheque.sayadId.replace(Regex("[^0-9]"), "").chunked(4).joinToString(" ")
                    )
                    Text(
                        text = "انتقال چک صیادی: \u200E$blockedSayad",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // مبلغ در خط پایین به حروف و برحسب تومان
                    val amountInTomanWords = com.example.core.util.MoneyToWordsConverter.convertRialToTomanWords(cheque.amount.toString())
                    Text(
                        text = "مبلغ: $amountInTomanWords",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = VazirmatnFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        ),
                        color = Color(0xFF8E24AA) // رنگ بنفش زیبا
                    )

                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.4f), thickness = 0.8.dp)

                    // فیلد نام تحویل‌گیرنده با قابلیت انتخاب از طرف‌های حساب یا تایپ مستقیم
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = transfereeName,
                            onValueChange = { transfereeName = it },
                            label = { Text("نام شخص یا شرکت تحویل‌گیرنده", fontFamily = VazirmatnFontFamily, fontSize = 12.sp) },
                            textStyle = TextStyle(
                                fontFamily = VazirmatnFontFamily,
                                fontSize = 13.5.sp,
                                textAlign = TextAlign.Right,
                                textDirection = TextDirection.Rtl
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (counterparties.isNotEmpty()) {
                                    IconButton(onClick = { isCounterpartyDropdownExpanded = !isCounterpartyDropdownExpanded }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "انتخاب از طرف‌های حساب")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (counterparties.isNotEmpty()) {
                            DropdownMenu(
                                expanded = isCounterpartyDropdownExpanded,
                                onDismissRequest = { isCounterpartyDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                counterparties.forEach { party ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(party.name, fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
                                                if (!party.phone.isNullOrBlank()) {
                                                    Text(
                                                        "تلفن: ${JalaliDate.toPersianDigits(party.phone)}",
                                                        fontFamily = VazirmatnFontFamily,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            transfereeName = party.name
                                            if (!party.nationalCode.isNullOrBlank()) {
                                                transfereeNationalCode = party.nationalCode
                                            }
                                            isCounterpartyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = transfereeNationalCode,
                        onValueChange = { transfereeNationalCode = JalaliDate.toEnglishDigits(it).filter { c -> c.isDigit() } },
                        label = { Text("کد ملی / شناسه ملی گیرنده (اختیاری)", fontFamily = VazirmatnFontFamily, fontSize = 12.sp) },
                        textStyle = TextStyle(
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // فیلد تاریخ انتقال بدون کلمه شمسی و با لمس تقویم جلالی
                    OutlinedTextField(
                        value = JalaliDate.toPersianDigits(transferDateText),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تاریخ انتقال", fontFamily = VazirmatnFontFamily, fontSize = 12.sp) },
                        textStyle = TextStyle(
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDatePickerOpen = true },
                        leadingIcon = {
                            IconButton(onClick = { isDatePickerOpen = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "تقویم", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("بابت و توضیحات انتقال", fontFamily = VazirmatnFontFamily, fontSize = 12.sp) },
                        textStyle = TextStyle(
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Right,
                            textDirection = TextDirection.Rtl
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (errorText != null) {
                        Text(
                            text = errorText!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (transfereeName.isBlank()) {
                            errorText = "نام تحویل‌گیرنده الزامی است."
                            return@Button
                        }
                        val endorsement = Endorsement(
                            chequeId = cheque.id,
                            transfereeName = transfereeName.trim(),
                            transfereeNationalId = transfereeNationalCode.trim().ifEmpty { null },
                            endorsementDateJalali = transferDateText.trim(),
                            endorsementTimestamp = (JalaliDate.parse(transferDateText) ?: JalaliDate.now()).toTimestamp(),
                            reason = description.trim().ifEmpty { null }
                        )
                        onConfirm(endorsement)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("تایید انتقال چک", fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                    Text("انصراف", fontFamily = VazirmatnFontFamily)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )

        // تقویم جلالی اختصاصی برنامه با انتخاب تاریخ
        AppJalaliDatePickerDialog(
            isOpen = isDatePickerOpen,
            title = "انتخاب تاریخ واگذاری",
            initialDateString = transferDateText,
            onDismiss = { isDatePickerOpen = false },
            onDateSelected = { formatted ->
                transferDateText = formatted
                isDatePickerOpen = false
            }
        )
    }
}
