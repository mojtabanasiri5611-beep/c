package com.example.domain.model

import com.example.core.common.JalaliDate

/**
 * High-level financial analytics and aggregates across cheques
 */
data class FinancialSummary(
    val totalPendingReceivable: Long = 0L,
    val totalPendingPayable: Long = 0L,
    val totalClearedReceivable: Long = 0L,
    val totalClearedPayable: Long = 0L,
    val totalBounced: Long = 0L,
    val totalTransferred: Long = 0L,
    val pendingReceivableCount: Int = 0,
    val pendingPayableCount: Int = 0,
    val bouncedCount: Int = 0,
    val criticalAlertCount: Int = 0
) {
    val netPendingCashFlow: Long
        get() = totalPendingReceivable - totalPendingPayable

    val formattedNetCashFlowPersian: String
        get() = JalaliDate.formatCurrency(netPendingCashFlow, isToman = false)

    val formattedPendingReceivablePersian: String
        get() = JalaliDate.formatCurrency(totalPendingReceivable, isToman = false)

    val formattedPendingPayablePersian: String
        get() = JalaliDate.formatCurrency(totalPendingPayable, isToman = false)

    val formattedBouncedPersian: String
        get() = JalaliDate.formatCurrency(totalBounced, isToman = false)

    val formattedClearedPersian: String
        get() = JalaliDate.formatCurrency(totalClearedReceivable + totalClearedPayable, isToman = false)
}
