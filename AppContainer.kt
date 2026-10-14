package com.example.di

import android.content.Context
import com.example.data.local.ChequeDatabase
import com.example.data.repository.BankAccountRepositoryImpl
import com.example.data.repository.ChequeBookRepositoryImpl
import com.example.data.repository.ChequeRepositoryImpl
import com.example.data.repository.CounterpartyRepositoryImpl
import com.example.domain.repository.BankAccountRepository
import com.example.domain.repository.ChequeBookRepository
import com.example.domain.repository.ChequeRepository
import com.example.domain.repository.CounterpartyRepository

interface AppContainer {
    val database: ChequeDatabase
    val chequeRepository: ChequeRepository
    val chequeBookRepository: ChequeBookRepository
    val bankAccountRepository: BankAccountRepository
    val counterpartyRepository: CounterpartyRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: ChequeDatabase by lazy {
        ChequeDatabase.getInstance(context)
    }

    override val chequeRepository: ChequeRepository by lazy {
        ChequeRepositoryImpl(
            chequeDao = database.chequeDao(),
            endorsementDao = database.endorsementDao()
        )
    }

    override val chequeBookRepository: ChequeBookRepository by lazy {
        ChequeBookRepositoryImpl(
            chequeBookDao = database.chequeBookDao()
        )
    }

    override val bankAccountRepository: BankAccountRepository by lazy {
        BankAccountRepositoryImpl(
            bankAccountDao = database.bankAccountDao()
        )
    }

    override val counterpartyRepository: CounterpartyRepository by lazy {
        CounterpartyRepositoryImpl(
            counterpartyDao = database.counterpartyDao()
        )
    }
}
