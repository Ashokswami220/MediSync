package com.example.medisync.ui.components.sheets
import com.example.medisync.utils.HapticHelper
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var memberName by remember { mutableStateOf("") }
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Member",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                IconButton(onClick = { HapticHelper.trigger(context, HapticHelper.Type.LIGHT); onDismiss() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = memberName,
                onValueChange = { newValue -> 
                    memberName = newValue.filter { !it.isWhitespace() }
                },
                label = { Text("Member Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.secondary,
                    focusedLabelColor = colorScheme.secondary,
                    cursorColor = colorScheme.secondary
                )
            )

            Text(
                text = "• Only enter first name\n• No spaces allowed",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (memberName.isNotBlank()) {
                        onSave(memberName)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
            ) {
                Text("Add Member", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSecondary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
