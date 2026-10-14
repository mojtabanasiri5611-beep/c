package com.example.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.LockType
import com.example.core.security.SecurityPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SecurityUiState(
    val selectedLockType: LockType = LockType.NONE,
    val isBiometricEnabled: Boolean = false,
    val backupKey: String = "",
    val backupKeyError: String? = null,
    val pinInput: String = "",
    val complexPinInput: String = "",
    val patternInput: List<Int> = emptyList(),
    val isPinDialogOpen: Boolean = false,
    val isComplexPinDialogOpen: Boolean = false,
    val isPatternDialogOpen: Boolean = false,
    val errorMessage: String? = null
)

class SecurityViewModel(
    private val securityPreferences: SecurityPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.update {
            it.copy(
                selectedLockType = securityPreferences.getLockType(),
                isBiometricEnabled = securityPreferences.isBiometricEnabled(),
                backupKey = securityPreferences.getBackupKey()
            )
        }
    }

    fun onLockTypeSelected(type: LockType) {
        when (type) {
            LockType.NONE -> {
                securityPreferences.setLockType(LockType.NONE)
                _uiState.update { it.copy(selectedLockType = LockType.NONE) }
            }
            LockType.PIN -> _uiState.update { it.copy(isPinDialogOpen = true) }
            LockType.COMPLEX_PIN -> _uiState.update { it.copy(isComplexPinDialogOpen = true) }
            LockType.PATTERN -> _uiState.update { it.copy(isPatternDialogOpen = true) }
            LockType.BIOMETRIC -> {
                securityPreferences.setLockType(LockType.BIOMETRIC)
                securityPreferences.setBiometricEnabled(true)
                _uiState.update { it.copy(selectedLockType = LockType.BIOMETRIC, isBiometricEnabled = true) }
            }
        }
    }

    fun onBackupKeyChanged(input: String) {
        // صرفاً کاراکترهای انگلیسی و اعداد مجاز هستند و حداکثر ۱۶ کاراکتر
        val filtered = input.filter { it.isLetterOrDigit() && it.code < 128 }.take(16)
        val error = if (filtered.isNotEmpty() && filtered.length < 16) {
            "کلید امنیتی باید دقیقاً ۱۶ کاراکتر باشد"
        } else null

        _uiState.update { it.copy(backupKey = filtered, backupKeyError = error) }

        if (filtered.length == 16) {
            securityPreferences.saveBackupKey(filtered)
        }
    }

    fun savePin(pin: String) {
        if (pin.length < 4 || !pin.all { it.isDigit() }) {
            _uiState.update { it.copy(errorMessage = "پین باید حداقل ۴ رقم عددی باشد") }
            return
        }
        securityPreferences.savePinHash(pin) // در محیط واقعی هش شود
        securityPreferences.setLockType(LockType.PIN)
        _uiState.update {
            it.copy(
                selectedLockType = LockType.PIN,
                isPinDialogOpen = false,
                pinInput = "",
                errorMessage = null
            )
        }
    }

    fun saveComplexPin(password: String) {
        if (password.length < 8) {
            _uiState.update { it.copy(errorMessage = "پین ترکیبی باید حداقل ۸ کاراکتر باشد") }
            return
        }
        securityPreferences.savePinHash(password)
        securityPreferences.setLockType(LockType.COMPLEX_PIN)
        _uiState.update {
            it.copy(
                selectedLockType = LockType.COMPLEX_PIN,
                isComplexPinDialogOpen = false,
                complexPinInput = "",
                errorMessage = null
            )
        }
    }

    fun dismissDialogs() {
        _uiState.update {
            it.copy(
                isPinDialogOpen = false,
                isComplexPinDialogOpen = false,
                isPatternDialogOpen = false,
                errorMessage = null
            )
        }
    }
}
