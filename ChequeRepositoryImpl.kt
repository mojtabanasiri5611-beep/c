package com.example.data.repository

import com.example.core.common.AppResult
import com.example.core.common.JalaliDate
import com.example.data.local.dao.ChequeDao
import com.example.data.local.dao.EndorsementDao
import com.example.domain.model.Cheque
import com.example.domain.model.ChequeDirection
import com.example.domain.model.ChequeStatus
import com.example.domain.model.Endorsement
import com.example.domain.model.FinancialSummary
import com.example.domain.repository.ChequeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ChequeRepositoryImpl(
    private val chequeDao: ChequeDao,
    private val endorsementDao: EndorsementDao
) : ChequeRepository {

    override fun getAllCheques(): Flow<List<Cheque>> {
        return chequeDao.getAllCheques().map { list ->
            list.map { it.toDomain(emptyList()) }
        }
    }

    override fun getChequeById(id: Long): Flow<Cheque?> {
        return combine(
            chequeDao.getChequeById(id),
            endorsementDao.getEndorsementsForCheque(id)
        ) { chequeEntity, endorsements ->
            chequeEntity?.toDomain(endorsements.map { it.toDomain() })
        }
    }

    override fun getChequesByDirection(direction: ChequeDirection): Flow<List<Cheque>> {
        return chequeDao.getChequesByDirection(direction).map { list ->
            list.map { it.toDomain(emptyList()) }
        }
    }

    override fun getChequesByStatus(status: ChequeStatus): Flow<List<Cheque>> {
        return chequeDao.getChequesByStatus(status).map { list ->
            list.map { it.toDomain(emptyList()) }
        }
    }

    override fun getUpcomingMaturingCheques(horizonDays: Int): Flow<List<Cheque>> {
        val startOfToday = JalaliDate.now().toTimestamp()
        val horizonEnd = startOfToday + (horizonDays.toLong() * 86_400_000L)
        return chequeDao.getUpcomingMaturingCheques(startOfToday, horizonEnd).map { list ->
            list.map { it.toDomain(emptyList()) }
        }
    }

    override fun getChequesBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<List<Cheque>> {
        return chequeDao.getChequesBetweenDates(startTimestamp, endTimestamp).map { list ->
            list.map { it.toDomain(emptyList()) }
        }
    }

    override fun searchCheques(query: String): Flow<List<Cheque>> {
        return chequeDao.searchCheques(query).map { list ->
            list.map { it.toDomain(emptyList()) }
        }
    }

    override fun getFinancialSummary(): Flow<FinancialSummary> {
        return chequeDao.getAllCheques().map { list ->
            var pendingRec = 0L
            var pendingPay = 0L
            var clearedRec = 0L
            var clearedPay = 0L
            var bounced = 0L
            var transferred = 0L
            var pendingRecCount = 0
            var pendingPayCount = 0
            var bouncedCount = 0
            var alertCount = 0

            val nowTs = JalaliDate.now().toTimestamp()
            val alertHorizonTs = nowTs + (7 * 86_400_000L)

            for (c in list) {
                when (c.status) {
                    ChequeStatus.PENDING, ChequeStatus.AT_HAND, ChequeStatus.DEPOSITED_TO_BANK, ChequeStatus.ISSUED -> {
                        if (c.direction == ChequeDirection.RECEIVABLE) {
                            pendingRec += c.amount
                            pendingRecCount++
                        } else {
                            pendingPay += c.amount
                            pendingPayCount++
                        }
                        if (c.dueDateTimestamp in nowTs..alertHorizonTs) {
                            alertCount++
                        }
                    }
                    ChequeStatus.CLEARED, ChequeStatus.PAID -> {
                        if (c.direction == ChequeDirection.RECEIVABLE) {
                            clearedRec += c.amount
                        } else {
                            clearedPay += c.amount
                        }
                    }
                    ChequeStatus.BOUNCED, ChequeStatus.BOUNCED_PAYABLE, ChequeStatus.BOUNCED_TRANSFERRED -> {
                        bounced += c.amount
                        bouncedCount++
                    }
                    ChequeStatus.TRANSFERRED -> {
                        transferred += c.amount
                    }
                    ChequeStatus.VOIDED, ChequeStatus.CANCELLED, ChequeStatus.RETURNED_TO_DRAWER, ChequeStatus.GUARANTEE -> {
                        // چک‌های باطله و امانی تاثیری در گردش جاری ندارند
                    }
                }
            }

            FinancialSummary(
                totalPendingReceivable = pendingRec,
                totalPendingPayable = pendingPay,
                totalClearedReceivable = clearedRec,
                totalClearedPayable = clearedPay,
                totalBounced = bounced,
                totalTransferred = transferred,
                pendingReceivableCount = pendingRecCount,
                pendingPayableCount = pendingPayCount,
                bouncedCount = bouncedCount,
                criticalAlertCount = alertCount
            )
        }
    }

    override suspend fun insertCheque(cheque: Cheque): AppResult<Long> {
        return try {
            val id = chequeDao.insertCheque(cheque.toEntity())
            if (cheque.endorsements.isNotEmpty()) {
                val endorsementEntities = cheque.endorsements.map { it.copy(chequeId = id).toEntity() }
                endorsementDao.insertEndorsements(endorsementEntities)
            }
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun updateCheque(cheque: Cheque): AppResult<Unit> {
        return try {
            chequeDao.updateCheque(cheque.toEntity())
            endorsementDao.deleteEndorsementsByChequeId(cheque.id)
            if (cheque.endorsements.isNotEmpty()) {
                val newEndorsements = cheque.endorsements.map { it.copy(chequeId = cheque.id).toEntity() }
                endorsementDao.insertEndorsements(newEndorsements)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override suspend fun updateChequeStatus(
        id: Long,
        status: ChequeStatus,
        description: String?
    ): AppResult<Unit> = try {
        chequeDao.updateChequeStatus(
            id = id,
            status = status,
            statusDate = System.currentTimeMillis(),
            description = description
        )
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun deleteCheque(id: Long): AppResult<Unit> {
        return try {
            chequeDao.deleteChequeById(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }

    override fun getEndorsementsForCheque(chequeId: Long): Flow<List<Endorsement>> {
        return endorsementDao.getEndorsementsForCheque(chequeId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addEndorsement(
        chequeId: Long,
        endorsement: Endorsement
    ): AppResult<Long> = try {
        val endorsementId = endorsementDao.insertEndorsement(endorsement.toEntity().copy(chequeId = chequeId))
        chequeDao.updateChequeStatus(
            id = chequeId,
            status = ChequeStatus.TRANSFERRED,
            statusDate = endorsement.endorsementTimestamp,
            description = "انتقال به ${endorsement.transfereeName}"
        )
        AppResult.Success(endorsementId)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun deleteEndorsement(endorsement: Endorsement): AppResult<Unit> = try {
        endorsementDao.deleteEndorsement(endorsement.toEntity())
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun transferCheque(
        chequeId: Long,
        transfereeName: String,
        nationalId: String,
        trackingCode: String,
        transferDate: String
    ): AppResult<Unit> = try {
        chequeDao.markChequeAsTransferred(
            chequeId = chequeId,
            newStatus = ChequeStatus.TRANSFERRED,
            transfereeName = transfereeName,
            nationalId = nationalId,
            transferDate = transferDate,
            trackingCode = trackingCode
        )
        // همچنین ثبت در تاریخچه ظهرنویسی
        endorsementDao.insertEndorsement(
            com.example.data.local.entity.EndorsementEntity(
                chequeId = chequeId,
                transfereeName = transfereeName,
                transfereeNationalId = nationalId,
                endorsementDateJalali = transferDate,
                endorsementTimestamp = JalaliDate.parse(transferDate)?.toTimestamp() ?: System.currentTimeMillis(),
                reason = "واگذاری چک به $transfereeName (رهگیری: $trackingCode)"
            )
        )
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override fun getActiveContingentLiabilities(): Flow<List<Cheque>> {
        return chequeDao.getActiveContingentLiabilities().map { list ->
            list.map { it.toDomain(emptyList()) }
        }
    }

    override suspend fun markTransferredChequeAsCleared(chequeId: Long): AppResult<Unit> = try {
        chequeDao.markTransferredChequeAsCleared(chequeId)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }

    override suspend fun markTransferredChequeAsBounced(chequeId: Long): AppResult<Unit> = try {
        chequeDao.markTransferredChequeAsBounced(chequeId)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e)
    }
}
