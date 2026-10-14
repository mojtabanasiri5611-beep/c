package com.example.data.repository

import com.example.core.common.AppResult
import com.example.data.local.dao.BankAccountDao
import com.example.domain.model.BankAccount
import com.example.domain.repository.BankAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BankAccountRepositoryImpl(
    private val bankAccountDao: BankAccountDao
) : BankAccountRepository {

    override fun getAllAccounts(): Flow<List<BankAccount>> {
        return bankAccountDao.getAllAccounts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAccountById(id: Long): Flow<BankAccount?> {
        return bankAccountDao.getAccountById(id).map { it?.toDomain() }
    }

    override suspend fun insertAccount(account: BankAccount): AppResult<Long> = try {
        val id = bankAccountDao.insertAccount(account.toEntity())
        AppResult.Success(id)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun updateAccount(account: BankAccount): AppResult<Unit> = try {
        bankAccountDao.updateAccount(account.toEntity())
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun updateBalance(accountId: Long, newBalance: Long): AppResult<Unit> = try {
        bankAccountDao.updateBalance(accountId, newBalance)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun deleteAccount(account: BankAccount): AppResult<Unit> = try {
        bankAccountDao.deleteAccount(account.toEntity())
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }
}
