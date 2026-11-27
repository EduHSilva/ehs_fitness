package com.ehs.fitness.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.model.DailyTracking
import com.ehs.fitness.data.model.Meal
import com.ehs.fitness.data.model.TrackingConfig
import com.ehs.fitness.data.model.Workout
import com.ehs.fitness.data.repository.DietRepository
import com.ehs.fitness.data.repository.TrackingRepository
import com.ehs.fitness.data.repository.WorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: String? = null,
    val workouts: List<Workout> = emptyList(),
    val meals: List<Meal> = emptyList(),
    val tracking: TrackingConfig? = null,
    val trackingToday: DailyTracking = DailyTracking(),
    val trackingSaving: Boolean = false,
    val trackingError: String? = null,
)

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val dietRepository: DietRepository,
    private val trackingRepository: TrackingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _state.update { it.copy(loading = true, error = null) }
            else _state.update { it.copy(refreshing = true) }
            val workouts = attempt { workoutRepository.workouts() }
            val meals = attempt { dietRepository.meals() }
            val config = attempt { trackingRepository.config() }
            val today = attempt { trackingRepository.today() }
            val failure = workouts.exceptionOrNull() ?: meals.exceptionOrNull()
            _state.update {
                it.copy(
                    loading = false,
                    refreshing = false,
                    error = if (workouts.isFailure && meals.isFailure) {
                        failure?.userMessage("Não foi possível carregar seu painel.")
                    } else {
                        null
                    },
                    workouts = workouts.getOrDefault(it.workouts),
                    meals = meals.getOrDefault(it.meals),
                    tracking = config.getOrNull() ?: it.tracking,
                    trackingToday = today.getOrNull() ?: it.trackingToday,
                )
            }
        }
    }

    /** Recarrega só o acompanhamento ao voltar para a tela; treino concluído marca o hábito sozinho. */
    fun refreshTracking() {
        if (_state.value.loading) return
        viewModelScope.launch {
            val config = attempt { trackingRepository.config() }
            val today = attempt { trackingRepository.today() }
            if (config.isFailure && today.isFailure) return@launch
            _state.update {
                it.copy(
                    tracking = config.getOrNull() ?: it.tracking,
                    trackingToday = today.getOrNull() ?: it.trackingToday,
                )
            }
        }
    }

    fun setCreatine(value: Boolean) {
        register(_state.value.trackingToday.copy(creatine = value))
    }

    private fun register(next: DailyTracking) {
        if (_state.value.trackingSaving) return
        viewModelScope.launch {
            _state.update { it.copy(trackingSaving = true, trackingError = null) }
            try {
                val saved = trackingRepository.register(next)
                _state.update { it.copy(trackingSaving = false, trackingToday = saved) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        trackingSaving = false,
                        trackingError = e.userMessage("Não foi possível salvar o acompanhamento."),
                    )
                }
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
