package com.hestalang.uswire.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.hestalang.uswire.core.model.VpnConfig
import com.hestalang.uswire.domain.repository.ConfigRepository
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidKeystoreConfigRepository(context: Context) : ConfigRepository {
    private val file = File(context.noBackupFilesDir, "vpn-config.enc")
    private val keyAlias = "uswire_config_key"

    override fun save(config: VpnConfig) {
        val json = JSONObject()
            .put("displayName", config.displayName)
            .put("rawConfig", config.rawConfig)
            .put("importedAtMillis", config.importedAtMillis)
            .toString()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val encryptedBytes = cipher.doFinal(json.toByteArray(Charsets.UTF_8))
        val payload = cipher.iv + encryptedBytes
        val encoded = Base64.encodeToString(payload, Base64.NO_WRAP)

        file.writeText(encoded, Charsets.UTF_8)
    }

    override fun load(): VpnConfig? {
        if (!file.exists()) return null

        return runCatching {
            val payload = Base64.decode(file.readText(Charsets.UTF_8), Base64.NO_WRAP)
            require(payload.size > IV_LENGTH) { "Archivo cifrado incompleto." }

            val iv = payload.copyOfRange(0, IV_LENGTH)
            val encryptedBytes = payload.copyOfRange(IV_LENGTH, payload.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(TAG_LENGTH_BITS, iv)
            )

            val clearText = cipher.doFinal(encryptedBytes)
            val json = JSONObject(String(clearText, Charsets.UTF_8))

            VpnConfig(
                displayName = json.getString("displayName"),
                rawConfig = json.getString("rawConfig"),
                importedAtMillis = json.getLong("importedAtMillis")
            )
        }.getOrNull()
    }

    override fun clear() {
        if (file.exists()) file.delete()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(keyAlias, null) as? SecretKey
        if (existingKey != null) return existingKey

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val IV_LENGTH = 12
        const val TAG_LENGTH_BITS = 128
    }
}
