package com.ehs.fitness.domain.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {
    @Test
    fun `login exige email e senha`() {
        assertNotNull(AuthValidation.loginError("", ""))
        assertNotNull(AuthValidation.loginError("aluno@ehs.com", ""))
    }

    @Test
    fun `login recusa email invalido`() {
        assertEquals(
            "Informe um e-mail válido.",
            AuthValidation.loginError("aluno@", "123456"),
        )
    }

    @Test
    fun `login aceita credenciais validas`() {
        assertNull(AuthValidation.loginError("aluno@ehs.com", "123456"))
    }

    @Test
    fun `cadastro exige nome com pelo menos dois caracteres`() {
        assertNotNull(AuthValidation.registerError("", "aluno@ehs.com", "123456"))
        assertNotNull(AuthValidation.registerError("A", "aluno@ehs.com", "123456"))
    }

    @Test
    fun `cadastro exige senha com seis caracteres`() {
        assertNotNull(AuthValidation.registerError("Ana", "ana@ehs.com", "12345"))
        assertNull(AuthValidation.registerError("Ana", "ana@ehs.com", "123456"))
    }
}
