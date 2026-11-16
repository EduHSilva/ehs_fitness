package com.ehs.fitness.core.settings

import androidx.compose.runtime.staticCompositionLocalOf
import com.ehs.fitness.core.prefs.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme { LIGHT, DARK }

/** Tema escolhido pelo usuário, persistido entre execuções. */
class AppSettings(private val prefs: AppPreferences) {
    private val _theme = MutableStateFlow(
        if (prefs.readString(KEY_THEME) == "dark") AppTheme.DARK else AppTheme.LIGHT,
    )
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _theme.value = theme
        prefs.writeString(KEY_THEME, if (theme == AppTheme.DARK) "dark" else "light")
    }

    fun toggleTheme() {
        setTheme(if (_theme.value == AppTheme.LIGHT) AppTheme.DARK else AppTheme.LIGHT)
    }

    private companion object {
        const val KEY_THEME = "ehs-fitness-theme"
    }
}

val LocalAppSettings = staticCompositionLocalOf<AppSettings> {
    error("AppSettings não fornecido")
}
