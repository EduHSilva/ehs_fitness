package com.ehs.fitness.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehs.fitness.core.settings.AppTheme
import com.ehs.fitness.core.settings.LocalAppSettings

/** Alterna entre tema claro e escuro; a escolha fica salva no aparelho. */
@Composable
fun ThemeToggleButton(modifier: Modifier = Modifier) {
    val settings = LocalAppSettings.current
    val theme by settings.theme.collectAsStateWithLifecycle()
    val dark = theme == AppTheme.DARK

    IconButton(onClick = settings::toggleTheme, modifier = modifier) {
        Icon(
            imageVector = if (dark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
            contentDescription = if (dark) "Usar tema claro" else "Usar tema escuro",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(22.dp),
        )
    }
}
