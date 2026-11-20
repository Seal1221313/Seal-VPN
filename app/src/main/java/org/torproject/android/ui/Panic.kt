package org.torproject.android.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import org.torproject.android.OrbotApp
import org.torproject.android.service.OrbotConstants
import org.torproject.android.service.OrbotService
import org.torproject.android.util.Prefs
import org.torproject.android.util.sendIntentToService
import org.torproject.jni.TorService

object Panic {
    fun trigger(context: Context) {
        context.sendIntentToService(
            Intent(context, OrbotService::class.java)
                .setAction(TorService.ACTION_STOP)
                .putExtra(OrbotConstants.ACTION_STOP_FOREGROUND_TASK, true)
        )
        Prefs.orbotServiceLogClear()
        OrbotApp.resetLockFlags()
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .appTasks.forEach { it.finishAndRemoveTask() }
    }
}
