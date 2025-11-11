package com.example.subsentry

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.subsentry.api.RetrofitClient
import com.google.gson.GsonBuilder


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Настройка Gson
        val gson = GsonBuilder()
            .serializeNulls()
            .create()

        RetrofitClient.init(this, gson)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "your_channel_id",  // Уникальный ID канала
                "Your Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}