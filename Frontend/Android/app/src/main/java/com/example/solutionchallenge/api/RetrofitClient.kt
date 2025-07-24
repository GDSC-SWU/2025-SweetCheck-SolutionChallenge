package com.example.solutionchallenge.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://34.47.80.132:8080/"

    // 👇 타임아웃 설정 추가 (30초)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // 서버 연결 시도 타임아웃
        .readTimeout(30, TimeUnit.SECONDS)    // 응답 대기 타임아웃
        .writeTimeout(30, TimeUnit.SECONDS)   // 요청 쓰기 타임아웃 (파일 업로드 시 중요)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
