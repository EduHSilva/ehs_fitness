package com.ehs.fitness.feature.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.device.RestVibrator
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.repository.TrackingRepository
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
import retrofit2.HttpException
import java.io.IOException

data class TrainingUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val session: TrainingSession? = null,
    val remainingSeconds: Int = 0,
    val loadSaving: Boolean = false,
    val loadError: String? = null,
    val trackingSaving: Boolean = false,
    val trackingSaved: Boolean = false,
    val trackingError: String? = null,
)

class TrainingViewModel(
    private val workouts: WorkoutRepository,
    private val sessions: TrainingSessionRepository,
    private val tracking: TrackingRepository,
    private val restVibrator: RestVibrator,
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
        if (next.isFinished && !session.isFinished) markWorkoutTrained()
    }

    fun selectExercise(index: Int) {
        val session = _state.value.session ?: return
        val next = session.selectExercise(index, System.currentTimeMillis())
        if (next == session) return
        sessions.save(next)
        publish(next)
    }

    fun skipRest() {
        val session = _state.value.session ?: return
        val next = session.skipRest(System.currentTimeMillis())
        sessions.save(next)
        publish(next)
    }

    /** Ajusta a carga localmente e tenta gravar no plano quando há conexão. */
    fun updateLoad(load: Double) {
        val session = _state.value.session ?: return
        val exercise = session.currentExercise ?: return
        if (_state.value.loadSaving) return
        val safe = load.coerceIn(0.0, 1000.0)
        val updated = session.updateLoad(safe, System.currentTimeMillis())
        sessions.save(updated)
        _state.update { it.copy(loadSaving = true, loadError = null) }
        publish(updated)
        viewModelScope.launch {
            try {
                workouts.updateLoad(session.workoutId, exercise.exerciseId, safe)
                _state.update { it.copy(loadSaving = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(loadSaving = false, loadError = loadSyncMessage(e)) }
            }
        }
    }

    private fun loadSyncMessage(cause: Exception): String = when {
        cause is HttpException && cause.code() == 403 ->
            "Seu profissional gerencia o treino; a carga vale apenas nesta execução."
        cause is IOException -> "Sem conexão; a carga vale apenas nesta execução."
        else -> "Não foi possível atualizar o plano; a carga vale apenas nesta execução."
    }

    /** Marca o hábito "treino" do dia quando a execução termina; falha vira aviso na tela final. */
    private fun markWorkoutTrained() {
        if (_state.value.trackingSaved || _state.value.trackingSaving) return
        _state.update { it.copy(trackingSaving = true, trackingError = null) }
        viewModelScope.launch {
            try {
                val today = tracking.today()
                if (!today.trained) tracking.register(today.copy(trained = true))
                _state.update { it.copy(trackingSaving = false, trackingSaved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        trackingSaving = false,
                        trackingError = e.userMessage(
                            "Treino concluído, mas não foi possível registrar o acompanhamento de hoje.",
                        ),
                    )
                }
            }
        }
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
        _state.update {
            val changedExercise = it.session?.exerciseIndex != session.exerciseIndex
            it.copy(
                loading = false,
                session = session,
                remainingSeconds = remaining,
                loadError = if (changedExercise) null else it.loadError,
            )
        }
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
                    // Vibra só se o fim foi presenciado agora (evita alerta atrasado após o aparelho dormir).
                    val endedAgo = current.restEndsAtMillis?.let { now - it }
                    if (endedAgo != null && endedAgo in 0..REST_ALERT_WINDOW_MILLIS) restVibrator.restFinished()
                    break
                }
                _state.update { it.copy(remainingSeconds = current.restRemainingSeconds(now)) }
            }
        }
    }

    private companion object {
        const val REST_ALERT_WINDOW_MILLIS = 60_000L
    }
}
