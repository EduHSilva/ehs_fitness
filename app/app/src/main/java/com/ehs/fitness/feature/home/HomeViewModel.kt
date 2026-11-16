package com.ehs.fitness.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.model.Meal
import com.ehs.fitness.data.model.Workout
import com.ehs.fitness.data.repository.DietRepository
import com.ehs.fitness.data.repository.WorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val workouts: List<Workout> = emptyList(),
    val meals: List<Meal> = emptyList(),
)

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val dietRepository: DietRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _state.update { it.copy(loading = true, error = null) }
            val workouts = attempt { workoutRepository.workouts() }
            val meals = attempt { dietRepository.meals() }
            val failure = workouts.exceptionOrNull() ?: meals.exceptionOrNull()
            _state.update {
                it.copy(
                    loading = false,
                    error = if (workouts.isFailure && meals.isFailure) {
                        failure?.userMessage("Não foi possível carregar seu painel.")
                    } else {
                        null
                    },
                    workouts = workouts.getOrDefault(it.workouts),
                    meals = meals.getOrDefault(it.meals),
                )
            }
        }
    }

    private suspend fun <T> attempt(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
