package com.ehs.fitness.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ehs.fitness.di.AppContainer

val LocalContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer não fornecido")
}

@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline create: (AppContainer) -> VM): VM {
    val container = LocalContainer.current
    return viewModel(
        factory = viewModelFactory {
            initializer { create(container) }
        },
    )
}
