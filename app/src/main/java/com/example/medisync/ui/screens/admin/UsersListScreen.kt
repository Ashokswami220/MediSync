@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.example.medisync.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R
import com.example.medisync.ui.components.ClearAdminDataDialog
import com.example.medisync.ui.components.DeleteUsersDialog
import com.example.medisync.ui.components.TopBar
import com.example.medisync.ui.components.UserAvatar
import com.example.medisync.ui.components.sheets.CreateUserBottomSheet
import com.example.medisync.utils.GlobalToastManager
import com.example.medisync.utils.HapticHelper
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserListScreen(
    onNavigateToUserDetail: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    viewModel: UserListViewModel = koinViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Default") }

    val users by viewModel.usersState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    val dataUpdatingMsg = stringResource(R.string.data_is_updating)
    LaunchedEffect(isLoading) {
        if (isLoading) {
            GlobalToastManager.showToast(dataUpdatingMsg)
        }
    }

    val sortedUsers = remember(users, selectedFilter, searchQuery) {
        var result = users
        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phoneNumber.contains(searchQuery) ||
                        it.email.contains(searchQuery, ignoreCase = true)
            }
        }
        when (selectedFilter) {
            "A-Z" -> result.sortedBy { it.name }
            "Z-A" -> result.sortedByDescending { it.name }
            "Oldest first" -> result.sortedBy { it.timestamp }
            "Newest first", "Default" -> result.sortedByDescending { it.timestamp }
            else -> result
        }
    }

    val selectedUsers = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

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

    var showCreateUserSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
        ) {
            if (selectedUsers.isNotEmpty()) {
                SelectionTopBar(
                    selectedCount = selectedUsers.size,
                    onClearSelection = { selectedUsers.clear() },
                    onDeleteClick = { showDeleteDialog = true },
                    onClearDataClick = { showClearDataDialog = true }
                )
            } else {
                TopBar(
                    title = stringResource(R.string.users),
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
                                    contentDescription = stringResource(R.string.filter),
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            MaterialTheme(
                                colorScheme = MaterialTheme.colorScheme,
                                shapes = MaterialTheme.shapes.copy(
                                    extraSmall = RoundedCornerShape(12.dp)
                                )
                            ) {
                                DropdownMenu(
                                    expanded = showFilterMenu,
                                    onDismissRequest = { showFilterMenu = false },
                                    modifier = Modifier
                                        .background(colorScheme.surface)
                                        .width(140.dp),
                                    offset = DpOffset(62.dp, 8.dp)
                                ) {
                                    val options =
                                        listOf(
                                            "Default", "A-Z", "Z-A", "Newest first", "Oldest first"
                                        )
                                    options.forEach { option ->
                                        DropdownMenuItem(
                                            modifier = Modifier.height(40.dp),
                                            contentPadding = PaddingValues(
                                                horizontal = 16.dp, vertical = 0.dp
                                            ),
                                            text = { Text(option) },
                                            onClick = {
                                                selectedFilter = option
                                                showFilterMenu = false
                                            },
                                            trailingIcon = if (selectedFilter == option) {
                                                {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = stringResource(
                                                            R.string.selected
                                                        )
                                                    )
                                                }
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
                    LoadingIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                    )
                }
            } else if (users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.no_users_found),
                        color = colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
                ) {
                    items(sortedUsers) { user ->
                        val isSelected = selectedUsers.contains(user.uid)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) colorScheme.secondary.copy(
                                        alpha = 0.15f
                                    ) else Color.Transparent
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (selectedUsers.isNotEmpty()) {
                                            if (isSelected) selectedUsers.remove(user.uid)
                                            else selectedUsers.add(user.uid)
                                        } else {
                                            onNavigateToUserDetail(user.uid)
                                        }
                                    },
                                    onLongClick = {
                                        if (isSelected) selectedUsers.remove(user.uid)
                                        else selectedUsers.add(user.uid)
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User Avatar with larger clickable area
                            Box(
                                modifier = Modifier
                                    .clickable { onNavigateToUserProfile(user.uid) }
                                    .padding(end = 12.dp, top = 8.dp, bottom = 8.dp)
                            ) {
                                Box(modifier = Modifier.size(50.dp)) {
                                    if (user.isPreRegistered) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(colorScheme.secondary.copy(0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PersonAdd,
                                                contentDescription = "Pre-Registered User",
                                                tint = colorScheme.secondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    } else {
                                        UserAvatar(
                                            avatarUrl = user.avatarUrl,
                                            size = 50.dp,
                                            borderWidth = 0.dp
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = stringResource(R.string.selected),
                                            tint = colorScheme.secondary,
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(20.dp)
                                                .background(Color.White, CircleShape)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

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

        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreateUserSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .padding(bottom = 90.dp), // Adjust for nav bar if needed
            shape = CircleShape,
            containerColor = colorScheme.secondary,
            contentColor = colorScheme.onSecondary
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = stringResource(R.string.pre_register_user)
            )
        }
    }

    if (showCreateUserSheet) {
        CreateUserBottomSheet(
            onDismiss = { showCreateUserSheet = false },
            onCreate = { firstName, lastName, contactMethod, contactValue ->
                viewModel.createPlaceholderUser(
                    firstName, lastName, contactMethod, contactValue
                ) { success, msg ->
                    GlobalToastManager.showToast(message = msg)
                    if (success) {
                        showCreateUserSheet = false
                        viewModel.fetchUsers() // Refresh list
                    }
                }
            },
            isLoading = isLoading
        )
    }

    if (showDeleteDialog) {
        DeleteUsersDialog(
            userCount = selectedUsers.size,
            onConfirm = {
                viewModel.deleteSelectedUsers(selectedUsers.toList()) { success, msg ->
                    GlobalToastManager.showToast(message = msg)
                    if (success) {
                        selectedUsers.clear()
                    }
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showClearDataDialog) {
        ClearAdminDataDialog(

            onConfirm = {
                viewModel.clearDataForSelectedUsers(selectedUsers.toList()) { success, msg ->
                    GlobalToastManager.showToast(message = msg)
                    if (success) {
                        selectedUsers.clear()
                    }
                }
            },
            onDismiss = { showClearDataDialog = false }
        )
    }
}

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDeleteClick: () -> Unit,
    onClearDataClick: () -> Unit
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
                contentDescription = stringResource(R.string.clear_selection),
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
                        contentDescription = stringResource(R.string.more),
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
                            text = {
                                Text(
                                    stringResource(R.string.delete_users), color = colorScheme.error
                                )
                            },
                            onClick = {
                                showSelectionMenu = false
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
                            text = {
                                Text(
                                    stringResource(R.string.clear_data), color = colorScheme.error
                                )
                            },
                            onClick = {
                                showSelectionMenu = false
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
        }
    }
}