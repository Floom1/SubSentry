package com.example.subsentry.models

data class SubscriptionRequest(
    val name: String,
    val price: Double,
    val next_payment_date: String,
    val category: Int?,
    val notification_days_before: Int
)