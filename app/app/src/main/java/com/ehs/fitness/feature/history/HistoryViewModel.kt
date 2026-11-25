package com.ehs.fitness.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehs.fitness.core.network.userMessage
import com.ehs.fitness.data.model.TrackingConfig
import com.ehs.fitness.data.model.TrackingDay
import com.ehs.fitness.data.repository.TrackingRepository
import com.ehs.fitness.domain.tracking.TrackingHistory
import com.ehs.fitness.domain.tracking.TrackingStats
import com.ehs.fitness.domain.tracking.TrackingType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class HistoryUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val monthLoading: Boolean = false,
    val error: String? = null,
    val config: TrackingConfig? = null,
    val month: YearMonth = YearMonth.now(),
    val filter: TrackingType? = null,
    val days: List<TrackingDay> = emptyList(),
) {
    val activeTypes: List<TrackingType> get() = TrackingHistory.activeTypes(config)
    val hasTracking: Boolean get() = activeTypes.isNotEmpty()
    val stats: TrackingStats get() = TrackingHistory.stats(days, config, month)
}

class HistoryViewModel(private val repository: TrackingRepository) : ViewModel() {
    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()
    private var monthJob: Job? = null

    init {
        load()
    }

    fun load(silent: Boolean = false) {
        monthJob?.cancel()
        viewModelScope.launch {
            _state.update { it.copy(loading = !silent, refreshing = silent, error = null) }
            try {
                val config = repository.config()
                val month = _state.value.month
                val days = repository.history(month.atDay(1), month.atEndOfMonth())
                val active = TrackingHistory.activeTypes(config)
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        config = config,
                        days = days,
                        filter = it.filter?.takeIf { type -> type in active },
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        error = e.userMessage("Não foi possível carregar os registros."),
                    )
                }
            }
        }
    }

    fun refresh() = load(silent = true)

    fun changeMonth(delta: Long) {
        val month = TrackingHistory.shiftMonth(_state.value.month, delta)
        _state.update { it.copy(month = month) }
        loadMonth(month)
    }

    fun goToday() {
        val month = YearMonth.now()
        if (month == _state.value.month) return
        _state.update { it.copy(month = month) }
        loadMonth(month)
    }

    fun setFilter(type: TrackingType?) {
        if (type != null && type !in _state.value.activeTypes) return
        _state.update { it.copy(filter = type) }
    }

    private fun loadMonth(month: YearMonth) {
        monthJob?.cancel()
        monthJob = viewModelScope.launch {
            _state.update { it.copy(monthLoading = true, error = null) }
            try {
                val days = repository.history(month.atDay(1), month.atEndOfMonth())
                _state.update {
                    if (it.month == month) it.copy(monthLoading = false, days = days) else it
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    if (it.month == month) {
                        it.copy(
                            monthLoading = false,
                            days = emptyList(),
                            error = e.userMessage("Não foi possível carregar os registros."),
                        )
                    } else {
                        it
                    }
                }
            }
        }
    }
}
