package com.example.solutionchallenge.api

import com.example.solutionchallenge.data.ConfirmRequest
import com.example.solutionchallenge.data.DailyMealResponse
import com.example.solutionchallenge.data.MealImagesResponse
import com.example.solutionchallenge.data.MealSummaryResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @Multipart
    @POST("analyze-day")
    suspend fun analyzeDay(
        @Part morning: MultipartBody.Part,
        @Part lunch: MultipartBody.Part,
        @Part dinner: MultipartBody.Part,
        @Part snack: MultipartBody.Part
    ): Response<AnalyzeResponse>

    @POST("/api/photo/menu")
    suspend fun analyzeMenuPhoto(
        @Part("userId") userId: MultipartBody.Part,
        @Part image: MultipartBody.Part
    ): MenuScanResponse

    @POST("/api/users/profile")
    suspend fun registerUserProfile(
        @Body request: UserProfileRequest
    )


    @GET("/api/home")
    suspend fun getHomeData(
        @Header("Authorization") token: String
    ): HomeDataResponse   // ✅ 연결!

    @GET("/api/me/{userId}")
    suspend fun getArchiveData(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): ArchiveDataResponse

    @GET("/api/meal/results")
    suspend fun getMealResults(
        @Header("Authorization") token: String
    ): MealResultResponse

    @GET("/api/meals/images")
    suspend fun getMealImages(
        @Query("userId") userId: String,
        @Query("date") date: String
    ): MealImagesResponse

    @GET("/api/meals/summary")
    suspend fun getMonthlySummary(
        @Query("userId") userId: String,
        @Query("month") month: String
    ): MealSummaryResponse

    @GET("/api/meals/{date}")
    suspend fun getDailyMeals(
        @Path("date") date: String
    ): DailyMealResponse


    @PUT("/api/meals/{mealId}/confirm")
    suspend fun confirmMeal(
        @Path("mealId") mealId: String,
        @Body body: ConfirmRequest
    ): Response<Unit>


}
