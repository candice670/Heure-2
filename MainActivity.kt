package com.claude.hourlychime

import android.Manifest
import android.app.AlarmManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text

class MainActivity : ComponentActivity() {

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* résultat ignoré : la vibration ne nécessite pas cette permission,
           on la demande juste par précaution sur certaines versions */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ChimeScreen(
                    onRequestExactAlarmPermission = { requestExactAlarmPermissionIfNeeded() }
                )
            }
        }
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Ouvre les paramètres système pour autoriser les alarmes exactes.
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                )
                startActivity(intent)
            }
        }
    }
}

@Composable
fun ChimeScreen(onRequestExactAlarmPermission: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var enabled by remember { mutableStateOf(VibrationScheduler.isEnabled(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hourly Chime",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.title3
        )

        Text(
            text = if (enabled) "Activé" else "Désactivé",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        Switch(
            checked = enabled,
            onCheckedChange = { checked ->
                enabled = checked
                if (checked) {
                    onRequestExactAlarmPermission()
                }
                VibrationScheduler.setEnabled(context, checked)
            }
        )

        Text(
            text = ":15 → 1 vibration\n:30 → 2 vibrations\n:45 → 3 vibrations\n:00 → 1 longue",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption2,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
