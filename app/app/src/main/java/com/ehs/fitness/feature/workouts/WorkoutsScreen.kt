package com.ehs.fitness.feature.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehs.fitness.core.ui.components.EmptyNote
import com.ehs.fitness.core.ui.components.IntroBanner
import com.ehs.fitness.core.ui.components.LoadingScreen
import com.ehs.fitness.core.ui.components.PageState
import com.ehs.fitness.core.ui.theme.FitnessTheme
import com.ehs.fitness.data.model.Workout
import com.ehs.fitness.domain.workouts.WorkoutFormat
import com.ehs.fitness.ui.appViewModel

@Composable
fun WorkoutsScreen() {
    val vm: WorkoutsViewModel = appViewModel { WorkoutsViewModel(it.workoutRepository) }
    val state by vm.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            IntroBanner(
                eyebrow = "Sua biblioteca",
                title = "Seus treinos",
                description = "Revise os exercícios e acompanhe sua rotina.",
                icon = Icons.Filled.FitnessCenter,
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
            EmptyNote("Para criar ou editar treinos, acesse a versão web do EHS Fitness.")
        }

        when {
            state.loading -> item { LoadingScreen("Carregando treinos...") }
            state.error != null -> item {
                PageState(
                    title = "Não foi possível carregar",
                    description = state.error!!,
                    icon = Icons.Filled.FitnessCenter,
                    actionLabel = "Tentar novamente",
                    onAction = { vm.load() },
                    isError = true,
                )
            }
            state.workouts.isEmpty() -> item {
                PageState(
                    title = "Nenhum treino cadastrado",
                    description = "O profissional responsável ainda não cadastrou treinos para você.",
                    icon = Icons.Filled.FitnessCenter,
                )
            }
            else -> items(state.workouts, key = { it.id }) { workout ->
                WorkoutCard(
                    workout = workout,
                    expanded = state.expandedId == workout.id,
                    onToggle = { vm.toggleExpanded(workout.id) },
                )
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, FitnessTheme.colors.border),
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .background(FitnessTheme.colors.primarySoft, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(workout.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        WorkoutFormat.exerciseCountLabel(workout.exercises.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Recolher" else "Expandir",
                    tint = FitnessTheme.colors.subtle,
                    modifier = Modifier.size(20.dp),
                )
            }

            if (expanded) {
                HorizontalDivider(color = FitnessTheme.colors.border)
                Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    workout.exercises.forEachIndexed { index, exercise ->
                        if (index > 0) HorizontalDivider(color = FitnessTheme.colors.border.copy(alpha = 0.6f))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                exercise.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                WorkoutFormat.exerciseDetail(exercise),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
