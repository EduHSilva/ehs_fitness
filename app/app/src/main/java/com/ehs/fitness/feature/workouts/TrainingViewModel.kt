package com.ehs.fitness.feature.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.repository.TrainingSessionRepository
import com.ehs.fitness.data.repository.WorkoutRepository
import com.ehs.fitness.domain.workouts.TrainingSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrainingUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val session: TrainingSession? = null,
    val remainingSeconds: Int = 0,
)

class TrainingViewModel(
    private val workouts: WorkoutRepository,
    private val sessions: TrainingSessionRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TrainingUiState())
    val state: StateFlow<TrainingUiState> = _state.asStateFlow()

    private var ticker: Job? = null
    private var loadedWorkoutId: Int? = null

    fun load(workoutId: Int) {
        if (loadedWorkoutId == workoutId && _state.value.session != null) return
        loadedWorkoutId = workoutId
        viewModelScope.launch {
            _state.value = TrainingUiState(loading = true)
            val now = System.currentTimeMillis()
            val stored = sessions.load()
            if (stored != null && stored.workoutId == workoutId && !stored.isFinished) {
                val settled = stored.settleRest(now)
                if (settled != stored) sessions.save(settled)
                publish(settled)
                return@launch
            }
            if (stored != null) sessions.clear()
            try {
                val workout = workouts.workouts().firstOrNull { it.id == workoutId }
                    ?: throw IllegalStateException("Treino não encontrado.")
                val session = TrainingSession.start(workout, now)
                    ?: throw IllegalStateException("Este treino não tem exercícios.")
                sessions.save(session)
                publish(session)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = TrainingUiState(
                    loading = false,
                    error = e.userMessage("Não foi possível iniciar o treino."),
                )
            }
        }
    }

    fun retry() {
        val workoutId = loadedWorkoutId ?: return
        loadedWorkoutId = null
        load(workoutId)
    }

    fun completeSeries() {
        val session = _state.value.session ?: return
        if (session.isFinished) return
        val next = session.completeSeries(System.currentTimeMillis())
        sessions.save(next)
        publish(next)
    }

    fun skipRest() {
        val session = _state.value.session ?: return
        val next = session.skipRest(System.currentTimeMillis())
        sessions.save(next)
        publish(next)
    }

    fun restart() {
        val session = _state.value.session ?: return
        val now = System.currentTimeMillis()
        val restarted = TrainingSession(
            workoutId = session.workoutId,
            workoutName = session.workoutName,
            exercises = session.exercises,
            startedAtMillis = now,
            updatedAtMillis = now,
        )
        sessions.save(restarted)
        publish(restarted)
    }

    fun finish() {
        ticker?.cancel()
        sessions.clear()
        loadedWorkoutId = null
        _state.value = TrainingUiState(loading = false)
    }

    private fun publish(session: TrainingSession) {
        ticker?.cancel()
        val remaining = session.restRemainingSeconds(System.currentTimeMillis())
        _state.value = TrainingUiState(
            loading = false,
            session = session,
            remainingSeconds = remaining,
        )
        if (remaining > 0) startTicker()
    }

    private fun startTicker() {
        ticker = viewModelScope.launch {
            while (true) {
                delay(500)
                val now = System.currentTimeMillis()
                val current = _state.value.session ?: break
                if (!current.isResting(now)) {
                    val settled = current.settleRest(now)
                    sessions.save(settled)
                    _state.update { it.copy(session = settled, remainingSeconds = 0) }
                    break
                }
                _state.update { it.copy(remainingSeconds = current.restRemainingSeconds(now)) }
            }
        }
    }
}
