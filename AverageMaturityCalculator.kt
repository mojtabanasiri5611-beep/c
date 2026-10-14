package com.example.core.calculator

import com.example.core.common.JalaliDate
import com.example.domain.model.Cheque
import kotlin.math.roundToLong

object AverageMaturityCalculator {

    data class MaturityResult(
        val totalAmount: Long,
        val weightedDaysSum: Long,
        val averageDueDateJalali: String,
        val adjustedPresentValue: Long
    )

    fun calculate(cheques: List<Cheque>, baseDateDays: Long, monthlyRate: Double): MaturityResult {
        if (cheques.isEmpty()) return MaturityResult(0, 0, "", 0)

        var totalAmount: Long = 0
        var weightedDaysSum: Long = 0

        for (cheque in cheques) {
            val amount = cheque.amount
            val dueDays = JalaliDate.parseToDays(cheque.dueDateJalali)
            val dayDifference = dueDays - baseDateDays
            totalAmount += amount
            weightedDaysSum += (amount * dayDifference)
        }

        if (totalAmount == 0L) return MaturityResult(0, 0, JalaliDate.parseFromDays(baseDateDays), 0)

        val averageDaysOffset = weightedDaysSum / totalAmount
        val finalTargetDays = baseDateDays + averageDaysOffset
        val averageDueDateStr = JalaliDate.parseFromDays(finalTargetDays)

        val dailyRate = (monthlyRate / 100.0) / 30.0
        val presentValue = if (monthlyRate > 0) {
            cheques.sumOf { ch ->
                val days = JalaliDate.parseToDays(ch.dueDateJalali) - baseDateDays
                (ch.amount / (1.0 + (dailyRate * days))).roundToLong()
            }
        } else {
            totalAmount
        }

        return MaturityResult(
            totalAmount = totalAmount,
            weightedDaysSum = weightedDaysSum,
            averageDueDateJalali = averageDueDateStr,
            adjustedPresentValue = presentValue
        )
    }
}
