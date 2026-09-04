package com.mydailytasks.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mydailytasks.app.data.local.AppDatabase
import com.mydailytasks.app.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            // Restore all task reminders when the Android device finishes booting
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                val allTasks = db.taskDao().getAllTasks()
                for (task in allTasks) {
                    AlarmScheduler.scheduleReminder(context, task)
                }
            }
        }
    }
}
