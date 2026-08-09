package com.example.medisync.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object NotificationService {
    suspend fun triggerPushNotification(userFcmToken: String, fileName: String) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            // IMPORTANT: Replace this URL with the actual Vercel project URL once deployed
            val url = "https://fcm-backend-drab.vercel.app/api/notify"

            val jsonBody = JSONObject().apply {
                put("fcmToken", userFcmToken)
                put("title", "New Report Uploaded")
                put("message", "A new report '$fileName' has been uploaded for you.")
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(request)
                    .execute()
                if (response.isSuccessful) {
                    println("Notification triggered successfully!")
                } else {
                    println("Failed to trigger notification. HTTP code: ${response.code}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
