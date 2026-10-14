package com.example.data.repository

import com.example.core.common.AppResult
import com.example.data.local.dao.CounterpartyDao
import com.example.domain.model.Counterparty
import com.example.domain.repository.CounterpartyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CounterpartyRepositoryImpl(
    private val counterpartyDao: CounterpartyDao
) : CounterpartyRepository {

    override fun getAllCounterparties(): Flow<List<Counterparty>> {
        return counterpartyDao.getAllCounterparties().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getCounterpartyById(id: Long): Flow<Counterparty?> {
        return counterpartyDao.getCounterpartyById(id).map { it?.toDomain() }
    }

    override fun searchCounterparties(query: String): Flow<List<Counterparty>> {
        return counterpartyDao.searchCounterparties(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertCounterparty(counterparty: Counterparty): AppResult<Long> = try {
        val id = counterpartyDao.insertCounterparty(counterparty.toEntity())
        AppResult.Success(id)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun updateCounterparty(counterparty: Counterparty): AppResult<Unit> = try {
        counterpartyDao.updateCounterparty(counterparty.toEntity())
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun deleteCounterparty(counterparty: Counterparty): AppResult<Unit> = try {
        counterpartyDao.deleteCounterparty(counterparty.toEntity())
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }
}
