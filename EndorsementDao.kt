package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.EndorsementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EndorsementDao {

    @Query("SELECT * FROM endorsements WHERE cheque_id = :chequeId ORDER BY endorsement_timestamp ASC")
    fun getEndorsementsForCheque(chequeId: Long): Flow<List<EndorsementEntity>>

    @Query("SELECT * FROM endorsements WHERE cheque_id = :chequeId ORDER BY endorsement_timestamp ASC")
    suspend fun getEndorsementsForChequeDirect(chequeId: Long): List<EndorsementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEndorsement(endorsement: EndorsementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEndorsements(endorsements: List<EndorsementEntity>)

    @Delete
    suspend fun deleteEndorsement(endorsement: EndorsementEntity)

    @Query("DELETE FROM endorsements WHERE cheque_id = :chequeId")
    suspend fun deleteEndorsementsByChequeId(chequeId: Long)
}
