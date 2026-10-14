package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "counterparties",
    indices = [
        Index(value = ["name"]),
        Index(value = ["national_code"])
    ]
)
data class CounterpartyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String, // نام شخص حقیقی یا حقوقی

    @ColumnInfo(name = "phone")
    val phone: String? = null,

    @ColumnInfo(name = "national_code")
    val nationalCode: String? = null, // شماره/کد ملی یا شناسه اقتصادی

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "credit_rating")
    val creditRating: String = "خوب", // وضعیت اعتباری (عالی، خوب، متوسط، نامناسب)

    @ColumnInfo(name = "total_receivable_volume")
    val totalReceivableVolume: Long = 0L, // مجموع مبالغ دریافتی از این شخص

    @ColumnInfo(name = "total_payable_volume")
    val totalPayableVolume: Long = 0L, // مجموع مبالغ پرداختی به این شخص

    @ColumnInfo(name = "bounced_count")
    val bouncedCount: Int = 0, // تعداد چک برگشتی

    @ColumnInfo(name = "visitor_name")
    val visitorName: String? = null, // نام ویزیتور

    @ColumnInfo(name = "visitor_phone")
    val visitorPhone: String? = null, // شماره تلفن ویزیتور

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
