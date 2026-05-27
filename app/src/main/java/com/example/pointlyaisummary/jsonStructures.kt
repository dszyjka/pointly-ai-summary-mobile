package com.example.pointlyaisummary

import android.icu.text.DateFormat
import com.google.gson.annotations.SerializedName


data class Summary (
    val id: Int,
    val userId: String,
    @SerializedName("filename")
    val fileName: String,
    val summary: String,
    @SerializedName("created_at")
    val createdAt: String
)