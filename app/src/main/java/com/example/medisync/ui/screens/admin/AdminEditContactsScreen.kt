package com.example.medisync.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R
import com.example.medisync.data.local.ContactConfig
import com.example.medisync.model.ContactModel
import com.example.medisync.ui.components.sheets.DoctorSheet
import com.example.medisync.ui.components.sheets.ExtraContactSheet
import com.example.medisync.ui.components.sheets.HeadingSheet
import com.example.medisync.ui.screens.common.ConfigViewModel
import com.example.medisync.utils.GlobalToastManager
import com.example.medisync.utils.HapticHelper
import org.koin.androidx.compose.koinViewModel

@Composable
fun AdminEditContactsScreen(
    onNavigateBack: () -> Unit,
    configViewModel: ConfigViewModel = koinViewModel()
) {
    val config by configViewModel.appConfig.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    
    val doctorContacts = config.contacts.filter { it.category == "Doctor" }.toMutableList()
    val doctor1 = doctorContacts.find { it.id == "predefined_doctor1" } ?: ContactModel(
        id = "predefined_doctor1",
        name = "Dr. Sawai Singh",
        role = "Doctor",
        experience = "10 years experience",
        phone = ContactConfig.pharmacistPhones.sawaiSingh,
        imageResName = "doctor1",
        category = "Doctor"
    ).also { doctorContacts.add(0, it) }

    val doctor2 = doctorContacts.find { it.id == "predefined_doctor2" } ?: ContactModel(
        id = "predefined_doctor2",
        name = "Dr. Govind Prasad Sau",
        role = "Doctor",
        experience = "10 years experience",
        phone = ContactConfig.pharmacistPhones.govind,
        imageResName = "doctor2",
        category = "Doctor"
    ).also { doctorContacts.add(1, it) }

    val extraContacts = config.contacts.filter { it.category == "ExtraContact" }

    var doctorSheetContact by remember { mutableStateOf<ContactModel?>(null) }
    var showDoctorSheet by remember { mutableStateOf(false) }

    var extraSheetContact by remember { mutableStateOf<ContactModel?>(null) }
    var showExtraSheet by remember { mutableStateOf(false) }
    
    var headingSheetContact by remember { mutableStateOf<ContactModel?>(null) }
    var showHeadingSheet by remember { mutableStateOf(false) }

    val contactUpdatedMsg = stringResource(R.string.contact_updated)
    val contactRemovedMsg = stringResource(R.string.contact_removed)

    fun updateContact(updatedContact: ContactModel) {
        val existingIndex = config.contacts.indexOfFirst {
            it.id == updatedContact.id
        }
        val newList = config.contacts.toMutableList()
        if (existingIndex >= 0) {
            newList[existingIndex] = updatedContact
        } else {
            newList.add(updatedContact)
        }
        configViewModel.updateConfig(config.copy(contacts = newList))
        GlobalToastManager.showToast(contactUpdatedMsg)
    }

    fun deleteContact(contactId: String) {
        val newList = config.contacts.filter { it.id != contactId }
        configViewModel.updateConfig(config.copy(contacts = newList))
        GlobalToastManager.showToast(contactRemovedMsg)
    }

    fun moveContact(contactId: String, direction: Int, category: String) {
        val categoryItems = config.contacts.filter { it.category == category }.toMutableList()
        // If doctors, ensure we have doctor1 and doctor2 in categoryItems if they aren't already there (they should be)
        if (category == "Doctor") {
            if (categoryItems.none { it.id == "predefined_doctor1" }) categoryItems.add(0, doctor1)
            if (categoryItems.none { it.id == "predefined_doctor2" }) categoryItems.add(1, doctor2)
        }
        
        val index = categoryItems.indexOfFirst { it.id == contactId }
        if (index >= 0 && index + direction in categoryItems.indices) {
            val temp = categoryItems[index]
            categoryItems[index] = categoryItems[index + direction]
            categoryItems[index + direction] = temp
            
            val newList = config.contacts.filter { it.category != category }.toMutableList()
            newList.addAll(categoryItems)
            configViewModel.updateConfig(config.copy(contacts = newList))
            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
        }
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
            // New Add Title and Add Card Buttons at the top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { showHeadingSheet = true; headingSheetContact = null },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Title", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Title")
                }
                
                Button(
                    onClick = { showDoctorSheet = true; doctorSheetContact = null },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Card", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Card")
                }
            }

            Text(
                text = stringResource(R.string.doctors),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = colorScheme.onBackground
            )

            doctorContacts.forEachIndexed { _, doc ->
                if (doc.headingItem) {
                    HeadingItem(
                        contact = doc,
                        colorScheme = colorScheme,
                        onEdit = { showHeadingSheet = true; headingSheetContact = doc },
                        onDelete = { deleteContact(doc.id) },
                        onMoveUp = { moveContact(doc.id, -1, "Doctor") },
                        onMoveDown = { moveContact(doc.id, 1, "Doctor") }
                    )
                } else {
                    val isPredefined = doc.id == "predefined_doctor1" || doc.id == "predefined_doctor2"
                    PharmacistCardItem(
                        contact = doc,
                        colorScheme = colorScheme,
                        onEdit = { showDoctorSheet = true; doctorSheetContact = doc },
                        onDelete = { deleteContact(doc.id) },
                        onMoveUp = { moveContact(doc.id, -1, "Doctor") },
                        onMoveDown = { moveContact(doc.id, 1, "Doctor") },
                        isPredefined = isPredefined
                    )
                }
            }

            SyringeDivider(colorScheme)

            ExtraContactsHeader(
                colorScheme = colorScheme,
                onAddClick = { showExtraSheet = true; extraSheetContact = null }
            )

            if (extraContacts.isNotEmpty()) {
                extraContacts.forEachIndexed { _, extra ->
                    if (extra.headingItem) {
                        HeadingItem(
                            contact = extra,
                            colorScheme = colorScheme,
                            onEdit = { showHeadingSheet = true; headingSheetContact = extra },
                            onDelete = { deleteContact(extra.id) },
                            onMoveUp = { moveContact(extra.id, -1, "ExtraContact") },
                            onMoveDown = { moveContact(extra.id, 1, "ExtraContact") }
                        )
                    } else {
                        ExtraContactCardItem(
                            contact = extra,
                            colorScheme = colorScheme,
                            onEdit = { showExtraSheet = true; extraSheetContact = extra },
                            onDelete = { deleteContact(extra.id) },
                            onMoveUp = { moveContact(extra.id, -1, "ExtraContact") },
                            onMoveDown = { moveContact(extra.id, 1, "ExtraContact") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showExtraSheet) {
        ExtraContactSheet(
            contact = extraSheetContact,
            colorScheme = colorScheme,
            onDismiss = { showExtraSheet = false },
            onSave = {
                updateContact(it)
                showExtraSheet = false
            }
        )
    }
    
    if (showHeadingSheet) {
        HeadingSheet(
            contact = headingSheetContact,
            colorScheme = colorScheme,
            onDismiss = { showHeadingSheet = false },
            onSave = {
                updateContact(it)
                showHeadingSheet = false
            }
        )
    }

    if (showDoctorSheet) {
        DoctorSheet(
            contact = doctorSheetContact,
            colorScheme = colorScheme,
            onDismiss = { showDoctorSheet = false },
            onSave = {
                updateContact(it)
                showDoctorSheet = false
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
                        text = stringResource(R.string.edit_contacts),
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
                        contentDescription = stringResource(R.string.back),
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
fun PharmacistCardItem(
    contact: ContactModel, 
    colorScheme: ColorScheme, 
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isPredefined: Boolean
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                    text = contact.role, fontSize = 15.sp,
                    color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium
                )
                Text(
                    text = contact.experience, fontSize = 15.sp,
                    color = colorScheme.onSurfaceVariant
                )
                Text(text = contact.phone, fontSize = 15.sp, color = colorScheme.onSurfaceVariant)
            }
            var dragAmountAccumulator by remember { mutableFloatStateOf(0f) }
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    onEdit()
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = colorScheme.secondary
                    )
                }
                if (!isPredefined) {
                    IconButton(onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                        onDelete()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = colorScheme.error
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Drag to reorder",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(top = 8.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = { dragAmountAccumulator = 0f },
                                onDragEnd = { dragAmountAccumulator = 0f },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAmountAccumulator += dragAmount
                                    if (dragAmountAccumulator > 60f) {
                                        onMoveDown()
                                        dragAmountAccumulator = 0f
                                    } else if (dragAmountAccumulator < -60f) {
                                        onMoveUp()
                                        dragAmountAccumulator = 0f
                                    }
                                }
                            )
                        }
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
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.extra_contacts),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = colorScheme.onBackground
        )
        IconButton(onClick = {
            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
            onAddClick()
        }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_contact),
                tint = colorScheme.secondary
            )
        }
    }
}

@Composable
fun HeadingItem(
    contact: ContactModel,
    colorScheme: ColorScheme,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    var dragAmountAccumulator by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = contact.name,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                onEdit()
            }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = colorScheme.secondary
                )
            }
            IconButton(onClick = {
                HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                onDelete()
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = colorScheme.error
                )
            }
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = "Drag to reorder",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(32.dp)
                    .padding(start = 8.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { dragAmountAccumulator = 0f },
                            onDragEnd = { dragAmountAccumulator = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragAmountAccumulator += dragAmount
                                if (dragAmountAccumulator > 60f) {
                                    onMoveDown()
                                    dragAmountAccumulator = 0f
                                } else if (dragAmountAccumulator < -60f) {
                                    onMoveUp()
                                    dragAmountAccumulator = 0f
                                }
                            }
                        )
                    }
            )
        }
    }
}

@Composable
fun ExtraContactCardItem(
    contact: ContactModel,
    colorScheme: ColorScheme,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    var dragAmountAccumulator by remember { mutableFloatStateOf(0f) }

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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(shape = CircleShape)
                        .background(colorScheme.surfaceVariant)
                ) {
                    val resId = when (contact.imageResName) {
                        "doctor1" -> R.drawable.doctor1
                        "doctor2" -> R.drawable.doctor2
                        "holding_flowers" -> R.drawable.holding_flowers
                        else -> 0
                    }
                    if (resId != 0 && contact.imageResName.isNotBlank()) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.holding_flowers),
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center).size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                        color = colorScheme.onSurface
                    )
                    Text(text = contact.phone, fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onEdit()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = colorScheme.secondary
                        )
                    }
                    IconButton(onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                        onDelete()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = colorScheme.error
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = "Drag to reorder",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 4.dp)
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = { dragAmountAccumulator = 0f },
                                    onDragEnd = { dragAmountAccumulator = 0f },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAmountAccumulator += dragAmount
                                        if (dragAmountAccumulator > 60f) {
                                            onMoveDown()
                                            dragAmountAccumulator = 0f
                                        } else if (dragAmountAccumulator < -60f) {
                                            onMoveUp()
                                            dragAmountAccumulator = 0f
                                        }
                                    }
                                )
                            }
                    )
                }
            }
        }
}
