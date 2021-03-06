package io.github.domi04151309.alwayson.receivers

import android.content.*
import android.icu.util.Calendar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import io.github.domi04151309.alwayson.actions.HeadsetActivity
import io.github.domi04151309.alwayson.actions.TurnOnScreenActivity
import io.github.domi04151309.alwayson.actions.alwayson.AlwaysOn
import io.github.domi04151309.alwayson.actions.ChargingCircleActivity
import io.github.domi04151309.alwayson.actions.ChargingFlashActivity
import io.github.domi04151309.alwayson.actions.ChargingIOSActivity
import io.github.domi04151309.alwayson.helpers.P
import io.github.domi04151309.alwayson.helpers.Rules
import io.github.domi04151309.alwayson.helpers.Global

class CombinedServiceReceiver : BroadcastReceiver() {

    companion object {
        var isScreenOn: Boolean = true
        var isAlwaysOnRunning: Boolean = false
        var hasRequestedStop: Boolean = false
    }

    override fun onReceive(c: Context, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(c)
        val rules = Rules(c, prefs)

        when (intent.action) {
            Intent.ACTION_HEADSET_PLUG -> {
                if (prefs.getBoolean("headphone_animation", false) && intent.getIntExtra("state", 0) == 1) {
                    if (!isScreenOn || isAlwaysOnRunning) {
                        if (isAlwaysOnRunning) LocalBroadcastManager.getInstance(c).sendBroadcast(Intent(Global.REQUEST_STOP))
                        c.startActivity(Intent(c, HeadsetActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
            }
            Intent.ACTION_POWER_CONNECTED -> {
                if (prefs.getBoolean("charging_animation", false)) {
                    if (!isScreenOn || isAlwaysOnRunning) {
                        if (isAlwaysOnRunning) LocalBroadcastManager.getInstance(c).sendBroadcast(Intent(Global.REQUEST_STOP))
                        val i: Intent = when (prefs.getString(P.CHARGING_STYLE, P.CHARGING_STYLE_DEFAULT)
                                ?: P.CHARGING_STYLE_DEFAULT) {
                            P.CHARGING_STYLE_CIRCLE -> Intent(c, ChargingCircleActivity::class.java)
                            P.CHARGING_STYLE_FLASH -> Intent(c, ChargingFlashActivity::class.java)
                            P.CHARGING_STYLE_IOS -> Intent(c, ChargingIOSActivity::class.java)
                            else -> return
                        }
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        c.startActivity(i)
                    }
                } else if (rules.isAlwaysOnDisplayEnabled()
                        && !rules.isAmbientMode()
                        && rules.matchesBatteryPercentage()
                        && rules.isInTimePeriod(Calendar.getInstance())
                        && !isScreenOn) {
                    c.startActivity(Intent(c, AlwaysOn::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                if (rules.isAlwaysOnDisplayEnabled()
                        && !rules.isAmbientMode()
                        && rules.matchesBatteryPercentage()
                        && rules.isInTimePeriod(Calendar.getInstance())
                        && !isScreenOn) {
                    c.startActivity(Intent(c, AlwaysOn::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            Intent.ACTION_SCREEN_OFF -> {
                isScreenOn = false
                val alwaysOn = prefs.getBoolean("always_on", false)
                if (alwaysOn && !hasRequestedStop) {
                    if (isAlwaysOnRunning) {
                        c.startActivity(Intent(c, TurnOnScreenActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        isAlwaysOnRunning = false
                    } else if (!rules.isAmbientMode()
                            && rules.matchesChargingState()
                            && rules.matchesBatteryPercentage()
                            && rules.isInTimePeriod(Calendar.getInstance())) {
                        c.startActivity(Intent(c, AlwaysOn::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                } else if (alwaysOn && hasRequestedStop) {
                    hasRequestedStop = false
                    isAlwaysOnRunning = false
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                isScreenOn = true
            }
        }
    }
}
