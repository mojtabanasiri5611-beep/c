package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.converters.Converters
import com.example.data.local.dao.BankAccountDao
import com.example.data.local.dao.ChequeBookDao
import com.example.data.local.dao.ChequeDao
import com.example.data.local.dao.CounterpartyDao
import com.example.data.local.dao.EndorsementDao
import com.example.data.local.entity.BankAccountEntity
import com.example.data.local.entity.ChequeBookEntity
import com.example.data.local.entity.ChequeEntity
import com.example.data.local.entity.CounterpartyEntity
import com.example.data.local.entity.EndorsementEntity

@Database(
    entities = [
        ChequeEntity::class,
        EndorsementEntity::class,
        ChequeBookEntity::class,
        BankAccountEntity::class,
        CounterpartyEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChequeDatabase : RoomDatabase() {

    abstract fun chequeDao(): ChequeDao
    abstract fun endorsementDao(): EndorsementDao
    abstract fun chequeBookDao(): ChequeBookDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun counterpartyDao(): CounterpartyDao

    companion object {
        const val DATABASE_NAME = "cheques.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE cheque_books ADD COLUMN voided_leaves INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE bank_accounts ADD COLUMN branch_number TEXT DEFAULT NULL")
                } catch (_: Exception) {
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE counterparties ADD COLUMN visitor_name TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE counterparties ADD COLUMN visitor_phone TEXT DEFAULT NULL")
                } catch (_: Exception) {
                }
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE cheques ADD COLUMN transferred_to_name TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE cheques ADD COLUMN transferred_to_national_id TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE cheques ADD COLUMN transfer_date TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE cheques ADD COLUMN transfer_tracking_code TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE cheques ADD COLUMN is_contingent_liability_active INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {
                }
            }
        }

        @Volatile
        private var INSTANCE: ChequeDatabase? = null

        fun getInstance(context: Context): ChequeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): ChequeDatabase {
            return Room.databaseBuilder(
                context,
                ChequeDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }
    }
}
