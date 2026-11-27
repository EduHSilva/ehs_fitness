package com.ehs.fitness.data.repository

import com.ehs.fitness.core.prefs.AppPreferences
import com.ehs.fitness.core.session.SessionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Cargas que o aluno ajustou no aparelho, por exercício e por conta. Vale como
 * fallback quando a API recusa a gravação no plano (aluno com profissional ativo).
 */
class LoadOverrideRepository(
    private val prefs: AppPreferences,
    private val session: SessionManager,
    private val json: Json,
) {
    fun overrides(): Map<Int, Double> {
        val raw = prefs.readString(key()) ?: return emptyMap()
        return runCatching { json.decodeFromString(StoredLoads.serializer(), raw).overrides }
            .getOrDefault(emptyMap())
    }

    fun save(exerciseId: Int, load: Double) {
        write(overrides() + (exerciseId to load.coerceIn(0.0, 1000.0)))
    }

    fun remove(exerciseId: Int) {
        val current = overrides()
        if (exerciseId !in current) return
        write(current - exerciseId)
    }

    private fun write(overrides: Map<Int, Double>) {
        prefs.writeString(key(), json.encodeToString(StoredLoads.serializer(), StoredLoads(overrides)))
    }

    private fun key(): String {
        val userId = session.session.value?.user?.id?.takeIf { it.isNotBlank() } ?: "anon"
        return "$KEY_PREFIX$userId"
    }

    @Serializable
    private data class StoredLoads(val overrides: Map<Int, Double> = emptyMap())

    private companion object {
        const val KEY_PREFIX = "load-overrides-"
    }
}