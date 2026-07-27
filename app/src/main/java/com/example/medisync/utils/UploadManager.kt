package com.example.medisync.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.medisync.repo.DocumentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.milliseconds

object UploadManager : KoinComponent {
    private val repository: DocumentRepository by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        userName: String,
        userUid: String,
        memberName: String
    ) {
        if (_uploadStatus.value.state == UploadState.UPLOADING) {
            return
        }

        _uploadStatus.value = UploadStatus(
            state = UploadState.UPLOADING,
            progress = 0f,
            docName = docName,
            targetUid = userUid
        )

        scope.launch {
            var tempFile: File? = null
            try {
                // Copy the content URI to a temporary file to avoid SecurityExceptions with WorkManager
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
                    extension = if (fileUri.toString()
                            .contains(".pdf")
                    ) "pdf" else "jpg"
                }

                tempFile =
                    File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}.$extension")
                val outputStream = FileOutputStream(tempFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // Check file size limit (10 MB = 10 * 1024 * 1024 bytes)
                if (tempFile.length() > 10 * 1024 * 1024) {
                    tempFile.delete()
                    _uploadStatus.value = _uploadStatus.value.copy(
                        state = UploadState.ERROR,
                        errorMsg = "File is too large (Maximum allowed is 10MB)"
                    )
                    scope.launch(Dispatchers.Main) {
                        delay(4000L.milliseconds)
                        dismiss()
                    }
                    return@launch
                }

                val safeUri = Uri.fromFile(tempFile)

                val progressJob = scope.launch {
                    var fakeProgress = 0f
                    while (fakeProgress < 0.9f) {
                        delay(300.milliseconds)
                        fakeProgress += 0.05f
                        _uploadStatus.value =
                            _uploadStatus.value.copy(progress = fakeProgress.coerceAtMost(0.9f))
                    }
                }

                MediaManager.get()
                    .upload(safeUri)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {}

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                            // Using fake progress instead because real progress is sometimes unreliable
                        }

                        override fun onSuccess(
                            requestId: String?, resultData: MutableMap<Any?, Any?>?
                        ) {
                            progressJob.cancel()
                            _uploadStatus.value = _uploadStatus.value.copy(progress = 1.0f)
                            tempFile.delete()
                            try {
                                val fileUrl = resultData?.get("secure_url") as? String ?: ""
                                val publicId = resultData?.get("public_id") as? String ?: ""
                                val resourceType =
                                    resultData?.get("resource_type") as? String ?: "image"
                                val uploaderEmail =
                                    FirebaseAuth.getInstance().currentUser?.email
                                        ?: ""

                                scope.launch {
                                    try {
                                        repository.saveDocumentMetadata(
                                            documentName = docName,
                                            fileUrl = fileUrl,
                                            linkedUser = userName,
                                            linkedUserUid = userUid,
                                            linkedMember = memberName,
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
                                scope.launch(Dispatchers.Main) {
                                    delay(4000L.milliseconds)
                                    dismiss()
                                }
                            }
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            progressJob.cancel()
                            tempFile.delete()
                            val rawError = error?.description ?: "Upload failed"
                            val friendlyError =
                                if (rawError.contains("file size too large", ignoreCase = true)) {
                                    "File is too large (Maximum allowed is 10MB)"
                                } else {
                                    rawError
                                }

                            _uploadStatus.value = _uploadStatus.value.copy(
                                state = UploadState.ERROR,
                                errorMsg = friendlyError
                            )
                            scope.launch(Dispatchers.Main) {
                                delay(4000L.milliseconds)
                                dismiss()
                            }
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            progressJob.cancel()
                            tempFile.delete()
                        }
                    })
                    .dispatch(context)
            } catch (e: Exception) {
                e.printStackTrace()
                tempFile?.delete()

                _uploadStatus.value = _uploadStatus.value.copy(
                    state = UploadState.ERROR,
                    errorMsg = e.message ?: "Failed to dispatch upload"
                )
                scope.launch(Dispatchers.Main) {
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
