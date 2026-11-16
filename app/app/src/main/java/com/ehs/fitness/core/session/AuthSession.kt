package com.ehs.fitness.core.session

import kotlinx.serialization.Serializable

@Serializable
data class AuthUser(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val roles: List<String> = emptyList(),
) {
    /** Rótulo do papel principal, usado no perfil e no menu. */
    val roleLabel: String
        get() = when {
            roles.contains("HEALTH_NUTRITIONIST") -> "Nutricionista"
            roles.contains("HEALTH_PERSONAL") -> "Personal trainer"
            roles.contains("ADMIN") -> "Administrador"
            else -> "Aluno"
        }
}

@Serializable
data class AuthSession(
    val token: String,
    val user: AuthUser,
)
