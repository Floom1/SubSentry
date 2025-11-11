package com.example.subsentry

import android.app.Application
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
    }
}