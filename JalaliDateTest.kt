package com.example

import com.example.core.common.JalaliDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliDateTest {

    @Test
    fun testKnownJalaliConversions() {
        // Test Norooz 1403 (March 20, 2024)
        val (jYear, jMonth, jDay) = JalaliDate.gregorianToJalali(2024, 3, 20)
        assertEquals(1403, jYear)
        assertEquals(1, jMonth)
        assertEquals(1, jDay)

        // Reverse conversion back to Gregorian
        val (gYear, gMonth, gDay) = JalaliDate.jalaliToGregorian(1403, 1, 1)
        assertEquals(2024, gYear)
        assertEquals(3, gMonth)
        assertEquals(20, gDay)
    }

    @Test
    fun testParseAndFormatting() {
        val parsed = JalaliDate.parse("1403/07/15")
        assertNotNull(parsed)
        assertEquals(1403, parsed?.year)
        assertEquals(7, parsed?.month)
        assertEquals(15, parsed?.day)
        assertEquals("مهر", parsed?.monthName)
        assertEquals("۱۴۰۳/۰۷/۱۵", parsed?.formattedPersianDigits)
    }

    @Test
    fun testLeapYearCalculation() {
        assertTrue(JalaliDate.isLeapYear(1403))
    }

    @Test
    fun testCurrencyFormatting() {
        val formatted = JalaliDate.formatCurrency(15000000L, isToman = false)
        assertTrue(formatted.contains("ریال"))
        assertTrue(formatted.contains("۱۵,۰۰۰,۰۰۰"))
    }
}
