package com.example.medisync.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R
import com.example.medisync.data.local.ContactConfig
import com.example.medisync.model.ContactModel
import com.example.medisync.ui.screens.common.ConfigViewModel
import com.example.medisync.utils.GlobalToastManager
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun AdminEditContactsScreen(
    onNavigateBack: () -> Unit,
    configViewModel: ConfigViewModel = koinViewModel()
) {
    val config by configViewModel.appConfig.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val doctor1 = config.contacts.find { it.imageResName == "doctor1" } ?: ContactModel(
        id = UUID.randomUUID()
            .toString(),
        name = "Sawai Singh",
        role = "Pharmacist",
        experience = "8 years experience",
        phone = ContactConfig.pharmacistPhones.sawaiSingh,
        imageResName = "doctor1"
    )

    val doctor2 = config.contacts.find { it.imageResName == "doctor2" } ?: ContactModel(
        id = UUID.randomUUID()
            .toString(),
        name = "Govind",
        role = "Pharmacist",
        experience = "5 years experience",
        phone = ContactConfig.pharmacistPhones.govind,
        imageResName = "doctor2"
    )

    val extraContacts =
        config.contacts.filter { it.imageResName != "doctor1" && it.imageResName != "doctor2" }

    var showAddSheet by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<ContactModel?>(null) }

    fun updateContact(updatedContact: ContactModel) {
        val existingIndex = config.contacts.indexOfFirst {
            it.id == updatedContact.id || (it.imageResName in listOf(
                "doctor1", "doctor2"
            ) && it.imageResName == updatedContact.imageResName)
        }
        val newList = config.contacts.toMutableList()
        if (existingIndex >= 0) {
            newList[existingIndex] = updatedContact
        } else {
            newList.add(updatedContact)
        }
        configViewModel.updateConfig(config.copy(contacts = newList))
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
        AdminEditContactsTopBar(colorScheme, onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Pharmacists",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = colorScheme.onBackground
            )

            PharmacistCardItem(
                contact = doctor1,
                colorScheme = colorScheme,
                onEdit = { editingContact = doctor1 }
            )

            PharmacistCardItem(
                contact = doctor2,
                colorScheme = colorScheme,
                onEdit = { editingContact = doctor2 }
            )

            SyringeDivider(colorScheme)

            ExtraContactsHeader(
                colorScheme = colorScheme,
                onAddClick = { showAddSheet = true }
            )

            if (extraContacts.isNotEmpty()) {
                extraContacts.forEach { extra ->
                    ExtraContactCardItem(
                        contact = extra,
                        colorScheme = colorScheme,
                        onEdit = { editingContact = extra },
                        onDelete = { deleteContact(extra.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showAddSheet) {
        AddContactSheet(
            colorScheme = colorScheme,
            onDismiss = { showAddSheet = false },
            onAdd = {
                updateContact(it)
                showAddSheet = false
            }
        )
    }

    if (editingContact != null) {
        EditContactSheet(
            contact = editingContact!!,
            colorScheme = colorScheme,
            onDismiss = { editingContact = null },
            onSave = {
                updateContact(it)
                editingContact = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditContactsTopBar(colorScheme: ColorScheme, onNavigateBack: () -> Unit) {
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
}

@Composable
fun PharmacistCardItem(contact: ContactModel, colorScheme: ColorScheme, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 120.dp)
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(0.dp)),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = contact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    color = colorScheme.onSurface
                )
                Text(
                    text = contact.experience, fontSize = 15.sp,
                    color = colorScheme.onSurfaceVariant
                )
                Text(text = contact.phone, fontSize = 15.sp, color = colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit, contentDescription = "Edit",
                    tint = colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun SyringeDivider(colorScheme: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.syringe),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorScheme.outlineVariant),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f), thickness = 1.dp, color = colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(id = R.drawable.syringe),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorScheme.outlineVariant),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ExtraContactsHeader(colorScheme: ColorScheme, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Extra Contacts",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = colorScheme.onBackground
        )
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add, contentDescription = "Add Contact",
                tint = colorScheme.secondary
            )
        }
    }
}

@Composable
fun ExtraContactCardItem(
    contact: ContactModel, colorScheme: ColorScheme, onEdit: () -> Unit, onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(0.dp)),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = contact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    color = colorScheme.onSurface
                )
                Text(text = contact.phone, fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit, contentDescription = "Edit",
                        tint = colorScheme.secondary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete, contentDescription = "Delete",
                        tint = colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactSheet(
    colorScheme: ColorScheme,
    onDismiss: () -> Unit,
    onAdd: (ContactModel) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surface,
        dragHandle = null
    ) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var showError by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Extra Contact", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
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
                label = { Text("Phone Number") },
                isError = showError,
                supportingText = {
                    if (showError) {
                        Text("Enter exactly 10 digits", color = colorScheme.error)
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
                        val newContact = ContactModel(
                            id = UUID.randomUUID()
                                .toString(),
                            name = name,
                            role = "Support",
                            experience = "",
                            phone = phone,
                            imageResName = "holding_flowers"
                        )
                        onAdd(newContact)
                    } else if (name.isBlank()) {
                        GlobalToastManager.showToast("Name is required")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                Text("Add", color = colorScheme.onSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactSheet(
    contact: ContactModel,
    colorScheme: ColorScheme,
    onDismiss: () -> Unit,
    onSave: (ContactModel) -> Unit
) {
    val isPharmacist = contact.imageResName == "doctor1" || contact.imageResName == "doctor2"
    val keyboardController = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surface,
        dragHandle = null
    ) {
        var name by remember { mutableStateOf(contact.name) }
        var experience by remember { mutableStateOf(contact.experience) }
        var phone by remember { mutableStateOf(contact.phone) }
        var showError by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Contact", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.secondary,
                    focusedLabelColor = colorScheme.secondary,
                    cursorColor = colorScheme.secondary
                )
            )
            if (isPharmacist) {
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Experience") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.secondary,
                        focusedLabelColor = colorScheme.secondary,
                        cursorColor = colorScheme.secondary
                    )
                )
            }
            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                        phone = newValue
                        showError = false
                    }
                },
                label = { Text("Phone Number") },
                isError = showError,
                supportingText = {
                    if (showError) {
                        Text("Enter exactly 10 digits", color = colorScheme.error)
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
                        onSave(contact.copy(name = name, experience = experience, phone = phone))
                    } else if (name.isBlank()) {
                        GlobalToastManager.showToast("Name is required")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                Text("Save Changes", color = colorScheme.onSecondary)
            }
        }
    }
}
