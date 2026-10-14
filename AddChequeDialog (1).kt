package com.mojtaba.chequemanager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mojtaba.chequemanager.core.ui.PersianDigitsVisualTransformation
import com.mojtaba.chequemanager.core.ui.components.AppText
import com.mojtaba.chequemanager.core.ui.theme.BNazaninFontFamily
import com.mojtaba.chequemanager.core.utils.JalaliDate
import com.mojtaba.chequemanager.core.utils.MoneyToWordsConverter
import com.mojtaba.chequemanager.core.utils.PersianNumberUtils
import com.mojtaba.chequemanager.domain.model.ChequeDirection
import com.mojtaba.chequemanager.presentation.components.AppJalaliDatePickerDialog
import com.mojtaba.chequemanager.presentation.components.ChequeConfirmationDialog
import com.mojtaba.chequemanager.presentation.components.ChequeSerialInputField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddChequeDialog(
    direction: ChequeDirection,
    firstAvailableSerial: String = "1001",
    serialRangeFrom: String = "1001",
    serialRangeTo: String = "1050",
    onDismissRequest: () -> Unit,
    onSaveCheque: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val amountBringIntoViewRequester = remember { BringIntoViewRequester() }
    val scrollState = rememberScrollState()

    var sayadId by remember { mutableStateOf("") }
    var bankAccount by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountRial by remember { mutableStateOf(0L) }
    var rawAmountText by remember { mutableStateOf("") }

    var issueDate by remember { mutableStateOf(JalaliDate.now()) }
    var dueDate by remember { mutableStateOf(JalaliDate.now()) }
    var activeDatePickerTitle by remember { mutableStateOf<String?>(null) }
    var showConfirmationPopup by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppText(
                    text = if (direction == ChequeDirection.PAYABLE) "ثبت چک صیادی پرداختی" else "ثبت چک صیادی دریافتی",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ۱. شناسه ۱۶ رقمی صیادی (وسط‌نویس)
                OutlinedTextField(
                    value = sayadId,
                    onValueChange = { if (it.length <= 16) sayadId = it },
                    label = { CenterLabel("شناسه ۱۶ رقمی صیادی") },
                    textStyle = TextStyle(fontFamily = BNazaninFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PersianDigitsVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ۲. شماره حساب بانکی (فارسی، هم‌اندازه با فونت شناسه صیادی: ۱۷sp و وسط‌نویس)
                OutlinedTextField(
                    value = bankAccount,
                    onValueChange = { bankAccount = it },
                    label = { CenterLabel("شماره حساب بانکی") },
                    textStyle = TextStyle(fontFamily = BNazaninFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PersianDigitsVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ۳. شماره سریال (وسط‌نویس + اولین سریال فعال + زیرنویس کلیک‌پذیر)
                if (direction == ChequeDirection.PAYABLE) {
                    ChequeSerialInputField(
                        suggestedFirstActiveSerial = firstAvailableSerial,
                        serialRangeFrom = serialRangeFrom,
                        serialRangeTo = serialRangeTo,
                        currentSerial = serialNumber,
                        onSerialChange = { serialNumber = it }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // ۴. شماره فاکتور (فارسی و وسط‌نویس)
                OutlinedTextField(
                    value = invoiceNumber,
                    onValueChange = { invoiceNumber = it },
                    label = { CenterLabel("شماره فاکتور") },
                    textStyle = TextStyle(fontFamily = BNazaninFontFamily, fontSize = 16.sp, textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PersianDigitsVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ۵. فیلد مبلغ چک (وسط‌نویس، کامای پایین‌نشین، اتواسکرول عمیق تا خط بالای فیلد پایینی)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(amountBringIntoViewRequester)
                        .padding(bottom = 60.dp) // رزرو فضای اسکرول برای رویت بوردِر فیلد پایینی
                ) {
                    OutlinedTextField(
                        value = PersianNumberUtils.toPersianDigits(rawAmountText),
                        onValueChange = { input ->
                            val cleanDigits = input.filter { it.isDigit() }
                            rawAmountText = cleanDigits
                            amountRial = cleanDigits.toLongOrNull() ?: 0L
                        },
                        label = { CenterLabel("مبلغ چک") },
                        leadingIcon = {
                            AppText("ریال", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 12.dp))
                        },
                        textStyle = TextStyle(fontFamily = BNazaninFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (it.isFocused) {
                                    coroutineScope.launch {
                                        delay(300)
                                        amountBringIntoViewRequester.bringIntoView()
                                    }
                                }
                            }
                    )
                    // زیرنویس به حروف
                    val words = if (amountRial > 0) MoneyToWordsConverter.convert(amountRial) + " ریال تمام" else ""
                    AppText(
                        text = if (words.isNotEmpty()) "به حروف: $words" else "مبلغ را وارد کنید",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    )
                }

                // ۶. تاریخ صدور و سررسید با تقویم جلالی
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { activeDatePickerTitle = "تاریخ صدور" }, modifier = Modifier.weight(1f)) {
                        AppText(PersianNumberUtils.toPersianDigits("صدور: ${issueDate.formatPersian()}"), fontSize = 13.sp)
                    }
                    OutlinedButton(onClick = { activeDatePickerTitle = "تاریخ سررسید" }, modifier = Modifier.weight(1f)) {
                        AppText(PersianNumberUtils.toPersianDigits("سررسید: ${dueDate.formatPersian()}"), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ۷. شرح و بابت سند (وسط‌نویس)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { CenterLabel("بابت / شرح سند") },
                    textStyle = TextStyle(fontFamily = BNazaninFontFamily, fontSize = 15.sp, textAlign = TextAlign.Center),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showConfirmationPopup = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppText("بررسی و تایید نهایی", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // پاپ‌آپ تایید نهایی با لحاظ سریال و شرح
    if (showConfirmationPopup) {
        ChequeConfirmationDialog(
            sayadId = sayadId,
            amountRial = amountRial,
            bankName = "ملت",
            accountNumber = bankAccount,
            serialNumber = serialNumber,
            description = description,
            dueDate = dueDate.formatPersian(),
            issueDate = issueDate.formatPersian(),
            invoiceNumber = invoiceNumber,
            onDismissRequest = { showConfirmationPopup = false },
            onConfirm = {
                showConfirmationPopup = false
                onSaveCheque()
                onDismissRequest()
            }
        )
    }

    // باز شدن تقویم جلالی
    activeDatePickerTitle?.let { title ->
        AppJalaliDatePickerDialog(
            datePickerTypeTitle = title,
            currentDate = if (title == "تاریخ صدور") issueDate else dueDate,
            onDateSelected = { selected ->
                if (title == "تاریخ صدور") issueDate = selected else dueDate = selected
            },
            onDismissRequest = { activeDatePickerTitle = null }
        )
    }
}

@Composable
private fun CenterLabel(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AppText(text, fontSize = 14.sp)
    }
}
