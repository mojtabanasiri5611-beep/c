package com.example.data.repository

import com.example.core.common.AppResult
import com.example.data.local.dao.ChequeBookDao
import com.example.domain.model.ChequeBook
import com.example.domain.repository.ChequeBookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChequeBookRepositoryImpl(
    private val chequeBookDao: ChequeBookDao
) : ChequeBookRepository {

    override fun getAllChequeBooks(): Flow<List<ChequeBook>> {
        return chequeBookDao.getAllChequeBooks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getActiveChequeBooks(): Flow<List<ChequeBook>> {
        return chequeBookDao.getActiveChequeBooks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getChequeBookById(id: Long): Flow<ChequeBook?> {
        return chequeBookDao.getChequeBookById(id).map { it?.toDomain() }
    }

    override fun getChequeBooksByAccount(bankAccountId: Long): Flow<List<ChequeBook>> {
        return chequeBookDao.getChequeBooksByAccount(bankAccountId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertChequeBook(chequeBook: ChequeBook): AppResult<Long> = try {
        val id = chequeBookDao.insertChequeBook(chequeBook.toEntity())
        AppResult.Success(id)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun updateChequeBook(chequeBook: ChequeBook): AppResult<Unit> = try {
        chequeBookDao.updateChequeBook(chequeBook.toEntity())
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun incrementUsedLeaves(bookId: Long): AppResult<Unit> = try {
        chequeBookDao.incrementUsedLeaves(bookId)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun deleteChequeBook(chequeBook: ChequeBook): AppResult<Unit> = try {
        chequeBookDao.deleteChequeBook(chequeBook.toEntity())
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }
}
