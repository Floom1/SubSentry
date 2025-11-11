package com.example.subsentry.api

import android.content.Context
import com.example.subsentry.utils.SessionManager
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/api/"

    private var sessionManager: SessionManager? = null
    private var gson: Gson? = null

    fun init(context: Context, gson: Gson) {
        this.sessionManager = SessionManager(context)
        this.gson = gson
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cookieInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()

        // Добавляем session cookie если пользователь вошел
        sessionManager?.let { sm ->
            if (sm.isLoggedIn()) {
                val sessionCookie = sm.getSessionCookie()
                if (sessionCookie.isNotEmpty()) {
                    requestBuilder.addHeader("Cookie", sessionCookie)
                }
            }
        }

        // Добавляем общие заголовки
        requestBuilder.addHeader("Accept", "application/json")
        requestBuilder.addHeader("Content-Type", "application/json")

        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(cookieInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}