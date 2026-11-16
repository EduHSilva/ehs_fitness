package com.ehs.fitness.domain.auth

object AuthValidation {
    private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")

    fun loginError(email: String, password: String): String? = when {
        email.isBlank() || password.isBlank() -> "Preencha seu e-mail e senha para continuar."
        !emailPattern.matches(email.trim()) -> "Informe um e-mail válido."
        else -> null
    }

    fun registerError(name: String, email: String, password: String): String? = when {
        name.isBlank() || email.isBlank() || password.isBlank() ->
            "Preencha seu nome, e-mail e senha para continuar."
        name.trim().length < 2 -> "Informe seu nome completo."
        !emailPattern.matches(email.trim()) -> "Informe um e-mail válido."
        password.length < 6 -> "A senha deve ter ao menos 6 caracteres."
        else -> null
    }
}
