package com.claude.hourlychime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Si la montre redémarre (mise à jour, batterie vide...), les alarmes programmées
 * sont perdues par le système. Ce receiver les reprogramme automatiquement
 * si l'utilisateur avait activé l'app.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (VibrationScheduler.isEnabled(context)) {
                VibrationScheduler.scheduleNext(context)
            }
        }
    }
}
