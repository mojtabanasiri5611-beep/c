package com.example.presentation.ui.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.JalaliDate
import com.example.core.utils.PersianNumberUtils
import com.example.domain.model.Cheque
import com.example.domain.model.ChequeDirection
import com.example.domain.model.ChequeStatus
import com.example.presentation.ui.cheques.components.ChequeItemCard
import com.example.presentation.ui.components.AppCompactNumericTextField
import com.example.presentation.ui.components.AppDatePickerField
import com.example.ui.theme.VazirmatnFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveReportScreen(
    allCheques: List<Cheque>,
    isToman: Boolean,
    modifier: Modifier = Modifier
) {
    val today = remember { JalaliDate.now() }

    // فیلترهای جستجوی پیشرفته
    var searchQuery by remember { mutableStateOf("") }
    var selectedDirection by remember { mutableStateOf<ChequeDirection?>(null) }
    var selectedStatus by remember { mutableStateOf<ChequeStatus?>(null) }
    var quickDatePeriod by remember { mutableStateOf("همه زمان‌ها") }

    var selectedBank by remember { mutableStateOf("همه بانک‌ها") }
    var startDate by remember { mutableStateOf("1400/01/01") }
    var endDate by remember { mutableStateOf(today.addDays(365).formattedString) }

    var minAmountStr by remember { mutableStateOf("") }
    var maxAmountStr by remember { mutableStateOf("") }

    var showAdvancedFilters by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }

    val bankList = remember(allCheques) {
        listOf("همه بانک‌ها") + allCheques.map { it.bankName }.distinct().sorted()
    }

    // فیلتر هوشمند آرشیو بر اساس کلیه پارامترها
    val filteredArchive = remember(
        allCheques, searchQuery, selectedDirection, selectedStatus,
        selectedBank, startDate, endDate, minAmountStr, maxAmountStr, isToman
    ) {
        val cleanSearch = JalaliDate.toEnglishDigits(searchQuery.trim().lowercase())
        val minAmt = minAmountStr.toLongOrNull()?.let { if (isToman) it * 10 else it } ?: 0L
        val maxAmt = maxAmountStr.toLongOrNull()?.let { if (isToman) it * 10 else it } ?: Long.MAX_VALUE

        allCheques.filter { cheque ->
            // ۱. فیلتر جهت (پرداختی / دریافتی)
            val matchDirection = selectedDirection == null || cheque.direction == selectedDirection

            // ۲. فیلتر وضعیت (در جریان / وصول شده / برگشتی)
            val matchStatus = selectedStatus == null || cheque.status == selectedStatus

            // ۳. فیلتر بانک
            val matchBank = selectedBank == "همه بانک‌ها" || cheque.bankName == selectedBank

            // ۴. فیلتر بازه تاریخ سررسید
            val matchDate = cheque.dueDateJalali >= startDate && cheque.dueDateJalali <= endDate

            // ۵. فیلتر بازه مبلغ
            val matchAmount = cheque.amount in minAmt..maxAmt

            // ۶. جستجوی پیشرفته در شناسه صیادی، سریال چک و طرف حساب
            val matchSearch = if (cleanSearch.isBlank()) true else {
                val sayadNorm = JalaliDate.toEnglishDigits(cheque.sayadId)
                val serialNorm = JalaliDate.toEnglishDigits(cheque.chequeNumber)
                val partyNorm = cheque.counterpartyName.lowercase()
                val bankNorm = cheque.bankName.lowercase()

                sayadNorm.contains(cleanSearch) ||
                serialNorm.contains(cleanSearch) ||
                partyNorm.contains(cleanSearch) ||
                bankNorm.contains(cleanSearch)
            }

            matchDirection && matchStatus && matchBank && matchDate && matchAmount && matchSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "آرشیو و گزارش‌گیری پیشرفته چک‌ها",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = VazirmatnFontFamily,
                fontWeight = FontWeight.Bold
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // کادر جستجوی پیشرفته (شناسه صیادی، سریال چک، طرف حساب)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    placeholder = {
                        Text(
                            text = "جستجوی پیشرفته (شناسه صیادی، سریال، طرف حساب)...",
                            fontSize = 11.5.sp,
                            fontFamily = VazirmatnFontFamily
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "جستجو", modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "پاک کردن", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily)
                )

                // ردیف فیلتر جهت چک (پرداختی / دریافتی / همه)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedDirection == null,
                        onClick = { selectedDirection = null },
                        label = { Text("همه جهت‌ها", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = selectedDirection == ChequeDirection.PAYABLE,
                        onClick = {
                            selectedDirection = if (selectedDirection == ChequeDirection.PAYABLE) null else ChequeDirection.PAYABLE
                        },
                        label = { Text("چک پرداختی", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = selectedDirection == ChequeDirection.RECEIVABLE,
                        onClick = {
                            selectedDirection = if (selectedDirection == ChequeDirection.RECEIVABLE) null else ChequeDirection.RECEIVABLE
                        },
                        label = { Text("چک دریافتی", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp) }
                    )
                }

                // ردیف فیلتر وضعیت چک (در انتظار / وصول شده / برگشتی)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { selectedStatus = null },
                        label = { Text("همه وضعیت‌ها", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = selectedStatus == ChequeStatus.PENDING,
                        onClick = {
                            selectedStatus = if (selectedStatus == ChequeStatus.PENDING) null else ChequeStatus.PENDING
                        },
                        label = { Text("در انتظار", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = selectedStatus == ChequeStatus.CLEARED,
                        onClick = {
                            selectedStatus = if (selectedStatus == ChequeStatus.CLEARED) null else ChequeStatus.CLEARED
                        },
                        label = { Text("وصول شده", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = selectedStatus == ChequeStatus.BOUNCED,
                        onClick = {
                            selectedStatus = if (selectedStatus == ChequeStatus.BOUNCED) null else ChequeStatus.BOUNCED
                        },
                        label = { Text("برگشتی", fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp) }
                    )
                }

                // ردیف دوره‌های زمانی سریع
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val periods = listOf("همه زمان‌ها", "امروز", "۷ روز آینده", "این ماه", "۳ ماه آینده")
                    periods.forEach { period ->
                        FilterChip(
                            selected = quickDatePeriod == period,
                            onClick = {
                                quickDatePeriod = period
                                when (period) {
                                    "امروز" -> {
                                        startDate = today.formattedString
                                        endDate = today.formattedString
                                    }
                                    "۷ روز آینده" -> {
                                        startDate = today.formattedString
                                        endDate = today.addDays(7).formattedString
                                    }
                                    "این ماه" -> {
                                        val monthLength = JalaliDate.getMonthLength(today.year, today.month)
                                        startDate = JalaliDate(today.year, today.month, 1).formattedString
                                        endDate = JalaliDate(today.year, today.month, monthLength).formattedString
                                    }
                                    "۳ ماه آینده" -> {
                                        startDate = today.formattedString
                                        endDate = today.addDays(90).formattedString
                                    }
                                    else -> {
                                        startDate = "1400/01/01"
                                        endDate = today.addDays(365).formattedString
                                    }
                                }
                            },
                            label = { Text(period, fontFamily = VazirmatnFontFamily, fontSize = 11.5.sp) }
                        )
                    }
                }

                // دکمه باز و بسته کردن فیلترهای پیشرفته‌تر (بازه تاریخ دستی، بانک و بازه مبلغ)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedFilters = !showAdvancedFilters }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (showAdvancedFilters) "بستن فیلترهای تکمیلی (بانک، بازه مبلغ و تاریخ)" else "فیلترهای تکمیلی (بانک، بازه مبلغ و تاریخ دستی)",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = if (showAdvancedFilters) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = showAdvancedFilters) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // انتخاب بانک
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { bankDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "بانک: $selectedBank",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily)
                                )
                            }
                            DropdownMenu(
                                expanded = bankDropdownExpanded,
                                onDismissRequest = { bankDropdownExpanded = false }
                            ) {
                                bankList.forEach { bank ->
                                    DropdownMenuItem(
                                        text = { Text(bank, fontFamily = VazirmatnFontFamily) },
                                        onClick = {
                                            selectedBank = bank
                                            bankDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // بازه تاریخ دستی
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppDatePickerField(
                                label = "از تاریخ سررسید",
                                date = JalaliDate.toPersianDigits(startDate),
                                onClick = { showStartDatePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            AppDatePickerField(
                                label = "تا تاریخ سررسید",
                                date = JalaliDate.toPersianDigits(endDate),
                                onClick = { showEndDatePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // بازه مبلغ (حداقل و حداکثر)
                        val unitLabel = if (isToman) "تومان" else "ریال"
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppCompactNumericTextField(
                                value = minAmountStr,
                                onValueChange = { minAmountStr = it },
                                label = "از مبلغ ($unitLabel)",
                                modifier = Modifier.weight(1f)
                            )
                            AppCompactNumericTextField(
                                value = maxAmountStr,
                                onValueChange = { maxAmountStr = it },
                                label = "تا مبلغ ($unitLabel)",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // نوار خلاصه نتایج فیلتر شده
        val totalSum = filteredArchive.sumOf { it.amount }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Text(
                text = "یافته شده: ${PersianNumberUtils.toPersianDigits(filteredArchive.size)} فقره  |  مجموع: ${JalaliDate.formatCurrency(totalSum, isToman)}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // فهرست کارت‌های فیلتر شده
        if (filteredArchive.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "هیچ چکی با فیلترهای انتخابی یافت نشد.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = VazirmatnFontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredArchive, key = { it.id }) { cheque ->
                    ChequeItemCard(
                        cheque = cheque,
                        isToman = isToman,
                        onEditClick = {},
                        onDeleteClick = {},
                        onUpdateStatus = {},
                        onEndorse = {}
                    )
                }
            }
        }
    }

    if (showStartDatePicker) {
        com.example.presentation.ui.components.AppJalaliDatePickerDialog(
            title = "از تاریخ",
            initialDateString = startDate,
            onDateSelected = { 
                startDate = it
                quickDatePeriod = "سفارشی"
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        com.example.presentation.ui.components.AppJalaliDatePickerDialog(
            title = "تا تاریخ",
            initialDateString = endDate,
            onDateSelected = { 
                endDate = it
                quickDatePeriod = "سفارشی"
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}
