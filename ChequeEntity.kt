package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.ChequeDirection
import com.example.domain.model.ChequeStatus

@Entity(
    tableName = "cheques",
    indices = [
        Index(value = ["sayad_id"]),
        Index(value = ["due_date_timestamp"]),
        Index(value = ["status"]),
        Index(value = ["direction"]),
        Index(value = ["counterparty_name"])
    ]
)
data class ChequeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "sayad_id")
    val sayadId: String, // 16 digits Sayad identification number

    @ColumnInfo(name = "cheque_number")
    val chequeNumber: String, // Cheque serial/leaf number

    @ColumnInfo(name = "amount")
    val amount: Long, // Exact amount in Rials/Tomans

    @ColumnInfo(name = "due_date_jalali")
    val dueDateJalali: String, // e.g. "1403/07/15"

    @ColumnInfo(name = "due_date_timestamp")
    val dueDateTimestamp: Long, // Epoch timestamp for date queries and sorting

    @ColumnInfo(name = "issue_date_jalali")
    val issueDateJalali: String,

    @ColumnInfo(name = "issue_date_timestamp")
    val issueDateTimestamp: Long,

    @ColumnInfo(name = "direction")
    val direction: ChequeDirection, // RECEIVABLE (دریافتی) or PAYABLE (پرداختی)

    @ColumnInfo(name = "counterparty_id")
    val counterpartyId: Long? = null,

    @ColumnInfo(name = "counterparty_name")
    val counterpartyName: String, // Name of person/company drawn from or issued to

    @ColumnInfo(name = "recipient_name")
    val recipientName: String, // در وجه

    @ColumnInfo(name = "bank_name")
    val bankName: String, // Bank name (ملی، ملت، صادرات، تجارت، ...)

    @ColumnInfo(name = "branch_name")
    val branchName: String? = null,

    @ColumnInfo(name = "bank_account_id")
    val bankAccountId: Long? = null,

    @ColumnInfo(name = "cheque_book_id")
    val chequeBookId: Long? = null,

    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String? = null, // شماره فاکتور یا قرارداد

    @ColumnInfo(name = "description")
    val description: String? = null, // بابت

    @ColumnInfo(name = "status")
    val status: ChequeStatus = ChequeStatus.PENDING,

    @ColumnInfo(name = "status_date_timestamp")
    val statusDateTimestamp: Long? = null,

    @ColumnInfo(name = "status_description")
    val statusDescription: String? = null,

    @ColumnInfo(name = "has_alarm")
    val hasAlarm: Boolean = true,

    @ColumnInfo(name = "alarm_lead_days")
    val alarmLeadDays: Int = 3,

    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,

    // فیلدهای تخصصی ظهرنویسی الکترونیکی صیاد و خرج‌کردن:
    @ColumnInfo(name = "transferred_to_name")
    val transferredToName: String? = null,

    @ColumnInfo(name = "transferred_to_national_id")
    val transferredToNationalId: String? = null,

    @ColumnInfo(name = "transfer_date")
    val transferDate: String? = null,

    @ColumnInfo(name = "transfer_tracking_code")
    val transferTrackingCode: String? = null,

    @ColumnInfo(name = "is_contingent_liability_active")
    val isContingentLiabilityActive: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
