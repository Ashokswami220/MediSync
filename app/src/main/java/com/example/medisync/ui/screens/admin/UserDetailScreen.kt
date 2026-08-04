package com.example.medisync.ui.screens.admin

import android.content.Intent
import android.content.Intent.ACTION_DIAL
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.medisync.model.DocumentMetadata
import com.example.medisync.ui.components.AddMemberBottomSheet
import com.example.medisync.ui.components.ClearAdminDataDialog
import com.example.medisync.ui.components.DeleteUsersDialog
import com.example.medisync.ui.components.MemberSwitcher
import com.example.medisync.ui.components.UserAvatar
import com.example.medisync.utils.GlobalToastManager
import com.example.medisync.utils.HapticHelper
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun UserDetailScreen(
    userUid: String,
    viewModel: UserDetailViewModel = koinViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToReportDetail: (String, String) -> Unit = { _, _ -> },
    onTopBarClick: () -> Unit = {}
) {
    LaunchedEffect(userUid) {
        viewModel.loadUser(userUid)
    }

    val userProfile by viewModel.userProfile.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val userName = userProfile?.firstName ?: "User"
    val members = userProfile?.members?.ifEmpty { listOf(userName) } ?: listOf(userName)

    // Automatically set the self member at top
    val displayMembers = listOf(userName) + members.filter { it != userName }
    var selectedMember by remember(displayMembers) { mutableStateOf(displayMembers[0]) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var uploadButtonCenter by remember { mutableStateOf(Offset.Zero) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            UserDetailTopBar(
                userName = userName,
                avatarUrl = userProfile?.avatarUrl,
                onBackClick = onBackClick,
                onDeleteClick = { showDeleteDialog = true },
                onClearDataClick = { showClearDataDialog = true },
                onTopBarClick = onTopBarClick
            )
        },
        bottomBar = {
            UserDetailBottomBar(
                selectedMember = selectedMember,
                onMemberSelected = { selectedMember = it },
                onAddMemberClick = { showAddMemberDialog = true },
                onUploadClick = { showUploadDialog = true },
                onUploadButtonPositioned = { uploadButtonCenter = it },
                members = displayMembers
            )
        }
    ) { paddingValues ->
        val isMainUser = selectedMember == userName
        val otherMembers = members.filter { it != userName }
        val filteredDocuments = documents.filter { doc ->
            if (isMainUser) {
                !otherMembers.contains(doc.linkedMember)
            } else {
                doc.linkedMember == selectedMember
            }
        }.reversed()

        UserDetailReportsList(
            selectedMember = selectedMember,
            documents = filteredDocuments,
            isLoading = isLoading,
            onNavigateToReportDetail = onNavigateToReportDetail,
            onDeleteReport = { docId ->
                viewModel.deleteReport(docId) { _, msg ->
                    GlobalToastManager.showToast(message = msg)
                }
            },
            paddingValues = paddingValues
        )
    }

    if (showAddMemberDialog) {
        AddMemberBottomSheet(
            onDismiss = { showAddMemberDialog = false },
            onSave = { memberName ->
                viewModel.addMember(userUid, memberName) { success, msg ->
                    GlobalToastManager.showToast(message = msg)
                    if (success) {
                        showAddMemberDialog = false
                    }
                }
            }
        )
    }

    AnimatedVisibility(
        visible = showUploadDialog,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        UploadDataDialog(
            onDismiss = { showUploadDialog = false },
            buttonCenter = uploadButtonCenter,
            preselectedUser = userProfile
        )
    }

    if (showDeleteDialog) {
        DeleteUsersDialog(
            userCount = 1,
            onConfirm = {
                viewModel.deleteUser(userUid) { success, msg ->
                    GlobalToastManager.showToast(message = msg)
                    if (success) {
                        onBackClick()
                    }
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showClearDataDialog) {
        ClearAdminDataDialog(
            userCount = 1,
            onConfirm = {
                viewModel.clearUserData(userUid) { _, msg ->
                    GlobalToastManager.showToast(message = msg)
                }
            },
            onDismiss = { showClearDataDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailTopBar(
    userName: String,
    avatarUrl: String?,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClearDataClick: () -> Unit,
    onTopBarClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var showTopMenu by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            modifier = Modifier.padding(horizontal = 4.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTopBarClick() }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    UserAvatar(
                        avatarUrl = avatarUrl,
                        size = 40.dp,
                        iconTint = Color.White,
                        backgroundColor = Color.Black.copy(alpha = 0.5f),
                        borderWidth = 0.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = userName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        color = colorScheme.onBackground
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onBackClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = colorScheme.onBackground
                    )
                }
            },
            actions = {
                val dialIntent = remember {
                    Intent(
                        ACTION_DIAL,
                        "tel:1234567890".toUri()
                    )
                }
                IconButton(
                    onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        context.startActivity(dialIntent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Call,
                        contentDescription = "Call",
                        tint = colorScheme.onBackground
                    )
                }
                Box {
                    IconButton(
                        onClick = {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            showTopMenu = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = colorScheme.onBackground
                        )
                    }
                    MaterialTheme(
                        colorScheme = MaterialTheme.colorScheme,
                        shapes = MaterialTheme.shapes.copy(
                            extraSmall = RoundedCornerShape(12.dp)
                        )
                    ) {
                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false },
                            modifier = Modifier.background(colorScheme.surface),
                            offset = DpOffset(0.dp, 8.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Users", color = colorScheme.error) },
                                onClick = {
                                    showTopMenu = false
                                    onDeleteClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete, contentDescription = null,
                                        tint = colorScheme.error
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Data", color = colorScheme.error) },
                                onClick = {
                                    showTopMenu = false
                                    onClearDataClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ClearAll, contentDescription = null,
                                        tint = colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.background
            )
        )
        HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
    }
}

@Composable
fun UserDetailBottomBar(
    selectedMember: String,
    onMemberSelected: (String) -> Unit,
    onAddMemberClick: () -> Unit,
    onUploadClick: () -> Unit,
    onUploadButtonPositioned: (Offset) -> Unit,
    members: List<String>
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bottom Left: Member Dropdown Pill
        MemberSwitcher(
            modifier = Modifier,
            selectedMember = selectedMember,
            onMemberSelected = onMemberSelected,
            containerColor = Color.Black.copy(alpha = 0.5f),
            contentColor = Color.White,
            icon = Icons.Default.ArrowDropDown,
            popupAlignment = Alignment.BottomStart,
            popupOffsetY = -20,
            chatStyle = true,
            members = members
        )

        // Bottom Right: Actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FloatingActionButton(
                onClick = onAddMemberClick,
                containerColor = colorScheme.surfaceVariant,
                contentColor = colorScheme.onSurfaceVariant,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Member")
            }
            FloatingActionButton(
                onClick = onUploadClick,
                containerColor = colorScheme.secondary,
                contentColor = colorScheme.onSecondary,
                shape = CircleShape,
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    val bounds = coordinates.boundsInWindow()
                    onUploadButtonPositioned(bounds.center)
                }
            ) {
                Icon(imageVector = Icons.Default.Upload, contentDescription = "Upload")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3ExpressiveApi::class,
       ExperimentalMaterial3Api::class
)
@Composable
fun UserDetailReportsList(
    selectedMember: String,
    documents: List<DocumentMetadata>,
    isLoading: Boolean,
    onNavigateToReportDetail: (String, String) -> Unit,
    onDeleteReport: (String) -> Unit,
    paddingValues: PaddingValues
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    if (documents.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.size(60.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                )
            } else {
                Text(
                    text = "No reports found for $selectedMember",
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    var showInfoSheet by remember { mutableStateOf<DocumentMetadata?>(null) }
    var reportMenuExpandedFor by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    var lastSeenDocCount by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(documents.size) {
        if (documents.isNotEmpty() && documents.size != lastSeenDocCount) {
            listState.scrollToItem(documents.size - 1)
            lastSeenDocCount = documents.size
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(paddingValues)
    ) {
        items(documents.size) { index ->
            val report = documents[index]
            val displayTime = dateFormatter.format(Date(report.uploadedAt))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 4.dp
                    ),
                    color = colorScheme.background,
                    border = BorderStroke(
                        1.dp, colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .clickable {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onNavigateToReportDetail(report.documentName, report.fileUrl)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorScheme.secondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MedicalInformation, contentDescription = null,
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                report.documentName, fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$displayTime • $selectedMember", fontSize = 13.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50)) // fully rounded
                                    .clickable {
                                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                        reportMenuExpandedFor = report.id
                                    }
                                    .padding(
                                        horizontal = 8.dp, vertical = 16.dp
                                    ), // more vertical padding, less horizontal
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }
                            MaterialTheme(
                                colorScheme = MaterialTheme.colorScheme,
                                shapes = MaterialTheme.shapes.copy(
                                    extraSmall = RoundedCornerShape(12.dp)
                                )
                            ) {
                                DropdownMenu(
                                    expanded = reportMenuExpandedFor == report.id,
                                    onDismissRequest = { reportMenuExpandedFor = null },
                                    modifier = Modifier
                                        .background(colorScheme.surface)
                                        .width(140.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Info") },
                                        onClick = {
                                            reportMenuExpandedFor = null
                                            showInfoSheet = report
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Info, contentDescription = null,
                                                tint = colorScheme.onSurface
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = colorScheme.error) },
                                        onClick = {
                                            reportMenuExpandedFor = null
                                            onDeleteReport(report.id)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete, contentDescription = null,
                                                tint = colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInfoSheet != null) {
        val report = showInfoSheet!!
        ModalBottomSheet(
            onDismissRequest = { showInfoSheet = null },
            containerColor = colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Report Info",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                val dt = Date(report.uploadedAt)
                val dateFmt = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)
                val timeFmt = SimpleDateFormat("hh:mm a", LocalLocale.current.platformLocale)

                InfoRow("File Name", report.documentName, colorScheme)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Date", dateFmt.format(dt), colorScheme)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Time", timeFmt.format(dt), colorScheme)
                Spacer(modifier = Modifier.height(8.dp))

                val emailDisplay = report.uploaderEmail.ifEmpty { "Not Available" }
                InfoRow("Uploaded by Mail", emailDisplay, colorScheme)

                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Uploaded by Name", report.linkedUser, colorScheme)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, colorScheme: ColorScheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(
            text = value, color = colorScheme.onSurface, fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}