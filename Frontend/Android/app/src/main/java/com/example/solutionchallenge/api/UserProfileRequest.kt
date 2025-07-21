package com.example.solutionchallenge.api

data class UserProfileRequest(
    val uid: String,
    val gender: String,
    val height: Int?,
    val weight: Int?,
    val age: Int?,
    val nickname: String? = null // ← nickname은 null 가능
)
