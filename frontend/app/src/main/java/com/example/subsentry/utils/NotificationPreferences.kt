package com.example.subsentry.utils

import android.content.Context
import android.content.SharedPreferences

class NotificationPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("notification_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_CHECK_INTERVAL = "check_interval"
        private const val DEFAULT_CHECK_INTERVAL = 60 // минут
    }

    fun isNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()
    }

    fun getCheckInterval(): Int {
        return sharedPreferences.getInt(KEY_CHECK_INTERVAL, DEFAULT_CHECK_INTERVAL)
    }

    fun setCheckInterval(minutes: Int) {
        // Ограничение: минимум 1 минута
        val validatedMinutes = if (minutes < 1) 1 else minutes
        sharedPreferences.edit()
            .putInt(KEY_CHECK_INTERVAL, validatedMinutes)
            .apply()
    }
}