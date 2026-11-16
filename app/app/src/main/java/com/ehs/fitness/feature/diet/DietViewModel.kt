package com.ehs.fitness.feature.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.model.Meal
import com.ehs.fitness.data.repository.DietRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DietUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val meals: List<Meal> = emptyList(),
)

class DietViewModel(
    private val repository: DietRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DietUiState())
    val state: StateFlow<DietUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _state.update { it.copy(loading = true, error = null) }
            try {
                val meals = repository.meals()
                _state.update { it.copy(loading = false, error = null, meals = meals) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.userMessage("Não foi possível carregar suas refeições."))
                }
            }
        }
    }
}
