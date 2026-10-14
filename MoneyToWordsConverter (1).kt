package com.mojtaba.chequemanager.core.utils

object MoneyToWordsConverter {
    fun convert(amount: Long, isToman: Boolean = false): String {
        return com.example.core.util.MoneyToWordsConverter.convert(amount, isToman)
    }

    fun convertNumberToWords(number: Long): String {
        return com.example.core.util.MoneyToWordsConverter.convertNumberToWords(number)
    }

    fun convertRialToTomanWords(rialAmountString: String): String {
        return com.example.core.util.MoneyToWordsConverter.convertRialToTomanWords(rialAmountString)
    }
}
