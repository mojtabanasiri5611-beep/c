package com.example.domain.model

/**
 * Cheque lifecycle states
 */
enum class ChequeStatus(val titlePersian: String) {
    // وضعیت‌های عمومی و دریافتنی
    PENDING("در انتظار وصول"),
    AT_HAND("نزد صندوق"),
    DEPOSITED_TO_BANK("در جریان وصول (خوابانده به حساب)"),
    CLEARED("وصول شده (پاس شده قطعی)"),
    TRANSFERRED("خرج شده / ظهرنویسی شده"),
    BOUNCED("برگشت خورده"),
    BOUNCED_TRANSFERRED("برگشتی از محل خرج‌شده"),
    RETURNED_TO_DRAWER("عودت داده شده به صاحب چک"),

    // وضعیت‌های پرداختنی
    ISSUED("صادر شده و تحویل طرف‌حساب"),
    PAID("پاس شده از حساب"),
    BOUNCED_PAYABLE("برگشت خورده (کسری موجودی)"),
    CANCELLED("باطل شده در دسته‌چک"),
    VOIDED("باطله"),

    // اسناد امانی
    GUARANTEE("ضمانتی / امانی");

    val faTitle: String
        get() = titlePersian

    val isCleared: Boolean
        get() = this == CLEARED || this == PAID

    val isBounced: Boolean
        get() = this == BOUNCED || this == BOUNCED_PAYABLE

    val isPending: Boolean
        get() = this == PENDING || this == AT_HAND || this == DEPOSITED_TO_BANK || this == ISSUED

    companion object {
        fun fromString(value: String): ChequeStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

/**
 * Direction of the cheque: Receivable (دریافتی از دیگران) or Payable (صادره به دیگران)
 */
enum class ChequeDirection(val titlePersian: String) {
    RECEIVABLE("دریافتی"),
    PAYABLE("پرداختی");

    companion object {
        fun fromString(value: String): ChequeDirection {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: RECEIVABLE
        }
    }
}
