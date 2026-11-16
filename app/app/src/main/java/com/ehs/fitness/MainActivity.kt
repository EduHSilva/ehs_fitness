package com.ehs.fitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ehs.fitness.core.settings.AppTheme
import com.ehs.fitness.core.settings.LocalAppSettings
import com.ehs.fitness.core.ui.theme.FitnessTheme
import com.ehs.fitness.di.AppContainer
import com.ehs.fitness.navigation.FitnessApp
import com.ehs.fitness.ui.LocalContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container: AppContainer = (application as FitnessApplication).container
        setContent {
            val settings = container.settings
            val theme by settings.theme.collectAsStateWithLifecycle()

            FitnessTheme(darkTheme = theme == AppTheme.DARK) {
                CompositionLocalProvider(
                    LocalAppSettings provides settings,
                    LocalContainer provides container,
                ) {
                    FitnessApp()
                }
            }
        }
    }
}
