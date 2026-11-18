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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehs.fitness.core.ui.components.LoadingScreen
import com.ehs.fitness.core.ui.components.PageState
import com.ehs.fitness.core.ui.components.ProgressBar
import com.ehs.fitness.core.ui.components.Tag
import com.ehs.fitness.core.ui.theme.FitnessTheme
import com.ehs.fitness.domain.workouts.TrainingExercise
import com.ehs.fitness.domain.workouts.TrainingSession
import com.ehs.fitness.domain.workouts.WorkoutFormat
import com.ehs.fitness.ui.appViewModel

@Composable
fun TrainingScreen(workoutId: Int, onClose: () -> Unit) {
    val vm: TrainingViewModel = appViewModel {
        TrainingViewModel(it.workoutRepository, it.trainingRepository)
    }
    val state by vm.state.collectAsStateWithLifecycle()
    var confirmExit by remember { mutableStateOf(false) }

    LaunchedEffect(workoutId) { vm.load(workoutId) }

    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingScreen("Preparando treino...")
        }
        state.error != null -> Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            PageState(
                title = "Não foi possível iniciar",
                description = state.error!!,
                icon = Icons.Filled.FitnessCenter,
                actionLabel = "Tentar novamente",
                onAction = vm::retry,
                isError = true,
            )
        }
        state.session?.isFinished == true -> FinishedTraining(
            session = state.session!!,
            onRestart = vm::restart,
            onDone = {
                vm.finish()
                onClose()
            },
        )
        state.session != null -> ActiveTraining(
            session = state.session!!,
            remainingSeconds = state.remainingSeconds,
            onCompleteSeries = vm::completeSeries,
            onSkipRest = vm::skipRest,
            onRequestExit = { confirmExit = true },
        )
    }

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("Encerrar treino?") },
            text = { Text("O progresso desta execução será descartado.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmExit = false
                    vm.finish()
                    onClose()
                }) {
                    Text("Encerrar", color = FitnessTheme.colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmExit = false }) {
                    Text("Continuar treinando")
                }
            },
        )
    }
}

@Composable
private fun ActiveTraining(
    session: TrainingSession,
    remainingSeconds: Int,
    onCompleteSeries: () -> Unit,
    onSkipRest: () -> Unit,
    onRequestExit: () -> Unit,
) {
    val exercise = session.currentExercise ?: return
    val nextExercise = session.exercises.getOrNull(session.exerciseIndex + 1)
    val progress = if (session.totalSeries == 0) 0f
    else session.completedSeriesOverall.toFloat() / session.totalSeries
    val lastSeries = session.currentSeries >= exercise.totalSeries

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "EXERCÍCIO ${session.exerciseIndex + 1} DE ${session.exercises.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitnessTheme.colors.subtle,
                    )
                    Text(
                        "${session.completedSeriesOverall} de ${session.totalSeries} séries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ProgressBar(fraction = progress)
            }
        }

        if (remainingSeconds > 0) {
            item {
                RestTimer(remainingSeconds = remainingSeconds, onSkip = onSkipRest)
            }
        }

        item {
            ExerciseCard(
                exercise = exercise,
                currentSeries = session.currentSeries,
                lastSeries = lastSeries,
                onCompleteSeries = onCompleteSeries,
            )
        }

        if (nextExercise != null) {
            item {
                Text(
                    "A seguir: ${nextExercise.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, FitnessTheme.colors.border),
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "TREINO",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitnessTheme.colors.subtle,
                    )
                    Text(session.workoutName, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = FitnessTheme.colors.border)
                    Spacer(Modifier.height(4.dp))
                    session.exercises.forEachIndexed { index, item ->
                        ExerciseRow(
                            order = index + 1,
                            exercise = item,
                            current = index == session.exerciseIndex,
                            done = index < session.exerciseIndex,
                        )
                    }
                }
            }
        }

        item {
            TextButton(
                onClick = onRequestExit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Encerrar treino", color = FitnessTheme.colors.danger)
            }
        }
    }
}

@Composable
private fun RestTimer(remainingSeconds: Int, onSkip: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = FitnessTheme.colors.primarySoft,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
    ) {
        Column(
            Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "DESCANSO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                WorkoutFormat.countdown(remainingSeconds),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Respire e prepare a próxima série.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(onClick = onSkip, shape = RoundedCornerShape(9.dp)) {
                Text("Pular descanso")
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: TrainingExercise,
    currentSeries: Int,
    lastSeries: Boolean,
    onCompleteSeries: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, FitnessTheme.colors.border),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (exercise.bodyPart.isNotBlank()) {
                    Tag(WorkoutFormat.bodyPartLabel(exercise.bodyPart))
                }
                Text(
                    if (exercise.isCardio) "Exercício por tempo"
                    else "Série $currentSeries de ${exercise.totalSeries}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                exercise.name,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                WorkoutFormat.exerciseDetail(exercise),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (exercise.notes.isNotBlank()) {
                Text(
                    exercise.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onCompleteSeries,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(7.dp))
                Text(if (lastSeries) "Concluir exercício" else "Concluir série")
            }
        }
    }
}

@Composable
private fun ExerciseRow(order: Int, exercise: TrainingExercise, current: Boolean, done: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(24.dp)
                .background(
                    when {
                        done -> FitnessTheme.colors.successSoft
                        current -> FitnessTheme.colors.primarySoft
                        else -> MaterialTheme.colorScheme.background
                    },
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (done) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Concluído",
                    tint = FitnessTheme.colors.success,
                    modifier = Modifier.size(14.dp),
                )
            } else {
                Text(
                    order.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (current) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            exercise.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            if (exercise.isCardio) WorkoutFormat.durationLabel(exercise.durationSeconds)
            else "${exercise.totalSeries}×${exercise.repetitions}",
            style = MaterialTheme.typography.labelSmall,
            color = FitnessTheme.colors.subtle,
        )
    }
}

@Composable
private fun FinishedTraining(session: TrainingSession, onRestart: () -> Unit, onDone: () -> Unit) {
    val elapsedMinutes = ((System.currentTimeMillis() - session.startedAtMillis) / 60000).coerceAtLeast(0)

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(64.dp)
                .background(FitnessTheme.colors.successSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = FitnessTheme.colors.success,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Treino concluído",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            session.workoutName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
            Metric(value = session.totalSeries.toString(), label = "séries")
            Metric(value = session.exercises.size.toString(), label = "exercícios")
            Metric(value = "${elapsedMinutes}min", label = "duração")
        }
        Spacer(Modifier.height(26.dp))
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("Finalizar")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(6.dp))
            Text("Repetir treino")
        }
    }
}

@Composable
private fun Metric(value: String, label: String) {
    Column(
        Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
