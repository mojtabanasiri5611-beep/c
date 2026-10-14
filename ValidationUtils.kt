package com.example.domain.usecase

object ValidationUtils {
    fun validateSheba(sheba: String): Boolean {
        // حذف IR یا فضاهای خالی جهت شمارش دقیق ۲۴ رقم
        val cleaned = sheba.replace(" ", "").uppercase().removePrefix("IR")
        return cleaned.length == 24 && cleaned.all { it.isDigit() }
    }

    fun validateSayad(sayad: String): Boolean {
        val cleaned = sayad.replace(" ", "").filter { it.isDigit() }
        return cleaned.length == 16
    }
}
