package com.example.solutionchallenge.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

object AuthRetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/" // 로그인 서버 주소

    private val okHttpClient = OkHttpClient.Builder().build()

    val apiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}
