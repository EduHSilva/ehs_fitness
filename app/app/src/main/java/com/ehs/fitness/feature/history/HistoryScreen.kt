package com.ehs.fitness.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehs.fitness.core.ui.components.ContentCard
import com.ehs.fitness.core.ui.components.ErrorBanner
import com.ehs.fitness.core.ui.components.LoadingScreen
import com.ehs.fitness.core.ui.components.RefreshableLazyColumn
import com.ehs.fitness.core.ui.theme.FitnessTheme
import com.ehs.fitness.data.model.TrackingConfig
import com.ehs.fitness.data.model.TrackingDay
import com.ehs.fitness.domain.tracking.TrackingHistory
import com.ehs.fitness.domain.tracking.TrackingType
import com.ehs.fitness.ui.appViewModel
import java.time.LocalDate
import java.time.YearMonth

private val weekdayLabels = listOf("D", "S", "T", "Q", "Q", "S", "S")

@Composable
fun HistoryScreen() {
    val vm: HistoryViewModel = appViewModel { HistoryViewModel(it.trackingRepository) }
    val state by vm.state.collectAsStateWithLifecycle()
    val records = remember(state.days) { state.days.associateBy { it.date } }

    RefreshableLazyColumn(
        isRefreshing = state.refreshing,
        onRefresh = vm::refresh,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "HISTÓRICO DIÁRIO",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitnessTheme.colors.subtle,
                )
                Text("Registros", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Creatina, treino e cardio do mês.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.loading) {
            item { LoadingScreen("Carregando registros...") }
        } else if (!state.hasTracking) {
            item {
                ContentCard {
                    Text("Nenhum acompanhamento ativo", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Peça ao seu profissional para ativar creatina, treino ou cardio no web.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            if (state.error != null) {
                item { ErrorBanner(state.error!!, onRetry = vm::refresh) }
            }
            item {
                MonthToolbar(
                    month = state.month,
                    filter = state.filter,
                    activeTypes = state.activeTypes,
                    onPrevious = { vm.changeMonth(-1) },
                    onNext = { vm.changeMonth(1) },
                    onToday = vm::goToday,
                    onFilter = vm::setFilter,
                )
            }
            item { StatsGrid(state) }
            item {
                CalendarCard(
                    month = state.month,
                    days = state.days,
                    config = state.config,
                    filter = state.filter,
                    monthLoading = state.monthLoading,
                    records = records,
                )
            }
        }
    }
}

@Composable
private fun MonthToolbar(
    month: YearMonth,
    filter: TrackingType?,
    activeTypes: List<TrackingType>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onFilter: (TrackingType?) -> Unit,
) {
    ContentCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Mês anterior")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(TrackingHistory.monthLabel(month), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onToday) {
                    Text("Hoje", style = MaterialTheme.typography.labelMedium)
                }
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Próximo mês")
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = filter == null,
                onClick = { onFilter(null) },
                label = { Text("Todos") },
            )
            activeTypes.forEach { type ->
                FilterChip(
                    selected = filter == type,
                    onClick = { onFilter(type) },
                    label = { Text(type.label) },
                    leadingIcon = {
                        Icon(iconFor(type), contentDescription = null, modifier = Modifier.size(15.dp))
                    },
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(state: HistoryUiState) {
    val tiles = buildList {
        add(
            StatTileData(
                "dias com registro",
                state.stats.registeredDays.toString(),
                Icons.Filled.EventAvailable,
            ),
        )
        if (TrackingType.CREATINE in state.activeTypes) {
            add(
                StatTileData(
                    "dias de creatina",
                    state.stats.creatineDays.toString(),
                    Icons.Filled.Medication,
                    tintFor(TrackingType.CREATINE),
                ),
            )
        }
        if (TrackingType.WORKOUT in state.activeTypes) {
            add(
                StatTileData(
                    "dias de treino",
                    state.stats.workoutDays.toString(),
                    Icons.Filled.FitnessCenter,
                    tintFor(TrackingType.WORKOUT),
                ),
            )
        }
        if (TrackingType.CARDIO in state.activeTypes) {
            val days = state.stats.cardioDays
            add(
                StatTileData(
                    "cardio no mês",
                    TrackingHistory.formatMinutes(state.stats.cardioMinutes),
                    Icons.Filled.Favorite,
                    tintFor(TrackingType.CARDIO),
                    hint = if (days > 0) {
                        "$days ${if (days == 1) "dia" else "dias"} · média " +
                            "${TrackingHistory.formatMinutes(state.stats.cardioAverage)}/dia"
                    } else {
                        "sem registros no mês"
                    },
                ),
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { tile -> StatTile(tile, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(data: StatTileData, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .border(1.dp, FitnessTheme.colors.border, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                data.icon,
                contentDescription = null,
                tint = data.tint ?: MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp),
            )
            Text(
                data.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            data.value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (data.hint != null) {
            Text(
                data.hint,
                style = MaterialTheme.typography.labelSmall,
                color = FitnessTheme.colors.subtle,
            )
        }
    }
}

@Composable
private fun CalendarCard(
    month: YearMonth,
    days: List<TrackingDay>,
    config: TrackingConfig?,
    filter: TrackingType?,
    monthLoading: Boolean,
    records: Map<LocalDate, TrackingDay>,
) {
    ContentCard {
        Row(Modifier.fillMaxWidth()) {
            weekdayLabels.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = FitnessTheme.colors.subtle,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        TrackingHistory.monthDays(month).chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    val record = date?.let(records::get)
                    val types = filteredTypes(record, config, filter)
                    DayCell(
                        date = date,
                        types = types,
                        cardioMinutes = if (record != null && TrackingType.CARDIO in types) record.cardio else 0.0,
                        isToday = date == LocalDate.now(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        val legendTypes = TrackingHistory.activeTypes(config)
        if (legendTypes.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                legendTypes.forEach { type ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            iconFor(type),
                            contentDescription = null,
                            tint = tintFor(type),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            type.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (monthLoading) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "Carregando...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (days.isEmpty()) {
            Text(
                "Nenhum registro em ${TrackingHistory.monthLabel(month)}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    types: Set<TrackingType>,
    cardioMinutes: Double,
    isToday: Boolean,
    modifier: Modifier = Modifier,
) {
    if (date == null) {
        Spacer(modifier.height(64.dp))
        return
    }
    val border = if (isToday) MaterialTheme.colorScheme.primary else FitnessTheme.colors.border
    Column(
        modifier
            .height(64.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (isToday) FitnessTheme.colors.primarySoft else MaterialTheme.colorScheme.background)
            .border(1.dp, border, RoundedCornerShape(9.dp))
            .padding(horizontal = 5.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (types.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                types.forEach { type ->
                    Box(
                        Modifier
                            .size(19.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(softFor(type)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            iconFor(type),
                            contentDescription = type.label,
                            tint = tintFor(type),
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
            }
        }
        if (cardioMinutes > 0.0) {
            Text(
                TrackingHistory.formatMinutes(cardioMinutes),
                style = MaterialTheme.typography.labelSmall,
                color = FitnessTheme.colors.success,
                maxLines = 1,
            )
        }
    }
}

private fun filteredTypes(
    record: TrackingDay?,
    config: TrackingConfig?,
    filter: TrackingType?,
): Set<TrackingType> {
    val types = TrackingHistory.typesFor(record, config)
    return if (filter == null) types else types.filterTo(mutableSetOf()) { it == filter }
}

private fun iconFor(type: TrackingType): ImageVector = when (type) {
    TrackingType.CREATINE -> Icons.Filled.Medication
    TrackingType.WORKOUT -> Icons.Filled.FitnessCenter
    TrackingType.CARDIO -> Icons.Filled.Favorite
}

@Composable
private fun tintFor(type: TrackingType): Color = when (type) {
    TrackingType.CREATINE -> FitnessTheme.colors.warning
    TrackingType.WORKOUT -> MaterialTheme.colorScheme.primary
    TrackingType.CARDIO -> FitnessTheme.colors.success
}

@Composable
private fun softFor(type: TrackingType): Color = when (type) {
    TrackingType.CREATINE -> FitnessTheme.colors.warningSoft
    TrackingType.WORKOUT -> FitnessTheme.colors.primarySoft
    TrackingType.CARDIO -> FitnessTheme.colors.successSoft
}

private data class StatTileData(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val tint: Color? = null,
    val hint: String? = null,
)
