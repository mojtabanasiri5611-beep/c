package com.example.data.repository

import com.example.data.local.entity.BankAccountEntity
import com.example.data.local.entity.ChequeBookEntity
import com.example.data.local.entity.ChequeEntity
import com.example.data.local.entity.CounterpartyEntity
import com.example.data.local.entity.EndorsementEntity
import com.example.domain.model.BankAccount
import com.example.domain.model.Cheque
import com.example.domain.model.ChequeBook
import com.example.domain.model.Counterparty
import com.example.domain.model.Endorsement

fun ChequeEntity.toDomain(endorsements: List<Endorsement> = emptyList()): Cheque = Cheque(
    id = id,
    sayadId = sayadId,
    chequeNumber = chequeNumber,
    amount = amount,
    dueDateJalali = dueDateJalali,
    dueDateTimestamp = dueDateTimestamp,
    issueDateJalali = issueDateJalali,
    issueDateTimestamp = issueDateTimestamp,
    direction = direction,
    counterpartyId = counterpartyId,
    counterpartyName = counterpartyName,
    recipientName = recipientName,
    bankName = bankName,
    branchName = branchName,
    bankAccountId = bankAccountId,
    chequeBookId = chequeBookId,
    invoiceNumber = invoiceNumber,
    description = description,
    status = status,
    statusDateTimestamp = statusDateTimestamp,
    statusDescription = statusDescription,
    hasAlarm = hasAlarm,
    alarmLeadDays = alarmLeadDays,
    imageUri = imageUri,
    transferredToName = transferredToName,
    transferredToNationalId = transferredToNationalId,
    transferDate = transferDate,
    transferTrackingCode = transferTrackingCode,
    isContingentLiabilityActive = isContingentLiabilityActive,
    endorsements = endorsements,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Cheque.toEntity(): ChequeEntity = ChequeEntity(
    id = id,
    sayadId = sayadId,
    chequeNumber = chequeNumber,
    amount = amount,
    dueDateJalali = dueDateJalali,
    dueDateTimestamp = dueDateTimestamp,
    issueDateJalali = issueDateJalali,
    issueDateTimestamp = issueDateTimestamp,
    direction = direction,
    counterpartyId = counterpartyId,
    counterpartyName = counterpartyName,
    recipientName = recipientName,
    bankName = bankName,
    branchName = branchName,
    bankAccountId = bankAccountId,
    chequeBookId = chequeBookId,
    invoiceNumber = invoiceNumber,
    description = description,
    status = status,
    statusDateTimestamp = statusDateTimestamp,
    statusDescription = statusDescription,
    hasAlarm = hasAlarm,
    alarmLeadDays = alarmLeadDays,
    imageUri = imageUri,
    transferredToName = transferredToName,
    transferredToNationalId = transferredToNationalId,
    transferDate = transferDate,
    transferTrackingCode = transferTrackingCode,
    isContingentLiabilityActive = isContingentLiabilityActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun EndorsementEntity.toDomain(): Endorsement = Endorsement(
    id = id,
    chequeId = chequeId,
    transfereeName = transfereeName,
    transfereeNationalId = transfereeNationalId,
    transfereePhone = transfereePhone,
    endorsementDateJalali = endorsementDateJalali,
    endorsementTimestamp = endorsementTimestamp,
    reason = reason,
    signatureImageUri = signatureImageUri,
    notes = notes,
    createdAt = createdAt
)

fun Endorsement.toEntity(): EndorsementEntity = EndorsementEntity(
    id = id,
    chequeId = chequeId,
    transfereeName = transfereeName,
    transfereeNationalId = transfereeNationalId,
    transfereePhone = transfereePhone,
    endorsementDateJalali = endorsementDateJalali,
    endorsementTimestamp = endorsementTimestamp,
    reason = reason,
    signatureImageUri = signatureImageUri,
    notes = notes,
    createdAt = createdAt
)

fun ChequeBookEntity.toDomain(): ChequeBook = ChequeBook(
    id = id,
    bankAccountId = bankAccountId,
    bankName = bankName,
    accountNumber = accountNumber,
    sayadSeries = sayadSeries,
    startSerial = startSerial,
    endSerial = endSerial,
    totalLeaves = totalLeaves,
    usedLeaves = usedLeaves,
    voidedLeaves = voidedLeaves,
    warningThreshold = warningThreshold,
    issueDateJalali = issueDateJalali,
    isActive = isActive,
    createdAt = createdAt
)

fun ChequeBook.toEntity(): ChequeBookEntity = ChequeBookEntity(
    id = id,
    bankAccountId = bankAccountId,
    bankName = bankName,
    accountNumber = accountNumber,
    sayadSeries = sayadSeries,
    startSerial = startSerial,
    endSerial = endSerial,
    totalLeaves = totalLeaves,
    usedLeaves = usedLeaves,
    voidedLeaves = voidedLeaves,
    warningThreshold = warningThreshold,
    issueDateJalali = issueDateJalali,
    isActive = isActive,
    createdAt = createdAt
)

fun BankAccountEntity.toDomain(): BankAccount = BankAccount(
    id = id,
    bankName = bankName,
    accountNumber = accountNumber,
    branchNumber = branchNumber,
    shabaNumber = shabaNumber,
    cardNumber = cardNumber,
    accountHolder = accountHolder,
    currentBalance = currentBalance,
    colorHex = colorHex,
    isDefault = isDefault,
    createdAt = createdAt
)

fun BankAccount.toEntity(): BankAccountEntity = BankAccountEntity(
    id = id,
    bankName = bankName,
    accountNumber = accountNumber,
    branchNumber = branchNumber,
    shabaNumber = shabaNumber,
    cardNumber = cardNumber,
    accountHolder = accountHolder,
    currentBalance = currentBalance,
    colorHex = colorHex,
    isDefault = isDefault,
    createdAt = createdAt
)

fun CounterpartyEntity.toDomain(): Counterparty = Counterparty(
    id = id,
    name = name,
    phone = phone,
    nationalCode = nationalCode,
    address = address,
    creditRating = creditRating,
    totalReceivableVolume = totalReceivableVolume,
    totalPayableVolume = totalPayableVolume,
    bouncedCount = bouncedCount,
    visitorName = visitorName,
    visitorPhone = visitorPhone,
    notes = notes,
    createdAt = createdAt
)

fun Counterparty.toEntity(): CounterpartyEntity = CounterpartyEntity(
    id = id,
    name = name,
    phone = phone,
    nationalCode = nationalCode,
    address = address,
    creditRating = creditRating,
    totalReceivableVolume = totalReceivableVolume,
    totalPayableVolume = totalPayableVolume,
    bouncedCount = bouncedCount,
    visitorName = visitorName,
    visitorPhone = visitorPhone,
    notes = notes,
    createdAt = createdAt
)
