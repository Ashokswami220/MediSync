package com.example.medisync.ui.screens.admin

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState.Visible
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.medisync.model.UserProfile
import com.example.medisync.model.UserRole

import com.example.medisync.repo.UserRepository
import com.example.medisync.ui.components.MemberSwitcher
import com.example.medisync.utils.GlobalToastManager
import com.example.medisync.utils.UploadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedVisibilityScope.UploadDataDialog(
    onDismiss: () -> Unit,
    buttonCenter: Offset,
    preselectedUser: UserProfile? = null
) {
    BackHandler { onDismiss() }

    val context = LocalContext.current

    val userRepository: UserRepository = koinInject()

    val allUsersList by userRepository.getAllUsers()
        .collectAsState(initial = emptyList())
    
    val users = remember(allUsersList) {
        val claimedPlaceholderUids = allUsersList.flatMap { it.previousUids }.toSet()
        allUsersList.filter { it.uid !in claimedPlaceholderUids }
    }

    var docName by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf(preselectedUser) }

    LaunchedEffect(users) {
        if (selectedUser != null) {
            val updated = users.find { it.uid == selectedUser?.uid }
            if (updated != null) {
                selectedUser = updated
            }
        }
    }

    var selectedMember by remember { mutableStateOf("") }
    var showUserSelectionDialog by remember { mutableStateOf(false) }

    var uploadState by remember {
        mutableStateOf(
            "normal"
        )
    } // "normal", "uploading", "success", "error"
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val windowInfo = LocalWindowInfo.current
    val screenCenter = remember(windowInfo) {
        val size = windowInfo.containerSize
        Offset(
            x = size.width / 2f,
            y = size.height / 2f
        )
    }

    val animationProgress by transition.animateFloat(
        transitionSpec = { tween(400, easing = FastOutSlowInEasing) },
        label = "MacMinimizeAnimation"
    ) { state ->
        if (state == Visible) 1f else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    val scale = 0.05f + (0.95f * animationProgress)
                    scaleX = scale
                    scaleY = scale
                    alpha = animationProgress
                    translationX = (buttonCenter.x - screenCenter.x) * (1f - animationProgress)
                    translationY = (buttonCenter.y - screenCenter.y) * (1f - animationProgress)
                }
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.background)
                .clickable(enabled = false, onClick = {}) // Prevent dismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Form content (Inner Container)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(color = Color.Black.copy(alpha = 0.06f))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    UploadDocCard(
                        uploadState = uploadState,
                        selectedFileUri = selectedFileUri,
                        onFileSelected = { uri ->
                            selectedFileUri = uri
                            uploadState = "ready"
                        },
                        onRemoveFile = {
                            selectedFileUri = null
                            uploadState = "normal"
                        }
                    )

                    DocumentNameField(docName = docName, onDocNameChange = { docName = it })

                    if (preselectedUser == null) {
                        SelectUserCard(
                            selectedUser = selectedUser,
                            onShowDialog = { showUserSelectionDialog = true }
                        )
                    }

                    val dynamicMembers = selectedUser?.let { user ->
                        listOf(user.firstName) + user.members
                    } ?: emptyList()

                    SelectMemberCard(
                        selectedMember = selectedMember,
                        selectedUser = selectedUser,
                        members = dynamicMembers,
                        onMemberSelected = { selectedMember = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                UploadActionButtons(
                    onDismiss = onDismiss,
                    onUpload = {
                        if (selectedFileUri == null) {
                            GlobalToastManager.showToast(
                                message = "Please select a document",
                                icon = Icons.Default.Info
                            )
                            return@UploadActionButtons
                        }
                        if (docName.isEmpty() || selectedUser == null || selectedMember.isEmpty()) {
                            GlobalToastManager.showToast(
                                message = "Please fill all fields",
                                icon = Icons.Default.Info
                            )
                            return@UploadActionButtons
                        }

                        val userName = "${selectedUser?.firstName} ${selectedUser?.lastName}".trim()
                        val userUid = selectedUser?.uid ?: ""
                        val memberName = selectedMember

                        val finalDocName = if (docName.trim()
                                .endsWith("Report", ignoreCase = true)
                        ) docName.trim() else "${docName.trim()} Report"

                        UploadManager.startUpload(
                            context = context,
                            fileUri = selectedFileUri!!,
                            docName = finalDocName,
                            userName = userName,
                            userUid = userUid,
                            memberName = memberName
                        )

                        onDismiss()
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showUserSelectionDialog,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            UserSelectionDialog(
                users = users.filter { it.role != UserRole.ADMIN },
                onDismiss = { showUserSelectionDialog = false },
                onUserSelected = {
                    selectedUser = it
                    selectedMember = "" // reset member when user changes
                }
            )
        }
    }
}

@Composable
fun UploadDocCard(
    uploadState: String,
    selectedFileUri: Uri?,
    onFileSelected: (Uri) -> Unit,
    onRemoveFile: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        if (uris.size > 3) {
            GlobalToastManager.showToast("Max 3 images allowed", Icons.Default.Info)
            return@rememberLauncherForActivityResult
        }
        val isAllImages = uris.all {
            context.contentResolver.getType(it)
                ?.startsWith("image/") == true
        }
        val isSinglePdf =
            uris.size == 1 && context.contentResolver.getType(uris.first()) == "application/pdf"

        if (isAllImages) {
            GlobalToastManager.showToast("Processing images to PDF...", Icons.Default.Info)
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val pdfDocument = PdfDocument()
                    uris.forEachIndexed { index, uri ->
                        var inSampleSize = 1
                        context.contentResolver.openInputStream(uri)
                            ?.use { stream ->
                                val options = BitmapFactory.Options()
                                options.inJustDecodeBounds = true
                                BitmapFactory.decodeStream(stream, null, options)
                                val maxDim = 2000
                                if (options.outHeight > maxDim || options.outWidth > maxDim) {
                                    val halfHeight = options.outHeight / 2
                                    val halfWidth = options.outWidth / 2
                                    while (halfHeight / inSampleSize >= maxDim && halfWidth / inSampleSize >= maxDim) {
                                        inSampleSize *= 2
                                    }
                                }
                            }

                        val bitmap = context.contentResolver.openInputStream(uri)
                            ?.use { stream ->
                                val options = BitmapFactory.Options()
                                options.inSampleSize = inSampleSize
                                BitmapFactory.decodeStream(stream, null, options)
                            }

                        if (bitmap != null) {
                            val a4Width = 595f
                            val a4Height = 842f
                            val pageInfo = PdfDocument.PageInfo.Builder(
                                a4Width.toInt(), a4Height.toInt(), index + 1
                            )
                                .create()
                            val page = pdfDocument.startPage(pageInfo)

                            val scale =
                                (a4Width / bitmap.width).coerceAtMost(a4Height / bitmap.height)
                            val scaledWidth = bitmap.width * scale
                            val scaledHeight = bitmap.height * scale
                            val left = (a4Width - scaledWidth) / 2f
                            val top = (a4Height - scaledHeight) / 2f

                            val destRect = RectF(
                                left, top, left + scaledWidth, top + scaledHeight
                            )
                            val paint = Paint(
                                Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG
                            )
                            page.canvas.drawBitmap(bitmap, null, destRect, paint)

                            pdfDocument.finishPage(page)
                            bitmap.recycle()
                        }
                    }
                    val sharedReportsDir = File(context.cacheDir, "shared_reports")
                    if (!sharedReportsDir.exists()) sharedReportsDir.mkdirs()
                    val pdfFile = File(
                        sharedReportsDir, "generated_report_${System.currentTimeMillis()}.pdf"
                    )
                    pdfFile.outputStream()
                        .use { pdfDocument.writeTo(it) }
                    pdfDocument.close()

                    withContext(Dispatchers.Main) {
                        val pdfUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            pdfFile
                        )
                        onFileSelected(pdfUri)
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        GlobalToastManager.showToast("Failed to process images", Icons.Default.Info)
                    }
                }
            }
        } else if (isSinglePdf) {
            onFileSelected(uris.first())
        } else {
            GlobalToastManager.showToast(
                "Please select 1 PDF or up to 3 images", Icons.Default.Info
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (uploadState == "normal") Color.Transparent else MaterialTheme.colorScheme.secondary.copy(
                    alpha = 0.1f
                )
            )
            .border(
                1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
            .clickable(enabled = uploadState == "normal") { launcher.launch("*/*") },
        contentAlignment = Alignment.Center
    ) {
        if (uploadState == "ready" && selectedFileUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Preview / Icon
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                        .border(
                            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            val mimeType = context.contentResolver.getType(selectedFileUri)
                                ?: if (selectedFileUri.toString()
                                        .contains(".pdf")
                                ) "application/pdf" else "*/*"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(selectedFileUri, mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                GlobalToastManager.showToast(
                                    message = "No app to view this content",
                                    icon = Icons.Default.ErrorOutline
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = "Document Preview",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right Text and Remove
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Document Selected",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onRemoveFile,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "Remove",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Upload",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Tap to select a document",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun DocumentNameField(docName: String, onDocNameChange: (String) -> Unit) {
    OutlinedTextField(
        value = docName,
        onValueChange = onDocNameChange,
        label = { Text("Document Name") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            focusedLabelColor = MaterialTheme.colorScheme.secondary,
            cursorColor = MaterialTheme.colorScheme.secondary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        suffix = {
            Text(
                "Report", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    )
}

@Composable
fun SelectUserCard(selectedUser: UserProfile?, onShowDialog: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onShowDialog() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedUser == null) {
                Column {
                    Text(
                        text = "Select User",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${selectedUser.firstName} ${selectedUser.lastName}".trim(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Select User",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SelectMemberCard(
    selectedMember: String, selectedUser: UserProfile?, members: List<String>,
    onMemberSelected: (String) -> Unit
) {
    val isEnabled = selectedUser != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(
                    alpha = if (isEnabled) 0.5f else 0.2f
                ),
                shape = CircleShape
            )
            .clip(CircleShape)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Member",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isEnabled) 1f else 0.5f
                    ),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Member",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = if (isEnabled) 1f else 0.5f
                )
            )
        }

        MemberSwitcher(
            selectedMember = selectedMember,
            onMemberSelected = onMemberSelected,
            enabled = isEnabled,
            members = members,
            popupAlignment = Alignment.BottomEnd,
            triggerContent = { _, onExpand ->
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(
                                alpha = if (isEnabled) 0.5f else 0.2f
                            ),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable(enabled = isEnabled, onClick = onExpand)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = selectedMember.ifEmpty { "Select" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (isEnabled) 1f else 0.5f
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (isEnabled) 1f else 0.5f
                            ),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun UploadActionButtons(onDismiss: () -> Unit, onUpload: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Text(
                "Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onUpload,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                "Upload", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}