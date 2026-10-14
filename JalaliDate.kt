package com.example.core.common

import java.util.Calendar
import java.util.TimeZone

/**
 * 100% Offline Jalali (Shamsi / Persian) Calendar Engine & Formatter.
 * Based on astronomical Jalali calendar conversion algorithms.
 */
data class JalaliDate(
    val year: Int,
    val month: Int, // 1 to 12
    val day: Int    // 1 to 31
) : Comparable<JalaliDate> {

    val monthName: String
        get() = PERSIAN_MONTH_NAMES.getOrElse(month - 1) { "" }

    val formattedString: String
        get() = String.format("%04d/%02d/%02d", year, month, day)

    fun formatPersian(): String = formattedString

    val formattedPersianDigits: String
        get() = toPersianDigits(formattedString)

    /**
     * Converts this Jalali date to Unix timestamp (milliseconds at start of day 00:00:00 in UTC/Local).
     */
    fun toTimestamp(): Long {
        val (gYear, gMonth, gDay) = jalaliToGregorian(year, month, day)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, gYear)
            set(Calendar.MONTH, gMonth - 1)
            set(Calendar.DAY_OF_MONTH, gDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Day of week (0 = Saturday/شنبه, 1 = Sunday/یکشنبه, ..., 6 = Friday/جمعه)
     */
    val dayOfWeek: Int
        get() {
            val (gYear, gMonth, gDay) = jalaliToGregorian(year, month, day)
            val cal = Calendar.getInstance().apply {
                set(gYear, gMonth - 1, gDay)
            }
            // In Java Calendar, SUNDAY=1, MONDAY=2, TUESDAY=3, WEDNESDAY=4, THURSDAY=5, FRIDAY=6, SATURDAY=7
            val javaDow = cal.get(Calendar.DAY_OF_WEEK)
            return when (javaDow) {
                Calendar.SATURDAY -> 0
                Calendar.SUNDAY -> 1
                Calendar.MONDAY -> 2
                Calendar.TUESDAY -> 3
                Calendar.WEDNESDAY -> 4
                Calendar.THURSDAY -> 5
                Calendar.FRIDAY -> 6
                else -> 0
            }
        }

    val dayOfWeekName: String
        get() = PERSIAN_DAY_OF_WEEK_NAMES.getOrElse(dayOfWeek) { "" }

    fun addDays(days: Int): JalaliDate {
        val ts = toTimestamp() + (days.toLong() * 86_400_000L)
        return fromTimestamp(ts)
    }

    fun daysUntil(other: JalaliDate): Int {
        val diffMs = other.toTimestamp() - this.toTimestamp()
        return (diffMs / 86_400_000L).toInt()
    }

    override fun compareTo(other: JalaliDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    companion object {
        val PERSIAN_MONTH_NAMES = listOf(
            "فروردین", "اردیبهشت", "خرداد",
            "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر",
            "دی", "بهمن", "اسفند"
        )

        val PERSIAN_DAY_OF_WEEK_NAMES = listOf(
            "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
        )

        val PERSIAN_DAY_OF_WEEK_SHORT = listOf(
            "ش", "ی", "د", "س", "چ", "پ", "ج"
        )

        fun now(): JalaliDate = fromTimestamp(System.currentTimeMillis())

        fun fromTimestamp(millis: Long): JalaliDate {
            val cal = Calendar.getInstance().apply {
                timeInMillis = millis
            }
            val gYear = cal.get(Calendar.YEAR)
            val gMonth = cal.get(Calendar.MONTH) + 1
            val gDay = cal.get(Calendar.DAY_OF_MONTH)
            val (jYear, jMonth, jDay) = gregorianToJalali(gYear, gMonth, gDay)
            return JalaliDate(jYear, jMonth, jDay)
        }

        fun parse(dateStr: String?): JalaliDate? {
            if (dateStr.isNullOrBlank()) return null
            return try {
                val cleaned = toEnglishDigits(dateStr.trim()).replace("-", "/")
                val parts = cleaned.split("/")
                if (parts.size == 3) {
                    val y = parts[0].toInt()
                    val m = parts[1].toInt()
                    val d = parts[2].toInt()
                    if (y in 1300..1500 && m in 1..12 && d in 1..31) {
                        JalaliDate(y, m, d)
                    } else null
                } else null
            } catch (e: Exception) {
                null
            }
        }

        fun isLeapYear(jYear: Int): Boolean {
            val (y1, m1, d1) = jalaliToGregorian(jYear, 1, 1)
            val (y2, m2, d2) = jalaliToGregorian(jYear + 1, 1, 1)
            val cal1 = Calendar.getInstance().apply { 
                set(y1, m1 - 1, d1, 12, 0, 0) 
            }
            val cal2 = Calendar.getInstance().apply { 
                set(y2, m2 - 1, d2, 12, 0, 0) 
            }
            val diffDays = ((cal2.timeInMillis - cal1.timeInMillis) / 86_400_000L).toInt()
            return diffDays == 366
        }

        fun getMonthLength(jYear: Int, jMonth: Int): Int {
            return when {
                jMonth in 1..6 -> 31
                jMonth in 7..11 -> 30
                jMonth == 12 -> if (isLeapYear(jYear)) 30 else 29
                else -> 30
            }
        }

        /**
         * Converts Gregorian date to Jalali date
         */
        fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
            val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

            val gy = gYear - 1600
            val gm = gMonth - 1
            val gd = gDay - 1

            var gDayNo = 365L * gy + ((gy + 3) / 4) - ((gy + 99) / 100) + ((gy + 399) / 400)

            for (i in 0 until gm) {
                gDayNo += gDaysInMonth[i]
            }
            if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
                gDayNo++
            }
            gDayNo += gd

            var jDayNo = gDayNo - 79

            val jNp = jDayNo / 12053
            jDayNo %= 12053

            var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
            jDayNo %= 1461

            if (jDayNo >= 366) {
                jy += ((jDayNo - 1) / 365)
                jDayNo = (jDayNo - 1) % 365
            }

            var jm = 0
            for (i in 0..11) {
                val len = if (i < 6) 31 else if (i < 11) 30 else if (isLeapYear(jy.toInt())) 30 else 29
                if (jDayNo < len) {
                    jm = i + 1
                    break
                }
                jDayNo -= len
            }
            val jd = (jDayNo + 1).toInt()

            return Triple(jy.toInt(), jm, jd)
        }

        /**
         * Converts Jalali date to Gregorian date
         */
        fun jalaliToGregorian(jYear: Int, jMonth: Int, jDay: Int): Triple<Int, Int, Int> {
            val jy = jYear - 979
            val jm = jMonth - 1
            val jd = jDay - 1

            var jDayNo = 365L * jy + (jy / 33) * 8 + ((jy % 33 + 3) / 4)
            for (i in 0 until jm) {
                jDayNo += if (i < 6) 31 else 30
            }
            jDayNo += jd

            var gDayNo = jDayNo + 79

            var gy = 1600 + 400 * (gDayNo / 146097)
            gDayNo %= 146097

            var leap = true
            if (gDayNo >= 36525) {
                gDayNo--
                gy += 100 * (gDayNo / 36524)
                gDayNo %= 36524

                if (gDayNo >= 365) {
                    gDayNo++
                } else {
                    leap = false
                }
            }

            gy += 4 * (gDayNo / 1461)
            gDayNo %= 1461

            if (gDayNo >= 366) {
                leap = false
                gDayNo--
                gy += (gDayNo / 365)
                gDayNo %= 365
            }

            val gDaysInMonth = intArrayOf(
                31,
                if (leap) 29 else 28,
                31, 30, 31, 30, 31, 31, 30, 31, 30, 31
            )

            var gm = 0
            for (i in 0..11) {
                if (gDayNo < gDaysInMonth[i]) {
                    gm = i + 1
                    break
                }
                gDayNo -= gDaysInMonth[i]
            }
            val gd = (gDayNo + 1).toInt()

            return Triple(gy.toInt(), gm, gd)
        }

        fun toPersianDigits(text: String): String {
            val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
            val sb = StringBuilder()
            for (ch in text) {
                if (ch in '0'..'9') {
                    sb.append(persianDigits[ch - '0'])
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }

        fun toEnglishDigits(text: String): String {
            val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
            val arabicDigits = "٠١٢٣٤٥٦٧٨٩"
            val sb = StringBuilder()
            for (ch in text) {
                val pIdx = persianDigits.indexOf(ch)
                val aIdx = arabicDigits.indexOf(ch)
                if (pIdx != -1) {
                    sb.append(pIdx)
                } else if (aIdx != -1) {
                    sb.append(aIdx)
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }

        /**
         * Converts Jalali date string to absolute days from a fixed epoch (1300/01/01).
         */
        fun parseToDays(dateStr: String): Long {
            val date = parse(dateStr) ?: return 0
            val (gYear, gMonth, gDay) = jalaliToGregorian(date.year, date.month, date.day)
            val cal = Calendar.getInstance().apply {
                set(gYear, gMonth - 1, gDay, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis / 86_400_000L
        }

        /**
         * Converts absolute days back to Jalali date string.
         */
        fun parseFromDays(days: Long): String {
            return fromTimestamp(days * 86_400_000L).formattedString
        }

        fun formatCurrency(amount: Long, isToman: Boolean = false): String {
            return CurrencyFormatter.format(amount, isToman)
        }
    }
}
