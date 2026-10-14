package com.example.domain.repository

import com.example.core.common.AppResult
import com.example.domain.model.Counterparty
import kotlinx.coroutines.flow.Flow

interface CounterpartyRepository {
    fun getAllCounterparties(): Flow<List<Counterparty>>
    fun getCounterpartyById(id: Long): Flow<Counterparty?>
    fun searchCounterparties(query: String): Flow<List<Counterparty>>

    suspend fun insertCounterparty(counterparty: Counterparty): AppResult<Long>
    suspend fun updateCounterparty(counterparty: Counterparty): AppResult<Unit>
    suspend fun deleteCounterparty(counterparty: Counterparty): AppResult<Unit>
}
