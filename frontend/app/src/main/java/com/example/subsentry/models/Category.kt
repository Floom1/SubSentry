package com.example.subsentry.models

data class Category(
    val id: Int,
    val name: String,
    val user: Int
)

data class CategoryRequest(
    val name: String
)