package com.example.medisync.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryHelper {

    suspend fun uploadImage(context: Context, fileUri: Uri): String = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            var tempFile: File? = null
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(fileUri)

                var extension = ""
                if (fileUri.scheme == "content") {
                    val mimeType = contentResolver.getType(fileUri)
                    if (mimeType != null) {
                        extension = MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(mimeType) ?: ""
                    }
                } else if (fileUri.scheme == "file") {
                    extension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString())
                }
                if (extension.isEmpty()) {
                    extension = "jpg"
                }

                tempFile = File(
                    context.cacheDir, "upload_contact_temp_${System.currentTimeMillis()}.$extension"
                )
                val outputStream = FileOutputStream(tempFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                if (tempFile.length() > 10 * 1024 * 1024) {
                    tempFile.delete()
                    continuation.resumeWithException(
                        Exception("File is too large (Maximum allowed is 10MB)")
                    )
                    return@suspendCancellableCoroutine
                }

                val safeUri = Uri.fromFile(tempFile)

                MediaManager.get()
                    .upload(safeUri)
                    .unsigned("medisync_preset")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {}
                        override fun onProgress(
                            requestId: String?, bytes: Long, totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(
                            requestId: String?, resultData: MutableMap<Any?, Any?>?
                        ) {
                            tempFile.delete()
                            val fileUrl = resultData?.get("secure_url") as? String
                            if (fileUrl != null) {
                                continuation.resume(fileUrl)
                            } else {
                                continuation.resumeWithException(
                                    Exception("Failed to retrieve secure URL")
                                )
                            }
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            tempFile.delete()
                            continuation.resumeWithException(
                                Exception(error?.description ?: "Upload failed")
                            )
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            tempFile.delete()
                        }
                    })
                    .dispatch(context)

            } catch (e: Exception) {
                tempFile?.delete()
                continuation.resumeWithException(e)
            }
        }
    }
}
