package com.example.solutionchallenge.api

data class HomeDataResponse(
    val dailyIntake: List<Int>,    // 일별 당류 섭취량 (그래프)
    val reportSummary: String,     // 간단한 리포트 텍스트
    val dailyMeals: List<DailyMeal> // 일별 식단 목록
)

data class DailyMeal(
    val mealType: String,
    val items: List<String>
)
