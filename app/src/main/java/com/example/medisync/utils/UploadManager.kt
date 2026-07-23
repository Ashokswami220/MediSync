package com.example.medisync.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.medisync.data.repository.DocumentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


object UploadManager {

    enum class UploadState {
        IDLE, UPLOADING, SUCCESS, ERROR
    }

    data class UploadStatus(
        val state: UploadState = UploadState.IDLE,
        val progress: Float = 0f,
        val docName: String = "",
        val targetUid: String = "",
        val errorMsg: String? = null
    )

    private val _uploadStatus = MutableStateFlow(UploadStatus())
    val status = _uploadStatus.asStateFlow()
    fun startUpload(
        context: Context,
        fileUri: Uri,
        docName: String,
        uName: String,
        uUid: String,
        cleanMember: String,
        repository: DocumentRepository
    ) {
        if (_uploadStatus.value.state == UploadState.UPLOADING) {
            return
        }

        _uploadStatus.value = UploadStatus(
            state = UploadState.UPLOADING,
            progress = 0f,
            docName = docName,
            targetUid = uUid
        )

        CoroutineScope(Dispatchers.IO).launch {
            var tempFile: java.io.File? = null
            try {
                // Copy the content URI to a temporary file to avoid SecurityExceptions with WorkManager
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(fileUri)
                tempFile = java.io.File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}")
                val outputStream = java.io.FileOutputStream(tempFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                val safeUri = Uri.fromFile(tempFile)

                MediaManager.get()
                    .upload(safeUri)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {}

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                            if (totalBytes > 0) {
                                val progress = bytes.toFloat() / totalBytes.toFloat()
                                _uploadStatus.value = _uploadStatus.value.copy(progress = progress)
                            }
                        }

                        override fun onSuccess(
                            requestId: String?, resultData: MutableMap<Any?, Any?>?
                        ) {
                            tempFile.delete()
                            try {
                                val fileUrl = resultData?.get("secure_url") as? String ?: ""
                                val publicId = resultData?.get("public_id") as? String ?: ""
                                val resourceType =
                                    resultData?.get("resource_type") as? String ?: "image"
                                val uploaderEmail =
                                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
                                        ?: ""

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        repository.saveDocumentMetadata(
                                            documentName = docName,
                                            fileUrl = fileUrl,
                                            linkedUser = uName,
                                            linkedUserUid = uUid,
                                            linkedMember = cleanMember,
                                            publicId = publicId,
                                            resourceType = resourceType,
                                            uploaderEmail = uploaderEmail
                                        )
                                            .onSuccess {
                                                _uploadStatus.value =
                                                    _uploadStatus.value.copy(
                                                        state = UploadState.SUCCESS
                                                    )
                                                delay(4000L.milliseconds)
                                                if (_uploadStatus.value.state == UploadState.SUCCESS) {
                                                    dismiss()
                                                }
                                            }
                                            .onFailure { e ->
                                                _uploadStatus.value = _uploadStatus.value.copy(
                                                    state = UploadState.ERROR,
                                                    errorMsg = e.message
                                                        ?: "Failed to save to Firestore"
                                                )
                                                delay(4000L.milliseconds)
                                                dismiss()
                                            }
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                        _uploadStatus.value = _uploadStatus.value.copy(
                                            state = UploadState.ERROR,
                                            errorMsg = e.message ?: "Crash inside coroutine"
                                        )
                                        delay(4000L.milliseconds)
                                        dismiss()
                                    }
                                }
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                _uploadStatus.value = _uploadStatus.value.copy(
                                    state = UploadState.ERROR,
                                    errorMsg = e.message ?: "Crash inside onSuccess"
                                )
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(4000L.milliseconds)
                                    dismiss()
                                }
                            }
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            tempFile.delete()
                            _uploadStatus.value = _uploadStatus.value.copy(
                                state = UploadState.ERROR,
                                errorMsg = error?.description ?: "Upload failed"
                            )
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(4000L.milliseconds)
                                dismiss()
                            }
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                    })
                    .dispatch(context)
            } catch (e: Exception) {
                e.printStackTrace()
                tempFile?.delete()

                _uploadStatus.value = _uploadStatus.value.copy(
                    state = UploadState.ERROR,
                    errorMsg = e.message ?: "Failed to dispatch upload"
                )
                CoroutineScope(Dispatchers.Main).launch {
                    delay(4000L.milliseconds)
                    dismiss()
                }
            }
        }
    }

    fun dismiss() {
        _uploadStatus.value = UploadStatus()
    }
}
