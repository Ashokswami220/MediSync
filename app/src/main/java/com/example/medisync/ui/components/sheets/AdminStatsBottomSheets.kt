package com.example.medisync.ui.components.sheets
import com.example.medisync.utils.HapticHelper
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotalUsersBottomSheet(
    totalUsers: Int,
    usersJoinedToday: Int,
    colorScheme: ColorScheme,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = colorScheme.background,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Icon, Title, and Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Total Users",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                IconButton(onClick = { HapticHelper.trigger(context, HapticHelper.Type.LIGHT); onDismissRequest() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Value and Label
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = totalUsers.toString(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    lineHeight = 48.sp
                )
            }

            // Status Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .background(
                        color = colorScheme.secondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "today's user count = $usersJoinedToday",
                    color = colorScheme.secondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }

            HorizontalDivider(color = colorScheme.outlineVariant, thickness = 1.dp)

            // Last Updated
            val formatter = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale)
            val todayDate = formatter.format(Date())

            Text(
                text = "Last updated: $todayDate",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsOpenedBottomSheet(
    reportsOpenedCount: Long,
    reportsOpenedTodayCount: Long,
    totalUploadedReportsCount: Long,
    colorScheme: ColorScheme,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = colorScheme.background,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Icon, Title, and Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorScheme.tertiary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Reports Opened",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                IconButton(onClick = { HapticHelper.trigger(context, HapticHelper.Type.LIGHT); onDismissRequest() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Value
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = reportsOpenedCount.toString(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    lineHeight = 48.sp
                )
            }

            // Status Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .background(
                        color = colorScheme.tertiary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "today's opened reports - $reportsOpenedTodayCount",
                    color = colorScheme.tertiary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }

            val formatter = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale)
            val todayDate = formatter.format(Date())

            Text(
                text = "Last updated: $todayDate",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            HorizontalDivider(color = colorScheme.outlineVariant, thickness = 1.dp)

            Spacer(modifier = Modifier.height(24.dp))

            // Second Section
            Text(
                text = "Total Reports",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = totalUploadedReportsCount.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                lineHeight = 48.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Last updated: $todayDate",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}
