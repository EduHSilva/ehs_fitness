package com.ehs.fitness.data.repository

import com.ehs.fitness.core.network.ApiException
import com.ehs.fitness.core.network.unwrap
import com.ehs.fitness.core.session.AuthSession
import com.ehs.fitness.core.session.AuthUser
import com.ehs.fitness.core.session.SessionManager
import com.ehs.fitness.data.remote.AuthApi
import com.ehs.fitness.data.remote.LoginDto
import com.ehs.fitness.data.remote.LoginRequest
import com.ehs.fitness.data.remote.RegisterRequest
import com.ehs.fitness.data.remote.UpdateUserRequest
import kotlinx.coroutines.CancellationException

private const val APP_SCOPE = "FITNESS"

class AuthRepository(
    private val api: AuthApi,
    private val sessionManager: SessionManager,
) {
    val session = sessionManager.session

    suspend fun login(email: String, password: String): AuthSession =
        saveSession(
            api.login(LoginRequest(email = email.trim(), password = password, app = APP_SCOPE)).unwrap(),
        )

    /** Mesmo endpoint; o serviço de usuários aceita `tokenGmail` no lugar de e-mail/senha. */
    suspend fun loginWithGoogle(idToken: String): AuthSession =
        saveSession(api.login(LoginRequest(tokenGmail = idToken, app = APP_SCOPE)).unwrap())

    /** O cadastro responde 202: a conta só entra depois da confirmação por e-mail. */
    suspend fun register(email: String, username: String, password: String, role: String) {
        api.register(
            RegisterRequest(
                email = email.trim(),
                username = username.trim(),
                password = password,
                role = role,
            ),
        ).unwrap()
    }

    /**
     * Revalida a sessão salva ao abrir o app. `/users/auth/me` devolve um token renovado
     * (expiração deslizante); o id só é sobrescrito quando vier preenchido.
     */
    suspend fun initAuth() {
        val stored = sessionManager.session.value ?: return
        try {
            val fresh = api.me().unwrap()
            sessionManager.save(
                AuthSession(
                    token = fresh.token?.takeIf { it.isNotBlank() } ?: stored.token,
                    user = toUser(fresh).copy(
                        id = fresh.id.ifBlank { stored.user.id },
                        name = fresh.username.ifBlank { stored.user.name },
                    ),
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Sessão inválida/expirada: o interceptor já pode ter limpado; garante o gate.
            sessionManager.expire()
        }
    }

    suspend fun updateName(name: String): AuthUser? {
        val current = sessionManager.session.value ?: return null
        val trimmed = name.trim()
        if (trimmed.isBlank()) throw ApiException("Informe seu nome.")
        api.updateUser(current.user.id, UpdateUserRequest(trimmed)).unwrap()
        val updated = current.user.copy(name = trimmed)
        sessionManager.updateUser(updated)
        return updated
    }

    fun logout() = sessionManager.expire()

    private fun saveSession(dto: LoginDto): AuthSession {
        val token = dto.token ?: throw ApiException("Login sem token")
        val session = AuthSession(token = token, user = toUser(dto))
        sessionManager.save(session)
        return session
    }

    private fun toUser(dto: LoginDto): AuthUser = AuthUser(
        id = dto.id,
        name = dto.username,
        email = dto.email,
        roles = dto.roles,
    )
}
