package com.ehs.fitness.core.prefs

import android.content.Context

/** Persistência local equivalente ao localStorage do web. */
class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("ehs-fitness", Context.MODE_PRIVATE)

    fun readString(key: String): String? = prefs.getString(key, null)

    fun writeString(key: String, value: String?) {
        prefs.edit().apply {
            if (value == null) remove(key) else putString(key, value)
        }.apply()
    }
}
