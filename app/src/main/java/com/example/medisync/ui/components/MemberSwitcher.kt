package com.example.medisync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.example.medisync.utils.HapticHelper

@Composable
fun MemberSwitcher(
    modifier: Modifier = Modifier,
    selectedMember: String,
    onMemberSelected: (String) -> Unit,
    containerColor: Color = Color.Black.copy(alpha = 0.5f),
    contentColor: Color = Color.White,
    icon: ImageVector = Icons.Default.ExpandMore,
    popupAlignment: Alignment = Alignment.TopCenter,
    popupOffsetY: Int = 0,
    chatStyle: Boolean = false
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val members = listOf("Ashok", "John Doe", "Jane Doe")
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        // The trigger button
        Box(
            modifier = Modifier
                .alpha(if (expandedMenu) 0f else 1f)
                .clip(RoundedCornerShape(if (chatStyle) 24.dp else 50.dp))
                .background(containerColor)
                .clickable {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    expandedMenu = true
                }
                .padding(
                    horizontal = 16.dp, 
                    vertical = if (chatStyle) 14.dp else 8.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedMember, 
                    fontWeight = FontWeight.Medium,
                    fontSize = if (chatStyle) 16.sp else 14.sp, 
                    color = contentColor,
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = "Expand", 
                    tint = contentColor,
                    modifier = if (chatStyle) Modifier else Modifier.size(16.dp)
                )
            }
        }

        if (expandedMenu) {
            val density = LocalDensity.current
            val yOffsetPx = with(density) { popupOffsetY.dp.roundToPx() }
            Popup(
                alignment = popupAlignment,
                offset = IntOffset(0, yOffsetPx),
                onDismissRequest = { expandedMenu = false }
            ) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    members.forEach { member ->
                        val isSelected = member == selectedMember
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) colorScheme.secondary else Color.Transparent
                                )
                                .clickable {
                                    HapticHelper.trigger(context, HapticHelper.Type.MEDIUM)
                                    onMemberSelected(member)
                                    expandedMenu = false
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
