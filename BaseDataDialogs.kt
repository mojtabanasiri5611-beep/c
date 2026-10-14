package com.example.presentation.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.presentation.ui.components.AppTextField
import com.example.presentation.ui.components.AppNumericTextField
import com.example.domain.usecase.ValidationUtils
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.JalaliDate
import com.example.core.financial.CurrencyAmountVisualTransformation
import com.example.domain.model.BankAccount
import com.example.domain.model.ChequeBook
import com.example.domain.model.Counterparty
import com.example.ui.theme.VazirmatnFontFamily

@Composable
fun AddBankAccountDialog(
    initialAccount: BankAccount? = null,
    onDismiss: () -> Unit,
    onSave: (BankAccount) -> Unit
) {
    val isEdit = initialAccount != null
    var bankName by remember { mutableStateOf(initialAccount?.bankName ?: "") }
    var branchNumber by remember { mutableStateOf(initialAccount?.branchNumber ?: "") }
    var accountNumber by remember { mutableStateOf(initialAccount?.accountNumber ?: "") }
    var accountHolder by remember { mutableStateOf(initialAccount?.accountHolder ?: "") }
    var shabaNumber by remember { mutableStateOf(initialAccount?.shabaNumber?.removePrefix("IR") ?: "") }
    var cardNumber by remember { mutableStateOf(initialAccount?.cardNumber ?: "") }
    var initialBalanceText by remember { mutableStateOf(initialAccount?.currentBalance?.toString() ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentDensity = LocalDensity.current
    val fixedFontDensity = remember(currentDensity.density) {
        Density(density = currentDensity.density, fontScale = 1.0f)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalDensity provides fixedFontDensity
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (isEdit) "ویرایش حساب بانکی" else "افزودن حساب بانکی جدید",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = "نام بانک (مانند ملت، سامان، صادرات)",
                        modifier = Modifier.testTag("add_bank_name_input")
                    )

                    AppTextField(
                        value = branchNumber,
                        onValueChange = { branchNumber = it },
                        label = "شماره / کد شعبه (اختیاری)"
                    )

                    AppNumericTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = "شماره حساب",
                        modifier = Modifier.testTag("add_account_number_input")
                    )

                    AppTextField(
                        value = accountHolder,
                        onValueChange = { accountHolder = it },
                        label = "نام صاحب حساب"
                    )

                    AppNumericTextField(
                        value = shabaNumber,
                        onValueChange = { shabaNumber = it },
                        label = "شماره شبا (۲۴ رقم بدون IR)",
                        maxLength = 24
                    )

                    AppNumericTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = "شماره کارت (۱۶ رقم اختیاری)",
                        maxLength = 16
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "موجودی اولیه حساب (ریال)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = initialBalanceText,
                            onValueChange = { initialBalanceText = JalaliDate.toEnglishDigits(it).filter { c -> c.isDigit() } },
                            textStyle = TextStyle(fontFamily = VazirmatnFontFamily, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                            visualTransformation = CurrencyAmountVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bankName.isBlank()) {
                            errorMessage = "لطفاً نام بانک را وارد کنید"
                            return@Button
                        }
                        if (accountNumber.isBlank()) {
                            errorMessage = "لطفاً شماره حساب را وارد کنید"
                            return@Button
                        }
                        if (accountHolder.isBlank()) {
                            errorMessage = "لطفاً نام صاحب حساب را وارد کنید"
                            return@Button
                        }

                        var formattedShaba = shabaNumber.trim().replace(" ", "").uppercase()
                        if (formattedShaba.isNotEmpty()) {
                            if (!formattedShaba.startsWith("IR")) {
                                formattedShaba = "IR$formattedShaba"
                            }
                            if (!ValidationUtils.validateSheba(formattedShaba)) {
                                errorMessage = "شماره شبا نامعتبر است (باید ۲۴ رقم بدون IR باشد)"
                                return@Button
                            }
                        }

                        if (cardNumber.isNotEmpty() && cardNumber.length != 16) {
                            errorMessage = "شماره کارت باید دقیقاً ۱۶ رقم باشد"
                            return@Button
                        }

                        val balance = initialBalanceText.toLongOrNull() ?: 0L
                        val account = BankAccount(
                            id = initialAccount?.id ?: 0L,
                            bankName = bankName.trim(),
                            accountNumber = accountNumber.trim(),
                            branchNumber = branchNumber.trim().ifBlank { null },
                            accountHolder = accountHolder.trim(),
                            shabaNumber = formattedShaba.ifBlank { null },
                            cardNumber = cardNumber.ifBlank { null },
                            currentBalance = balance
                        )
                        onSave(account)
                    },
                    modifier = Modifier.testTag("confirm_add_bank_account_btn")
                ) {
                    Text(
                        text = if (isEdit) "ویرایش حساب" else "ذخیره حساب",
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("انصراف", fontFamily = VazirmatnFontFamily)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChequeBookDialog(
    bankAccounts: List<BankAccount>,
    initialChequeBook: ChequeBook? = null,
    onDismiss: () -> Unit,
    onSave: (ChequeBook) -> Unit
) {
    val isEdit = initialChequeBook != null
    var selectedAccount by remember {
        mutableStateOf(
            if (isEdit) {
                bankAccounts.find { it.id == initialChequeBook?.bankAccountId }
            } else {
                bankAccounts.firstOrNull()
            }
        )
    }
    var isAccountDropdownExpanded by remember { mutableStateOf(false) }

    var sayadSeries by remember { mutableStateOf(initialChequeBook?.sayadSeries ?: "") }
    var startSerialText by remember { mutableStateOf(initialChequeBook?.startSerial?.toString() ?: "") }
    var totalLeavesText by remember { mutableStateOf(initialChequeBook?.totalLeaves?.toString() ?: "25") }
    var warningThresholdText by remember { mutableStateOf(initialChequeBook?.warningThreshold?.toString() ?: "5") }
    var voidedLeavesText by remember { mutableStateOf(initialChequeBook?.voidedLeaves?.toString() ?: "0") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentDensity = LocalDensity.current
    val fixedFontDensity = remember(currentDensity.density) {
        Density(density = currentDensity.density, fontScale = 1.0f)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalDensity provides fixedFontDensity
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (isEdit) "ویرایش دسته‌چک" else "افزودن دسته‌چک جدید",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (bankAccounts.isEmpty()) {
                        Text(
                            text = "هیچ حساب بانکی ثبت نشده است. ابتدا یک حساب بانکی اضافه کنید.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "حساب بانکی متصل",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = isAccountDropdownExpanded,
                                onExpandedChange = { isAccountDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedAccount?.let { "${it.bankName} - ${it.accountNumber}" } ?: "انتخاب حساب بانکی",
                                    onValueChange = {},
                                    readOnly = true,
                                    textStyle = TextStyle(fontFamily = VazirmatnFontFamily),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAccountDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = isAccountDropdownExpanded,
                                    onDismissRequest = { isAccountDropdownExpanded = false }
                                ) {
                                    bankAccounts.forEach { acc ->
                                        DropdownMenuItem(
                                            text = { Text("${acc.bankName} - ${acc.accountNumber} (${acc.accountHolder})", fontFamily = VazirmatnFontFamily) },
                                            onClick = {
                                                selectedAccount = acc
                                                isAccountDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        AppNumericTextField(
                            value = startSerialText,
                            onValueChange = { startSerialText = it },
                            label = "شماره سریال شروع (مثلاً ۱۰۰۱)",
                            modifier = Modifier.testTag("add_start_serial_input")
                        )

                        AppNumericTextField(
                            value = totalLeavesText,
                            onValueChange = { totalLeavesText = it },
                            label = "تعداد برگ دسته‌چک مثلاً (۵۰ یا ۱۰۰)"
                        )

                        AppNumericTextField(
                            value = warningThresholdText,
                            onValueChange = { warningThresholdText = it },
                            label = "آستانه هشدار اتمام برگ‌ها (پیش‌فرض ۵ برگ)"
                        )

                        AppNumericTextField(
                            value = voidedLeavesText,
                            onValueChange = { voidedLeavesText = it },
                            label = "تعداد برگ‌های باطله"
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedAccount == null) {
                            errorMessage = "لطفاً ابتدا یک حساب بانکی انتخاب کنید"
                            return@Button
                        }
                        val startSerial = startSerialText.toLongOrNull()
                        if (startSerial == null || startSerial <= 0) {
                            errorMessage = "لطفاً شماره سریال شروع معتبر وارد نمایید"
                            return@Button
                        }
                        val totalLeaves = totalLeavesText.toIntOrNull()
                        if (totalLeaves == null || totalLeaves <= 0) {
                            errorMessage = "تعداد برگ دسته‌چک باید بزرگتر از صفر باشد"
                            return@Button
                        }
                        val warningThreshold = warningThresholdText.toIntOrNull() ?: 5
                        val voidedLeaves = voidedLeavesText.toIntOrNull() ?: 0
                        if (voidedLeaves < 0 || voidedLeaves > totalLeaves) {
                            errorMessage = "تعداد برگ‌های باطله نمی‌تواند منفی یا بیشتر از کل برگ‌های دسته‌چک باشد"
                            return@Button
                        }
                        val endSerial = startSerial + totalLeaves - 1

                        val chequeBook = ChequeBook(
                            id = initialChequeBook?.id ?: 0L,
                            bankAccountId = selectedAccount!!.id,
                            bankName = selectedAccount!!.bankName,
                            accountNumber = selectedAccount!!.accountNumber,
                            sayadSeries = sayadSeries.ifBlank { "عمومی" },
                            startSerial = startSerial,
                            endSerial = endSerial,
                            totalLeaves = totalLeaves,
                            usedLeaves = initialChequeBook?.usedLeaves ?: 0,
                            voidedLeaves = voidedLeaves,
                            warningThreshold = warningThreshold,
                            issueDateJalali = initialChequeBook?.issueDateJalali ?: JalaliDate.now().formattedString
                        )
                        onSave(chequeBook)
                    },
                    enabled = bankAccounts.isNotEmpty(),
                    modifier = Modifier.testTag("confirm_add_cheque_book_btn")
                ) {
                    Text(
                        text = if (isEdit) "ویرایش دسته‌چک" else "ذخیره دسته‌چک",
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("انصراف", fontFamily = VazirmatnFontFamily)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCounterpartyDialog(
    initialCounterparty: Counterparty? = null,
    onDismiss: () -> Unit,
    onSave: (Counterparty) -> Unit
) {
    val isEdit = initialCounterparty != null
    var name by remember { mutableStateOf(initialCounterparty?.name ?: "") }
    var phone by remember { mutableStateOf(initialCounterparty?.phone ?: "") }
    var nationalCode by remember { mutableStateOf(initialCounterparty?.nationalCode ?: "") }
    var creditRating by remember { mutableStateOf(initialCounterparty?.creditRating ?: "5 ستاره (عالی)") }
    var visitorName by remember { mutableStateOf(initialCounterparty?.visitorName ?: "") }
    var visitorPhone by remember { mutableStateOf(initialCounterparty?.visitorPhone ?: "") }
    var isCreditRatingDropdownExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(initialCounterparty?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentDensity = LocalDensity.current
    val fixedFontDensity = remember(currentDensity.density) {
        Density(density = currentDensity.density, fontScale = 1.0f)
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalDensity provides fixedFontDensity
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (isEdit) "ویرایش طرف‌حساب" else "افزودن طرف‌حساب جدید",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "نام شخص یا شرکت (مشتری/گیرنده)",
                        modifier = Modifier.testTag("add_counterparty_name_input")
                    )

                    AppNumericTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "شماره تماس (اختیاری)"
                    )

                    AppNumericTextField(
                        value = nationalCode,
                        onValueChange = { nationalCode = it },
                        label = "کد ملی یا شناسه ملی (اختیاری)",
                        maxLength = 11
                    )

                    // فیلد درجه اعتبار به صورت دراپ‌لیست امتیاز ۱ تا ۵
                    val creditRatingsList = listOf(
                        "5 امتیاز (عالی)",
                        "4 امتیاز (خیلی خوب)",
                        "3 امتیاز (خوب)",
                        "2 امتیاز (متوسط)",
                        "1 امتیاز (ضعیف)"
                    )
                    Column {
                        Text(
                            text = "درجه اعتبار طرف‌حساب (۱ تا ۵ امتیاز):",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = isCreditRatingDropdownExpanded,
                            onExpandedChange = { isCreditRatingDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = creditRating,
                                onValueChange = {},
                                readOnly = true,
                                textStyle = TextStyle(fontFamily = VazirmatnFontFamily),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCreditRatingDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = isCreditRatingDropdownExpanded,
                                onDismissRequest = { isCreditRatingDropdownExpanded = false }
                            ) {
                                creditRatingsList.forEach { rating ->
                                    DropdownMenuItem(
                                        text = { Text(rating, fontFamily = VazirmatnFontFamily) },
                                        onClick = {
                                            creditRating = rating
                                            isCreditRatingDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // دو فیلد جدید اختیاری: نام ویزیتور و شماره تلفن ویزیتور
                    AppTextField(
                        value = visitorName,
                        onValueChange = { visitorName = it },
                        label = "نام ویزیتور (اختیاری)"
                    )

                    AppNumericTextField(
                        value = visitorPhone,
                        onValueChange = { visitorPhone = it },
                        label = "شماره تلفن ویزیتور (اختیاری)"
                    )

                    AppTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "توضیحات و یادداشت (اختیاری)"
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "لطفاً نام طرف‌حساب را وارد کنید"
                            return@Button
                        }
                        val counterparty = Counterparty(
                            id = initialCounterparty?.id ?: 0L,
                            name = name.trim(),
                            phone = phone.ifBlank { null },
                            nationalCode = nationalCode.ifBlank { null },
                            creditRating = creditRating.trim().ifBlank { "5 امتیاز (عالی)" },
                            visitorName = visitorName.trim().ifBlank { null },
                            visitorPhone = visitorPhone.trim().ifBlank { null },
                            notes = notes.ifBlank { null },
                            totalReceivableVolume = initialCounterparty?.totalReceivableVolume ?: 0L,
                            bouncedCount = initialCounterparty?.bouncedCount ?: 0
                        )
                        onSave(counterparty)
                    },
                    modifier = Modifier.testTag("confirm_add_counterparty_btn")
                ) {
                    Text(
                        text = if (isEdit) "ویرایش طرف‌حساب" else "ذخیره طرف‌حساب",
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("انصراف", fontFamily = VazirmatnFontFamily)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
