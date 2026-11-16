package com.ehs.fitness.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.repository.AuthRepository
import com.ehs.fitness.domain.auth.AuthValidation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val pending: Boolean = false,
    val error: String? = null,
    val registerPending: Boolean = false,
    val registerError: String? = null,
    val registerSuccess: Boolean = false,
    val registeredEmail: String = "",
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        AuthValidation.loginError(email, password)?.let { message ->
            _state.update { it.copy(error = message) }
            return
        }
        if (_state.value.pending) return
        viewModelScope.launch {
            _state.update { it.copy(pending = true, error = null) }
            try {
                authRepository.login(email, password)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.userMessage("Não foi possível entrar. Confira suas credenciais."))
                }
            } finally {
                _state.update { it.copy(pending = false) }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        if (_state.value.pending) return
        viewModelScope.launch {
            _state.update { it.copy(pending = true, error = null) }
            try {
                authRepository.loginWithGoogle(idToken)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(error = e.userMessage("Não foi possível entrar com o Google.")) }
            } finally {
                _state.update { it.copy(pending = false) }
            }
        }
    }

    fun register(name: String, email: String, password: String, role: String) {
        AuthValidation.registerError(name, email, password)?.let { message ->
            _state.update { it.copy(registerError = message) }
            return
        }
        if (_state.value.registerPending) return
        viewModelScope.launch {
            _state.update {
                it.copy(registerPending = true, registerError = null, registerSuccess = false)
            }
            try {
                authRepository.register(email, name, password, role)
                _state.update {
                    it.copy(registerSuccess = true, registeredEmail = email.trim())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(registerError = e.userMessage("Não foi possível criar sua conta."))
                }
            } finally {
                _state.update { it.copy(registerPending = false) }
            }
        }
    }

    fun resetRegisterSuccess() {
        _state.update { it.copy(registerSuccess = false) }
    }

    fun clearErrors() {
        _state.update { it.copy(error = null, registerError = null) }
    }

    fun reportGoogleFailure() {
        _state.update { it.copy(error = "Não foi possível entrar com o Google. Tente novamente.") }
    }
}

class SessionViewModel(private val authRepository: AuthRepository) : ViewModel() {
    val session = authRepository.session

    init {
        viewModelScope.launch { authRepository.initAuth() }
    }

    fun logout() = authRepository.logout()
}
