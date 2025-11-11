package com.example.subsentry.api

import com.example.subsentry.models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register/")
    suspend fun registerUser(
        @Body user: RegisterRequest
    ): Response<User>

    @POST("auth/login/")
    suspend fun loginUser(
        @Body credentials: LoginRequest
    ): Response<User>

    @POST("auth/logout/")
    suspend fun logoutUser(): Response<LogoutResponse>

    @GET("auth/user/")
    suspend fun getCurrentUser(): Response<User>

    @GET("subscriptions/")
    suspend fun getSubscriptions(): Response<List<Subscription>>

    @GET("subscriptions/{id}/")
    suspend fun getSubscription(@Path("id") id: Int): Response<Subscription>

    @POST("subscriptions/")
    suspend fun createSubscription(
        @Body subscription: SubscriptionRequest
    ): Response<Subscription>

    @PUT("subscriptions/{id}/")
    suspend fun updateSubscription(
        @Path("id") id: Int,
        @Body subscription: SubscriptionRequest
    ): Response<Subscription>

    @HTTP(method = "DELETE", path = "subscriptions/{id}/", hasBody = false)
    suspend fun deleteSubscription(
        @Path("id") id: Int
    ): Response<Unit>

    @GET("subscription-summary/")
    suspend fun getSubscriptionSummary(): Response<SubscriptionSummary>

    @GET("categories/")
    suspend fun getCategories(): Response<List<Category>>

    @POST("categories/")
    suspend fun createCategory(
        @Body category: CategoryRequest
    ): Response<Category>

    @POST("check-notifications/")
    suspend fun checkNotifications(): Response<CheckNotificationsResponse>

    @GET("notifications/pending/")
    suspend fun getPendingNotifications(): Response<List<Notification>>

    @POST("notifications/{id}/mark_as_shown/")
    suspend fun markNotificationAsShown(
        @Path("id") id: Int
    ): Response<NotificationResponse>
}



