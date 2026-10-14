package com.example.domain.model

import com.example.core.common.JalaliDate

/**
 * Domain model representing a Bank Account
 */
data class BankAccount(
    val id: Long = 0L,
    val bankName: String,
    val accountNumber: String,
    val branchNumber: String? = null,
    val shabaNumber: String? = null,
    val cardNumber: String? = null,
    val accountHolder: String,
    val currentBalance: Long = 0L,
    val colorHex: String = "#1E88E5",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedBalancePersian: String
        get() = JalaliDate.formatCurrency(currentBalance, isToman = false)

    val formattedAccountNumberPersian: String
        get() = JalaliDate.toPersianDigits(accountNumber)
}
