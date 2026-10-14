package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.AppResult
import com.example.core.common.JalaliDate
import com.example.domain.model.BankAccount
import com.example.domain.model.Cheque
import com.example.domain.model.ChequeBook
import com.example.domain.model.ChequeDirection
import com.example.domain.model.ChequeStatus
import com.example.domain.model.Counterparty
import com.example.domain.model.Endorsement
import com.example.domain.model.FinancialSummary
import com.example.domain.repository.BankAccountRepository
import com.example.domain.repository.ChequeBookRepository
import com.example.domain.repository.ChequeRepository
import com.example.domain.repository.CounterpartyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ChequeSortOrder(val title: String) {
    DEFAULT("پیش‌فرض"),
    DUE_DATE("تاریخ سررسید"),
    AMOUNT_DESC("مبلغ (از زیاد به کم)"),
    AMOUNT_ASC("مبلغ (از کم به زیاد)"),
    COUNTERPARTY("نام گیرنده / طرف حساب (در وجه)")
}

data class ChequeFilter(
    val searchQuery: String = "",
    val direction: ChequeDirection? = null,
    val status: ChequeStatus? = null,
    val sortOrder: ChequeSortOrder = ChequeSortOrder.DEFAULT
)

data class ChequeUiState(
    val selectedTab: Int = 0, // 0: Dashboard, 1: Cheques & Calendar, 2: Base Data, 3: Calculators
    val viewMode: Int = 0, // 0: List View, 1: Calendar View
    val isToman: Boolean = false,
    val filter: ChequeFilter = ChequeFilter(),
    val calendarYear: Int = JalaliDate.now().year,
    val calendarMonth: Int = JalaliDate.now().month,
    val selectedCalendarDate: JalaliDate? = JalaliDate.now(),
    val isAddChequeDialogVisible: Boolean = false,
    val isEndorsementDialogVisible: Boolean = false,
    val isSettingsDialogVisible: Boolean = false,
    val alertLeadDays: Int = 3,
    val isSoundAlertEnabled: Boolean = true,
    val isVibrationAlertEnabled: Boolean = true,
    val activeChequeForAction: Cheque? = null,
    val editingCheque: Cheque? = null,
    val snackbarMessage: String? = null
)

sealed class ChequeUiEvent {
    data class ShowError(val message: String) : ChequeUiEvent()
    data class ShowVoidConfirmationDialog(val skipped: List<Long>, val target: Long) : ChequeUiEvent()
    data object NavigateToNextStep : ChequeUiEvent()
}

class ChequeViewModel(
    private val chequeRepository: ChequeRepository,
    private val chequeBookRepository: ChequeBookRepository,
    private val bankAccountRepository: BankAccountRepository,
    private val counterpartyRepository: CounterpartyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChequeUiState())
    val uiState: StateFlow<ChequeUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableStateFlow<ChequeUiEvent?>(null)
    val uiEvent: StateFlow<ChequeUiEvent?> = _uiEvent.asStateFlow()

    val financialSummary: StateFlow<FinancialSummary> = chequeRepository.getFinancialSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    val allCheques: StateFlow<List<Cheque>> = chequeRepository.getAllCheques()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingAlertCheques: StateFlow<List<Cheque>> = chequeRepository.getUpcomingMaturingCheques(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chequeBooks: StateFlow<List<ChequeBook>> = chequeBookRepository.getAllChequeBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankAccounts: StateFlow<List<BankAccount>> = bankAccountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val counterparties: StateFlow<List<Counterparty>> = counterpartyRepository.getAllCounterparties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // فیلتر هوشمند با نرمال‌سازی ارقام فارسی به انگلیسی و تقویم
    val filteredCheques: StateFlow<List<Cheque>> = combine(
        allCheques,
        _uiState
    ) { cheques, state ->
        val rawQuery = state.filter.searchQuery.trim()
        val englishQuery = JalaliDate.toEnglishDigits(rawQuery)

        val baseFiltered = cheques.filter { cheque ->
            val matchesQuery = rawQuery.isEmpty() ||
                    cheque.sayadId.contains(englishQuery, ignoreCase = true) ||
                    cheque.chequeNumber.contains(englishQuery, ignoreCase = true) ||
                    cheque.counterpartyName.contains(rawQuery, ignoreCase = true) ||
                    cheque.bankName.contains(rawQuery, ignoreCase = true) ||
                    (cheque.invoiceNumber?.contains(englishQuery, ignoreCase = true) == true) ||
                    (cheque.description?.contains(rawQuery, ignoreCase = true) == true)

            val matchesDirection = state.filter.direction == null || cheque.direction == state.filter.direction
            val matchesStatus = state.filter.status == null || cheque.status == state.filter.status

            val matchesDate = if (state.viewMode == 1 && state.selectedCalendarDate != null) {
                cheque.dueDateJalali == state.selectedCalendarDate.formattedString
            } else {
                true
            }

            matchesQuery && matchesDirection && matchesStatus && matchesDate
        }

        when (state.filter.sortOrder) {
            ChequeSortOrder.DEFAULT -> baseFiltered
            ChequeSortOrder.DUE_DATE -> baseFiltered.sortedWith(
                compareBy<Cheque> { it.dueDateTimestamp }.thenBy { it.id }
            )
            ChequeSortOrder.AMOUNT_DESC -> baseFiltered.sortedByDescending { it.amount }
            ChequeSortOrder.AMOUNT_ASC -> baseFiltered.sortedBy { it.amount }
            ChequeSortOrder.COUNTERPARTY -> baseFiltered.sortedWith(
                compareBy(String.CASE_INSENSITIVE_ORDER) { it.counterpartyName.ifBlank { it.recipientName } }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setViewMode(mode: Int) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun toggleCurrencyUnit() {
        _uiState.update { it.copy(isToman = !it.isToman) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(filter = it.filter.copy(searchQuery = query)) }
    }

    fun setFilterDirection(direction: ChequeDirection?) {
        _uiState.update { it.copy(filter = it.filter.copy(direction = direction)) }
    }

    fun setFilterStatus(status: ChequeStatus?) {
        _uiState.update { it.copy(filter = it.filter.copy(status = status)) }
    }

    fun setSortOrder(sortOrder: ChequeSortOrder) {
        _uiState.update { it.copy(filter = it.filter.copy(sortOrder = sortOrder)) }
    }

    fun prevCalendarMonth() {
        _uiState.update { state ->
            var m = state.calendarMonth - 1
            var y = state.calendarYear
            if (m < 1) {
                m = 12
                y--
            }
            state.copy(calendarYear = y, calendarMonth = m)
        }
    }

    fun nextCalendarMonth() {
        _uiState.update { state ->
            var m = state.calendarMonth + 1
            var y = state.calendarYear
            if (m > 12) {
                m = 1
                y++
            }
            state.copy(calendarYear = y, calendarMonth = m)
        }
    }

    fun setCalendarYear(year: Int) {
        _uiState.update { it.copy(calendarYear = year) }
    }

    fun setCalendarMonth(month: Int) {
        _uiState.update { it.copy(calendarMonth = month) }
    }

    fun selectCalendarDate(date: JalaliDate) {
        _uiState.update { it.copy(selectedCalendarDate = date) }
    }

    fun showAddChequeDialog() {
        _uiState.update { it.copy(isAddChequeDialogVisible = true) }
    }

    fun hideAddChequeDialog() {
        _uiState.update { it.copy(isAddChequeDialogVisible = false, editingCheque = null) }
    }

    fun showEditChequeDialog(cheque: Cheque) {
        _uiState.update { it.copy(isAddChequeDialogVisible = true, editingCheque = cheque) }
    }

    fun updateCheque(cheque: Cheque) {
        viewModelScope.launch {
            val res = chequeRepository.updateCheque(cheque)
            if (res.isSuccess) {
                _uiState.update {
                    it.copy(
                        isAddChequeDialogVisible = false,
                        editingCheque = null,
                        snackbarMessage = "چک صیادی با موفقیت ویرایش شد"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        snackbarMessage = "خطا در ویرایش چک: ${(res as? AppResult.Error)?.message ?: "خطای ناشناخته"}"
                    )
                }
            }
        }
    }

    fun showEndorsementDialog(cheque: Cheque) {
        if (cheque.direction == ChequeDirection.PAYABLE) {
            _uiState.update { it.copy(snackbarMessage = "فقط چک‌های دریافتی قابلیت ظهرنویسی و خرج کردن دارند.") }
            return
        }
        _uiState.update {
            it.copy(
                isEndorsementDialogVisible = true,
                activeChequeForAction = cheque
            )
        }
    }

    fun hideEndorsementDialog() {
        _uiState.update {
            it.copy(
                isEndorsementDialogVisible = false,
                activeChequeForAction = null
            )
        }
    }

    fun saveCheque(cheque: Cheque, updatedUsedLeaves: Int? = null) {
        viewModelScope.launch {
            val res = chequeRepository.insertCheque(cheque)
            if (res.isSuccess) {
                // اگر چک پرداختی از حساب متصل و دسته‌چک بود، شمارنده برگ مصرف‌شده را در اطلاعات پایه به‌روزرسانی کن
                if (cheque.direction == ChequeDirection.PAYABLE && cheque.bankAccountId != null) {
                    val matchingBook = chequeBooks.value.firstOrNull { it.bankAccountId == cheque.bankAccountId }
                    matchingBook?.let { book ->
                        val newUsed = (updatedUsedLeaves ?: (book.usedLeaves + 1)).coerceIn(0, book.totalLeaves)
                        chequeBookRepository.updateChequeBook(book.copy(usedLeaves = newUsed))
                    }
                }

                _uiState.update {
                    it.copy(
                        isAddChequeDialogVisible = false,
                        snackbarMessage = "چک صیادی با موفقیت در دفاتر مالی ثبت شد"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        snackbarMessage = "خطا در ثبت سند: ${(res as? AppResult.Error)?.message ?: "خطای ناشناخته"}"
                    )
                }
            }
        }
    }

    fun updateChequeStatus(chequeId: Long, status: ChequeStatus, description: String? = null) {
        viewModelScope.launch {
            val res = chequeRepository.updateChequeStatus(chequeId, status, description)
            if (res.isSuccess) {
                _uiState.update { it.copy(snackbarMessage = "وضعیت چک به «${status.titlePersian}» تغییر یافت") }
            }
        }
    }

    fun deleteCheque(chequeId: Long) {
        viewModelScope.launch {
            val res = chequeRepository.deleteCheque(chequeId)
            if (res.isSuccess) {
                _uiState.update { it.copy(snackbarMessage = "سند چک مورد نظر با موفقیت حذف شد") }
            }
        }
    }

    fun addEndorsement(chequeId: Long, endorsement: Endorsement) {
        viewModelScope.launch {
            val res = chequeRepository.addEndorsement(chequeId, endorsement)
            if (res.isSuccess) {
                // به‌روزرسانی اتوماتیک وضعیت چک به انتقال‌یافته (TRANSFERRED)
                chequeRepository.updateChequeStatus(
                    id = chequeId,
                    status = ChequeStatus.TRANSFERRED,
                    description = "خرج شده به نام ${endorsement.transfereeName}"
                )
                _uiState.update {
                    it.copy(
                        isEndorsementDialogVisible = false,
                        activeChequeForAction = null,
                        snackbarMessage = "چک با موفقیت به نام ${endorsement.transfereeName} منتقل گردید"
                    )
                }
            }
        }
    }

    /**
     * ثبت انتقال و خرج کردن چک همراه با اطلاعات تخصصی صیاد و تعهد احتمالی
     */
    fun transferCheque(
        chequeId: Long,
        transfereeName: String,
        nationalId: String,
        trackingCode: String,
        transferDate: String = JalaliDate.now().formattedString
    ) {
        viewModelScope.launch {
            val res = chequeRepository.transferCheque(
                chequeId = chequeId,
                transfereeName = transfereeName,
                nationalId = nationalId,
                trackingCode = trackingCode,
                transferDate = transferDate
            )
            if (res.isSuccess) {
                _uiState.update {
                    it.copy(
                        isEndorsementDialogVisible = false,
                        activeChequeForAction = null,
                        snackbarMessage = "انتقال چک به $transfereeName با موفقیت ثبت شد و در تعهدات احتمالی قرار گرفت."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        snackbarMessage = "خطا در ثبت انتقال چک: ${(res as? AppResult.Error)?.message ?: "خطای سرور یا دیتابیس"}"
                    )
                }
            }
        }
    }

    fun addChequeBook(book: ChequeBook) {
        viewModelScope.launch {
            chequeBookRepository.insertChequeBook(book)
            _uiState.update { it.copy(snackbarMessage = "دسته‌چک جدید در سیستم ثبت شد") }
        }
    }

    fun updateChequeBook(book: ChequeBook) {
        viewModelScope.launch {
            chequeBookRepository.updateChequeBook(book)
            _uiState.update { it.copy(snackbarMessage = "دسته‌چک با موفقیت ویرایش شد") }
        }
    }

    fun deleteChequeBook(book: ChequeBook) {
        viewModelScope.launch {
            chequeBookRepository.deleteChequeBook(book)
            _uiState.update { it.copy(snackbarMessage = "دسته‌چک با موفقیت حذف شد") }
        }
    }

    fun addBankAccount(account: BankAccount) {
        viewModelScope.launch {
            bankAccountRepository.insertAccount(account)
            _uiState.update { it.copy(snackbarMessage = "حساب بانکی جدید با موفقیت ذخیره شد") }
        }
    }

    fun updateBankAccount(account: BankAccount) {
        viewModelScope.launch {
            bankAccountRepository.updateAccount(account)
            _uiState.update { it.copy(snackbarMessage = "حساب بانکی با موفقیت ویرایش شد") }
        }
    }

    fun deleteBankAccount(account: BankAccount) {
        viewModelScope.launch {
            bankAccountRepository.deleteAccount(account)
            _uiState.update { it.copy(snackbarMessage = "حساب بانکی با موفقیت حذف شد") }
        }
    }

    fun addCounterparty(counterparty: Counterparty) {
        viewModelScope.launch {
            counterpartyRepository.insertCounterparty(counterparty)
            _uiState.update { it.copy(snackbarMessage = "طرف‌حساب تجاری ذخیره گردید") }
        }
    }

    fun updateCounterparty(counterparty: Counterparty) {
        viewModelScope.launch {
            counterpartyRepository.updateCounterparty(counterparty)
            _uiState.update { it.copy(snackbarMessage = "طرف‌حساب تجاری با موفقیت ویرایش شد") }
        }
    }

    fun deleteCounterparty(counterparty: Counterparty) {
        viewModelScope.launch {
            counterpartyRepository.deleteCounterparty(counterparty)
            _uiState.update { it.copy(snackbarMessage = "طرف‌حساب تجاری با موفقیت حذف شد") }
        }
    }

    fun showSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogVisible = true) }
    }

    fun hideSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogVisible = false) }
    }

    fun updateAlertSettings(leadDays: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        _uiState.update {
            it.copy(
                alertLeadDays = leadDays,
                isSoundAlertEnabled = soundEnabled,
                isVibrationAlertEnabled = vibrationEnabled,
                isSettingsDialogVisible = false,
                snackbarMessage = "تنظیمات یادآور و هشدارها ذخیره شد"
            )
        }
    }

    fun voidSerials(serials: List<Long>, bankAccount: BankAccount?, chequeBook: ChequeBook?) {
        viewModelScope.launch {
            // چک‌های باطله دیگر در لیست‌های اصلی ذخیره نمی‌شوند و به صورت آمار تعداد برگ‌های باطله در دسته‌چک ثبت می‌شوند
            chequeBook?.let { book ->
                val maxVoided = serials.maxOrNull() ?: 0L
                val neededUsedCount = (maxVoided - book.startSerial + 1).toInt()
                val updatedUsed = if (neededUsedCount > book.usedLeaves) neededUsedCount else book.usedLeaves
                val updatedVoided = book.voidedLeaves + serials.size
                chequeBookRepository.updateChequeBook(book.copy(usedLeaves = updatedUsed, voidedLeaves = updatedVoided))
            }
            
            _uiState.update { it.copy(snackbarMessage = "${serials.size} برگ چک به عنوان باطله در دسته‌چک ثبت شد") }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onSerialEntered(
        enteredSerial: Long,
        startSerial: Long,
        totalLeaves: Int,
        usedSerials: Set<Long>,
        voidedSerials: Set<Long>,
        currentSuggested: Long
    ) {
        val endSerial = if (totalLeaves > 0) startSerial + totalLeaves - 1 else Long.MAX_VALUE
        if (enteredSerial < startSerial || enteredSerial > endSerial) {
            _uiEvent.value = ChequeUiEvent.ShowError("شماره سریال وارد شده در محدوده شماره سریال‌های این دسته‌چک نمی‌باشد.")
            return
        }

        if (usedSerials.contains(enteredSerial) || voidedSerials.contains(enteredSerial)) {
            _uiEvent.value = ChequeUiEvent.ShowError("این شماره سریال قبلاً مصرف یا باطل شده است.")
            return
        }

        if (enteredSerial > currentSuggested) {
            val skippedSerials = (currentSuggested until enteredSerial).filter {
                !usedSerials.contains(it) && !voidedSerials.contains(it)
            }
            if (skippedSerials.isNotEmpty()) {
                _uiEvent.value = ChequeUiEvent.ShowVoidConfirmationDialog(skippedSerials, enteredSerial)
                return
            }
        }
        _uiEvent.value = ChequeUiEvent.NavigateToNextStep
    }

    fun clearEvent() {
        _uiEvent.value = null
    }

    companion object {
        fun provideFactory(
            chequeRepository: ChequeRepository,
            chequeBookRepository: ChequeBookRepository,
            bankAccountRepository: BankAccountRepository,
            counterpartyRepository: CounterpartyRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChequeViewModel(
                    chequeRepository,
                    chequeBookRepository,
                    bankAccountRepository,
                    counterpartyRepository
                ) as T
            }
        }
    }
}
