package com.hestalang.uswire.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import android.util.Base64
import com.hestalang.uswire.core.model.VpnConfig
import com.hestalang.uswire.domain.repository.ConfigRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the complete profile library as one AES-GCM encrypted file.
 *
 * The key alias and legacy file name intentionally remain unchanged so an
 * in-place app update can decrypt the profile created by older USWire builds.
 */
class AndroidKeystoreConfigRepository(context: Context) : ConfigRepository {
    private val profilesFile = File(context.noBackupFilesDir, PROFILES_FILE_NAME)
    private val legacyFile = File(context.noBackupFilesDir, LEGACY_FILE_NAME)
    private val keyAlias = "uswire_config_key"

    @Synchronized
    override fun save(config: VpnConfig) {
        val store = readStore()
        val existingIndex = store.profiles.indexOfFirst { it.id == config.id }
        if (existingIndex >= 0) {
            store.profiles[existingIndex] = config
        } else {
            store.profiles.add(config)
        }
        store.activeProfileId = config.id
        writeStore(store)
    }

    @Synchronized
    override fun load(): VpnConfig? {
        val store = readStore()
        return store.profiles.firstOrNull { it.id == store.activeProfileId }
            ?: store.profiles.firstOrNull()
    }

    @Synchronized
    override fun loadAll(): List<VpnConfig> = readStore().profiles.toList()

    @Synchronized
    override fun setActive(profileId: String) {
        val store = readStore()
        require(store.profiles.any { it.id == profileId }) {
            "El perfil seleccionado ya no existe."
        }
        store.activeProfileId = profileId
        writeStore(store)
    }

    @Synchronized
    override fun delete(profileId: String) {
        val store = readStore()
        store.profiles.removeAll { it.id == profileId }
        if (store.activeProfileId == profileId ||
            store.profiles.none { it.id == store.activeProfileId }
        ) {
            store.activeProfileId = store.profiles.firstOrNull()?.id
        }
        writeStore(store)
    }

    @Synchronized
    override fun clear() {
        writeStore(ProfileStore(mutableListOf(), null))
    }

    /**
     * Reads the new encrypted library. If this is the first launch after an
     * update, it migrates the old single-profile file without deleting it.
     * The old file is intentionally retained as a recovery copy.
     */
    private fun readStore(): ProfileStore {
        if (profilesFile.exists() || File(profilesFile.path + ".bak").exists()) {
            return decodeStore(readEncryptedJson(profilesFile))
        }

        if (legacyFile.exists()) {
            val legacyJson = readLegacyEncryptedJson()
            val legacyProfile = VpnConfig(
                displayName = legacyJson.getString("displayName"),
                rawConfig = legacyJson.getString("rawConfig"),
                importedAtMillis = legacyJson.getLong("importedAtMillis"),
                id = UUID.randomUUID().toString()
            )
            val migrated = ProfileStore(mutableListOf(legacyProfile), legacyProfile.id)
            // Do not remove or overwrite vpn-config.enc. It remains available
            // until the new encrypted library has been written successfully.
            writeStore(migrated)
            return migrated
        }

        return ProfileStore(mutableListOf(), null)
    }

    private fun decodeStore(json: JSONObject): ProfileStore {
        val items = json.optJSONArray("profiles") ?: JSONArray()
        val profiles = mutableListOf<VpnConfig>()
        for (index in 0 until items.length()) {
            val item = items.getJSONObject(index)
            profiles.add(
                VpnConfig(
                    id = item.getString("id"),
                    displayName = item.getString("displayName"),
                    rawConfig = item.getString("rawConfig"),
                    importedAtMillis = item.getLong("importedAtMillis")
                )
            )
        }

        val requestedActiveId = json.optString("activeProfileId")
            .takeIf { it.isNotBlank() && it != "null" }
        val activeId = requestedActiveId?.takeIf { id ->
            profiles.any { it.id == id }
        } ?: profiles.firstOrNull()?.id

        return ProfileStore(profiles, activeId)
    }

    private fun writeStore(store: ProfileStore) {
        if (store.activeProfileId == null ||
            store.profiles.none { it.id == store.activeProfileId }
        ) {
            store.activeProfileId = store.profiles.firstOrNull()?.id
        }

        val items = JSONArray()
        store.profiles.forEach { profile ->
            items.put(
                JSONObject()
                    .put("id", profile.id)
                    .put("displayName", profile.displayName)
                    .put("rawConfig", profile.rawConfig)
                    .put("importedAtMillis", profile.importedAtMillis)
            )
        }

        val json = JSONObject()
            .put("version", STORAGE_VERSION)
            .put("activeProfileId", store.activeProfileId)
            .put("profiles", items)

        val payload = encrypt(json.toString())
        val atomicFile = AtomicFile(profilesFile)
        var output: FileOutputStream? = null
        try {
            val stream = atomicFile.startWrite()
            output = stream
            stream.write(payload.toByteArray(Charsets.UTF_8))
            atomicFile.finishWrite(stream)
            output = null
        } catch (error: Exception) {
            output?.let { atomicFile.failWrite(it) }
            throw error
        }
    }

    private fun readLegacyEncryptedJson(): JSONObject {
        val encoded = legacyFile.readText(Charsets.UTF_8)
        return decrypt(encoded)
    }

    private fun readEncryptedJson(file: File): JSONObject {
        val encoded = AtomicFile(file).openRead().bufferedReader(Charsets.UTF_8).use {
            it.readText()
        }
        return decrypt(encoded)
    }

    private fun encrypt(clearText: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encryptedBytes = cipher.doFinal(clearText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + encryptedBytes, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): JSONObject {
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
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
        return JSONObject(String(clearText, Charsets.UTF_8))
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

    private data class ProfileStore(
        val profiles: MutableList<VpnConfig>,
        var activeProfileId: String?
    )

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val LEGACY_FILE_NAME = "vpn-config.enc"
        const val PROFILES_FILE_NAME = "vpn-profiles.enc"
        const val STORAGE_VERSION = 1
        const val IV_LENGTH = 12
        const val TAG_LENGTH_BITS = 128
    }
}
