package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CounterpartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterpartyDao {

    @Query("SELECT * FROM counterparties ORDER BY name ASC")
    fun getAllCounterparties(): Flow<List<CounterpartyEntity>>

    @Query("SELECT * FROM counterparties WHERE id = :id")
    fun getCounterpartyById(id: Long): Flow<CounterpartyEntity?>

    @Query("SELECT * FROM counterparties WHERE id = :id")
    suspend fun getCounterpartyByIdDirect(id: Long): CounterpartyEntity?

    @Query("""
        SELECT * FROM counterparties 
        WHERE name LIKE '%' || :query || '%' 
           OR national_code LIKE '%' || :query || '%' 
           OR phone LIKE '%' || :query || '%' 
        ORDER BY name ASC
    """)
    fun searchCounterparties(query: String): Flow<List<CounterpartyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounterparty(counterparty: CounterpartyEntity): Long

    @Update
    suspend fun updateCounterparty(counterparty: CounterpartyEntity)

    @Query("""
        UPDATE counterparties 
        SET total_receivable_volume = total_receivable_volume + :receivableDelta,
            total_payable_volume = total_payable_volume + :payableDelta
        WHERE id = :id
    """)
    suspend fun updateVolumes(id: Long, receivableDelta: Long, payableDelta: Long)

    @Query("UPDATE counterparties SET bounced_count = bounced_count + 1 WHERE id = :id")
    suspend fun incrementBouncedCount(id: Long)

    @Delete
    suspend fun deleteCounterparty(counterparty: CounterpartyEntity)
}
