package com.ehs.fitness.feature.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.model.WeekDay
import com.ehs.fitness.data.repository.StudentRepository
import com.ehs.fitness.data.repository.WorkoutRepository
import com.ehs.fitness.domain.students.ManagedPlan
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeekPlanUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: String? = null,
    val days: List<WeekDay> = emptyList(),
    val managedBy: String? = null,
)

class WeekPlanViewModel(
    private val repository: WorkoutRepository,
    private val students: StudentRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(WeekPlanUiState())
    val state: StateFlow<WeekPlanUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _state.update { it.copy(loading = true, error = null) }
            else _state.update { it.copy(refreshing = true) }
            val managedBy = personalManager()
            try {
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        error = null,
                        days = repository.week(),
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
                        error = e.userMessage("Não foi possível carregar a agenda."),
                        managedBy = managedBy,
                    )
                }
            }
        }
    }

    /** Vínculo é decoração da tela: falha silenciosa mantém o valor anterior. */
    private suspend fun personalManager(): String? = try {
        ManagedPlan.manager(students.links(), ManagedPlan.PERSONAL)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        _state.value.managedBy
    }
}
