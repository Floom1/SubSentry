package com.example.subsentry.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Notification(
    val id: Int,
    @SerializedName("subscription") val subscriptionId: Int,
    @SerializedName("subscription_name") val subscriptionName: String,
    val message: String,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("shown_at") val shownAt: String?
)