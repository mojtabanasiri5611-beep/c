package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BankAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankAccountDao {

    @Query("SELECT * FROM bank_accounts ORDER BY is_default DESC, bank_name ASC")
    fun getAllAccounts(): Flow<List<BankAccountEntity>>

    @Query("SELECT * FROM bank_accounts WHERE id = :id")
    fun getAccountById(id: Long): Flow<BankAccountEntity?>

    @Query("SELECT * FROM bank_accounts WHERE id = :id")
    suspend fun getAccountByIdDirect(id: Long): BankAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: BankAccountEntity): Long

    @Update
    suspend fun updateAccount(account: BankAccountEntity)

    @Query("UPDATE bank_accounts SET current_balance = :newBalance WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, newBalance: Long)

    @Delete
    suspend fun deleteAccount(account: BankAccountEntity)
}
