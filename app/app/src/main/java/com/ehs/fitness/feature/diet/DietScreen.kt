package com.ehs.fitness.feature.diet

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
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
import com.ehs.fitness.core.ui.components.RefreshableLazyColumn
import com.ehs.fitness.core.ui.components.Tag
import com.ehs.fitness.core.ui.theme.FitnessTheme
import com.ehs.fitness.data.model.Meal
import com.ehs.fitness.domain.diet.MealDraft
import com.ehs.fitness.domain.diet.NutritionFormat
import com.ehs.fitness.ui.appViewModel

@Composable
fun DietScreen() {
    val vm: DietViewModel = appViewModel { DietViewModel(it.dietRepository) }
    val state by vm.state.collectAsStateWithLifecycle()

    RefreshableLazyColumn(
        isRefreshing = state.refreshing,
        onRefresh = { vm.load(silent = true) },
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            IntroBanner(
                eyebrow = "Seu plano alimentar",
                title = "Alimentação",
                description = "Consulte as refeições planejadas para a sua rotina.",
                icon = Icons.Filled.Restaurant,
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
            EmptyNote("Para alterar a dieta, acesse a versão web do EHS Fitness.")
        }

        when {
            state.loading -> item { LoadingScreen("Carregando refeições...") }
            state.error != null -> item {
                PageState(
                    title = "Não foi possível carregar",
                    description = state.error!!,
                    icon = Icons.Filled.Restaurant,
                    actionLabel = "Tentar novamente",
                    onAction = { vm.load() },
                    isError = true,
                )
            }
            state.meals.isEmpty() -> item {
                PageState(
                    title = "Nenhuma refeição cadastrada",
                    description = "O profissional responsável ainda não cadastrou refeições para você.",
                    icon = Icons.Filled.Restaurant,
                )
            }
            else -> items(state.meals, key = { it.id }) { meal ->
                MealCard(meal)
            }
        }
    }
}

@Composable
private fun MealCard(meal: Meal) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, FitnessTheme.colors.border),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .background(FitnessTheme.colors.secondarySoft, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = FitnessTheme.colors.secondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(meal.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${meal.hour.ifBlank { "Sem horário" }} · ${MealDraft.itemsLabel(meal.foods.size)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val totals = NutritionFormat.totals(meal)
                    if (totals.isNotEmpty()) {
                        Text(
                            "Total: $totals",
                            style = MaterialTheme.typography.bodySmall,
                            color = FitnessTheme.colors.subtle,
                        )
                    }
                }
                Tag(
                    text = meal.hour.ifBlank { "--:--" },
                    color = FitnessTheme.colors.secondary,
                    background = FitnessTheme.colors.secondarySoft,
                )
            }

            if (meal.foods.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = FitnessTheme.colors.border)
                meal.foods.forEach { food ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                food.name.ifBlank { "Alimento" },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                listOfNotNull(
                                    food.quantity.takeIf { it > 0 }?.let(MealDraft::formatQuantity),
                                    food.observation.takeIf { it.isNotBlank() },
                                ).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        val macros = NutritionFormat.macros(food)
                        if (macros.isNotEmpty()) {
                            Text(
                                macros,
                                style = MaterialTheme.typography.bodySmall,
                                color = FitnessTheme.colors.subtle,
                            )
                        }
                    }
                }
            }
        }
    }
}
