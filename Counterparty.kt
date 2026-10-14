package com.example.domain.model

import com.example.core.common.JalaliDate

/**
 * Domain model representing a Counterparty / Contact (طرف حساب)
 */
data class Counterparty(
    val id: Long = 0L,
    val name: String,
    val phone: String? = null,
    val nationalCode: String? = null,
    val address: String? = null,
    val creditRating: String = "خوب",
    val totalReceivableVolume: Long = 0L,
    val totalPayableVolume: Long = 0L,
    val bouncedCount: Int = 0,
    val visitorName: String? = null,
    val visitorPhone: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedReceivablePersian: String
        get() = JalaliDate.formatCurrency(totalReceivableVolume, isToman = false)

    val formattedPayablePersian: String
        get() = JalaliDate.formatCurrency(totalPayableVolume, isToman = false)
}
