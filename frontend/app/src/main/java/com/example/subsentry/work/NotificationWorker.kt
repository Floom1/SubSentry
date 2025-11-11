package com.example.subsentry.work

import android.content.Context
import androidx.work.*
import com.example.subsentry.api.RetrofitClient
import com.example.subsentry.utils.NotificationManager
import com.example.subsentry.utils.NotificationPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "subscription_notification_worker"
        const val DEMO_INTERVAL_MINUTES = 1L

        // СДЕЛАЙТЕ МЕТОД СТАТИЧЕСКИМ (в companion object)
        fun scheduleWork(context: Context) {
            val preferences = NotificationPreferences(context)
            val interval = preferences.getCheckInterval().toLong()
            val actualInterval = DEMO_INTERVAL_MINUTES // Для демонстрации

            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
                actualInterval, TimeUnit.MINUTES,
                0, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val preferences = NotificationPreferences(applicationContext)
            if (!preferences.isNotificationsEnabled()) {
                return Result.success()
            }

            // Проверяем уведомления через API
            val response = RetrofitClient.apiService.checkNotifications()

            if (response.isSuccessful) {
                val responseBody = response.body()
                // Используем безопасное получение значения
                val notificationsCreated = responseBody?.notificationsCreated ?: 0

                // Показываем уведомление, если есть новые
                if (notificationsCreated > 0) {
                    val manager = NotificationManager(applicationContext)
                    manager.showNotification(
                        id = System.currentTimeMillis().toInt(),
                        title = "Новые уведомления",
                        message = "Обнаружено $notificationsCreated предстоящих платежей"
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

}