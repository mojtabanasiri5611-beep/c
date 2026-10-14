package com.example.presentation.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.presentation.ui.components.AppJalaliDatePickerDialog
import com.example.presentation.components.ChequeSerialInputField
import com.example.presentation.components.ChequeAmountInputField
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.JalaliDate
import com.example.core.financial.SayadValidator
import com.example.core.util.MoneyToWordsConverter
import com.example.domain.model.BankAccount
import com.example.domain.model.Cheque
import com.example.domain.model.ChequeBook
import com.example.domain.model.ChequeDirection
import com.example.domain.model.ChequeStatus
import com.example.domain.model.Counterparty
import com.example.presentation.ui.components.AppAutoScrollTextField
import com.example.presentation.ui.components.AppCompactNumericTextField
import com.example.presentation.ui.components.AppCompactTextField
import androidx.compose.ui.text.input.KeyboardType
import com.example.presentation.ui.components.AppDatePickerField
import com.example.presentation.ui.components.clearFocusOnTap
import com.example.ui.theme.VazirmatnFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SerialValidationResult {
    VALID,
    OUT_OF_RANGE,
    ALREADY_USED,
    EMPTY
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddChequeDialog(
    bankAccounts: List<BankAccount> = emptyList(),
    chequeBooks: List<ChequeBook> = emptyList(),
    allCheques: List<Cheque> = emptyList(),
    counterparties: List<Counterparty> = emptyList(),
    initialCheque: Cheque? = null,
    onDismiss: () -> Unit,
    onSave: (Cheque, updatedUsedLeaves: Int?) -> Unit,
    onVoidSerials: (List<Long>, BankAccount?, ChequeBook?) -> Unit = { _, _, _ -> }
) {
    val isEdit = initialCheque != null
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val amountBringIntoViewRequester = remember { BringIntoViewRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    var showFinalConfirmationDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScannerDialog = true
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("برای استفاده از اسکنر، اجازه دسترسی به دوربین الزامی است.")
            }
        }
    }

    // ۱. جهت چک (پرداختی / دریافتی)
    var direction by remember { mutableStateOf(initialCheque?.direction ?: ChequeDirection.PAYABLE) }
    val isReceivedCheque = direction == ChequeDirection.RECEIVABLE

    // فیلدهای مشترک و اختصاصی - در حالت ثبت جدید همه فیلدهای پایین کاملاً خالی هستند
    var bankName by remember { mutableStateOf(initialCheque?.bankName ?: "") }
    var sayadId by remember { mutableStateOf(initialCheque?.sayadId ?: "") }

    var selectedBankAccount by remember {
        mutableStateOf(
            if (isEdit) bankAccounts.find { it.id == initialCheque?.bankAccountId }
            else null
        )
    }
    var accountNumber by remember {
        mutableStateOf(selectedBankAccount?.accountNumber ?: "")
    }

    var selectedChequeBook by remember {
        mutableStateOf(
            if (isEdit) chequeBooks.find { it.id == initialCheque?.chequeBookId }
            else null
        )
    }

    // لیست برگه‌های باطل‌شده محلی در صورت پرش
    val voidedSerials = remember(selectedChequeBook) { mutableStateListOf<Long>() }

    // واکشی تمام سریال‌های ثبت‌شده قبلی از دیتابیس برای این دسته‌چک و حساب (استثنا کردن چک جاری در حالت ویرایش)
    val registeredSerialsForBook = remember(selectedChequeBook, allCheques, initialCheque) {
        if (selectedChequeBook != null) {
            allCheques.filter {
                it.direction == ChequeDirection.PAYABLE &&
                (it.chequeBookId == selectedChequeBook?.id || (it.bankAccountId != null && it.bankAccountId == selectedChequeBook?.bankAccountId)) &&
                (initialCheque == null || it.id != initialCheque.id)
            }.mapNotNull { it.chequeNumber.toLongOrNull() }
        } else emptyList()
    }

    // محاسبه پویای اولین سریال پیشنهادی و بازه فعال با تجمیع اطلاعات پایه و چک‌های قبلی
    val startSerial = selectedChequeBook?.startSerial ?: 0L
    val totalLeaves = selectedChequeBook?.totalLeaves ?: 0
    val endSerial = if (totalLeaves > 0 && startSerial > 0) startSerial + totalLeaves - 1 else 0L
    val usedLeavesCount = selectedChequeBook?.usedLeaves ?: 0

    val usedSerialsList = remember(selectedChequeBook, usedLeavesCount, registeredSerialsForBook, voidedSerials.size) {
        if (selectedChequeBook != null && totalLeaves > 0 && startSerial > 0) {
            val baseList = (startSerial until (startSerial + usedLeavesCount)).toList()
            (baseList + registeredSerialsForBook + voidedSerials).distinct().sorted()
        } else (registeredSerialsForBook + voidedSerials).distinct().sorted()
    }

    // محاسبه اولین سریال آزاد (در صورت نبود دسته‌چک، مقدار منفی ۱ برای جلوگیری از درج خودکار عدد ۱)
    val suggestedSerial = remember(selectedChequeBook, usedSerialsList) {
        if (selectedChequeBook != null && totalLeaves > 0 && startSerial > 0) {
            var s = startSerial
            while (s <= endSerial) {
                if (!usedSerialsList.contains(s)) return@remember s
                s++
            }
            -1L
        } else -1L
    }

    val availableSerials = remember(selectedChequeBook, usedSerialsList) {
        if (selectedChequeBook != null && totalLeaves > 0 && startSerial > 0) {
            (startSerial..endSerial).filter { !usedSerialsList.contains(it) }
        } else emptyList()
    }

    val activeRangeText = if (availableSerials.isNotEmpty()) {
        "${availableSerials.first()} تا ${availableSerials.last()}"
    } else if (selectedChequeBook != null) {
        "دسته‌چک پایان یافته"
    } else ""

    var chequeSerial by remember { mutableStateOf(initialCheque?.chequeNumber ?: "") }

    var amount by remember { mutableStateOf(initialCheque?.amount?.toString() ?: "") }
    var payee by remember { mutableStateOf(initialCheque?.counterpartyName ?: "") } // در وجه (پرداختی)
    var issueDate by remember { mutableStateOf(initialCheque?.issueDateJalali ?: JalaliDate.now().formattedString) }
    var dueDate by remember { mutableStateOf(initialCheque?.dueDateJalali ?: JalaliDate.now().addDays(30).formattedString) }
    var invoiceNumber by remember { mutableStateOf(initialCheque?.invoiceNumber ?: "") }
    var chequeDescription by remember { mutableStateOf(initialCheque?.description ?: "") }

    // فیلدهای مخصوص دریافتی
    var accountOwnerName by remember { mutableStateOf(initialCheque?.counterpartyName ?: "") }
    var recipientName by remember { mutableStateOf(initialCheque?.recipientName ?: "") }

    // تنظیمات واریز به حساب (دریافتی)
    var isDepositToAccountChecked by remember { mutableStateOf(false) }
    var selectedDepositAccount by remember { mutableStateOf("") }

    // کنترل پاپ‌آپ‌های اعتبارسنجی سریال و خطاها
    var showOutOfRangeDialog by remember { mutableStateOf(false) }
    var showAlreadyUsedDialog by remember { mutableStateOf(false) }
    var skippedSerialsToConfirm by remember { mutableStateOf<List<Long>?>(null) }

    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var chequeBookDropdownExpanded by remember { mutableStateOf(false) }
    var serialDropdownExpanded by remember { mutableStateOf(false) }
    var payeeDropdownExpanded by remember { mutableStateOf(false) }
    var depositAccountDropdownExpanded by remember { mutableStateOf(false) }

    var showIssueDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var dateValidationError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // اعتبارسنجی غیر مانع صیادی
    val isSayadInvalid = sayadId.isNotEmpty() && sayadId.length != 16

    val bankList = bankAccounts.map { it.bankName }.distinct()
    val contactsList = counterparties.map { it.name }.distinct()

    /**
     * صحت‌سنجی جامع شماره سریال (پشتیبانی از چک‌های پرداختی و دریافتی و حالت ویرایش)
     */
    fun validateSerial(): SerialValidationResult {
        if (chequeSerial.isBlank()) {
            return SerialValidationResult.EMPTY
        }

        // در زمان ویرایش چک، بررسی یکتا بودن شماره سریال غیرفعال می‌شود
        if (initialCheque != null) {
            if (selectedChequeBook != null) {
                val input = chequeSerial.toLongOrNull() ?: return SerialValidationResult.OUT_OF_RANGE
                if (input < startSerial || input > endSerial) {
                    return SerialValidationResult.OUT_OF_RANGE
                }
            }
            return SerialValidationResult.VALID
        }

        // صحت‌سنجی عدم تکرار شناسه صیاد + سریال برای چک‌های دریافتی
        if (isReceivedCheque) {
            val isDuplicateReceivable = allCheques.any {
                it.direction == ChequeDirection.RECEIVABLE &&
                it.sayadId.isNotBlank() &&
                it.sayadId == sayadId &&
                it.chequeNumber == chequeSerial &&
                (initialCheque == null || it.id != initialCheque.id)
            }
            return if (isDuplicateReceivable) SerialValidationResult.ALREADY_USED else SerialValidationResult.VALID
        }

        if (selectedChequeBook == null) return SerialValidationResult.VALID

        val input = chequeSerial.toLongOrNull() ?: return SerialValidationResult.OUT_OF_RANGE

        if (input < startSerial || input > endSerial) {
            return SerialValidationResult.OUT_OF_RANGE
        }

        if (usedSerialsList.contains(input)) {
            return SerialValidationResult.ALREADY_USED
        }

        return SerialValidationResult.VALID
    }

    /**
     * موتور اعتبارسنجی جامع تاریخ‌های صدور و سررسید
     */
    fun validateDates(issueDateStr: String, dueDateStr: String): String? {
        val issueJalali = JalaliDate.parse(issueDateStr) ?: return "تاریخ صدور معتبر نیست"
        val dueJalali = JalaliDate.parse(dueDateStr) ?: return "تاریخ سررسید معتبر نیست"

        // ۱. بررسی عدم تقدم تاریخ صدور بر سررسید
        if (dueJalali < issueJalali) {
            return "تاریخ سررسید نمی‌تواند قبل از تاریخ صدور باشد"
        }

        // ۲. قانون سقف ۳ ساله چک صیادی (۱۰۹۵ روز)
        val daysBetween = issueJalali.daysUntil(dueJalali)
        if (daysBetween > 1095) {
            return "تاریخ سررسید چک صیادی نمی‌تواند بیش از ۳ سال (۱۰۹۵ روز) از تاریخ صدور باشد"
        }

        // ۳. کنترل هشدار تاریخ صدور در گذشته خیلی دور (بیش از ۳۶۵ روز قبل)
        val today = JalaliDate.now()
        if (issueJalali.daysUntil(today) > 365) {
            return "تاریخ صدور وارد شده بیش از ۱ سال در گذشته است؛ لطفاً تاریخ را بررسی کنید"
        }

        return null
    }

    var pendingUpdatedUsedLeaves by remember { mutableStateOf<Int?>(null) }

    fun performSave(updatedUsedLeaves: Int? = null) {
        val amt = amount.toLongOrNull() ?: return
        val dueParsed = JalaliDate.parse(dueDate) ?: JalaliDate.now().addDays(30)
        val issueParsed = JalaliDate.parse(issueDate) ?: JalaliDate.now()
        val finalSayad = SayadValidator.cleanDigits(sayadId).ifEmpty { sayadId.trim() }

        val finalDescription = buildString {
            append(chequeDescription.trim())
            if (isReceivedCheque && isDepositToAccountChecked && selectedDepositAccount.isNotEmpty()) {
                if (isNotEmpty()) append(" • ")
                append("واریز به حساب: $selectedDepositAccount")
            }
        }.trim().ifEmpty { null }

        val newCheque = Cheque(
            id = initialCheque?.id ?: 0L,
            sayadId = finalSayad,
            chequeNumber = chequeSerial,
            amount = amt,
            dueDateJalali = dueParsed.formattedString,
            dueDateTimestamp = dueParsed.toTimestamp(),
            issueDateJalali = issueParsed.formattedString,
            issueDateTimestamp = issueParsed.toTimestamp(),
            direction = direction,
            counterpartyName = if (isReceivedCheque) accountOwnerName.trim() else payee.trim(),
            recipientName = if (isReceivedCheque) recipientName.trim() else "",
            bankName = bankName.trim(),
            bankAccountId = if (isReceivedCheque) {
                if (isDepositToAccountChecked) {
                    bankAccounts.find { "${it.bankName} - ${it.accountNumber}" == selectedDepositAccount }?.id
                } else null
            } else selectedBankAccount?.id,
            chequeBookId = if (isReceivedCheque) null else selectedChequeBook?.id,
            invoiceNumber = invoiceNumber.trim().ifEmpty { null },
            description = finalDescription,
            status = initialCheque?.status ?: ChequeStatus.PENDING,
            hasAlarm = initialCheque?.hasAlarm ?: true,
            alarmLeadDays = initialCheque?.alarmLeadDays ?: 3,
            createdAt = initialCheque?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        if (!isReceivedCheque && selectedChequeBook != null) {
            val usedCount = updatedUsedLeaves ?: (selectedChequeBook!!.usedLeaves + 1)
            val remaining = (selectedChequeBook!!.totalLeaves - usedCount).coerceAtLeast(0)
            com.example.presentation.settings.checkChequeBookRemainingCapacity(
                remainingLeafCount = remaining,
                bankName = selectedChequeBook!!.bankName,
                accountNumber = selectedChequeBook!!.accountNumber,
                onTriggerAlert = { bName, _ ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("هشدار: تنها $remaining برگ از دسته‌چک $bName باقی مانده است.")
                    }
                }
            )
        }
        onSave(newCheque, updatedUsedLeaves)
    }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) "ویرایش چک صیادی" else "ثبت چک صیادی جدید",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.SnackbarHost(hostState = snackbarHostState)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .clearFocusOnTap()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ۱. انتخاب نوع چک با ابعاد ثابت، بدون شکستن متن و با بوردر و هایلایت
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { 
                                    direction = ChequeDirection.PAYABLE
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .testTag("cheque_type_payable"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!isReceivedCheque) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent
                                ),
                                border = BorderStroke(
                                    width = if (!isReceivedCheque) 2.dp else 1.dp,
                                    color = if (!isReceivedCheque) MaterialTheme.colorScheme.primary else Color.LightGray
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (!isReceivedCheque) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "چک پرداختی",
                                        fontFamily = VazirmatnFontFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (!isReceivedCheque) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isReceivedCheque) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { 
                                    direction = ChequeDirection.RECEIVABLE
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .testTag("cheque_type_receivable"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isReceivedCheque) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color.Transparent
                                ),
                                border = BorderStroke(
                                    width = if (isReceivedCheque) 2.dp else 1.dp,
                                    color = if (isReceivedCheque) Color(0xFF2E7D32) else Color.LightGray
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isReceivedCheque) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "چک دریافتی",
                                        fontFamily = VazirmatnFontFamily,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isReceivedCheque) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isReceivedCheque) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // -------------------------------------------------------------
                        // حالت ۱: فیلدهای چک پرداختی
                        // -------------------------------------------------------------
                        if (!isReceivedCheque) {
                            // ۱. نام بانک (دراپ‌لیست)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                AppCompactTextField(
                                    value = bankName,
                                    onValueChange = { 
                                        bankName = it
                                        bankDropdownExpanded = true
                                    },
                                    label = "نام بانک",
                                    readOnly = bankList.isNotEmpty(),
                                    leadingIcon = if (bankList.isNotEmpty()) {
                                        {
                                             IconButton(onClick = { bankDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "انتخاب بانک")
                                            }
                                        }
                                    } else null
                                )
                                if (bankList.isNotEmpty()) {
                                    DropdownMenu(
                                        expanded = bankDropdownExpanded,
                                        onDismissRequest = { bankDropdownExpanded = false }
                                    ) {
                                        bankList.forEach { bank ->
                                            DropdownMenuItem(
                                                text = { Text(bank, fontFamily = VazirmatnFontFamily) },
                                                onClick = {
                                                    bankName = bank
                                                    // پاکسازی کامل تمام فیلدهای پایین به جز تاریخ‌ها
                                                    selectedBankAccount = null
                                                    accountNumber = ""
                                                    selectedChequeBook = null
                                                    sayadId = ""
                                                    chequeSerial = ""
                                                    amount = ""
                                                    payee = ""
                                                    invoiceNumber = ""
                                                    chequeDescription = ""
                                                    accountOwnerName = ""
                                                    recipientName = ""
                                                    isDepositToAccountChecked = false
                                                    selectedDepositAccount = ""
                                                    bankDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // ۲. شناسه صیادی (۱۶ رقم) با آیکون اسکنر بارکد
                            AppCompactNumericTextField(
                                value = sayadId,
                                onValueChange = { sayadId = it },
                                label = "شناسه صیادی",
                                maxLength = 16,
                                isError = isSayadInvalid,
                                errorMessage = if (isSayadInvalid) "شناسه صیادی باید ۱۶ رقم باشد (غیر مانع)" else null,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                showScannerDialog = true
                                            } else {
                                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp).testTag("open_scanner_btn")
                                    ) {
                                        Icon(
                                            Icons.Default.QrCodeScanner,
                                            contentDescription = "اسکنر صیاد",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("sayad_input")
                            )

                            // ۳. حساب بانکی
                            val availableAccounts = bankAccounts.filter { it.bankName == bankName }
                            val hasAccounts = availableAccounts.isNotEmpty()
                            Box(modifier = Modifier.fillMaxWidth()) {
                                AppCompactTextField(
                                    value = accountNumber,
                                    onValueChange = { accountNumber = it },
                                    label = "حساب بانکی",
                                    readOnly = hasAccounts,
                                    leadingIcon = if (hasAccounts) {
                                        {
                                            IconButton(onClick = { accountDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "انتخاب حساب")
                                            }
                                        }
                                    } else null
                                )
                                if (hasAccounts) {
                                    DropdownMenu(
                                        expanded = accountDropdownExpanded,
                                        onDismissRequest = { accountDropdownExpanded = false }
                                    ) {
                                        availableAccounts.forEach { acc ->
                                            DropdownMenuItem(
                                                text = { Text("${acc.accountNumber} (${acc.accountHolder})", fontFamily = VazirmatnFontFamily) },
                                                onClick = {
                                                    selectedBankAccount = acc
                                                    accountNumber = acc.accountNumber
                                                    accountDropdownExpanded = false

                                                    // بارگذاری دسته‌چک‌های این حساب
                                                    val booksForAccount = chequeBooks.filter { it.bankAccountId == acc.id }
                                                    selectedChequeBook = booksForAccount.firstOrNull()
                                                    // شماره سریال پاک می‌ماند تا کاربر از لیست انتخاب کند یا دستی بنویسد
                                                    chequeSerial = ""
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // ۳.۱. در صورت وجود بیش از یک دسته‌چک برای این حساب
                            val booksForSelectedAccount = if (selectedBankAccount != null) {
                                chequeBooks.filter { it.bankAccountId == selectedBankAccount?.id }
                            } else emptyList()

                            if (booksForSelectedAccount.size > 1) {
                                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                                    AppCompactTextField(
                                        value = selectedChequeBook?.let { "سری ${it.sayadSeries} (سریال ${it.startSerial} تا ${it.startSerial + it.totalLeaves - 1})" } ?: "انتخاب دسته‌چک",
                                        onValueChange = {},
                                        label = "دسته‌چک فعال",
                                        readOnly = true,
                                        leadingIcon = {
                                            IconButton(onClick = { chequeBookDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "انتخاب دسته‌چک")
                                            }
                                        }
                                    )
                                    DropdownMenu(
                                        expanded = chequeBookDropdownExpanded,
                                        onDismissRequest = { chequeBookDropdownExpanded = false }
                                    ) {
                                        booksForSelectedAccount.forEach { book ->
                                            DropdownMenuItem(
                                                text = { Text("سری ${book.sayadSeries} (سریال ${book.startSerial} تا ${book.startSerial + book.totalLeaves - 1})", fontFamily = VazirmatnFontFamily) },
                                                onClick = {
                                                    selectedChequeBook = book
                                                    chequeBookDropdownExpanded = false
                                                    chequeSerial = ""
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // ۴. شماره سریال برگه چک (اتصال مستقیم به اولین برگه سفید بدون دراپ‌لیست)
                            if (direction == ChequeDirection.PAYABLE) {
                                val firstAvailableSerial = if (suggestedSerial > 0) suggestedSerial.toString() else ""
                                val rangeStart = selectedChequeBook?.startSerial?.toString() ?: ""
                                val rangeEnd = selectedChequeBook?.let { (it.startSerial + it.totalLeaves - 1).toString() } ?: ""
                                ChequeSerialInputField(
                                    suggestedFirstActiveSerial = firstAvailableSerial,
                                    serialRangeFrom = rangeStart,
                                    serialRangeTo = rangeEnd,
                                    currentSerial = chequeSerial,
                                    onSerialChange = { chequeSerial = it }
                                )
                            } else {
                                AppCompactNumericTextField(
                                    value = chequeSerial,
                                    onValueChange = { chequeSerial = it },
                                    label = "شماره سریال چک",
                                    subText = null
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // ۵. فیلد مبلغ با اتواسکرول اختصاصی و زیرنویس حروفی کامل
                            ChequeAmountInputField(
                                amountRial = amount.toLongOrNull() ?: 0L,
                                onAmountChange = { newAmt ->
                                    amount = if (newAmt > 0) newAmt.toString() else ""
                                },
                                currencyUnit = "ریال",
                                modifier = Modifier.testTag("amount_input")
                            )

                            // ۶. در وجه (دراپ‌لیست طرف حساب)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                AppCompactTextField(
                                    value = payee,
                                    onValueChange = { 
                                        payee = it
                                        payeeDropdownExpanded = true
                                    },
                                    label = "در وجه",
                                    leadingIcon = if (contactsList.isNotEmpty()) {
                                        {
                                            IconButton(onClick = { payeeDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "انتخاب طرف حساب")
                                            }
                                        }
                                    } else null
                                )
                                if (contactsList.isNotEmpty()) {
                                    DropdownMenu(
                                        expanded = payeeDropdownExpanded,
                                        onDismissRequest = { payeeDropdownExpanded = false }
                                    ) {
                                        contactsList.forEach { contact ->
                                            DropdownMenuItem(
                                                text = { Text(contact, fontFamily = VazirmatnFontFamily) },
                                                onClick = {
                                                    payee = contact
                                                    payeeDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // ۷. تاریخ صدور و سررسید
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppDatePickerField(
                                    label = "تاریخ صدور",
                                    date = JalaliDate.toPersianDigits(issueDate),
                                    onClick = { showIssueDatePicker = true },
                                    modifier = Modifier.weight(1f)
                                )
                                AppDatePickerField(
                                    label = "سررسید چک",
                                    date = JalaliDate.toPersianDigits(dueDate),
                                    onClick = { showDueDatePicker = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            dateValidationError?.let { err ->
                                Text(
                                    text = err,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // ۸. شماره فاکتور
                            AppCompactNumericTextField(
                                value = invoiceNumber,
                                onValueChange = { invoiceNumber = it },
                                label = "شماره فاکتور"
                            )

                            // ۹. شرح چک
                            AppCompactTextField(
                                value = chequeDescription,
                                onValueChange = { chequeDescription = it },
                                label = "شرح چک"
                            )

                        } else {
                            // -------------------------------------------------------------
                            // حالت ۲: فیلدهای چک دریافتی
                            // -------------------------------------------------------------
                            Box(modifier = Modifier.fillMaxWidth()) {
                                AppCompactTextField(
                                    value = bankName,
                                    onValueChange = { 
                                        bankName = it
                                    },
                                    label = "نام بانک",
                                    leadingIcon = if (bankList.isNotEmpty()) {
                                        {
                                            IconButton(onClick = { bankDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "انتخاب بانک")
                                            }
                                        }
                                    } else null
                                )
                                if (bankList.isNotEmpty()) {
                                    DropdownMenu(
                                        expanded = bankDropdownExpanded,
                                        onDismissRequest = { bankDropdownExpanded = false }
                                    ) {
                                        bankList.forEach { bank ->
                                            DropdownMenuItem(
                                                text = { Text(bank, fontFamily = VazirmatnFontFamily) },
                                                onClick = {
                                                    bankName = bank
                                                    bankDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            AppCompactNumericTextField(
                                value = sayadId,
                                onValueChange = { sayadId = it },
                                label = "شناسه صیادی",
                                maxLength = 16,
                                isError = isSayadInvalid,
                                errorMessage = if (isSayadInvalid) "شناسه صیادی باید ۱۶ رقم باشد (غیر مانع)" else null,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                showScannerDialog = true
                                            } else {
                                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp).testTag("open_scanner_btn_rec")
                                    ) {
                                        Icon(
                                            Icons.Default.QrCodeScanner,
                                            contentDescription = "اسکنر صیاد",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            )

                            AppCompactNumericTextField(
                                value = accountNumber,
                                onValueChange = { accountNumber = it },
                                label = "شماره حساب"
                            )

                            AppCompactNumericTextField(
                                value = chequeSerial,
                                onValueChange = { chequeSerial = it },
                                label = "شماره سریال چک"
                            )

                            ChequeAmountInputField(
                                amountRial = amount.toLongOrNull() ?: 0L,
                                onAmountChange = { newAmt ->
                                    amount = if (newAmt > 0) newAmt.toString() else ""
                                },
                                currencyUnit = "ریال",
                                modifier = Modifier.testTag("amount_input_received")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppDatePickerField(
                                    label = "تاریخ صدور",
                                    date = JalaliDate.toPersianDigits(issueDate),
                                    onClick = { showIssueDatePicker = true },
                                    modifier = Modifier.weight(1f)
                                )
                                AppDatePickerField(
                                    label = "سررسید چک",
                                    date = JalaliDate.toPersianDigits(dueDate),
                                    onClick = { showDueDatePicker = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            dateValidationError?.let { err ->
                                Text(
                                    text = err,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            AppCompactTextField(
                                value = accountOwnerName,
                                onValueChange = { accountOwnerName = it },
                                label = "نام صاحب حساب"
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isDepositToAccountChecked,
                                    onCheckedChange = { isDepositToAccountChecked = it }
                                )
                                Text("واریز به حساب", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = VazirmatnFontFamily))
                            }

                            if (isDepositToAccountChecked) {
                                val allAccountsList = bankAccounts.map { "${it.bankName} - ${it.accountNumber}" }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    AppCompactTextField(
                                        value = selectedDepositAccount,
                                        onValueChange = { selectedDepositAccount = it },
                                        label = "حساب جهت واریز",
                                        readOnly = allAccountsList.isNotEmpty(),
                                        leadingIcon = if (allAccountsList.isNotEmpty()) {
                                            {
                                                IconButton(onClick = { depositAccountDropdownExpanded = true }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "انتخاب حساب واریز")
                                                }
                                            }
                                        } else null
                                    )
                                    if (allAccountsList.isNotEmpty()) {
                                        DropdownMenu(
                                            expanded = depositAccountDropdownExpanded,
                                            onDismissRequest = { depositAccountDropdownExpanded = false }
                                        ) {
                                            allAccountsList.forEach { accStr ->
                                                DropdownMenuItem(
                                                    text = { Text(accStr, fontFamily = VazirmatnFontFamily) },
                                                    onClick = {
                                                        selectedDepositAccount = accStr
                                                        depositAccountDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            AppCompactTextField(
                                value = recipientName,
                                onValueChange = { recipientName = it },
                                label = "نام گیرنده چک"
                            )

                            AppCompactNumericTextField(
                                value = invoiceNumber,
                                onValueChange = { invoiceNumber = it },
                                label = "شماره فاکتور (اختیاری)"
                            )

                            AppCompactTextField(
                                value = chequeDescription,
                                onValueChange = { chequeDescription = it },
                                label = "شرح چک (اختیاری)"
                            )
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val amt = amount.toLongOrNull()

                        // اعتبارسنجی جامع فیلدهای الزامی و نمایش اسنک‌بار
                        if (chequeSerial.isBlank()) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("سریال چک وارد نشده است") }
                            return@Button
                        }
                        
                        when (val result = validateSerial()) {
                            SerialValidationResult.OUT_OF_RANGE -> {
                                coroutineScope.launch { snackbarHostState.showSnackbar("سریال وارد شده خارج از محدوده دسته‌چک است") }
                                return@Button
                            }
                            SerialValidationResult.ALREADY_USED -> {
                                coroutineScope.launch { snackbarHostState.showSnackbar("این سریال قبلاً ثبت یا باطل شده است") }
                                return@Button
                            }
                            else -> {}
                        }

                        if (amt == null || amt <= 0) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("مبلغ وارد شده معتبر نیست") }
                            return@Button
                        }

                        if (bankName.isBlank()) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("نام بانک را وارد کنید") }
                            return@Button
                        }

                        if (isReceivedCheque && accountOwnerName.isBlank()) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("نام صاحب حساب را وارد کنید") }
                            return@Button
                        }

                        if (!isReceivedCheque && payee.isBlank()) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("نام گیرنده چک را وارد کنید") }
                            return@Button
                        }

                        // بررسی تاریخ‌ها
                        val dateErr = validateDates(issueDate, dueDate)
                        if (dateErr != null) {
                            coroutineScope.launch { snackbarHostState.showSnackbar(dateErr) }
                            return@Button
                        }

                        // چک کردن برای پرش سریال (فقط برای چک پرداختی)
                        if (!isReceivedCheque) {
                            val inputNum = chequeSerial.toLongOrNull()
                            if (inputNum != null && suggestedSerial > 0 && inputNum > suggestedSerial) {
                                val skipped = (suggestedSerial until inputNum).filter {
                                    !usedSerialsList.contains(it)
                                }
                                if (skipped.isNotEmpty()) {
                                    skippedSerialsToConfirm = skipped
                                    return@Button
                                }
                            }
                        }
                        showFinalConfirmationDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().testTag("submit_cheque_button"),
                    enabled = amount.isNotEmpty() && bankName.isNotEmpty()
                ) {
                    Text("ثبت و بررسی چک", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text("انصراف", fontFamily = VazirmatnFontFamily)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )

        // -------------------------------------------------------------
        // پاپ‌آپ‌های اعتبارسنجی سریال
        // -------------------------------------------------------------
        if (showOutOfRangeDialog) {
            AlertDialog(
                onDismissRequest = { showOutOfRangeDialog = false },
                title = { Text("خطای شماره سریال", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold) },
                text = { 
                    Text(
                        "شماره سریال وارد شده در محدوده شماره سریال‌های این دسته‌چک (از $startSerial تا $endSerial) نمی‌باشد.",
                        fontFamily = VazirmatnFontFamily
                    ) 
                },
                confirmButton = {
                    TextButton(onClick = { showOutOfRangeDialog = false }) {
                        Text("متوجه شدم", fontFamily = VazirmatnFontFamily)
                    }
                }
            )
        }

        if (showAlreadyUsedDialog) {
            AlertDialog(
                onDismissRequest = { showAlreadyUsedDialog = false },
                title = { Text("سریال تکراری یا باطل‌شده", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold) },
                text = { 
                    Text(
                        "شماره سریال $chequeSerial قبلاً در این دسته‌چک ثبت شده یا جزو برگه‌های باطل‌شده می‌باشد.",
                        fontFamily = VazirmatnFontFamily
                    ) 
                },
                confirmButton = {
                    TextButton(onClick = { showAlreadyUsedDialog = false }) {
                        Text("اصلاح سریال", fontFamily = VazirmatnFontFamily)
                    }
                }
            )
        }

        skippedSerialsToConfirm?.let { skipped ->
            AlertDialog(
                onDismissRequest = { skippedSerialsToConfirm = null },
                title = { Text("استعلام برگه‌های قبل از سریال $chequeSerial", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "آیا سریال‌های ${skipped.joinToString(" و ")} به عنوان چک باطله ثبت شوند؟",
                        fontFamily = VazirmatnFontFamily
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onVoidSerials(skipped, selectedBankAccount, selectedChequeBook)
                            skippedSerialsToConfirm = null
                            val inputNum = chequeSerial.toLongOrNull() ?: startSerial
                            pendingUpdatedUsedLeaves = (inputNum - startSerial + 1).toInt().coerceIn(0, totalLeaves)
                            showFinalConfirmationDialog = true
                        }
                    ) {
                        Text("بلی (ثبت باطله و ادامه)", fontFamily = VazirmatnFontFamily)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = {
                                skippedSerialsToConfirm = null
                                showFinalConfirmationDialog = true
                            }
                        ) {
                            Text("خیر (ادامه)", fontFamily = VazirmatnFontFamily)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(onClick = { skippedSerialsToConfirm = null }) {
                            Text("انصراف", fontFamily = VazirmatnFontFamily)
                        }
                    }
                }
            )
        }

        if (showIssueDatePicker) {
            AppJalaliDatePickerDialog(
                title = "تاریخ صدور چک",
                initialDateString = issueDate,
                onDateSelected = { selected ->
                    issueDate = selected
                    showIssueDatePicker = false
                    dateValidationError = validateDates(selected, dueDate)
                },
                onDismiss = { showIssueDatePicker = false }
            )
        }

        if (showDueDatePicker) {
            AppJalaliDatePickerDialog(
                title = "تاریخ سررسید چک",
                initialDateString = dueDate,
                onDateSelected = { selected ->
                    dueDate = selected
                    showDueDatePicker = false
                    dateValidationError = validateDates(issueDate, selected)
                },
                onDismiss = { showDueDatePicker = false }
            )
        }

        if (showScannerDialog) {
            SayadScannerDialog(
                onDismiss = { showScannerDialog = false },
                onCodeScanned = { scannedSayad ->
                    sayadId = scannedSayad
                }
            )
        }

        // پاپ‌آپ تایید نهایی پیش از ذخیره قطعی
        if (showFinalConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showFinalConfirmationDialog = false },
                title = { Text("تایید نهایی ثبت چک", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("مبلغ: ${JalaliDate.formatCurrency(amount.toLongOrNull() ?: 0L, false)}", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
                        
                        // نمایش مبلغ به حروف به تومان با رنگ قرمز پررنگ
                        val rialAmt = amount.toLongOrNull() ?: 0L
                        val tomanAmt = rialAmt / 10
                        val words = MoneyToWordsConverter.convertNumberToWords(tomanAmt)
                        com.example.presentation.ui.cheques.components.ChequeFinalConfirmationPreview(
                            amountInWords = words,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text("تاریخ سررسید: ${JalaliDate.toPersianDigits(dueDate)}", fontFamily = VazirmatnFontFamily)
                        Text("در وجه: $payee", fontFamily = VazirmatnFontFamily)
                        Text("بانک: $bankName | سریال: $chequeSerial", fontFamily = VazirmatnFontFamily)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showFinalConfirmationDialog = false
                            performSave(updatedUsedLeaves = pendingUpdatedUsedLeaves)
                        }
                    ) {
                        Text("تایید قطعی و ذخیره", fontFamily = VazirmatnFontFamily)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showFinalConfirmationDialog = false }) {
                        Text("اصلاح اطلاعات", fontFamily = VazirmatnFontFamily)
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun ChequeSerialSubtextIndicator(
    suggestedSerial: Long,
    startRange: Long,
    endRange: Long,
    onQuickInsert: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (suggestedSerial > 0) {
            Text(
                text = "پیشنهاد: برگه $suggestedSerial",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = VazirmatnFontFamily,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.clickable { onQuickInsert() }
            )
        }
        if (startRange > 0 && endRange >= startRange) {
            Text(
                text = "محدوده: $startRange تا $endRange",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = VazirmatnFontFamily,
                    color = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}
