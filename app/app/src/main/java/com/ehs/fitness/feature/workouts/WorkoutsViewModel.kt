package com.ehs.fitness.feature.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.model.Workout
import com.ehs.fitness.data.repository.WorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val workouts: List<Workout> = emptyList(),
    val expandedId: Int? = null,
)

class WorkoutsViewModel(
    private val repository: WorkoutRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(WorkoutsUiState())
    val state: StateFlow<WorkoutsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _state.update { it.copy(loading = true, error = null) }
            try {
                val workouts = repository.workouts()
                _state.update {
                    it.copy(
                        loading = false,
                        error = null,
                        workouts = workouts,
                        expandedId = it.expandedId?.takeIf { id -> workouts.any { workout -> workout.id == id } },
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.userMessage("Não foi possível carregar seus treinos."))
                }
            }
        }
    }

    fun toggleExpanded(id: Int) {
        _state.update { it.copy(expandedId = if (it.expandedId == id) null else id) }
    }
}
