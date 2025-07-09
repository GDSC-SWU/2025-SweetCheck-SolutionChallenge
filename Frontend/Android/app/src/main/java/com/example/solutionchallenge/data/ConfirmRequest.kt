package com.example.solutionchallenge.data

data class ConfirmRequest(
    val userId: String,
    val imageUrl: String,
    val mealDateTime: String,
    val mealType: String,
    val totalSugar: Float,
    val items: List<FoodItem>
)
