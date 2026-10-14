package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "endorsements",
    foreignKeys = [
        ForeignKey(
            entity = ChequeEntity::class,
            parentColumns = ["id"],
            childColumns = ["cheque_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cheque_id"])
    ]
)
data class EndorsementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "cheque_id")
    val chequeId: Long,

    @ColumnInfo(name = "transferee_name")
    val transfereeName: String, // نام منتقل‌الیه (تحویل‌گیرنده جدید)

    @ColumnInfo(name = "transferee_national_id")
    val transfereeNationalId: String? = null, // کد ملی / شناسه ملی

    @ColumnInfo(name = "transferee_phone")
    val transfereePhone: String? = null,

    @ColumnInfo(name = "endorsement_date_jalali")
    val endorsementDateJalali: String,

    @ColumnInfo(name = "endorsement_timestamp")
    val endorsementTimestamp: Long,

    @ColumnInfo(name = "reason")
    val reason: String? = null, // بابت تسویه حساب، خرید کالا و...

    @ColumnInfo(name = "signature_image_uri")
    val signatureImageUri: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
