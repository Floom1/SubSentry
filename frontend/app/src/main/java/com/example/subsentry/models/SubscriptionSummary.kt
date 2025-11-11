package com.example.subsentry.models

data class SubscriptionSummary(
    val subscriptions: List<Subscription>,
    val total_amount: Double
)