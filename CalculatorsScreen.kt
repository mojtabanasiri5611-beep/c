package com.example.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import com.example.core.common.CurrencyFormatter
import com.example.core.common.JalaliDate
import com.example.core.financial.CurrencyAmountVisualTransformation
import com.example.core.financial.FinancialCalculator
import com.example.presentation.ui.dialogs.AppDeleteConfirmDialog
import com.example.presentation.ui.components.AppJalaliDatePickerDialog
import com.example.ui.theme.VazirmatnFontFamily
import com.example.domain.model.ChequeDirection

data class AveragingInputItem(
    val id: Long = System.currentTimeMillis() + (1..1000).random(),
    var amountText: String = "",
    var jalaliDateText: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(
    isToman: Boolean
) {
    var selectedToolTab by remember { mutableStateOf(0) } // 0: راس‌گیری, 1: تنزیل و سود

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calculators_screen")
    ) {
        PrimaryTabRow(selectedTabIndex = selectedToolTab) {
            Tab(
                selected = selectedToolTab == 0,
                onClick = { selectedToolTab = 0 },
                text = { Text("راس‌گیری چک‌ها", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Calculate, contentDescription = null) }
            )
            Tab(
                selected = selectedToolTab == 1,
                onClick = { selectedToolTab = 1 },
                text = { Text("محاسبه‌گر تنزیل و سود", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.TrendingDown, contentDescription = null) }
            )
        }

        if (selectedToolTab == 0) {
            ChequeMaturityAveragingView(isToman = isToman)
        } else {
            ChequeDiscountingView(isToman = isToman)
        }
    }
}

@Composable
fun ChequeMaturityAveragingView(isToman: Boolean) {
    val today = remember { JalaliDate.now() }
    var baseDateText by remember { mutableStateOf(today.formattedString) }
    var interestRateText by remember { mutableStateOf("") }
    var isShowingBaseDatePicker by remember { mutableStateOf(false) }
    var activePickingItemIndex by remember { mutableStateOf<Int?>(null) }

    val items = remember {
        mutableStateListOf<AveragingInputItem>()
    }

    var resultModel by remember {
        mutableStateOf<com.example.core.calculator.AverageMaturityCalculator.MaturityResult?>(null)
    }
    var itemToDeleteIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // نام بخش صرفاً «راسگیری» در یک خط بدون شکستن
            Text(
                text = "راسگیری",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // تاریخ مبنا قابل تغییر با استفاده از کامپوننت جدید
                    com.example.presentation.ui.components.AppDatePickerField(
                        label = "تاریخ مبنای راسگیری",
                        date = JalaliDate.toPersianDigits(baseDateText),
                        onClick = { isShowingBaseDatePicker = true }
                    )

                    // عنوان سود ماهانه در بالای باکس و خارج از آن (در واقع اینجا داخل کارد ولی با چیدمان درخواستی)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "سود ماهانه % (اختیاری)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = interestRateText,
                            onValueChange = { interestRateText = it },
                            placeholder = { Text("مثال: 3", fontFamily = VazirmatnFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فهرست چک‌های مبادله‌ای:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        items.add(AveragingInputItem(jalaliDateText = today.addDays(30).formattedString))
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("افزودن ردیف", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = JalaliDate.toPersianDigits("${index + 1}."),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )

                    OutlinedTextField(
                        value = item.amountText,
                        onValueChange = {
                            item.amountText = JalaliDate.toEnglishDigits(it).filter { c -> c.isDigit() }
                        },
                        placeholder = { Text("مبلغ (ریال)", fontFamily = VazirmatnFontFamily, fontSize = 11.sp) },
                        textStyle = TextStyle(
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        visualTransformation = CurrencyAmountVisualTransformation(),
                        modifier = Modifier.weight(1.3f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = JalaliDate.toPersianDigits(item.jalaliDateText),
                        onValueChange = { item.jalaliDateText = JalaliDate.toEnglishDigits(it) },
                        placeholder = { Text("سررسید", fontFamily = VazirmatnFontFamily, fontSize = 11.sp) },
                        textStyle = TextStyle(
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = { activePickingItemIndex = index },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "تقویم", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        },
                        singleLine = true
                    )

                    IconButton(
                        onClick = { itemToDeleteIndex = index },
                        enabled = items.size > 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف ردیف",
                            tint = if (items.size > 1) MaterialTheme.colorScheme.error else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val baseDays = JalaliDate.parseToDays(baseDateText)
                    val rate = interestRateText.toDoubleOrNull() ?: 0.0
                    val chequeList = items.mapNotNull {
                        val amt = it.amountText.toLongOrNull() ?: return@mapNotNull null
                        if (it.jalaliDateText.isBlank()) return@mapNotNull null
                        com.example.domain.model.Cheque(
                            amount = amt,
                            dueDateJalali = it.jalaliDateText,
                            sayadId = "",
                            chequeNumber = "",
                            bankName = "",
                            issueDateJalali = "",
                            counterpartyName = "",
                            recipientName = "",
                            dueDateTimestamp = JalaliDate.parseToDays(it.jalaliDateText) * 86400000L,
                            issueDateTimestamp = System.currentTimeMillis(),
                            direction = ChequeDirection.PAYABLE
                        )
                    }
                    val maturityResult = com.example.core.calculator.AverageMaturityCalculator.calculate(chequeList, baseDays, rate)
                    resultModel = maturityResult
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("محاسبه راس چک‌ها", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Result Display Card
        if (resultModel != null) {
            val res = resultModel!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "نتیجه راس‌گیری وزنی چک‌ها:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تاریخ دقیق راس چک:", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                JalaliDate.toPersianDigits(res.averageDueDateJalali),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مجموع مبالغ:", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = CurrencyFormatter.format(res.totalAmount, isToman),
                                style = CurrencyFormatter.LtrNumberStyle.copy(
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        if (res.adjustedPresentValue < res.totalAmount) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ارزش فعلی (تنزیل شده):", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    text = CurrencyFormatter.format(res.adjustedPresentValue, isToman),
                                    style = CurrencyFormatter.LtrNumberStyle.copy(
                                        fontSize = 15.sp,
                                        color = Color(0xFFD97706)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    if (isShowingBaseDatePicker) {
        AppJalaliDatePickerDialog(
            initialDateString = baseDateText,
            onDateSelected = { baseDateText = it },
            onDismiss = { isShowingBaseDatePicker = false }
        )
    }

    activePickingItemIndex?.let { idx ->
        if (idx in items.indices) {
            AppJalaliDatePickerDialog(
                initialDateString = items[idx].jalaliDateText,
                onDateSelected = { selected ->
                    items[idx].jalaliDateText = selected
                },
                onDismiss = { activePickingItemIndex = null }
            )
        }
    }

    if (itemToDeleteIndex != null) {
        val targetIdx = itemToDeleteIndex!!
        AppDeleteConfirmDialog(
            itemName = "ردیف ${JalaliDate.toPersianDigits((targetIdx + 1).toString())} راس‌گیری",
            onConfirm = {
                if (items.size > 1 && targetIdx in items.indices) {
                    items.removeAt(targetIdx)
                }
                itemToDeleteIndex = null
            },
            onDismiss = { itemToDeleteIndex = null }
        )
    }
}

@Composable
fun ChequeDiscountingView(isToman: Boolean) {
    var amountText by remember { mutableStateOf("100000000") }
    var annualRateText by remember { mutableStateOf("24") }
    var daysText by remember { mutableStateOf("60") }

    var result by remember {
        mutableStateOf<FinancialCalculator.DiscountingResult?>(null)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ورودی‌های تنزیل و خرید دین",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = JalaliDate.toEnglishDigits(it).filter { c -> c.isDigit() } },
                        label = { Text("مبلغ اسمی چک (ریال)") },
                        textStyle = TextStyle(
                            fontFamily = VazirmatnFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        visualTransformation = CurrencyAmountVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = annualRateText,
                            onValueChange = { annualRateText = it },
                            label = { Text("نرخ سود سالانه (%)") },
                            textStyle = TextStyle(
                                fontFamily = VazirmatnFontFamily,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = daysText,
                            onValueChange = { daysText = JalaliDate.toEnglishDigits(it).filter { c -> c.isDigit() } },
                            label = { Text("روز مانده تا سررسید") },
                            textStyle = TextStyle(
                                fontFamily = VazirmatnFontFamily,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amt = amountText.toLongOrNull() ?: 0L
                            val rate = annualRateText.toDoubleOrNull() ?: 0.0
                            val days = daysText.toIntOrNull() ?: 0
                            result = FinancialCalculator.calculateDiscounting(amt, days, rate)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("محاسبه تنزیل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (result != null) {
            val res = result!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "نتایج محاسبه تنزیل چک:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مبلغ خالص دریافتی:", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                text = CurrencyFormatter.format(res.netPayout, isToman),
                                style = CurrencyFormatter.LtrNumberStyle.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF047857)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("کارمزد کسر شده بابت تنزیل:", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                text = CurrencyFormatter.format(res.discountFee, isToman),
                                style = CurrencyFormatter.LtrNumberStyle.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFFDC2626)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نرخ بازده موثر سالانه:", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                JalaliDate.toPersianDigits(String.format("%.2f %%", res.effectiveAnnualRatePercent)),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
