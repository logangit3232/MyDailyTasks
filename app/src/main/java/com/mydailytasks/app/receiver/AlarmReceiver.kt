package com.mydailytasks.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mydailytasks.app.data.local.AppDatabase
import com.mydailytasks.app.util.AlarmScheduler
import com.mydailytasks.app.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("TASK_ID", -1L)
        val taskName = intent.getStringExtra("TASK_NAME") ?: "Daily Task Reminder"
        val taskDesc = intent.getStringExtra("TASK_DESC") ?: ""

        if (taskId != -1L) {
            // 1. Post Android Notification
            NotificationHelper.showTaskReminder(context, taskId, taskName, taskDesc)

            // 2. Reschedule next occurrence for repeating task
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                val task = db.taskDao().getTaskById(taskId)
                if (task != null) {
                    AlarmScheduler.scheduleReminder(context, task)
                }
            }
        }
    }
}
