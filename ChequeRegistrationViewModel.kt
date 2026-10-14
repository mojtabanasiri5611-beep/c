package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChequeBookConfig(
    val startSerial: Long = 1L,
    val endSerial: Long = 25L
)

sealed class SerialValidationResult {
    object Valid : SerialValidationResult()
    data class OutOfRange(val message: String) : SerialValidationResult()
    data class AlreadyUsedOrVoided(val message: String) : SerialValidationResult()
    data class RequiresVoidConfirmation(val skippedSerials: List<Long>, val targetSerial: Long) : SerialValidationResult()
}

class ChequeRegistrationViewModel : ViewModel() {

    private var currentBookConfig = ChequeBookConfig(startSerial = 1, endSerial = 25)

    // مجموعه‌های مربوط به چک‌های مصرف‌شده و باطل‌شده
    private val usedSerials = mutableSetOf<Long>()
    private val voidedSerials = mutableSetOf<Long>()

    private val _suggestedSerial = MutableStateFlow(1L)
    val suggestedSerial: StateFlow<Long> = _suggestedSerial.asStateFlow()

    init {
        recalculateSuggestedSerial()
    }

    fun updateBookConfig(start: Long, end: Long, used: Set<Long> = emptySet(), voided: Set<Long> = emptySet()) {
        currentBookConfig = ChequeBookConfig(startSerial = start, endSerial = end)
        usedSerials.clear()
        usedSerials.addAll(used)
        voidedSerials.clear()
        voidedSerials.addAll(voided)
        recalculateSuggestedSerial()
    }

    /**
     * محاسبه پویا و دقیق اولین سریال در دسترس.
     */
    private fun recalculateSuggestedSerial() {
        var current = currentBookConfig.startSerial
        while (current <= currentBookConfig.endSerial) {
            if (!usedSerials.contains(current) && !voidedSerials.contains(current)) {
                _suggestedSerial.value = current
                return
            }
            current++
        }
        _suggestedSerial.value = -1L // نشان‌دهنده اتمام دسته‌چک
    }

    /**
     * اعتبارسنجی ورودی کاربر هنگام کلیک روی دکمه ثبت
     */
    fun validateEnteredSerial(enteredSerialStr: String): SerialValidationResult {
        val enteredSerial = enteredSerialStr.toLongOrNull()
            ?: return SerialValidationResult.OutOfRange("فرمت سریال نامعتبر است.")

        // کنترل ۱: خارج از رنج دسته‌چک
        if (enteredSerial < currentBookConfig.startSerial || enteredSerial > currentBookConfig.endSerial) {
            return SerialValidationResult.OutOfRange(
                "شماره سریال خارج از محدوده دسته‌چک است (محدوده مجاز: ${currentBookConfig.startSerial} تا ${currentBookConfig.endSerial})."
            )
        }

        // کنترل ۲: استفاده یا باطل شدن قبلی
        if (usedSerials.contains(enteredSerial) || voidedSerials.contains(enteredSerial)) {
            return SerialValidationResult.AlreadyUsedOrVoided("این شماره سریال قبلاً مصرف یا باطل شده است.")
        }

        // کنترل ۳: پرش از روی سریال‌ها (نیاز به تایید ابطال)
        val suggested = _suggestedSerial.value
        if (suggested > 0 && enteredSerial > suggested) {
            val skipped = (suggested until enteredSerial).filter {
                !usedSerials.contains(it) && !voidedSerials.contains(it)
            }
            if (skipped.isNotEmpty()) {
                return SerialValidationResult.RequiresVoidConfirmation(skipped, enteredSerial)
            }
        }

        return SerialValidationResult.Valid
    }

    /**
     * ثبت نهایی سریال بر اساس پاسخ کاربر به دیالوگ ابطال
     */
    fun confirmRegistration(targetSerial: Long, voidSkippedSerials: List<Long>?) {
        // اگر کاربر «بلی» را انتخاب کرد، سریال‌های جاافتاده باطل می‌شوند
        if (!voidSkippedSerials.isNullOrEmpty()) {
            voidedSerials.addAll(voidSkippedSerials)
        }

        // سریال وارد شده به عنوان مصرف‌شده ثبت می‌شود
        usedSerials.add(targetSerial)

        // محاسبه مجدد پیشنهاد برای چک بعدی
        recalculateSuggestedSerial()
    }
}
