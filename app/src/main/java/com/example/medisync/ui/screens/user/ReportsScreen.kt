package com.example.medisync.ui.screens.user

import kotlinx.coroutines.delay

import com.example.medisync.model.DocumentMetadata
import com.example.medisync.R
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.example.medisync.ui.components.CustomToast
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.pullrefresh.pullRefreshIndicatorTransform
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.navigation.TopBar
import com.example.medisync.utils.HapticHelper
import com.example.medisync.repo.AuthRepository
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserReportsScreen(
    viewModel: ReportsViewModel = koinViewModel(),
    onNavigateToReportDetail: (String, String) -> Unit = { _, _ -> },
    selectedMember: String = "User",
    onMemberSelected: (String) -> Unit = {},
    members: List<String> = listOf("User"),
    onNavigateToLogin: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val reportsState by viewModel.reportsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDocuments()
    }

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Newest First") }

    var isRefreshing by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.refresh()
            delay(3000.milliseconds)
            isRefreshing = false
            showToast = true
        }
    }

    if (isSearchActive) {
        BackHandler {
            isSearchActive = false
            searchQuery = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        TopBar(
            title = "Reports",
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            onSearchActiveChange = { active ->
                isSearchActive = active
                if (active) selectedCategory = "All"
                if (!active) searchQuery = ""
            },
            onSearchQueryChange = { searchQuery = it },
            selectedMember = selectedMember,
            onMemberSelected = onMemberSelected,
            members = members
        )

        val authRepo: AuthRepository = koinInject()
        val currentUser = authRepo.getCurrentUserSync()
        val isLoggedIn = currentUser != null

        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { isRefreshing = true }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                ReportsFilterRow(
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    showSortMenu = showSortMenu,
                    onShowSortMenuChange = { showSortMenu = it },
                    selectedSort = selectedSort,
                    onSortSelected = { selectedSort = it },
                    colorScheme = colorScheme,
                    context = context
                )

                if (!isLoggedIn) {
                    NotLoggedInState(
                        onNavigateToLogin = onNavigateToLogin,
                        colorScheme = colorScheme
                    )
                } else {
                    when (val state = reportsState) {
                        is ReportsState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingIndicator(
                                    modifier = Modifier.size(60.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                                )
                            }
                        }
                        is ReportsState.Empty -> {
                            EmptyReportsState(colorScheme = colorScheme)
                        }
                        is ReportsState.Success -> {
                            val mainUser = members.firstOrNull() ?: "User"
                            val isMainUser = selectedMember == mainUser
                            val otherMembers = members.filter { it != mainUser }
                            
                            val filteredDocuments = state.documents.filter { doc ->
                                if (isMainUser) {
                                    // Fallback for old/legacy reports: if it's the main user, 
                                    // show reports that don't explicitly belong to sub-members.
                                    !otherMembers.contains(doc.linkedMember)
                                } else {
                                    doc.linkedMember == selectedMember
                                }
                            }
                            if (filteredDocuments.isEmpty()) {
                                EmptyReportsState(colorScheme = colorScheme)
                            } else {
                                ReportsList(
                                    filteredDocuments = filteredDocuments,
                                    onNavigateToReportDetail = onNavigateToReportDetail,
                                    colorScheme = colorScheme
                                )
                            }
                        }
                        is ReportsState.Error -> {
                            // Show error or empty state
                            EmptyReportsState(colorScheme = colorScheme)
                        }
                    }
                }

                // Add padding at the bottom for NavBar
                Spacer(modifier = Modifier.height(120.dp))
            }
            
            // Custom Pull to Refresh Indicator
            if (pullRefreshState.progress > 0 || isRefreshing) {
                val scale = if (isRefreshing) 1f else (pullRefreshState.progress * 1.2f).coerceIn(0f, 1.2f)
                val alpha = if (isRefreshing) 1f else pullRefreshState.progress.coerceIn(0f, 1f)
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .pullRefreshIndicatorTransform(pullRefreshState)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                ) {
                    LoadingIndicator(
                        modifier = Modifier.size(60.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                    )
                }
            }
            
            // Custom Toast Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                CustomToast(
                    message = "Reports updated",
                    isVisible = showToast,
                    onDismiss = { showToast = false }
                )
            }
        }
    }
}

@Composable
fun ReportsFilterRow(
    searchQuery: String,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    selectedSort: String,
    onSortSelected: (String) -> Unit,
    colorScheme: ColorScheme,
    context: Context
) {
    if (searchQuery.isEmpty()) {
        val categories = listOf("All", "Lab", "Blood", "Others")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scrolling categories
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) colorScheme.secondary else colorScheme.surface
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else colorScheme.outlineVariant,
                                RoundedCornerShape(50)
                            )
                            .clickable {
                                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                onCategorySelected(category)
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.width(4.dp))

            // Filter Menu
            Box {
                IconButton(onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    onShowSortMenuChange(true)
                }) {
                    Icon(
                        Icons.Default.FilterAlt,
                        contentDescription = "Filter",
                        tint = colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { onShowSortMenuChange(false) },
                    modifier = Modifier.background(colorScheme.surface)
                ) {
                    listOf("Newest First", "Oldest First").forEach { sortOption ->
                        DropdownMenuItem(
                            text = { Text(sortOption, color = colorScheme.onSurface) },
                            onClick = {
                                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                onSortSelected(sortOption)
                                onShowSortMenuChange(false)
                            },
                            trailingIcon = {
                                if (selectedSort == sortOption) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = colorScheme.secondary
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotLoggedInState(
    onNavigateToLogin: () -> Unit,
    colorScheme: ColorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
            .padding(top = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(240.dp)
                    .background(
                        Color.Black.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(32.dp)
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.person_with_doc),
                contentDescription = "Login Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(100.dp),
            colors = outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                1.dp, colorScheme.outlineVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Login to see reports", color = colorScheme.onSurface, fontSize = 16.sp
            )
        }
    }
}

@Composable
fun EmptyReportsState(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_doc),
            contentDescription = "No reports",
            modifier = Modifier.size(110.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Reports available",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Try changing member",
            fontSize = 16.sp,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReportsList(
    filteredDocuments: List<DocumentMetadata>,
    onNavigateToReportDetail: (String, String) -> Unit,
    colorScheme: ColorScheme
) {
    val dateFormatter =
        SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)
    val groupFormatter = SimpleDateFormat("yyyy", LocalLocale.current.platformLocale)

    val groupedReports = filteredDocuments.groupBy { doc ->
        groupFormatter.format(Date(doc.uploadedAt))
    }

    for ((year, yearReports) in groupedReports) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = year,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                fontSize = 14.sp
            )
            Text(
                text = "${yearReports.size} reports",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        // List Items
        for (report in yearReports) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigateToReportDetail(
                            report.documentName, report.fileUrl
                        )
                    }
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                colorScheme.secondary.copy(alpha = 0.07f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MedicalInformation,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            report.documentName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${
                                dateFormatter.format(
                                    Date(report.uploadedAt)
                                )
                            } • ${report.linkedMember}", fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Details",
                        tint = colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(
                    color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}