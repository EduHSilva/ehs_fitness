package com.ehs.fitness.data.repository

import com.ehs.fitness.core.prefs.AppPreferences
import com.ehs.fitness.domain.workouts.TrainingSession
import kotlinx.serialization.json.Json

/** Progresso da execução local; nunca vai para a API. */
class TrainingSessionRepository(
    private val prefs: AppPreferences,
    private val json: Json,
) {
    fun load(): TrainingSession? {
        val raw = prefs.readString(KEY) ?: return null
        return runCatching { json.decodeFromString(TrainingSession.serializer(), raw) }.getOrNull()
    }

    fun save(session: TrainingSession) {
        prefs.writeString(KEY, json.encodeToString(TrainingSession.serializer(), session))
    }

    fun clear() {
        prefs.writeString(KEY, null)
    }

    private companion object {
        const val KEY = "training-session"
    }
}
