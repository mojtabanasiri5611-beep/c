package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChequeBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChequeBookDao {

    @Query("SELECT * FROM cheque_books ORDER BY is_active DESC, created_at DESC")
    fun getAllChequeBooks(): Flow<List<ChequeBookEntity>>

    @Query("SELECT * FROM cheque_books WHERE is_active = 1 ORDER BY created_at DESC")
    fun getActiveChequeBooks(): Flow<List<ChequeBookEntity>>

    @Query("SELECT * FROM cheque_books WHERE id = :id")
    fun getChequeBookById(id: Long): Flow<ChequeBookEntity?>

    @Query("SELECT * FROM cheque_books WHERE id = :id")
    suspend fun getChequeBookByIdDirect(id: Long): ChequeBookEntity?

    @Query("SELECT * FROM cheque_books WHERE bank_account_id = :bankAccountId")
    fun getChequeBooksByAccount(bankAccountId: Long): Flow<List<ChequeBookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChequeBook(chequeBook: ChequeBookEntity): Long

    @Update
    suspend fun updateChequeBook(chequeBook: ChequeBookEntity)

    @Query("UPDATE cheque_books SET used_leaves = used_leaves + 1 WHERE id = :bookId AND used_leaves < total_leaves")
    suspend fun incrementUsedLeaves(bookId: Long)

    @Delete
    suspend fun deleteChequeBook(chequeBook: ChequeBookEntity)
}
