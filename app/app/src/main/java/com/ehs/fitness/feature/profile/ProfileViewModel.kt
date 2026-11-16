package com.ehs.fitness.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val pending: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    val session = authRepository.session

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun updateName(name: String) {
        if (name.isBlank()) {
            _state.update { it.copy(error = "Informe seu nome.", saved = false) }
            return
        }
        if (_state.value.pending) return
        viewModelScope.launch {
            _state.update { it.copy(pending = true, error = null, saved = false) }
            try {
                authRepository.updateName(name)
                _state.update { it.copy(saved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(error = e.userMessage("Não foi possível salvar seu nome.")) }
            } finally {
                _state.update { it.copy(pending = false) }
            }
        }
    }

    fun clearFeedback() {
        _state.update { it.copy(error = null, saved = false) }
    }
}
