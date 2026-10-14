package com.example.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

enum class LockType {
    NONE,
    PIN,
    COMPLEX_PIN,
    PATTERN,
    BIOMETRIC
}

class SecurityPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_app_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setLockType(type: LockType) {
        sharedPreferences.edit().putString(KEY_LOCK_TYPE, type.name).apply()
    }

    fun getLockType(): LockType {
        val name = sharedPreferences.getString(KEY_LOCK_TYPE, LockType.NONE.name)
        return try { LockType.valueOf(name!!) } catch (e: Exception) { LockType.NONE }
    }

    fun savePinHash(pinHash: String) {
        sharedPreferences.edit().putString(KEY_PIN_HASH, pinHash).apply()
    }

    fun getPinHash(): String? = sharedPreferences.getString(KEY_PIN_HASH, null)

    fun saveBackupKey(key: String) {
        require(key.length == 16) { "Backup key must be exactly 16 characters" }
        sharedPreferences.edit().putString(KEY_BACKUP_ENCRYPTION_KEY, key).apply()
    }

    fun getBackupKey(): String {
        return sharedPreferences.getString(KEY_BACKUP_ENCRYPTION_KEY, "") ?: ""
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    companion object {
        private const val KEY_LOCK_TYPE = "key_lock_type"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_BACKUP_ENCRYPTION_KEY = "key_backup_encryption_key"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
    }
}
