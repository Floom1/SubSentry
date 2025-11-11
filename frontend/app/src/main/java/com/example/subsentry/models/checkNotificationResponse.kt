package com.example.subsentry.models

import com.google.gson.annotations.SerializedName

data class CheckNotificationsResponse(
    @SerializedName("notifications_created") val notificationsCreated: Int,
    val message: String
)