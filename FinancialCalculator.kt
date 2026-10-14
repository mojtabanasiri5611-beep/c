package com.example.core.financial

import com.example.core.common.JalaliDate
import kotlin.math.roundToLong

/**
 * Financial calculation engine for Iranian Cheque Management:
 * 1. Cheque Maturity Averaging (راس‌گیری چک)
 * 2. Discounting & Present Value (تنزیل و خرید دین)
 */
object FinancialCalculator {

    data class ChequeItemForAveraging(
        val id: Long = System.currentTimeMillis() + (1..1000).random(),
        val amount: Long,
        val dueDate: JalaliDate
    )

    data class MaturityAveragingResult(
        val totalAmount: Long,
        val averageDaysFromBase: Int,
        val averageDueDate: JalaliDate,
        val weightedSum: Long,
        val estimatedInterestAdjustment: Long = 0L // جبران تاخیر بر مبنای نرخ بهره ماهانه
    )

    /**
     * Calculates weighted average maturity date (راس‌گیری چک).
     * Formula: Sum(Amount_i * Days_i) / Sum(Amount_i)
     */
    fun calculateMaturityAveraging(
        baseDate: JalaliDate,
        cheques: List<ChequeItemForAveraging>,
        monthlyInterestRatePercent: Double = 0.0
    ): MaturityAveragingResult? {
        if (cheques.isEmpty()) return null

        val totalAmount = cheques.sumOf { it.amount }
        if (totalAmount <= 0) return null

        var weightedDaysSum = 0.0
        for (item in cheques) {
            val daysFromBase = baseDate.daysUntil(item.dueDate)
            weightedDaysSum += (item.amount.toDouble() * daysFromBase)
        }

        val averageDays = (weightedDaysSum / totalAmount).roundToLong().toInt()
        val averageDueDate = baseDate.addDays(averageDays)

        // Calculate interest adjustment if rate specified (e.g. 2% or 3% per month)
        val dailyRate = (monthlyInterestRatePercent / 100.0) / 30.0
        val interest = (totalAmount * dailyRate * averageDays).roundToLong()

        return MaturityAveragingResult(
            totalAmount = totalAmount,
            averageDaysFromBase = averageDays,
            averageDueDate = averageDueDate,
            weightedSum = weightedDaysSum.roundToLong(),
            estimatedInterestAdjustment = interest
        )
    }

    data class DiscountingResult(
        val nominalAmount: Long,
        val daysToMaturity: Int,
        val annualRatePercent: Double,
        val discountFee: Long,
        val netPayout: Long,
        val effectiveAnnualRatePercent: Double
    )

    /**
     * Calculates Cheque Discounting / Early Cashing (تنزیل چک).
     * Discount Fee = Amount * (AnnualRate / 365) * Days
     * Net Payout = Amount - Discount Fee
     */
    fun calculateDiscounting(
        nominalAmount: Long,
        daysToMaturity: Int,
        annualRatePercent: Double
    ): DiscountingResult {
        if (nominalAmount <= 0 || daysToMaturity <= 0 || annualRatePercent <= 0.0) {
            return DiscountingResult(nominalAmount, daysToMaturity, annualRatePercent, 0, nominalAmount, 0.0)
        }

        val dailyRate = (annualRatePercent / 100.0) / 365.0
        val discountFee = (nominalAmount * dailyRate * daysToMaturity).roundToLong()
        val netPayout = (nominalAmount - discountFee).coerceAtLeast(0L)

        val effectiveRate = if (netPayout > 0) {
            ((discountFee.toDouble() / netPayout.toDouble()) * (365.0 / daysToMaturity)) * 100.0
        } else {
            0.0
        }

        return DiscountingResult(
            nominalAmount = nominalAmount,
            daysToMaturity = daysToMaturity,
            annualRatePercent = annualRatePercent,
            discountFee = discountFee,
            netPayout = netPayout,
            effectiveAnnualRatePercent = effectiveRate
        )
    }
}
