package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "bank_name")
    val bankName: String, // e.g. ملی، ملت، صادرات، تجارت، پاسارگاد...

    @ColumnInfo(name = "account_number")
    val accountNumber: String,

    @ColumnInfo(name = "branch_number")
    val branchNumber: String? = null,

    @ColumnInfo(name = "shaba_number")
    val shabaNumber: String? = null, // IR...

    @ColumnInfo(name = "card_number")
    val cardNumber: String? = null,

    @ColumnInfo(name = "account_holder")
    val accountHolder: String,

    @ColumnInfo(name = "current_balance")
    val currentBalance: Long = 0L,

    @ColumnInfo(name = "color_hex")
    val colorHex: String = "#1E88E5",

    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
