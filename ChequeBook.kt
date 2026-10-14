package com.example.domain.model

import com.example.core.common.JalaliDate

/**
 * Domain model representing a Cheque Book (دسته چک)
 */
data class ChequeBook(
    val id: Long = 0L,
    val bankAccountId: Long,
    val bankName: String,
    val accountNumber: String,
    val sayadSeries: String,
    val startSerial: Long,
    val endSerial: Long,
    val totalLeaves: Int,
    val usedLeaves: Int = 0,
    val voidedLeaves: Int = 0,
    val warningThreshold: Int = 5,
    val issueDateJalali: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val remainingLeaves: Int
        get() = (totalLeaves - usedLeaves).coerceAtLeast(0)

    val usagePercentage: Float
        get() = if (totalLeaves > 0) (usedLeaves.toFloat() / totalLeaves.toFloat()) else 0f

    val isLowLeavesWarning: Boolean
        get() = isActive && remainingLeaves <= warningThreshold

    val formattedRemainingLeavesPersian: String
        get() = JalaliDate.toPersianDigits(remainingLeaves.toString())
}
