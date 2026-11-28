package com.example.vitrine.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.vitrine.data.model.AuthResponse
import com.google.gson.Gson
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SessionManager(context: Context) {

    private val file = File(context.noBackupFilesDir, "vitrine_session")
    private val gson = Gson()
    private val keyAlias = "VitrineSessionKey"

    data class Session(
        val accessToken: String,
        val refreshToken: String,
        val expiresAt: Long,
        val userId: String,
        val email: String,
        val fullName: String,
        val groupCode: String
    )

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val key = keyStore.getKey(keyAlias, null) as? SecretKey
        if (key != null) return key

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        generator.init(spec)
        return generator.generateKey()
    }

    fun saveSession(response: AuthResponse): Boolean {
        val user = response.user ?: return false
        val access = response.accessToken ?: return false
        val refresh = response.refreshToken ?: return false
        val expires = response.expiresIn ?: return false

        if (access.isBlank() || refresh.isBlank() || expires <= 0)
            return false

        val session = Session(
            access,
            refresh,
            System.currentTimeMillis() + expires * 1000,
            user.id,
            user.email.orEmpty(),
            user.userMetadata?.fullName.orEmpty(),
            user.userMetadata?.groupCode.orEmpty()
        )

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getKey())

            val json = gson.toJson(session)
            val encrypted = cipher.doFinal(json.toByteArray(Charsets.UTF_8))

            file.writeText("${encode(cipher.iv)}:${encode(encrypted)}")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getSession(): Session? {
        if (!file.exists()) return null

        return try {
            val parts = file.readText().split(":", limit = 2)
            if (parts.size != 2) return null

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                getKey(),
                GCMParameterSpec(128, decode(parts[0]))
            )

            val data = cipher.doFinal(decode(parts[1]))
            gson.fromJson(String(data, Charsets.UTF_8), Session::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getAccessToken(): String? {
        val session = getSession() ?: return null

        if (System.currentTimeMillis() >= session.expiresAt)
            return null

        return session.accessToken
    }

    fun getRefreshToken(): String? = getSession()?.refreshToken

    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank()

    fun clearSession() {
        file.delete()
    }

    private fun encode(data: ByteArray): String =
        Base64.encodeToString(data, Base64.NO_WRAP)

    private fun decode(data: String): ByteArray =
        Base64.decode(data, Base64.NO_WRAP)
}
