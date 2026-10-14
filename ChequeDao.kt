package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChequeEntity
import com.example.domain.model.ChequeDirection
import com.example.domain.model.ChequeStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ChequeDao {

    @Query("SELECT * FROM cheques ORDER BY due_date_timestamp ASC")
    fun getAllCheques(): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques WHERE id = :id")
    fun getChequeById(id: Long): Flow<ChequeEntity?>

    @Query("SELECT * FROM cheques WHERE id = :id")
    suspend fun getChequeByIdDirect(id: Long): ChequeEntity?

    @Query("SELECT * FROM cheques WHERE sayad_id = :sayadId LIMIT 1")
    suspend fun getChequeBySayadId(sayadId: String): ChequeEntity?

    @Query("SELECT * FROM cheques WHERE direction = :direction ORDER BY due_date_timestamp ASC")
    fun getChequesByDirection(direction: ChequeDirection): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques WHERE status = :status ORDER BY due_date_timestamp ASC")
    fun getChequesByStatus(status: ChequeStatus): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques WHERE direction = :direction AND status = :status ORDER BY due_date_timestamp ASC")
    fun getChequesByDirectionAndStatus(direction: ChequeDirection, status: ChequeStatus): Flow<List<ChequeEntity>>

    @Query("""
        SELECT * FROM cheques 
        WHERE status = 'PENDING' 
          AND due_date_timestamp >= :startOfDayTimestamp 
          AND due_date_timestamp <= :endHorizonTimestamp 
        ORDER BY due_date_timestamp ASC
    """)
    fun getUpcomingMaturingCheques(startOfDayTimestamp: Long, endHorizonTimestamp: Long): Flow<List<ChequeEntity>>

    @Query("""
        SELECT * FROM cheques 
        WHERE due_date_timestamp >= :startTimestamp AND due_date_timestamp <= :endTimestamp 
        ORDER BY due_date_timestamp ASC
    """)
    fun getChequesBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<List<ChequeEntity>>

    @Query("""
        SELECT * FROM cheques 
        WHERE sayad_id LIKE '%' || :query || '%' 
           OR cheque_number LIKE '%' || :query || '%' 
           OR counterparty_name LIKE '%' || :query || '%' 
           OR recipient_name LIKE '%' || :query || '%' 
           OR bank_name LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
        ORDER BY due_date_timestamp ASC
    """)
    fun searchCheques(query: String): Flow<List<ChequeEntity>>

    // Financial calculations
    @Query("SELECT COALESCE(SUM(amount), 0) FROM cheques WHERE status = :status")
    fun getTotalAmountByStatus(status: ChequeStatus): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM cheques WHERE direction = :direction AND status = :status")
    fun getTotalAmountByDirectionAndStatus(direction: ChequeDirection, status: ChequeStatus): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM cheques WHERE direction = :direction")
    fun getTotalAmountByDirection(direction: ChequeDirection): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheque(cheque: ChequeEntity): Long

    @Update
    suspend fun updateCheque(cheque: ChequeEntity)

    @Query("UPDATE cheques SET status = :status, status_date_timestamp = :statusDate, status_description = :description, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateChequeStatus(id: Long, status: ChequeStatus, statusDate: Long, description: String?, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteCheque(cheque: ChequeEntity)

    @Query("DELETE FROM cheques WHERE id = :id")
    suspend fun deleteChequeById(id: Long)

    /**
     * متد تخصصی خزانه‌داری: ثبت انتقال و خرج کردن چک به شخص ثالث
     */
    @Query("""
        UPDATE cheques 
        SET status = :newStatus,
            transferred_to_name = :transfereeName,
            transferred_to_national_id = :nationalId,
            transfer_date = :transferDate,
            transfer_tracking_code = :trackingCode,
            is_contingent_liability_active = 1,
            updated_at = :updatedAt
        WHERE id = :chequeId
    """)
    suspend fun markChequeAsTransferred(
        chequeId: Long,
        newStatus: ChequeStatus = ChequeStatus.TRANSFERRED,
        transfereeName: String,
        nationalId: String,
        transferDate: String,
        trackingCode: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * گزارش تعهدات احتمالی: چک‌های خرج‌شده که هنوز سررسید آنها فرانرسیده و فعال هستند
     */
    @Query("SELECT * FROM cheques WHERE status = 'TRANSFERRED' AND is_contingent_liability_active = 1 ORDER BY due_date_timestamp ASC")
    fun getActiveContingentLiabilities(): Flow<List<ChequeEntity>>

    /**
     * اعلام پاس شدن چک خرج‌شده در سررسید و ابطال تعهد احتمالی
     */
    @Query("UPDATE cheques SET status = 'CLEARED', is_contingent_liability_active = 0, updated_at = :updatedAt WHERE id = :chequeId")
    suspend fun markTransferredChequeAsCleared(chequeId: Long, updatedAt: Long = System.currentTimeMillis())

    /**
     * ثبت برگشت خوردن چک خرج‌شده (ایجاد بدهکاری مجدد صادرکننده و تعهد به شخص ثالث)
     */
    @Query("UPDATE cheques SET status = 'BOUNCED_TRANSFERRED', is_contingent_liability_active = 0, updated_at = :updatedAt WHERE id = :chequeId")
    suspend fun markTransferredChequeAsBounced(chequeId: Long, updatedAt: Long = System.currentTimeMillis())
}
