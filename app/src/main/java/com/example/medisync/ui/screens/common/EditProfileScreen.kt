package com.example.medisync.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.utils.GlobalToastManager
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        email = FirebaseAuth.getInstance().currentUser?.email ?: ""
    }

    var editingField by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }
    val isLoading = updateState is ProfileUpdateState.Saving

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val p = (profileState as ProfileState.Success).profile
            name = "${p.firstName} ${p.lastName}".trim()
            number = p.phoneNumber
            avatarUrl = p.avatarUrl
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is ProfileUpdateState.Success) {
            GlobalToastManager.showToast(
                message = "Profile updated successfully"
            )
            viewModel.resetUpdateState()
            editingField = null
        } else if (updateState is ProfileUpdateState.Error) {
            GlobalToastManager.showToast(
                message = (updateState as ProfileUpdateState.Error).message
            )
            viewModel.resetUpdateState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column {
            TopAppBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .wrapContentWidth()
                    ) {
                        Text(
                            text = "Edit Profile",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { onBackClick() }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Profile Image Header
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                com.example.medisync.ui.components.UserAvatar(
                    avatarUrl = avatarUrl,
                    size = 100.dp,
                    borderWidth = 1.5.dp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Edit Fields Container (like Settings screen components: no rounded corners)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
                    .border(1.dp, colorScheme.outlineVariant)
            ) {
                ProfileItem(
                    icon = Icons.Default.Person,
                    label = "Name",
                    value = name,
                    isEditing = editingField == "Name",
                    editValue = editValue,
                    onEditValueChange = { editValue = it },
                    onEditClick = {
                        editingField = "Name"
                        editValue = name
                    },
                    onCancelClick = { editingField = null },
                    onSaveClick = {
                        val parts = editValue.trim()
                            .split(" ", limit = 2)
                        val fName = parts.getOrNull(0) ?: ""
                        val lName = parts.getOrNull(1) ?: ""
                        viewModel.updateProfile(fName, lName, number)
                    },
                    isLoading = isLoading && editingField == "Name"
                )

                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)

                ProfileItem(
                    icon = Icons.Default.Phone,
                    label = "Phone Number",
                    value = number,
                    isEditing = editingField == "Number",
                    editValue = editValue,
                    onEditValueChange = { editValue = it },
                    onEditClick = {
                        editingField = "Number"
                        editValue = number
                    },
                    onCancelClick = { editingField = null },
                    onSaveClick = {
                        val parts = name.trim()
                            .split(" ", limit = 2)
                        val fName = parts.getOrNull(0) ?: ""
                        val lName = parts.getOrNull(1) ?: ""
                        viewModel.updateProfile(fName, lName, editValue)
                    },
                    isLoading = isLoading && editingField == "Number"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
                    .border(1.dp, colorScheme.outlineVariant)
            ) {
                ProfileItem(
                    icon = Icons.Default.Email,
                    label = "Email Address",
                    value = email,
                    isEditing = false,
                    editValue = "",
                    onEditValueChange = {},
                    onEditClick = {
                        GlobalToastManager.showToast(
                            message = "Email editing coming soon!"
                        )
                    },
                    onCancelClick = {},
                    onSaveClick = {},
                    showEditIcon = false
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons moved inside ProfileItem

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileItem(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    editValue: String,
    onEditValueChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    isLoading: Boolean = false,
    showEditIcon: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (isEditing) {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = onEditValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp, fontWeight = FontWeight.Medium
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.secondary,
                        focusedLabelColor = colorScheme.secondary,
                        cursorColor = colorScheme.secondary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = colorScheme.secondary, modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = onSaveClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.secondary,
                                contentColor = colorScheme.onSecondary
                            )
                        ) {
                            Text("Save", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
            }
        }

        if (showEditIcon && !isEditing) {
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit $label",
                    tint = colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
