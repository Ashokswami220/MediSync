package com.example.medisync.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.data.local.ContactConfig
import com.example.medisync.model.ContactModel
import com.example.medisync.ui.screens.common.ConfigViewModel
import com.example.medisync.ui.screens.common.ProfileItem
import com.example.medisync.utils.GlobalToastManager
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditContactsScreen(
    onNavigateBack: () -> Unit,
    configViewModel: ConfigViewModel = koinViewModel()
) {
    val config by configViewModel.appConfig.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val doctor1 = config.contacts.find { it.imageResName == "doctor1" } ?: ContactModel(
        id = UUID.randomUUID().toString(),
        name = "Sawai Singh",
        role = "Pharmacist",
        experience = "8 years experience",
        phone = ContactConfig.pharmacistPhones.sawaiSingh,
        imageResName = "doctor1"
    )

    val doctor2 = config.contacts.find { it.imageResName == "doctor2" } ?: ContactModel(
        id = UUID.randomUUID().toString(),
        name = "Govind",
        role = "Pharmacist",
        experience = "5 years experience",
        phone = ContactConfig.pharmacistPhones.govind,
        imageResName = "doctor2"
    )
    
    val extraContacts = config.contacts.filter { it.imageResName != "doctor1" && it.imageResName != "doctor2" }

    var editingField by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    
    fun updateContact(updatedContact: ContactModel) {
        val existingIndex = config.contacts.indexOfFirst { it.id == updatedContact.id || it.imageResName == updatedContact.imageResName }
        val newList = config.contacts.toMutableList()
        if (existingIndex >= 0) {
            newList[existingIndex] = updatedContact
        } else {
            newList.add(updatedContact)
        }
        configViewModel.updateConfig(config.copy(contacts = newList))
        editingField = null
        GlobalToastManager.showToast("Contact updated")
    }
    
    fun deleteContact(contactId: String) {
        val newList = config.contacts.filter { it.id != contactId }
        configViewModel.updateConfig(config.copy(contacts = newList))
        GlobalToastManager.showToast("Contact removed")
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
                            text = "Edit Contacts",
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
                            .clickable { onNavigateBack() }
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
                .verticalScroll(rememberScrollState())
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
                    .border(1.dp, colorScheme.outlineVariant)
            ) {
                // SECTION 1: Pharmacist 1
                Text(
                    text = "Pharmacist 1",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                
                ProfileItem(
                    icon = Icons.Default.Person,
                    label = "Name",
                    value = doctor1.name,
                    isEditing = editingField == "Doc1_Name",
                    editValue = editValue,
                    onEditValueChange = { editValue = it },
                    onEditClick = {
                        editingField = "Doc1_Name"
                        editValue = doctor1.name
                    },
                    onCancelClick = { editingField = null },
                    onSaveClick = { updateContact(doctor1.copy(name = editValue.trim())) }
                )
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                ProfileItem(
                    icon = Icons.Default.Star,
                    label = "Experience",
                    value = doctor1.experience,
                    isEditing = editingField == "Doc1_Exp",
                    editValue = editValue,
                    onEditValueChange = { editValue = it },
                    onEditClick = {
                        editingField = "Doc1_Exp"
                        editValue = doctor1.experience
                    },
                    onCancelClick = { editingField = null },
                    onSaveClick = { updateContact(doctor1.copy(experience = editValue.trim())) }
                )
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                ProfileItem(
                    icon = Icons.Default.Phone,
                    label = "Phone Number",
                    value = doctor1.phone,
                    isEditing = editingField == "Doc1_Phone",
                    editValue = editValue,
                    onEditValueChange = { editValue = it },
                    onEditClick = {
                        editingField = "Doc1_Phone"
                        editValue = doctor1.phone
                    },
                    onCancelClick = { editingField = null },
                    onSaveClick = { updateContact(doctor1.copy(phone = editValue.trim())) }
                )

                HorizontalDivider(thickness = 4.dp, color = colorScheme.background)

                // SECTION 2: Pharmacist 2
                Text(
                    text = "Pharmacist 2",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                ProfileItem(
                    icon = Icons.Default.Person,
                    label = "Name",
                    value = doctor2.name,
                    isEditing = editingField == "Doc2_Name",
                    editValue = editValue,
                    onEditValueChange = { editValue = it },
                    onEditClick = {
                        editingField = "Doc2_Name"
                        editValue = doctor2.name
                    },
                    onCancelClick = { editingField = null },
                    onSaveClick = { updateContact(doctor2.copy(name = editValue.trim())) }
                )
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                ProfileItem(
                    icon = Icons.Default.Star,
                    label = "Experience",
                    value = doctor2.experience,
                    isEditing = editingField == "Doc2_Exp",
                    editValue = editValue,
                    onEditValueChange = { editValue = it },
                    onEditClick = {
                        editingField = "Doc2_Exp"
                        editValue = doctor2.experience
                    },
                    onCancelClick = { editingField = null },
                    onSaveClick = { updateContact(doctor2.copy(experience = editValue.trim())) }
                )
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                ProfileItem(
                    icon = Icons.Default.Phone,
                    label = "Phone Number",
                    value = doctor2.phone,
                    isEditing = editingField == "Doc2_Phone",
                    editValue = editValue,
                    onEditValueChange = { editValue = it },
                    onEditClick = {
                        editingField = "Doc2_Phone"
                        editValue = doctor2.phone
                    },
                    onCancelClick = { editingField = null },
                    onSaveClick = { updateContact(doctor2.copy(phone = editValue.trim())) }
                )
                
                HorizontalDivider(thickness = 4.dp, color = colorScheme.background)
                
                // SECTION 3: Extra Contacts
                if (extraContacts.isNotEmpty()) {
                    Text(
                        text = "Call Us Extra Contacts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    
                    extraContacts.forEach { extra ->
                        ProfileItem(
                            icon = Icons.Default.Person,
                            label = "Name",
                            value = extra.name,
                            isEditing = editingField == "${extra.id}_Name",
                            editValue = editValue,
                            onEditValueChange = { editValue = it },
                            onEditClick = {
                                editingField = "${extra.id}_Name"
                                editValue = extra.name
                            },
                            onCancelClick = { editingField = null },
                            onSaveClick = { updateContact(extra.copy(name = editValue.trim())) }
                        )
                        HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                        ProfileItem(
                            icon = Icons.Default.Star,
                            label = "Experience",
                            value = extra.experience,
                            isEditing = editingField == "${extra.id}_Exp",
                            editValue = editValue,
                            onEditValueChange = { editValue = it },
                            onEditClick = {
                                editingField = "${extra.id}_Exp"
                                editValue = extra.experience
                            },
                            onCancelClick = { editingField = null },
                            onSaveClick = { updateContact(extra.copy(experience = editValue.trim())) }
                        )
                        HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                        ProfileItem(
                            icon = Icons.Default.Phone,
                            label = "Phone Number",
                            value = extra.phone,
                            isEditing = editingField == "${extra.id}_Phone",
                            editValue = editValue,
                            onEditValueChange = { editValue = it },
                            onEditClick = {
                                editingField = "${extra.id}_Phone"
                                editValue = extra.phone
                            },
                            onCancelClick = { editingField = null },
                            onSaveClick = { updateContact(extra.copy(phone = editValue.trim())) }
                        )
                        
                        // Delete Button for extra contacts
                        TextButton(
                            onClick = { deleteContact(extra.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete ${extra.name}", color = colorScheme.error)
                        }
                        
                        HorizontalDivider(thickness = 2.dp, color = colorScheme.background)
                    }
                }
                
                // Add New Contact Button (always at the bottom of the card)
                TextButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Extra Contact", fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var experience by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = experience,
                        onValueChange = { experience = it },
                        label = { Text("Experience (e.g., 5 years)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            val newContact = ContactModel(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                role = "Support", // Generic role
                                experience = experience,
                                phone = phone,
                                imageResName = "holding_flowers"
                            )
                            updateContact(newContact)
                            showAddDialog = false
                        } else {
                            GlobalToastManager.showToast("Name and Phone required")
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
