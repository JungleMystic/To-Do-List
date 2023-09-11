package com.lrm.todolist.utils

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lrm.todolist.MainActivity
import com.lrm.todolist.R
import com.lrm.todolist.constants.CHANNEL_ID
import java.util.Date

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        // Open the MainActivity, when we press on Notification
        val i = Intent(context, MainActivity::class.java)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val notificationTitle: String = intent.getStringExtra("Title").toString()
        val messageText: String = intent.getStringExtra("Message").toString()
        val requestCode = intent.getIntExtra("RequestCode", 0)

        val pendingIntent = PendingIntent.getActivity(context, requestCode, i, PendingIntent.FLAG_IMMUTABLE)

        // Building a Notification here...
        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(notificationTitle)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setGroup("ToDo Reminders")
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            notify(getNotificationId(), builder)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }, 2000)
    }

    private fun getNotificationId(): Int = (Date().time/1000 % Integer.MAX_VALUE).toInt()
}