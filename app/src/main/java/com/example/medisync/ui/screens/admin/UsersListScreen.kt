package com.example.medisync.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Check
import androidx.activity.compose.BackHandler
import com.example.medisync.utils.HapticHelper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.ui.navigation.TopBar
import org.koin.androidx.compose.koinViewModel

data class UserAdminModel(
    val name: String,
    val lastReportName: String,
    val lastReportTime: String,
    val hasViewed: Boolean
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun UserListScreen(
    onNavigateToUserDetail: (String) -> Unit = {},
    viewModel: UserListViewModel = koinViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Default") }
    
    val users by viewModel.usersState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val selectedUsers = remember { mutableStateListOf<String>() }

    if (selectedUsers.isNotEmpty()) {
        BackHandler {
            selectedUsers.clear()
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
        if (selectedUsers.isNotEmpty()) {
            SelectionTopBar(
                selectedCount = selectedUsers.size,
                onClearSelection = { selectedUsers.clear() }
            )
        } else {
            TopBar(
                title = "Users",
                showName = false,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchActiveChange = { isSearchActive = it },
                onSearchQueryChange = { searchQuery = it },
                extraActions = {
                    Box {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { showFilterMenu = true }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = "Filter",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        MaterialTheme(
                            colorScheme = MaterialTheme.colorScheme,
                            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false },
                                modifier = Modifier
                                    .background(colorScheme.surface)
                                    .width(140.dp),
                                offset = DpOffset(62.dp, 8.dp)
                            ) {
                                val options = listOf("Default", "A-Z", "Z-A", "Newest first", "Oldest first")
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        modifier = Modifier.height(40.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                        text = { Text(option) },
                                        onClick = {
                                            selectedFilter = option
                                            showFilterMenu = false
                                        },
                                        trailingIcon = if (selectedFilter == option) {
                                            { Icon(Icons.Default.Check, contentDescription = "Selected") }
                                        } else null
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        if (!isSearchActive) {
            HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
        }

        if (isLoading && users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No users found", color = colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            items(users) { user ->
                val isSelected = selectedUsers.contains(user.name)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) colorScheme.secondary.copy(alpha = 0.15f) else Color.Transparent)
                        .combinedClickable(
                            onClick = {
                                if (selectedUsers.isNotEmpty()) {
                                    if (isSelected) selectedUsers.remove(user.name)
                                    else selectedUsers.add(user.name)
                                } else {
                                    onNavigateToUserDetail(user.name)
                                }
                            },
                            onLongClick = {
                                if (isSelected) selectedUsers.remove(user.name)
                                else selectedUsers.add(user.name)
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Avatar
                    Box(modifier = Modifier.size(50.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(colorScheme.secondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User",
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = colorScheme.secondary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(20.dp)
                                    .background(Color.White, CircleShape)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Name and Subtitle
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = user.lastReportTime,
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (user.hasViewed) Icons.Default.DoneAll else Icons.Default.Check,
                                contentDescription = if (user.hasViewed) "Viewed" else "Sent",
                                tint = if (user.hasViewed) colorScheme.primary else colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.lastReportName,
                                fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 82.dp, end = 16.dp),
                    thickness = 0.5.dp,
                    color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
        }
    }
}

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var showSelectionMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 12.dp, end = 4.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Clear Selection",
                tint = colorScheme.onSurface,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { 
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onClearSelection() 
                    }
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = selectedCount.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { showSelectionMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme,
                    shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp))
                ) {
                    DropdownMenu(
                        expanded = showSelectionMenu,
                        onDismissRequest = { showSelectionMenu = false },
                        modifier = Modifier.background(colorScheme.surface),
                        offset = DpOffset(0.dp, 8.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Users", color = colorScheme.error) },
                            onClick = { showSelectionMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = colorScheme.error) }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Data", color = colorScheme.error) },
                            onClick = { showSelectionMenu = false },
                            leadingIcon = { Icon(Icons.Default.ClearAll, contentDescription = null, tint = colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}