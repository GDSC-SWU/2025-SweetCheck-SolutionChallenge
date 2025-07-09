package com.example.solutionchallenge.api

data class UserProfileRequest(
    val uid: String,
    val gender: String,
    val height: String,
    val weight: String,
    val age: String
)
