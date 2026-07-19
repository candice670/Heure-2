package com.claude.hourlychime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reçu à chaque quart d'heure quand l'app est activée.
 * Déclenche la vibration puis reprogramme immédiatement la prochaine alarme,
 * ce qui permet à l'app de continuer indéfiniment en arrière-plan.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!VibrationScheduler.isEnabled(context)) return
        VibrationScheduler.vibrateForCurrentQuarter(context)
        VibrationScheduler.scheduleNext(context)
    }
}
