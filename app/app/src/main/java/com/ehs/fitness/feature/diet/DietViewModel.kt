package com.ehs.fitness.feature.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.model.Meal
import com.ehs.fitness.data.repository.DietRepository
import com.ehs.fitness.data.repository.StudentRepository
import com.ehs.fitness.domain.students.ManagedPlan
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DietUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: String? = null,
    val meals: List<Meal> = emptyList(),
    val managedBy: String? = null,
)

class DietViewModel(
    private val repository: DietRepository,
    private val students: StudentRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DietUiState())
    val state: StateFlow<DietUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _state.update { it.copy(loading = true, error = null) }
            else _state.update { it.copy(refreshing = true) }
            val managedBy = nutritionistManager()
            try {
                val meals = repository.meals()
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        error = null,
                        meals = meals,
                        managedBy = managedBy,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        error = e.userMessage("Não foi possível carregar suas refeições."),
                        managedBy = managedBy,
                    )
                }
            }
        }
    }

    /** Vínculo é decoração da tela: falha silenciosa mantém o valor anterior. */
    private suspend fun nutritionistManager(): String? = try {
        ManagedPlan.manager(students.links(), ManagedPlan.NUTRITIONIST)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        _state.value.managedBy
    }
}
