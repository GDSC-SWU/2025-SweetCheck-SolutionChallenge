package com.example.solutionchallenge.api

data class ArchiveDataResponse(
    val todaySugarGrams: Float,
    val meals: List<MealItem>,
    val calendarData: List<CalendarEntry>
)

data class MealItem(
    val mealType: String,
    val imageUrl: String,
    val description: String
)

data class CalendarEntry(
    val date: String,
    val hasRecord: Boolean
)
