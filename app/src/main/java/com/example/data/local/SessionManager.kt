package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("medilens_session_prefs", Context.MODE_PRIVATE)

    private val _currentUserId = MutableStateFlow<String?>(prefs.getString(KEY_USER_ID, null))
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(prefs.getString(KEY_USER_NAME, null))
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(prefs.getString(KEY_USER_EMAIL, null))
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    fun saveSession(userId: String, name: String, email: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()
        _currentUserId.value = userId
        _currentUserName.value = name
        _currentUserEmail.value = email
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _currentUserId.value = null
        _currentUserName.value = null
        _currentUserEmail.value = null
    }

    fun isLoggedIn(): Boolean = _currentUserId.value != null

    companion object {
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_EMAIL = "key_user_email"

        fun hashPassword(password: String): String {
            // Secure SHA-256 password hashing with application salt
            val salt = "MediLens_SecureSalt_2026"
            val input = "$password:$salt"
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }

        fun verifyPassword(password: String, storedHash: String): Boolean {
            return hashPassword(password) == storedHash
        }
    }
}
