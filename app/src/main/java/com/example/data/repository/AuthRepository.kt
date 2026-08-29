package com.example.data.repository

import com.example.data.local.SessionManager
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    val currentUserId: Flow<String?> = sessionManager.currentUserId
    val currentUserName: Flow<String?> = sessionManager.currentUserName
    val currentUserEmail: Flow<String?> = sessionManager.currentUserEmail

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    suspend fun register(name: String, email: String, password: String): AuthResult {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            return AuthResult.Error("Name cannot be empty.")
        }
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return AuthResult.Error("Please enter a valid email address.")
        }
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters.")
        }

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return AuthResult.Error("An account with this email already exists. Please log in.")
        }

        val passwordHash = SessionManager.hashPassword(password)
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            email = trimmedEmail,
            passwordHash = passwordHash
        )

        userDao.insertUser(user)
        sessionManager.saveSession(user.id, user.name, user.email)
        return AuthResult.Success(user)
    }

    suspend fun login(email: String, password: String): AuthResult {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            return AuthResult.Error("Email and password are required.")
        }

        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return AuthResult.Error("No account found with this email. Please register.")

        if (!SessionManager.verifyPassword(password, user.passwordHash)) {
            return AuthResult.Error("Incorrect password. Please try again.")
        }

        sessionManager.saveSession(user.id, user.name, user.email)
        return AuthResult.Success(user)
    }

    suspend fun getCurrentUser(): UserEntity? {
        val userId = sessionManager.currentUserId.value ?: return null
        return userDao.getUserByIdSync(userId)
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
