package com.example.pointlyaisummary

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    // url for testing
    private const val Base_URL = "http://10.0.2.2:8000"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Base_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}