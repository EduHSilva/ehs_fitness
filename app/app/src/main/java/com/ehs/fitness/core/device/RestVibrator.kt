package com.ehs.fitness.core.device

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Alerta tátil quando o tempo de descanso entre séries termina. */
class RestVibrator(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun restFinished() {
        val device = vibrator?.takeIf { it.hasVibrator() } ?: return
        val pattern = longArrayOf(0L, 350L, 180L, 350L)
        runCatching { device.vibrate(VibrationEffect.createWaveform(pattern, -1)) }
    }
}
