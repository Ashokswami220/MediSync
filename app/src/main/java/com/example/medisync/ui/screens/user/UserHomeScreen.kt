package com.example.medisync.ui.screens.user

import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.Spring.StiffnessMedium
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.medisync.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.example.medisync.ui.navigation.UserHomeTopBar

import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.medisync.ui.components.CallUsBottomSheet
import com.example.medisync.ui.components.HealthStatBottomSheet
import com.example.medisync.ui.components.HealthStatDetails
import androidx.core.net.toUri

@Composable
fun UserHomeScreen(
    onNavigateToReportDetail: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val context = LocalContext.current

    var showCallSheet by remember { mutableStateOf(false) }
    var selectedHealthStat by remember { mutableStateOf<HealthStatDetails?>(null) }

    val collapseRangePx = with(density) { 70.dp.toPx() }
    val scrollFraction = (scrollState.value / collapseRangePx).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .verticalScroll(scrollState)
                .padding(top = 196.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
        ) {
            HealthStatsGrid(
                onCallUsClick = { showCallSheet = true },
                onStatClick = { stat -> selectedHealthStat = stat },
                context = context,
                colorScheme = colorScheme
            )

            Spacer(modifier = Modifier.height(32.dp))

            RecentReportsSection(
                onNavigateToReportDetail = onNavigateToReportDetail,
                colorScheme = colorScheme
            )
        }

        UserHomeTopBar(
            modifier = Modifier.align(Alignment.TopCenter),
            scrollFraction = scrollFraction
        )
        
        if (showCallSheet) {
            CallUsBottomSheet(
                onDismissRequest = { showCallSheet = false },
                onCallClick = { number ->
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:$number".toUri()
                    }
                    context.startActivity(intent)
                    showCallSheet = false
                }
            )
        }
        
        HealthStatBottomSheet(
            statDetails = selectedHealthStat,
            onDismissRequest = { selectedHealthStat = null }
        )
    }
}

fun openMedicalCoordinates(context: Context) {
    val uri = "geo:0,0?q=28.026437,74.466879(Balaji Medical & Labs)"
    val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
    intent.setPackage("com.google.android.apps.maps")
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
    }
}

@Composable
fun HealthStatsGrid(
    onCallUsClick: () -> Unit,
    onStatClick: (HealthStatDetails) -> Unit,
    context: Context,
    colorScheme: ColorScheme
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant)
    ) {
        val syringeInteractionSource = remember { MutableInteractionSource() }
        val isSyringePressed by syringeInteractionSource.collectIsPressedAsState()
        val syringeOffsetY by animateDpAsState(
            targetValue = if (isSyringePressed) (-6).dp else 0.dp,
            animationSpec = spring(
                dampingRatio = DampingRatioMediumBouncy,
                stiffness = StiffnessMedium
            )
        )

        // New Top Info Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Call Us Cell (38%)
            Box(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .clickable { onCallUsClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 8.dp)
                ) {
                    Text(
                        "Call us",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Call us",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colorScheme.outlineVariant)
            )

            // Medical Cell (38%)
            Box(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .clickable { openMedicalCoordinates(context) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Go to",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            Icons.Default.Directions,
                            contentDescription = "Directions",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Medical",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colorScheme.outlineVariant)
            )

            // Empty Cell for SVG (24%)
            Box(
                modifier = Modifier
                    .weight(0.24f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .clickable(
                        interactionSource = syringeInteractionSource,
                        indication = LocalIndication.current
                    ) { /* TODO */ },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.syringe),
                    contentDescription = "Syringe",
                    modifier = Modifier
                        .size(36.dp)
                        .offset(y = syringeOffsetY)
                        .rotate(-20f)
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)

        // Blood Pressure Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface)
                .clickable {
                    onStatClick(
                        HealthStatDetails(
                            title = "Blood Pressure",
                            value = "120/80",
                            unit = "mmHg",
                            status = "Normal",
                            date = "28 Jun 2026",
                            icon = Icons.Default.MonitorHeart,
                            color = colorScheme.primary
                        )
                    )
                }
        ) {
            // Left Content
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 24.dp, bottom = 24.dp)
            ) {
                // 1. Icon + Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primaryContainer.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MonitorHeart, contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Blood Pressure", fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2. Huge Number + Unit
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "120/80", fontSize = 36.sp, fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "mmHg", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }


            }

            // SVG Image (Bottom Right)
            Image(
                painter = painterResource(id = R.drawable.blood_test),
                contentDescription = null,
                alignment = Alignment.BottomEnd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(160.dp) // Maintain SVG scale width
            )
        }

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Blood Type Cell
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .clickable {
                        onStatClick(
                            HealthStatDetails(
                                title = "Blood Type",
                                value = "O+",
                                unit = "",
                                status = "Donor Eligible",
                                date = "15 Jan 2026",
                                icon = Icons.Default.Bloodtype,
                                color = colorScheme.secondary
                            )
                        )
                    }
            ) {
                Column(modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    colorScheme.secondaryContainer.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bloodtype, contentDescription = null,
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Blood Type", fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "O+", fontSize = 36.sp, fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        lineHeight = 36.sp
                    )


                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colorScheme.outlineVariant)
            )

            // Blood Sugar Cell
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .clickable {
                        onStatClick(
                            HealthStatDetails(
                                title = "Blood Sugar",
                                value = "95",
                                unit = "mg/dL",
                                status = "Normal",
                                date = "28 Jun 2026",
                                icon = Icons.Default.Bloodtype,
                                color = colorScheme.error
                            )
                        )
                    }
            ) {
                Column(modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colorScheme.errorContainer.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bloodtype, contentDescription = null,
                                tint = colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Blood Sugar", fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "95", fontSize = 36.sp, fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground,
                            lineHeight = 36.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "mg/dL", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }


                }
            }
        }
    }
}

@Composable
fun RecentReportsSection(
    onNavigateToReportDetail: () -> Unit,
    colorScheme: ColorScheme
) {
    // Recent Reports Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent Reports",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )
        TextButton(
            onClick = { /* TODO */ },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, colorScheme.outlineVariant)
        ) {
            Text("View All", fontWeight = FontWeight.SemiBold, color = colorScheme.primary)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Reports List
    val reports = listOf(
        ReportItem(
            "Comprehensive Metabolic Panel", "Oct 24, 2023 • Lab Results",
            Icons.Default.Science, colorScheme.onPrimaryContainer,
            colorScheme.primaryContainer
        ),
        ReportItem(
            "Chest X-Ray", "Oct 12, 2023 • Imaging", Icons.Default.MedicalInformation,
            colorScheme.onSecondaryContainer, colorScheme.secondaryContainer
        ),
        ReportItem(
            "Prescription Renewal", "Sep 30, 2023 • Clinical Notes", Icons.Default.Science,
            colorScheme.onSurface, colorScheme.surfaceContainerHighest
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant)
    ) {
        reports.take(3)
            .forEachIndexed { index, report ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surface)
                        .clickable { onNavigateToReportDetail() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                report.icon, contentDescription = null,
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                report.title, fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                report.subtitle, fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight, contentDescription = null,
                            tint = colorScheme.outline
                        )
                    }
                }

                if (index < 2) {
                    HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                }
            }
    }
}

data class ReportItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color
)
