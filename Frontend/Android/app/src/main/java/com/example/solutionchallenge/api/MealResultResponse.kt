package com.example.solutionchallenge.api

data class MealResultResponse(
    val user: String,
    val date: String,
    val totalSugar: Int,
    val totalSugarCubes: Int,
    val meals: Map<String, MealDetail>
)

data class MealDetail(
    val foods: List<FoodItem>
)

data class FoodItem(
    val name: String,
    val sugar: Int
)
