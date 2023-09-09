package com.lrm.todolist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.lrm.todolist.constants.CHANNEL_DESCRIPTION
import com.lrm.todolist.constants.CHANNEL_ID
import com.lrm.todolist.constants.CHANNEL_NAME
import com.lrm.todolist.database.ToDoRoomDatabase

class ToDoApplication: Application() {

    val database: ToDoRoomDatabase by lazy {
        ToDoRoomDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}