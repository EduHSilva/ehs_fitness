package com.ehs.fitness

import android.app.Application
import com.ehs.fitness.di.AppContainer

class FitnessApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
