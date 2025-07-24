package com.example.solutionchallenge.api

import com.example.solutionchallenge.data.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ✅ 1. 로그인
    @POST("/api/users/login")
    suspend fun login(@Body request: TokenRequest): UserInfoResponse

    // ✅ 2. 닉네임 등록 (별도 API)
    @POST("/api/users/nickname")
    suspend fun registerNickname(
        @Header("Authorization") bearerToken: String,
        @Body nickname: NicknameRequest
    ): Response<Void>

    // ✅ 3. 프로필 상세정보 등록
    @POST("/api/users/profile")
    suspend fun registerProfile(
        @Header("Authorization") bearerToken: String,
        @Body profile: UserProfileRequest
    ): Response<Void>

    // ✅ 메뉴 이미지 분석
    @POST("/api/photo/menu")
    suspend fun analyzeMenuPhoto(
        @Part("userId") userId: MultipartBody.Part,
        @Part image: MultipartBody.Part
    ): MenuScanResponse

    // ✅ 하루 이미지 분석
    @Multipart
    @POST("analyze-day")
    suspend fun analyzeDay(
        @Part morning: MultipartBody.Part,
        @Part lunch: MultipartBody.Part,
        @Part dinner: MultipartBody.Part,
        @Part snack: MultipartBody.Part
    ): Response<AnalyzeResponse>

    // ✅ 홈화면
    @GET("/api/home")
    suspend fun getHomeData(
        @Header("Authorization") token: String
    ): HomeDataResponse

    // ✅ 아카이브 날짜별 요약
    @GET("/api/me/{userId}")
    suspend fun getArchiveData(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): ArchiveDataResponse

    // ✅ 분석 결과 전체 목록
    @GET("/api/meal/results")
    suspend fun getMealResults(
        @Header("Authorization") token: String
    ): MealResultResponse

    // ✅ 날짜별 식사 이미지 목록
    @GET("/api/meals/images")
    suspend fun getMealImages(
        @Query("userId") userId: String,
        @Query("date") date: String
    ): MealImagesResponse

    // ✅ 월간 요약 데이터
    @GET("/api/meals/summary")
    suspend fun getMonthlySummary(
        @Query("userId") userId: String,
        @Query("month") month: String
    ): MealSummaryResponse

    // ✅ 특정 날짜의 식단
    @GET("/api/meals/{date}")
    suspend fun getDailyMeals(
        @Path("date") date: String
    ): DailyMealResponse

    // ✅ 식단 확정
    @POST("/api/meals/{mealId}/confirm")
    suspend fun confirmMeal(
        @Path("mealId") mealId: String,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

}
