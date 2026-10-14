package com.example.domain.model

import com.example.core.common.JalaliDate

/**
 * Domain model representing an Endorsement (خرج کردن چک به شخص ثالث)
 */
data class Endorsement(
    val id: Long = 0L,
    val chequeId: Long,
    val transfereeName: String,
    val transfereeNationalId: String? = null,
    val transfereePhone: String? = null,
    val endorsementDateJalali: String,
    val endorsementTimestamp: Long,
    val reason: String? = null,
    val signatureImageUri: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedDatePersian: String
        get() = JalaliDate.toPersianDigits(endorsementDateJalali)
}
