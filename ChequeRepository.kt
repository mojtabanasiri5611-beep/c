package com.example.domain.repository

import com.example.core.common.AppResult
import com.example.domain.model.Cheque
import com.example.domain.model.ChequeDirection
import com.example.domain.model.ChequeStatus
import com.example.domain.model.Endorsement
import com.example.domain.model.FinancialSummary
import kotlinx.coroutines.flow.Flow

interface ChequeRepository {
    fun getAllCheques(): Flow<List<Cheque>>
    fun getChequeById(id: Long): Flow<Cheque?>
    fun getChequesByDirection(direction: ChequeDirection): Flow<List<Cheque>>
    fun getChequesByStatus(status: ChequeStatus): Flow<List<Cheque>>
    fun getUpcomingMaturingCheques(horizonDays: Int): Flow<List<Cheque>>
    fun getChequesBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<List<Cheque>>
    fun searchCheques(query: String): Flow<List<Cheque>>
    fun getFinancialSummary(): Flow<FinancialSummary>

    suspend fun insertCheque(cheque: Cheque): AppResult<Long>
    suspend fun updateCheque(cheque: Cheque): AppResult<Unit>
    suspend fun updateChequeStatus(id: Long, status: ChequeStatus, description: String?): AppResult<Unit>
    suspend fun deleteCheque(id: Long): AppResult<Unit>

    // Chain of endorsement & transfer
    fun getEndorsementsForCheque(chequeId: Long): Flow<List<Endorsement>>
    suspend fun addEndorsement(chequeId: Long, endorsement: Endorsement): AppResult<Long>
    suspend fun deleteEndorsement(endorsement: Endorsement): AppResult<Unit>

    suspend fun transferCheque(
        chequeId: Long,
        transfereeName: String,
        nationalId: String,
        trackingCode: String,
        transferDate: String
    ): AppResult<Unit>

    fun getActiveContingentLiabilities(): Flow<List<Cheque>>
    suspend fun markTransferredChequeAsCleared(chequeId: Long): AppResult<Unit>
    suspend fun markTransferredChequeAsBounced(chequeId: Long): AppResult<Unit>
}
