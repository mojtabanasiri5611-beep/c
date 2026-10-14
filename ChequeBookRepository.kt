package com.example.domain.repository

import com.example.core.common.AppResult
import com.example.domain.model.ChequeBook
import kotlinx.coroutines.flow.Flow

interface ChequeBookRepository {
    fun getAllChequeBooks(): Flow<List<ChequeBook>>
    fun getActiveChequeBooks(): Flow<List<ChequeBook>>
    fun getChequeBookById(id: Long): Flow<ChequeBook?>
    fun getChequeBooksByAccount(bankAccountId: Long): Flow<List<ChequeBook>>

    suspend fun insertChequeBook(chequeBook: ChequeBook): AppResult<Long>
    suspend fun updateChequeBook(chequeBook: ChequeBook): AppResult<Unit>
    suspend fun incrementUsedLeaves(bookId: Long): AppResult<Unit>
    suspend fun deleteChequeBook(chequeBook: ChequeBook): AppResult<Unit>
}
