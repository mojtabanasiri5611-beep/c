package com.example.domain.usecase

data class ChequeBookInfo(
    val startSerial: Long,
    val totalLeaves: Int,
    val usedSerials: List<Long>,
    val voidedSerials: List<Long>
)

object ChequeBookCalculator {

    /**
     * محاسبه بازه فعال دسته‌چک (مثلاً 15 تا 34)
     */
    fun getActiveSerialRange(info: ChequeBookInfo): Pair<Long, Long>? {
        val endSerial = info.startSerial + info.totalLeaves - 1
        val unavailableSerials = (info.usedSerials + info.voidedSerials).toSet()

        var current = info.startSerial
        while (current <= endSerial && unavailableSerials.contains(current)) {
            current++
        }

        return if (current <= endSerial) Pair(current, endSerial) else null
    }

    /**
     * محاسبه درصد مصرف/ابطال جهت به‌روزرسانی اطلاعات پایه
     */
    fun calculateUsagePercentage(info: ChequeBookInfo): Float {
        if (info.totalLeaves <= 0) return 0f
        val unavailableCount = (info.usedSerials + info.voidedSerials).distinct().size
        return (unavailableCount.toFloat() / info.totalLeaves.toFloat()) * 100f
    }
}
