package com.example.solutionchallenge.data

data class MealSummaryResponse(
    val month: String,
    val totalSugar: Float,
    val dailyRecords: List<DailyRecord>
)

data class DailyRecord(
    val date: String,
    val totalSugar: Float,
    val imageUrls: List<String>
)
