package com.example.pointlyaisummary

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class SummaryRepository(
    private val userId: String
) {
    private val api = RetrofitClient.apiService

    suspend fun loadHistory(): List<Summary> {
        return withContext(Dispatchers.IO) {
            api.getHistory(userId)
        }
    }

    suspend fun searchFile(query: String): List<Summary> {
        return withContext(Dispatchers.IO) {
            api.getSearchedFiles(userId, query)
        }
    }

    suspend fun sendFileToSummarize(
        file: File,
        responseType: String,
        userRules: String,
        onChunkReceived: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val responseTypeBody = responseType.toRequestBody(
                "text/plain".toMediaTypeOrNull()
            )

            val userRulesBody = userRules.toRequestBody(
                "text/plain".toMediaTypeOrNull()
            )

            val fileRequestBody = file.asRequestBody(
                "application/octet-stream".toMediaTypeOrNull()
            )

            val filePart = MultipartBody.Part.createFormData(
                "file", file.name, fileRequestBody
            )

            val response = api.summarizeFile(
                userId,
                filePart,
                responseTypeBody,
                userRulesBody
            )

            if (response.isSuccessful) {
                val body = response.body()

                body?.byteStream()?.reader(Charsets.UTF_8)?.use { reader ->
                    val charBuffer = CharArray(1024)
                    var readCount: Int

                    while (reader.read(charBuffer).also { readCount = it } != -1) {
                        val chunk = String(charBuffer, 0, readCount)

                        withContext(Dispatchers.Main) {
                            onChunkReceived(chunk)
                        }
                    }
                }
            } else {
                throw Exception("Server error: ${response.code()}")
            }
        }
    }

    suspend fun deleteSummary(summaryId: Int) {
        return withContext(Dispatchers.IO) {
            api.deleteSummary(summaryId)
        }
    }
}