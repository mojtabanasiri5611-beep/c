package com.example.data.local.converters

import androidx.room.TypeConverter
import com.example.domain.model.ChequeDirection
import com.example.domain.model.ChequeStatus

class Converters {

    @TypeConverter
    fun fromChequeStatus(status: ChequeStatus?): String {
        return status?.name ?: ChequeStatus.PENDING.name
    }

    @TypeConverter
    fun toChequeStatus(value: String?): ChequeStatus {
        return value?.let { ChequeStatus.fromString(it) } ?: ChequeStatus.PENDING
    }

    @TypeConverter
    fun fromChequeDirection(direction: ChequeDirection?): String {
        return direction?.name ?: ChequeDirection.RECEIVABLE.name
    }

    @TypeConverter
    fun toChequeDirection(value: String?): ChequeDirection {
        return value?.let { ChequeDirection.fromString(it) } ?: ChequeDirection.RECEIVABLE
    }
}
