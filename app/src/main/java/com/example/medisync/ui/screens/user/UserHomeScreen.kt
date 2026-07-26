package com.example.medisync.ui.screens.user

import android.content.ActivityNotFoundException
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.example.medisync.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import com.example.medisync.ui.navigation.HomeTopBar

import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import android.content.Intent
import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.medisync.ui.components.CallUsBottomSheet
import com.example.medisync.ui.components.HealthStatBottomSheet
import com.example.medisync.ui.components.HealthStatDetails
import androidx.core.net.toUri
import com.example.medisync.data.local.ContactConfig
import com.example.medisync.utils.HapticHelper
import com.example.medisync.utils.GlobalToastManager
import androidx.compose.material.icons.filled.Notifications

@Composable
fun UserHomeScreen(
    onNavigateToReportDetail: (String, String) -> Unit = { _, _ -> },
    onNavigateToReports: () -> Unit = {},
    selectedMember: String = "User",
    onMemberSelected: (String) -> Unit = {},
    members: List<String> = emptyList(),
    bloodPressure: String = "",
    bloodType: String = "",
    bloodSugar: String = "",
    onRefreshProfile: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onRefreshProfile()
    }
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val context = LocalContext.current

    var showCallSheet by remember { mutableStateOf(false) }
    var selectedHealthStat by remember { mutableStateOf<HealthStatDetails?>(null) }

    val collapseRangePx = with(density) { 70.dp.toPx() }
    val scrollFraction by remember {
        derivedStateOf {
            (scrollState.value / collapseRangePx).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .verticalScroll(scrollState)
                .padding(top = 196.dp, bottom = 120.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                HealthStatsGrid(
                    onCallUsClick = { showCallSheet = true },
                    onStatClick = { stat -> selectedHealthStat = stat },
                    context = context,
                    colorScheme = colorScheme,
                    bloodPressure = bloodPressure,
                    bloodType = bloodType,
                    bloodSugar = bloodSugar
                )

                Spacer(modifier = Modifier.height(32.dp))

                RecentReportsSection(
                    onNavigateToReportDetail = onNavigateToReportDetail,
                    onNavigateToReports = onNavigateToReports,
                    colorScheme = colorScheme
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PharmacistSection(
                colorScheme = colorScheme
            )

            Spacer(modifier = Modifier.height(48.dp))

            PromotionCard()

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedSloganText(scrollState = scrollState)
        }

        HomeTopBar(
            modifier = Modifier.align(Alignment.TopCenter),
            scrollFraction = scrollFraction,
            onBellClick = {
                GlobalToastManager.showToast(
                    message = "No new notifications",
                    icon = Icons.Default.Notifications
                )
            },
            selectedMember = selectedMember,
            onMemberSelected = onMemberSelected,
            members = members
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
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
    }
}

@Composable
fun HealthStatsGrid(
    onCallUsClick: () -> Unit,
    onStatClick: (HealthStatDetails) -> Unit,
    context: Context,
    colorScheme: ColorScheme,
    bloodPressure: String,
    bloodType: String,
    bloodSugar: String
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
                    .clickable { 
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onCallUsClick() 
                    },
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
                    .clickable { 
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        openMedicalCoordinates(context) 
                    },
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
                    ) { 
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.syringe),
                    contentDescription = "Syringe",
                    modifier = Modifier
                        .size(36.dp)
                        .offset { IntOffset(0, syringeOffsetY.roundToPx()) }
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
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    onStatClick(
                        HealthStatDetails(
                            title = "Blood Pressure",
                            value = bloodPressure.ifEmpty { "--/--" },
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
                        bloodPressure.ifEmpty { "--/--" }, fontSize = 36.sp, fontWeight = FontWeight.Bold,
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
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onStatClick(
                            HealthStatDetails(
                                title = "Blood Type",
                                value = bloodType.ifEmpty { "--" },
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
                        bloodType.ifEmpty { "--" }, fontSize = 36.sp, fontWeight = FontWeight.Bold,
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
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onStatClick(
                            HealthStatDetails(
                                title = "Blood Sugar",
                                value = bloodSugar.ifEmpty { "--" },
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
                            bloodSugar.ifEmpty { "--" }, fontSize = 36.sp, fontWeight = FontWeight.Bold,
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
    onNavigateToReportDetail: (String, String) -> Unit,
    onNavigateToReports: () -> Unit,
    colorScheme: ColorScheme
) {
    val context = LocalContext.current

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
            onClick = { 
                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                onNavigateToReports() 
            },
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
                        .clickable { onNavigateToReportDetail(report.title, "") }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.secondary.copy(alpha = 0.07f)),
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

@Composable
fun PharmacistSection(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Pharmacist",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val context = LocalContext.current
            PharmacistCard(
                name = "Sawai Singh",
                specialty = "Pharmacist",
                experience = "8 years experience",
                imageRes = R.drawable.doctor1,
                colorScheme = colorScheme,
                onCallClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:${ContactConfig.pharmacistPhones.sawaiSingh}".toUri()
                    }
                    context.startActivity(intent)
                }
            )
            PharmacistCard(
                name = "Govind",
                specialty = "Pharmacist",
                experience = "5 years experience",
                imageRes = R.drawable.doctor2,
                colorScheme = colorScheme,
                onCallClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:${ContactConfig.pharmacistPhones.govind}".toUri()
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun PharmacistCard(
    name: String,
    specialty: String,
    experience: String,
    imageRes: Int,
    colorScheme: ColorScheme,
    onCallClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surface)
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Image touches left and bottom
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Doctor Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
            )

            // Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = specialty,
                        fontSize = 16.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = experience,
                        fontSize = 14.sp,
                        color = colorScheme.outline,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Contact Button
                Row(
                    modifier = Modifier
                        .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .clickable { onCallClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Contact",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Contact",
                        color = colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PromotionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Shree BalaJi Medical & Labs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.flask),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.medical_image),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                contentScale = ContentScale.FillWidth
            )
        }
    }
}

@Composable
fun AnimatedSloganText(scrollState: ScrollState) {
    val textAnimationProgress by remember {
        derivedStateOf {
            if (scrollState.maxValue == 0) {
                1f
            } else {
                val threshold = scrollState.maxValue - 600
                if (scrollState.value <= threshold) {
                    0f
                } else {
                    val range = 600f
                    ((scrollState.value - threshold).toFloat() / range).coerceIn(0f, 1f)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 30.dp)
            .graphicsLayer {
                alpha = textAnimationProgress
                translationY = (1f - textAnimationProgress) * 200f
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Every Medicine",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Any Emergency",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
    }
}