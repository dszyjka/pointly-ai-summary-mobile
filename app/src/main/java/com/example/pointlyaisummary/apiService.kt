package com.example.pointlyaisummary

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming


interface ApiService {
    @GET("history")
    suspend fun getHistory(@Header("X-USER-ID") userId: String): List<Summary>

    @GET("search")
    suspend fun getSearchedFiles(@Header("X-USER-ID") userId: String,
                                 @Query("searched_file") searchedFile: String
    ): List<Summary>

    @Streaming
    @Multipart
    @POST("summarize")
    suspend fun  summarizeFile(
        @Header("X-USER-ID") userId: String,
        @Part file: MultipartBody.Part,
        @Part("response_type") responseType: RequestBody,
        @Part("user_rules") userRules: RequestBody
    ): Response<ResponseBody>
}