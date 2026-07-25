@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)
package com.example.medisync.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.model.UserProfile
import com.example.medisync.ui.components.AddMemberBottomSheet
import com.example.medisync.ui.components.UserAvatar
import com.example.medisync.utils.GlobalToastManager
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userUid: String,
    onBackClick: () -> Unit,
    viewModel: AdminUserProfileViewModel = koinViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(userUid) {
        viewModel.loadUser(userUid)
    }

    val userProfile by viewModel.userProfile.collectAsState()

    var showEditSheet by remember { mutableStateOf(false) }
    var editField by remember { mutableStateOf("") }
    var editValue by remember { mutableStateOf("") }
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    var showAddMemberDialog by remember { mutableStateOf(false) }

    if (userProfile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator(
                color = colorScheme.secondary,
                polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
            )
        }
        return
    }

    val profile = userProfile!!

    val handleSaveField = { field: String, value: String ->
        if (field == "fullName") {
            val parts = value.trim().split(" ", limit = 2)
            val first = parts.getOrNull(0) ?: ""
            val last = parts.getOrNull(1) ?: ""
            viewModel.updateUserFields(userUid, mapOf("firstName" to first, "lastName" to last)) { _, msg ->
                GlobalToastManager.showToast(message = msg)
                showEditSheet = false
            }
        } else {
            viewModel.updateUserField(userUid, field, value) { _, msg ->
                GlobalToastManager.showToast(message = msg)
                showEditSheet = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${profile.firstName} ${profile.lastName}".trim().ifEmpty { "User Profile" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground
                )
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                UserProfileHeader(
                    profile = profile,
                    onEditClick = { field, value ->
                        editField = field
                        editValue = value
                        showEditSheet = true
                    }
                )
            }

            item {
                UserProfileVitals(
                    profile = profile,
                    onEditClick = { field, value ->
                        editField = field
                        editValue = value
                        showEditSheet = true
                    }
                )
            }

            item {
                UserProfileMembersHeader(onAddMemberClick = { showAddMemberDialog = true })
            }

            items(profile.members) { member ->
                MemberEditItem(
                    name = member,
                    onDelete = {
                        val updatedList = profile.members.toMutableList()
                        updatedList.remove(member)
                        viewModel.updateUserField(userUid, "members", updatedList) { _, m ->
                            GlobalToastManager.showToast(message = m)
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showAddMemberDialog) {
        AddMemberBottomSheet(
            onDismiss = { showAddMemberDialog = false },
            onSave = { newMemberName ->
                if (newMemberName.isNotBlank()) {
                    val updatedList = profile.members + newMemberName.trim()
                    viewModel.updateUserField(userUid, "members", updatedList) { _, m ->
                        GlobalToastManager.showToast(message = m)
                    }
                }
                showAddMemberDialog = false
            }
        )
    }

    if (showEditSheet) {
        EditFieldBottomSheet(
            sheetState = sheetState,
            editField = editField,
            editValue = editValue,
            onValueChange = { editValue = it },
            onSave = { handleSaveField(editField, editValue) },
            onDismiss = { showEditSheet = false }
        )
    }
}

@Composable
fun UserProfileHeader(
    profile: UserProfile,
    onEditClick: (String, String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar separated from the card
        UserAvatar(
            avatarUrl = profile.avatarUrl,
            size = 110.dp,
            backgroundColor = colorScheme.secondary.copy(alpha = 0.1f),
            iconTint = colorScheme.secondary,
            borderWidth = 0.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card containing details
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colorScheme.surface)
                .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                ProfileDetailRow(
                    icon = Icons.Default.Person,
                    text = "${profile.firstName} ${profile.lastName}".trim(),
                    onEditClick = { onEditClick("fullName", "${profile.firstName} ${profile.lastName}".trim()) }
                )

                // Number
                ProfileDetailRow(
                    icon = Icons.Default.Phone,
                    text = profile.phoneNumber.ifEmpty { "No phone number" },
                    onEditClick = { onEditClick("phoneNumber", profile.phoneNumber) }
                )

                // Email
                ProfileDetailRow(
                    icon = Icons.Default.Email,
                    text = profile.email.ifEmpty { "No email provided" },
                    onEditClick = null
                )
            }
        }
    }
}

@Composable
fun ProfileDetailRow(
    icon: ImageVector,
    text: String,
    onEditClick: (() -> Unit)?
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp, // Same size for all
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (onEditClick != null) {
            IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp), tint = colorScheme.outline)
            }
        }
    }
}

@Composable
fun UserProfileVitals(
    profile: UserProfile,
    onEditClick: (String, String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Vitals",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VitalCard(
                title = "Blood Pressure",
                value = profile.bloodPressure,
                unit = "mmHg",
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f),
                onClick = { onEditClick("bloodPressure", profile.bloodPressure) }
            )
            VitalCard(
                title = "Blood Sugar",
                value = profile.bloodSugar,
                unit = "mg/dL",
                icon = Icons.Default.WaterDrop,
                modifier = Modifier.weight(1f),
                onClick = { onEditClick("bloodSugar", profile.bloodSugar) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VitalCard(
                title = "Blood Type",
                value = profile.bloodType,
                unit = "",
                icon = Icons.Default.Bloodtype,
                modifier = Modifier.weight(1f),
                onClick = { onEditClick("bloodType", profile.bloodType) }
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun UserProfileMembersHeader(onAddMemberClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Members",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onAddMemberClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Member", tint = colorScheme.secondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldBottomSheet(
    sheetState: SheetState,
    editField: String,
    editValue: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            val displayFieldName = editField.replace(Regex("([a-z])([A-Z]+)"), "$1 $2").replaceFirstChar { it.uppercase() }
            Text(
                text = "Edit $displayFieldName",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = editValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.secondary,
                    focusedLabelColor = colorScheme.secondary
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSecondary)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun VitalCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colorScheme.surface)
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = colorScheme.secondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 13.sp, color = colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value.ifEmpty { "--" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                if (value.isNotEmpty() && unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MemberEditItem(
    name: String,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surface)
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Member", tint = colorScheme.error)
        }
    }
}
