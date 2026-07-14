package com.example.medisync.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.components.MemberSwitcher
import com.example.medisync.utils.HapticHelper
import androidx.core.net.toUri

@Composable
fun UserDetailScreen(
    userName: String = "Ashok",
    onBackClick: () -> Unit = {},
    onNavigateToReportDetail: () -> Unit = {}
) {
    val members = listOf(userName, "John Doe", "Jane Doe")
    var selectedMember by remember { mutableStateOf(members[0]) }

    Scaffold(
        topBar = {
            UserDetailTopBar(
                userName = userName,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            UserDetailBottomBar(
                selectedMember = selectedMember,
                onMemberSelected = { selectedMember = it }
            )
        }
    ) { paddingValues ->
        UserDetailReportsList(
            selectedMember = selectedMember,
            onNavigateToReportDetail = onNavigateToReportDetail,
            paddingValues = paddingValues
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailTopBar(
    userName: String,
    onBackClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var showTopMenu by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            modifier = Modifier.padding(horizontal = 4.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User",
                            tint = Color.White
                        )
                    }
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
                    android.content.Intent(
                        android.content.Intent.ACTION_DIAL,
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
                                onClick = { showTopMenu = false },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete, contentDescription = null,
                                        tint = colorScheme.error
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Data", color = colorScheme.error) },
                                onClick = { showTopMenu = false },
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
    onMemberSelected: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

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
            chatStyle = true
        )

        // Bottom Right: Upload Button
        FloatingActionButton(
            onClick = { HapticHelper.trigger(context, HapticHelper.Type.MEDIUM) },
            containerColor = colorScheme.secondary,
            contentColor = colorScheme.onSecondary,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Upload, contentDescription = "Upload")
        }
    }
}

@Composable
fun UserDetailReportsList(
    selectedMember: String,
    onNavigateToReportDetail: () -> Unit,
    paddingValues: PaddingValues
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val reports = listOf(
        Triple("Comprehensive Metabolic Panel", "Oct 24, 2026 • Lab Results", Icons.Default.Science),
        Triple("Chest X-Ray", "Oct 12, 2026 • Imaging", Icons.Default.MedicalInformation),
        Triple("Prescription Renewal", "Sep 30, 2026 • Clinical Notes", Icons.Default.Science)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(paddingValues)
    ) {
        items(reports.size) { index ->
            val report = reports[index]
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
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .clickable {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onNavigateToReportDetail()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
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
                                report.third, contentDescription = null,
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                report.first, fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${report.second} • $selectedMember", fontSize = 13.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50)) // fully rounded
                                .clickable { HapticHelper.trigger(context, HapticHelper.Type.LIGHT) }
                                .padding(horizontal = 8.dp, vertical = 16.dp), // more vertical padding, less horizontal
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}