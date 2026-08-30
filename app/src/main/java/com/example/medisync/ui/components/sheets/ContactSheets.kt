package com.example.medisync.ui.components.sheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R
import com.example.medisync.model.ContactModel
import com.example.medisync.utils.GlobalToastManager
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadingSheet(
    contact: ContactModel?,
    colorScheme: ColorScheme,
    onDismiss: () -> Unit,
    onSave: (ContactModel) -> Unit
) {
    val isAdd = contact == null
    val keyboardController = LocalSoftwareKeyboardController.current

    @Suppress("DEPRECATION")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface,
        dragHandle = null
    ) {
        var title by remember { mutableStateOf(contact?.name ?: "") }
        val nameIsRequiredMsg = stringResource(R.string.name_is_required)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isAdd) "Add Heading" else "Edit Heading",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Heading Title") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.secondary,
                    focusedLabelColor = colorScheme.secondary,
                    cursorColor = colorScheme.secondary
                )
            )

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        if (isAdd) {
                            val newHeading = ContactModel(
                                id = UUID.randomUUID()
                                    .toString(),
                                name = title,
                                headingItem = true,
                                category = "Doctor"
                            )
                            onSave(newHeading)
                        } else {
                            onSave(contact.copy(name = title))
                        }
                    } else {
                        GlobalToastManager.showToast(nameIsRequiredMsg)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                Text(
                    if (isAdd) stringResource(R.string.add) else stringResource(
                        R.string.save_changes
                    ), color = colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
private fun CircularImageUploadSection(colorScheme: ColorScheme, imageResName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceVariant)
                .border(1.dp, colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val resId = when (imageResName) {
                "doctor1" -> R.drawable.doctor1
                "doctor2" -> R.drawable.doctor2
                "holding_flowers" -> R.drawable.holding_flowers
                else -> 0
            }
            if (resId != 0 && imageResName.isNotBlank()) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Contact Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.holding_flowers),
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        OutlinedButton(
            onClick = {
                GlobalToastManager.showToast("Image upload coming soon")
            }
        ) {
            Icon(
                Icons.Default.Upload, contentDescription = "Upload", modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload")
        }
    }
}

@Composable
private fun RectangularImageUploadSection(colorScheme: ColorScheme, imageResName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.surfaceVariant)
                .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            val resId = when (imageResName) {
                "doctor1" -> R.drawable.doctor1
                "doctor2" -> R.drawable.doctor2
                "holding_flowers" -> R.drawable.holding_flowers
                else -> 0
            }
            if (resId != 0 && imageResName.isNotBlank()) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Contact Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.holding_flowers),
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        OutlinedButton(
            onClick = {
                GlobalToastManager.showToast("Image upload coming soon")
            }
        ) {
            Icon(
                Icons.Default.Upload, contentDescription = "Upload", modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraContactSheet(
    contact: ContactModel?,
    colorScheme: ColorScheme,
    onDismiss: () -> Unit,
    onSave: (ContactModel) -> Unit
) {
    val isAdd = contact == null
    val keyboardController = LocalSoftwareKeyboardController.current

    @Suppress("DEPRECATION")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface,
        dragHandle = null
    ) {
        var name by remember { mutableStateOf(contact?.name ?: "") }
        var phone by remember { mutableStateOf(contact?.phone ?: "") }
        var showError by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isAdd) "Add Extra Contact" else "Edit Contact",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }

            CircularImageUploadSection(
                colorScheme = colorScheme,
                imageResName = contact?.imageResName ?: "holding_flowers"
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.secondary,
                    focusedLabelColor = colorScheme.secondary,
                    cursorColor = colorScheme.secondary
                )
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                        phone = newValue
                        showError = false
                    }
                },
                label = { Text(stringResource(R.string.phone_number)) },
                isError = showError,
                supportingText = {
                    if (showError) {
                        Text(
                            stringResource(R.string.enter_exactly_10_digits),
                            color = colorScheme.error
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.secondary,
                    focusedLabelColor = colorScheme.secondary,
                    cursorColor = colorScheme.secondary
                )
            )

            Button(
                onClick = {
                    if (phone.length != 10) {
                        showError = true
                    }
                    if (name.isNotBlank() && phone.length == 10) {
                        if (isAdd) {
                            val newContact = ContactModel(
                                id = UUID.randomUUID()
                                    .toString(),
                                name = name,
                                role = "Support",
                                experience = "",
                                phone = phone,
                                imageResName = "holding_flowers",
                                category = "ExtraContact"
                            )
                            onSave(newContact)
                        } else {
                            onSave(contact.copy(name = name, phone = phone))
                        }
                    } else if (name.isBlank()) {
                        GlobalToastManager.showToast("Name is required")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                Text(if (isAdd) "Add" else "Save Changes", color = colorScheme.onSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSheet(
    contact: ContactModel?,
    colorScheme: ColorScheme,
    onDismiss: () -> Unit,
    onSave: (ContactModel) -> Unit
) {
    val isAdd = contact == null
    val keyboardController = LocalSoftwareKeyboardController.current

    @Suppress("DEPRECATION")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface,
        dragHandle = null
    ) {
        var name by remember { mutableStateOf(contact?.name ?: "") }
        var role by remember { mutableStateOf(contact?.role ?: "Doctor") }
        var experience by remember { mutableStateOf(contact?.experience ?: "") }
        var phone by remember { mutableStateOf(contact?.phone ?: "") }
        var showError by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isAdd) "Add Card" else "Edit Doctor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }

            RectangularImageUploadSection(
                colorScheme = colorScheme,
                imageResName = contact?.imageResName ?: "doctor1"
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.secondary,
                    focusedLabelColor = colorScheme.secondary,
                    cursorColor = colorScheme.secondary
                )
            )

            OutlinedTextField(
                value = role,
                onValueChange = { role = it },
                label = { Text("Role") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.secondary,
                    focusedLabelColor = colorScheme.secondary,
                    cursorColor = colorScheme.secondary
                )
            )

            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                label = { Text(stringResource(R.string.experience)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.secondary,
                    focusedLabelColor = colorScheme.secondary,
                    cursorColor = colorScheme.secondary
                )
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                        phone = newValue
                        showError = false
                    }
                },
                label = { Text(stringResource(R.string.phone_number)) },
                isError = showError,
                supportingText = {
                    if (showError) {
                        Text(
                            stringResource(R.string.enter_exactly_10_digits),
                            color = colorScheme.error
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.secondary,
                    focusedLabelColor = colorScheme.secondary,
                    cursorColor = colorScheme.secondary
                )
            )

            Button(
                onClick = {
                    if (phone.length != 10) {
                        showError = true
                    }
                    if (name.isNotBlank() && phone.length == 10) {
                        if (isAdd) {
                            val newContact = ContactModel(
                                id = UUID.randomUUID()
                                    .toString(),
                                name = name,
                                role = role,
                                experience = experience,
                                phone = phone,
                                imageResName = "holding_flowers",
                                category = "Doctor"
                            )
                            onSave(newContact)
                        } else {
                            onSave(
                                contact.copy(
                                    name = name, role = role, experience = experience, phone = phone
                                )
                            )
                        }
                    } else if (name.isBlank()) {
                        GlobalToastManager.showToast("Name is required")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                Text(if (isAdd) "Add" else "Save Changes", color = colorScheme.onSecondary)
            }
        }
    }
}
