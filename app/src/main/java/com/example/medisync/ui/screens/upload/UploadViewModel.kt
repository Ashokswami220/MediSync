package com.example.medisync.ui.screens.upload

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Progress(val percent: Int) : UploadState()
    data class Success(val url: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

class UploadViewModel : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val functions = Firebase.functions

    fun resetState() {
        _uploadState.value = UploadState.Idle
    }

    fun uploadFile(fileUri: Uri, context: Context) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            try {
                // 1. Ask Firebase for a signature
                val result = functions
                    .getHttpsCallable("getCloudinarySignature")
                    .call()
                    .await()
                
                val data = result.getData() as? Map<*, *> ?: throw Exception("Invalid response from server")
                val signature = data["signature"] as? String ?: throw Exception("Missing signature")
                // Cloud Functions usually convert Long to Int in JSON if it fits, so we cast to Number and get toLong
                val timestamp = (data["timestamp"] as? Number)?.toLong() ?: throw Exception("Missing timestamp")
                val apiKey = data["apiKey"] as? String ?: throw Exception("Missing apiKey")

                MediaManager.get().upload(fileUri)
                    .option("signature", signature)
                    .option("timestamp", timestamp)
                    .option("api_key", apiKey)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            _uploadState.value = UploadState.Progress(0)
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val percent = ((bytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
                            _uploadState.value = UploadState.Progress(percent)
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val secureUrl = resultData["secure_url"] as? String ?: ""
                            _uploadState.value = UploadState.Success(secureUrl)
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e("UploadViewModel", "Upload error: ${error.description}")
                            _uploadState.value = UploadState.Error(error.description)
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            // Handled similarly to error for now
                            _uploadState.value = UploadState.Error("Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch(context)

            } catch (e: Exception) {
                Log.e("UploadViewModel", "Upload process failed", e)
                _uploadState.value = UploadState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
