package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cheque_books",
    foreignKeys = [
        ForeignKey(
            entity = BankAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["bank_account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bank_account_id"])
    ]
)
data class ChequeBookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "bank_account_id")
    val bankAccountId: Long,

    @ColumnInfo(name = "bank_name")
    val bankName: String,

    @ColumnInfo(name = "account_number")
    val accountNumber: String,

    @ColumnInfo(name = "sayad_series")
    val sayadSeries: String, // شناسه/سری صیادی دسته چک

    @ColumnInfo(name = "start_serial")
    val startSerial: Long, // شماره اولین برگ

    @ColumnInfo(name = "end_serial")
    val endSerial: Long, // شماره آخرین برگ

    @ColumnInfo(name = "total_leaves")
    val totalLeaves: Int, // 25, 50, 100 ...

    @ColumnInfo(name = "used_leaves")
    val usedLeaves: Int = 0, // تعداد برگ مصرف شده

    @ColumnInfo(name = "voided_leaves", defaultValue = "0")
    val voidedLeaves: Int = 0, // تعداد برگ‌های باطله

    @ColumnInfo(name = "warning_threshold")
    val warningThreshold: Int = 5, // حداقل برگ برای هشدار سفارش دسته چک جدید

    @ColumnInfo(name = "issue_date_jalali")
    val issueDateJalali: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
