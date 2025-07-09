package com.example.solutionchallenge.data

data class DailyMealResponse(
    val date: String,
    val dailyTotalSugar: Float,
    val meals: List<Meal>
)

data class Meal(
    val mealType: String,
    val imageUrl: String,
    val totalSugar: Float,
    val mealItems: List<MealItem>
)

data class MealItem(
    val name: String,
    val sugar: Float
)
