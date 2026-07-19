package com.claude.hourlychime

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.Calendar

object VibrationScheduler {

    private const val PREFS_NAME = "hourly_chime_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val REQUEST_CODE = 1001

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            scheduleNext(context)
        } else {
            cancel(context)
        }
    }

    /** Calcule le prochain multiple de 15 minutes (00, 15, 30, 45) après maintenant. */
    private fun nextQuarterHourMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val currentMinute = cal.get(Calendar.MINUTE)
        val minutesToAdd = 15 - (currentMinute % 15)
        cal.add(Calendar.MINUTE, minutesToAdd)
        return cal.timeInMillis
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
    }

    fun scheduleNext(context: Context) {
        if (!isEnabled(context)) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextQuarterHourMillis()
        val pi = pendingIntent(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                // Permission pas encore accordée : on programme quand même en mode non-exact
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
    }

    /** Fait vibrer la montre selon le motif propre à chaque quart d'heure. */
    fun vibrateForCurrentQuarter(context: Context) {
        val cal = Calendar.getInstance()
        val minute = cal.get(Calendar.MINUTE)

        val vibrator = getVibrator(context)

        // Motifs : timings alternant [pause, vibration, pause, vibration, ...]
        val pattern: LongArray = when (minute) {
            0 -> longArrayOf(0, 900)                 // heure pile : une vibration longue
            15 -> longArrayOf(0, 200)                 // et quart : une vibration
            30 -> longArrayOf(0, 200, 200, 200)        // et demi : deux vibrations
            45 -> longArrayOf(0, 200, 200, 200, 200, 200) // trois-quarts : trois vibrations
            else -> longArrayOf(0, 200) // sécurité si déclenché en dehors d'un quart exact
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
