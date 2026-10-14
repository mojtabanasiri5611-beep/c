package com.example.domain.repository

import com.example.core.common.AppResult
import com.example.domain.model.BankAccount
import kotlinx.coroutines.flow.Flow

interface BankAccountRepository {
    fun getAllAccounts(): Flow<List<BankAccount>>
    fun getAccountById(id: Long): Flow<BankAccount?>

    suspend fun insertAccount(account: BankAccount): AppResult<Long>
    suspend fun updateAccount(account: BankAccount): AppResult<Unit>
    suspend fun updateBalance(accountId: Long, newBalance: Long): AppResult<Unit>
    suspend fun deleteAccount(account: BankAccount): AppResult<Unit>
}
