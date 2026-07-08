package com.example.medisync.ui.screens.common

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("Ashok Swami") }
    var number by remember { mutableStateOf("+91 98765 43210") }
    var email by remember { mutableStateOf("ashok@example.com") }

    var editingField by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .border(1.5.dp, colorScheme.primary.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
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
                        coroutineScope.launch {
                            isLoading = true
                            delay(1000.milliseconds)
                            isLoading = false
                            name = editValue
                            Toast.makeText(context, "Name updated successfully", Toast.LENGTH_SHORT).show()
                            editingField = null
                        }
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
                        coroutineScope.launch {
                            isLoading = true
                            delay(1000.milliseconds)
                            isLoading = false
                            number = editValue
                            Toast.makeText(context, "Number updated successfully", Toast.LENGTH_SHORT).show()
                            editingField = null
                        }
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
                        Toast.makeText(context, "Email editing coming soon!", Toast.LENGTH_SHORT).show()
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
            modifier = Modifier.size(24.dp).padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            if (isEditing) {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = onEditValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorScheme.primary, modifier = Modifier.size(24.dp))
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = onSaveClick,
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary
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
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
