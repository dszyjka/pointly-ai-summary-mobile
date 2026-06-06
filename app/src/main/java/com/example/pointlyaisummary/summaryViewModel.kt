package com.example.pointlyaisummary

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File


class SummaryViewModel(
    private val repository: SummaryRepository
): ViewModel() {
    var summaryText by mutableStateOf("")
    private set

    var isLoading by mutableStateOf(false)
        private set

    var historyList by mutableStateOf<List<Summary>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun summarizeUploadedFile(
        file: File,
        responseType: String,
        rawUserRules: String?
    ) {
        summaryText = ""
        isLoading = true
        errorMessage = null
        val userRules: String = rawUserRules ?: ""

        viewModelScope.launch {
            try {
                repository.sendFileToSummarize(
                    file, responseType, userRules,
                    onChunkReceived = {chunk -> summaryText += chunk}
                )
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unknown error during summarize"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteUserSummary(summaryId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteSummary(summaryId)
                historyList = repository.loadHistory()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unknown error during deletion"
            }
        }
    }

    fun loadUserHistory() {
        viewModelScope.launch {
            try {
                historyList = repository.loadHistory()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unknown error during history loading"
            }
        }
    }

    fun searchUserFile(query: String) {
        viewModelScope.launch {
            try {
                historyList = repository.searchFile(query)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unknown error during searching"
            }
        }
    }

    fun clearSummaryText() {
        this.summaryText = ""
    }
}