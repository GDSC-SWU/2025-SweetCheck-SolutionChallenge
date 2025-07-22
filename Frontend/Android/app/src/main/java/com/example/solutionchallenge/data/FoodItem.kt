package com.example.solutionchallenge.data

import java.io.Serializable

data class FoodItem(
    var name: String,
    var amount: Int,
    var sugar: Float
): Serializable
