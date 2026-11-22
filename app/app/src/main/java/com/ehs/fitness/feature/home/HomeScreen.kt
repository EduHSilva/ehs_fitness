package com.ehs.fitness.feature.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehs.fitness.core.session.AuthUser
import com.ehs.fitness.core.ui.components.ContentCard
import com.ehs.fitness.core.ui.components.ErrorBanner
import com.ehs.fitness.core.ui.components.LoadingScreen
import com.ehs.fitness.core.ui.components.ProgressBar
import com.ehs.fitness.core.ui.components.RefreshableLazyColumn
import com.ehs.fitness.core.ui.components.ScreenDivider
import com.ehs.fitness.core.ui.components.Tag
import com.ehs.fitness.core.ui.theme.FitnessTheme
import com.ehs.fitness.data.model.DailyTracking
import com.ehs.fitness.data.model.TrackingConfig
import com.ehs.fitness.data.model.Workout
import com.ehs.fitness.domain.workouts.WeekPlan
import com.ehs.fitness.domain.workouts.WorkoutFormat
import com.ehs.fitness.ui.appViewModel
import java.time.LocalDate
import java.util.Locale

@Composable
fun HomeScreen(
    user: AuthUser,
    onOpenWorkouts: () -> Unit,
    onOpenDiet: () -> Unit,
    onOpenWeek: () -> Unit,
) {
    val vm: HomeViewModel = appViewModel {
        HomeViewModel(it.workoutRepository, it.dietRepository, it.trackingRepository)
    }
    val state by vm.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refreshTracking() }

    val today = LocalDate.now().dayOfWeek.value
    val firstName = user.name.trim().substringBefore(' ').ifBlank { "atleta" }
    val todayWorkout = WeekPlan.todayWorkout(state.workouts, today)
    val next = WeekPlan.nextWorkout(state.workouts, today)
    val plannedDays = state.workouts.count { it.dayOfWeek in 1..7 }

    RefreshableLazyColumn(
        isRefreshing = state.refreshing,
        onRefresh = { vm.load(silent = true) },
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "SEU PAINEL DE BEM-ESTAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitnessTheme.colors.subtle,
                )
                Text(
                    "Olá, $firstName 👋",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    "Pequenos passos também levam longe.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.loading) {
            item { LoadingScreen("Carregando seu painel...") }
        } else {
            if (state.error != null) {
                item { ErrorBanner(state.error!!, onRetry = { vm.load() }) }
            }

            item {
                FocusCard(
                    todayWorkout = todayWorkout?.name,
                    next = next?.let { (day, workout) -> "${WeekPlan.dayLabel(day)} · ${workout.name}" },
                    onOpenWorkouts = onOpenWorkouts,
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        value = state.workouts.size.toString(),
                        label = "Treinos",
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        value = state.meals.size.toString(),
                        label = "Refeições",
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        value = "$plannedDays/7",
                        label = "Dias ativos",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            val tracking = state.tracking
            if (tracking != null && tracking.enabled) {
                item {
                    DailyTrackingCard(
                        config = tracking,
                        today = state.trackingToday,
                        saving = state.trackingSaving,
                        error = state.trackingError,
                        onCreatine = vm::setCreatine,
                        onTrained = vm::setTrained,
                        onCardio = vm::setCardio,
                    )
                }
            }

            item {
                ContentCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                "SUA SEMANA",
                                style = MaterialTheme.typography.labelSmall,
                                color = FitnessTheme.colors.subtle,
                            )
                            Text("Constância é tudo", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            "$plannedDays ${if (plannedDays == 1) "dia" else "dias"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        WeekPlan.days.forEach { (day, label) ->
                            val planned = state.workouts.any { it.dayOfWeek == day }
                            val isToday = day == today
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    Modifier
                                        .size(32.dp)
                                        .background(
                                            when {
                                                planned -> MaterialTheme.colorScheme.primary
                                                isToday -> FitnessTheme.colors.primarySoft
                                                else -> MaterialTheme.colorScheme.background
                                            },
                                            CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        label.take(1),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when {
                                            planned -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                                if (isToday) {
                                    Text(
                                        "hoje",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    ProgressBar(fraction = plannedDays / 7f)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when {
                            plannedDays == 0 -> "Defina seus treinos na agenda para acompanhar a semana."
                            plannedDays == 7 -> "Semana completa! Só falta seguir o plano."
                            else -> "Você está indo muito bem. Continue assim!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                val upcoming: Pair<Int, Workout>? = todayWorkout?.let { today to it } ?: next
                if (upcoming != null) {
                    ContentCard {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (upcoming.first == today) "TREINO DE HOJE" else "PRÓXIMO TREINO",
                                style = MaterialTheme.typography.labelSmall,
                                color = FitnessTheme.colors.secondary,
                            )
                            Tag(WeekPlan.dayLabel(upcoming.first))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(upcoming.second.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Icons.Filled.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                WorkoutFormat.exerciseCountLabel(upcoming.second.exercises.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = onOpenWorkouts,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(9.dp),
                        ) {
                            Text("Ver treinos")
                        }
                    }
                } else {
                    ContentCard {
                        Text("Nenhum treino planejado", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Seu profissional ainda não distribuiu treinos na agenda semanal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = onOpenWeek,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(9.dp),
                        ) {
                            Text("Abrir agenda")
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAction(
                        icon = Icons.Filled.Restaurant,
                        label = "Refeições",
                        onClick = onOpenDiet,
                        modifier = Modifier.weight(1f),
                    )
                    QuickAction(
                        icon = Icons.Filled.CalendarMonth,
                        label = "Agenda",
                        onClick = onOpenWeek,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (state.meals.isNotEmpty()) {
                item {
                    ContentCard {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Refeições de hoje", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = onOpenDiet) {
                                Text("Ver todas", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        state.meals.sortedBy { it.hour }.take(3).forEach { meal ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    Modifier
                                        .size(34.dp)
                                        .background(FitnessTheme.colors.secondarySoft, RoundedCornerShape(9.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Schedule,
                                        contentDescription = null,
                                        tint = FitnessTheme.colors.secondary,
                                        modifier = Modifier.size(17.dp),
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(meal.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${meal.hour.ifBlank { "sem horário" }} · ${meal.foods.size} ${if (meal.foods.size == 1) "item" else "itens"}",
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
    }
}

@Composable
private fun FocusCard(todayWorkout: String?, next: String?, onOpenWorkouts: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF4F46E5), Color(0xFF6D28D9))),
                RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 22.dp, vertical = 24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "SEU FOCO DE HOJE",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFE0E7FF),
            )
            Text(
                when {
                    todayWorkout != null -> todayWorkout
                    next != null -> next
                    else -> "Sua evolução começa agora."
                },
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (todayWorkout != null) "O treino de hoje já está definido. Vamos?"
                else if (next != null) "Seu próximo treino da semana está pronto."
                else "Cadastre um treino e comece a acompanhar sua rotina.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFEDE9FE),
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onOpenWorkouts,
                shape = RoundedCornerShape(9.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF4338CA),
                ),
            ) {
                Text("Abrir treinos", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun DailyTrackingCard(
    config: TrackingConfig,
    today: DailyTracking,
    saving: Boolean,
    error: String?,
    onCreatine: (Boolean) -> Unit,
    onTrained: (Boolean) -> Unit,
    onCardio: (Double) -> Unit,
) {
    ContentCard {
        Text(
            "ACOMPANHAMENTO DE HOJE",
            style = MaterialTheme.typography.labelSmall,
            color = FitnessTheme.colors.subtle,
        )
        Text(trackingTitle(config), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Solicitado pelo seu profissional no web.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (config.showCreatine) {
            Spacer(Modifier.height(12.dp))
            TrackingSwitchRow(
                title = "Creatina",
                status = if (today.creatine) "Registrada hoje" else "Ainda não registrada",
                checked = today.creatine,
                enabled = !saving,
                onCheckedChange = onCreatine,
            )
        }

        if (config.showWorkout) {
            if (config.showCreatine) TrackingSectionDivider() else Spacer(Modifier.height(12.dp))
            TrackingSwitchRow(
                title = "Treino",
                status = if (today.trained) "Registrado hoje" else "Ainda não registrado",
                checked = today.trained,
                enabled = !saving,
                onCheckedChange = onTrained,
            )
        }

        if (config.cardioEnabled) {
            if (config.showCreatine || config.showWorkout) {
                TrackingSectionDivider()
            } else {
                Spacer(Modifier.height(12.dp))
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Cardio do dia", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${trackingMinutes(today.cardio)} de ${trackingMinutes(config.dailyCardio)} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = FitnessTheme.colors.secondary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            ProgressBar(fraction = (today.cardio / config.dailyCardio).toFloat())
            Spacer(Modifier.height(12.dp))
            CardioForm(saving = saving, onSave = onCardio)
        }

        if (error != null) {
            Spacer(Modifier.height(10.dp))
            ErrorBanner(error)
        }
    }
}

@Composable
private fun TrackingSwitchRow(
    title: String,
    status: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                status,
                style = MaterialTheme.typography.bodySmall,
                color = if (checked) FitnessTheme.colors.success else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun TrackingSectionDivider() {
    Spacer(Modifier.height(10.dp))
    ScreenDivider()
    Spacer(Modifier.height(12.dp))
}

private fun trackingTitle(config: TrackingConfig): String {
    val labels = buildList {
        if (config.showCreatine) add("Creatina")
        if (config.showWorkout) add("Treino")
        if (config.cardioEnabled) add("Cardio")
    }
    return when (labels.size) {
        0, 1 -> labels.firstOrNull().orEmpty()
        2 -> "${labels[0]} e ${labels[1]}"
        else -> labels.dropLast(1).joinToString(", ") + " e " + labels.last()
    }
}

@Composable
private fun CardioForm(saving: Boolean, onSave: (Double) -> Unit) {
    var minutes by remember { mutableStateOf("") }
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = minutes,
            onValueChange = { value -> minutes = value.filter(Char::isDigit).take(3) },
            label = { Text("Minutos hoje") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(9.dp),
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = { minutes.toDoubleOrNull()?.let(onSave) },
            enabled = !saving && minutes.isNotBlank(),
            shape = RoundedCornerShape(9.dp),
            modifier = Modifier.height(54.dp),
        ) {
            Text("Registrar")
        }
    }
}

private fun trackingMinutes(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(Locale.forLanguageTag("pt-BR"), "%.1f", value)

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(9.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
