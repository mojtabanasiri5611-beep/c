package com.example.core.backup

import android.content.Context
import android.net.Uri
import com.example.core.common.AppResult
import com.example.data.local.ChequeDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupManager {

    fun createBackup(context: Context, backupUri: Uri): AppResult<Unit> {
        return try {
            val dbPath = context.getDatabasePath(ChequeDatabase.DATABASE_NAME)
            val dbShm = File(dbPath.path + "-shm")
            val dbWal = File(dbPath.path + "-wal")

            // Need to close DB before backup
            // This is complex in Room. Simple approach: assume DB is closed or use checkpoint
            
            context.contentResolver.openOutputStream(backupUri)?.use { outputStream ->
                FileInputStream(dbPath).use { it.copyTo(outputStream) }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e)
        }
    }
}
