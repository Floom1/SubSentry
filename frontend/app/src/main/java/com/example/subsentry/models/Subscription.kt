package com.example.subsentry.models

import com.google.gson.annotations.SerializedName

data class Subscription(
    val id: Int,
    val name: String,
    val price: Double,
    @SerializedName("next_payment_date") val nextPaymentDate: String,
    @SerializedName("category") val categoryId: Int?,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("user") val userId: Int,
    @SerializedName("notification_days_before") val notificationDaysBefore: Int
) {
    fun toSubscriptionRequest(): SubscriptionRequest {
        return SubscriptionRequest(
            name = name,
            price = price,
            next_payment_date = nextPaymentDate,
            category = categoryId,
            notification_days_before = notificationDaysBefore
        )
    }
}