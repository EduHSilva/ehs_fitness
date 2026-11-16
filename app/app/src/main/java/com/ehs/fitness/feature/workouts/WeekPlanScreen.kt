package com.ehs.fitness.feature.workouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehs.fitness.core.ui.components.EmptyNote
import com.ehs.fitness.core.ui.components.IntroBanner
import com.ehs.fitness.core.ui.components.LoadingScreen
import com.ehs.fitness.core.ui.components.PageState
import com.ehs.fitness.core.ui.theme.FitnessTheme
import com.ehs.fitness.domain.workouts.WeekPlan
import com.ehs.fitness.ui.appViewModel

@Composable
fun WeekPlanScreen() {
    val vm: WeekPlanViewModel = appViewModel { WeekPlanViewModel(it.workoutRepository) }
    val state by vm.state.collectAsStateWithLifecycle()
    val plannedCount = state.days.count { it.workoutId != null }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            IntroBanner(
                eyebrow = "Sua rotina",
                title = "Agenda semanal",
                description = "Veja como seus treinos estão distribuídos na semana.",
                icon = Icons.Filled.CalendarMonth,
            )
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = { vm.load() },
                    enabled = !state.loading,
                    shape = RoundedCornerShape(9.dp),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Atualizar", modifier = Modifier.size(17.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Atualizar")
                }
            }
        }

        item {
            EmptyNote("Para alterar a agenda, acesse a versão web do EHS Fitness.")
        }

        when {
            state.loading -> item { LoadingScreen("Carregando agenda...") }
            state.error != null -> item {
                PageState(
                    title = "Não foi possível carregar",
                    description = state.error!!,
                    actionLabel = "Tentar novamente",
                    onAction = { vm.load() },
                    isError = true,
                )
            }
            else -> {
                item {
                    Text(
                        "$plannedCount de 7 dias com treino",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(WeekPlan.days, key = { it.first }) { (day, label) ->
                    val entry = state.days.firstOrNull { it.dayOfWeek == day }
                    DayCard(
                        day = day,
                        label = label,
                        workoutName = entry?.name?.takeIf { it.isNotBlank() },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCard(
    day: Int,
    label: String,
    workoutName: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, FitnessTheme.colors.border),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .background(
                        if (workoutName != null) FitnessTheme.colors.primarySoft else MaterialTheme.colorScheme.background,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    day.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (workoutName != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    workoutName ?: "Descanso",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (workoutName != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            "Treino planejado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
