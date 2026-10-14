package com.example.domain.model

import com.example.core.common.JalaliDate

/**
 * Domain model representing a Cheque
 */
data class Cheque(
    val id: Long = 0L,
    val sayadId: String,
    val chequeNumber: String,
    val amount: Long,
    val dueDateJalali: String,
    val dueDateTimestamp: Long,
    val issueDateJalali: String,
    val issueDateTimestamp: Long,
    val direction: ChequeDirection,
    val counterpartyId: Long? = null,
    val counterpartyName: String,
    val recipientName: String,
    val bankName: String,
    val branchName: String? = null,
    val bankAccountId: Long? = null,
    val chequeBookId: Long? = null,
    val invoiceNumber: String? = null,
    val description: String? = null,
    val status: ChequeStatus = ChequeStatus.PENDING,
    val statusDateTimestamp: Long? = null,
    val statusDescription: String? = null,
    val hasAlarm: Boolean = true,
    val alarmLeadDays: Int = 3,
    val imageUri: String? = null,
    val transferredToName: String? = null,
    val transferredToNationalId: String? = null,
    val transferDate: String? = null,
    val transferTrackingCode: String? = null,
    val isContingentLiabilityActive: Boolean = false,
    val endorsements: List<Endorsement> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val amountRial: Long
        get() = amount
    val formattedAmountPersian: String
        get() = JalaliDate.formatCurrency(amount, isToman = false)

    val formattedDueDatePersian: String
        get() = JalaliDate.toPersianDigits(dueDateJalali)

    val daysUntilDue: Int
        get() {
            val nowTs = JalaliDate.now().toTimestamp()
            val diff = dueDateTimestamp - nowTs
            return (diff / 86_400_000L).toInt()
        }

    val isOverdue: Boolean
        get() = status == ChequeStatus.PENDING && daysUntilDue < 0

    val isDueToday: Boolean
        get() = status == ChequeStatus.PENDING && daysUntilDue == 0

    val isDueSoon: Boolean
        get() = status == ChequeStatus.PENDING && daysUntilDue in 1..alarmLeadDays
}
