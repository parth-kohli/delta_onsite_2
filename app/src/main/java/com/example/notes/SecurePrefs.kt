package com.example.notes

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
object SecurePrefs {
    private const val FILE_NAME = "auth_prefs"
    private const val TOKEN_KEY = "token"
    private lateinit var sharedPrefs: EncryptedSharedPreferences
    fun init(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPrefs = EncryptedSharedPreferences.create(
            FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }
    fun saveToken(token: String) {
        sharedPrefs.edit().putString(TOKEN_KEY, token).apply()
    }
    fun getToken(): String? {
        return sharedPrefs.getString(TOKEN_KEY, null)
    }
    fun clearToken() {
        sharedPrefs.edit().remove(TOKEN_KEY).apply()
    }
}
