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
        // Custom Top Bar with iOS Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp),
                    tint = colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Edit Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
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
                        .border(2.dp, colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = colorScheme.primary,
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
                // Name Field
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
                    }
                )

                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)

                // Number Field
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
                    }
                )

                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)

                // Email Field (Read Only)
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
                    showEditIcon = false
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom Action Buttons
            if (editingField != null) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorScheme.primary)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingField = null },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    delay(1000) // Simulate network request
                                    isLoading = false
                                    if (editingField == "Name") {
                                        name = editValue
                                    } else if (editingField == "Number") {
                                        number = editValue
                                    }
                                    Toast.makeText(context, "$editingField updated successfully", Toast.LENGTH_SHORT).show()
                                    editingField = null
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary
                            )
                        ) {
                            Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
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
    showEditIcon: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        
        if (isEditing) {
            OutlinedTextField(
                value = editValue,
                onValueChange = onEditValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium)
            )
        } else {
            Column(modifier = Modifier.weight(1f)) {
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
