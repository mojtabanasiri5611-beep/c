package com.example.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages database encryption keys using hardware-backed Android KeyStore.
 * Completely offline with Zero-Cloud footprint.
 */
class DatabasePassphraseProvider(private val context: Context) {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null)
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        ensureMasterKey()
    }

    private fun ensureMasterKey() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEY_STORE
            )
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Retrieves or generates the 256-bit database passphrase encrypted by Android KeyStore.
     */
    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        val encryptedPassphraseHex = prefs.getString(PREF_ENCRYPTED_KEY, null)
        val ivHex = prefs.getString(PREF_IV, null)

        if (encryptedPassphraseHex != null && ivHex != null) {
            try {
                return decryptPassphrase(
                    hexToBytes(encryptedPassphraseHex),
                    hexToBytes(ivHex)
                )
            } catch (e: Exception) {
                // If decryption failed, fallback to new generation
            }
        }

        // Generate new random 32-byte (256-bit) passphrase
        val newPassphrase = ByteArray(32)
        java.security.SecureRandom().nextBytes(newPassphrase)

        // Encrypt with Android KeyStore Master Key
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(newPassphrase)

        prefs.edit()
            .putString(PREF_ENCRYPTED_KEY, bytesToHex(encrypted))
            .putString(PREF_IV, bytesToHex(iv))
            .apply()

        return newPassphrase
    }

    private fun decryptPassphrase(encryptedBytes: ByteArray, iv: ByteArray): ByteArray {
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(encryptedBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) +
                    Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "ChequeMasterDatabaseKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val PREFS_NAME = "cheque_vault_prefs"
        private const val PREF_ENCRYPTED_KEY = "vault_db_key_enc"
        private const val PREF_IV = "vault_db_iv"
    }
}
