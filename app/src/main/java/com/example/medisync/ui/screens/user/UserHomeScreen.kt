package com.example.medisync.ui.screens.user

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.Spring.StiffnessMedium
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.medisync.R
import com.example.medisync.data.local.ContactConfig
import com.example.medisync.model.ContactModel
import com.example.medisync.ui.components.HomeTopBar
import com.example.medisync.ui.components.HorizEmptyReportsState
import com.example.medisync.ui.components.NotLoggedInState
import com.example.medisync.ui.components.sheets.CallUsBottomSheet
import com.example.medisync.ui.components.sheets.HealthStatBottomSheet
import com.example.medisync.ui.components.sheets.HealthStatDetails
import com.example.medisync.ui.screens.common.ConfigViewModel
import com.example.medisync.utils.GlobalToastManager
import com.example.medisync.utils.HapticHelper
import com.example.medisync.utils.HealthStatusHelper
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date

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
    bloodTypeLastUpdated: Long = 0L,
    bloodPressureLastUpdated: Long = 0L,
    bloodSugarLastUpdated: Long = 0L,
    onNavigateToLogin: () -> Unit,
    onRefreshProfile: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val density = LocalDensity.current

    val reportsViewModel: ReportsViewModel = koinViewModel()
    val reportsState by reportsViewModel.reportsState.collectAsState()
    val configViewModel: ConfigViewModel = koinViewModel()
    val appConfig by configViewModel.appConfig.collectAsState()
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    LaunchedEffect(Unit) {
        onRefreshProfile()
        if (isLoggedIn) {
            reportsViewModel.loadDocuments()
        }
    }
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
                    bloodSugar = bloodSugar,
                    bloodTypeLastUpdated = bloodTypeLastUpdated,
                    bloodPressureLastUpdated = bloodPressureLastUpdated,
                    bloodSugarLastUpdated = bloodSugarLastUpdated
                )

                Spacer(modifier = Modifier.height(32.dp))

                RecentReportsSection(
                    onNavigateToReportDetail = onNavigateToReportDetail,
                    onNavigateToReports = onNavigateToReports,
                    colorScheme = colorScheme,
                    reportsState = reportsState,
                    isLoggedIn = isLoggedIn,
                    onNavigateToLogin = onNavigateToLogin
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PharmacistSection(
                colorScheme = colorScheme,
                contacts = appConfig.contacts
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
            members = members,
            showAllOption = false
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
    bloodSugar: String,
    bloodTypeLastUpdated: Long,
    bloodPressureLastUpdated: Long,
    bloodSugarLastUpdated: Long
) {
    val formatter = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale)
    val bpDateString = if (bloodPressureLastUpdated > 0L) formatter.format(Date(bloodPressureLastUpdated)) else "N/A"
    val btDateString = if (bloodTypeLastUpdated > 0L) formatter.format(Date(bloodTypeLastUpdated)) else "N/A"
    val bsDateString = if (bloodSugarLastUpdated > 0L) formatter.format(Date(bloodSugarLastUpdated)) else "N/A"

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val totalWidth = maxWidth
        val dividerWidth = 1.dp
        
        // Top row calculations
        val availableTopWidth = totalWidth - (dividerWidth * 2)
        val topBox1Width = availableTopWidth * 0.38f
        val topBox2Width = availableTopWidth * 0.38f
        val topBox3Width = availableTopWidth - topBox1Width - topBox2Width
        
        // Bottom row calculations
        val availableBottomWidth = totalWidth - dividerWidth
        val bottomBoxWidth = availableBottomWidth / 2f

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
                    .width(topBox1Width)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(colorScheme.surface)
                        .clickable {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onCallUsClick()
                        }
                )
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
                    .width(topBox2Width)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(colorScheme.surface)
                        .clickable {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            openMedicalCoordinates(context)
                        }
                )
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
                    .width(topBox3Width)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(colorScheme.surface)
                        .clickable(
                            interactionSource = syringeInteractionSource,
                            indication = LocalIndication.current
                        ) {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        }
                )
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(colorScheme.surface)
                    .clickable {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onStatClick(
                            HealthStatDetails(
                                title = "Blood Pressure",
                                value = bloodPressure.ifEmpty { "--/--" },
                                unit = "mmHg",
                                status = HealthStatusHelper.getBloodPressureStatus(bloodPressure),
                                date = bpDateString,
                                icon = Icons.Default.MonitorHeart,
                                color = colorScheme.primary
                            )
                        )
                    }
            )

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
                        bloodPressure.ifEmpty { "--/--" }, fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
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
                    .width(160.dp) // Fixed width to prevent height inflation on tablets
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
                    .width(bottomBoxWidth)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(colorScheme.surface)
                        .clickable {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onStatClick(
                                HealthStatDetails(
                                    title = "Blood Type",
                                    value = bloodType.ifEmpty { "--" },
                                    unit = "",
                                    status = HealthStatusHelper.getBloodTypeStatus(bloodType),
                                    date = btDateString,
                                    icon = Icons.Default.Bloodtype,
                                    color = colorScheme.secondary
                                )
                            )
                        }
                )
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
                    .width(bottomBoxWidth)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(colorScheme.surface)
                        .clickable {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onStatClick(
                                HealthStatDetails(
                                    title = "Blood Sugar",
                                    value = bloodSugar.ifEmpty { "--" },
                                    unit = "mg/dL",
                                    status = HealthStatusHelper.getBloodSugarStatus(bloodSugar),
                                    date = bsDateString,
                                    icon = Icons.Default.Bloodtype,
                                    color = colorScheme.error
                                )
                            )
                        }
                )
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
                            bloodSugar.ifEmpty { "--" }, fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
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
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecentReportsSection(
    onNavigateToReportDetail: (String, String) -> Unit,
    onNavigateToReports: () -> Unit,
    colorScheme: ColorScheme,
    reportsState: ReportsState,
    isLoggedIn: Boolean,
    onNavigateToLogin: () -> Unit
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

    if (!isLoggedIn) {
        NotLoggedInState(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalPadding = 0.dp,
            onNavigateToLogin = onNavigateToLogin,
            colorScheme = colorScheme
        )
    } else {
        when (reportsState) {
            is ReportsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(
                        modifier = Modifier.size(50.dp),
                        color = colorScheme.secondary,
                        polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                    )
                }
            }

            is ReportsState.Empty -> {
                HorizEmptyReportsState(
                    modifier = Modifier.padding(vertical = 8.dp),
                    colorScheme = colorScheme
                )
            }

            is ReportsState.Success -> {
                val dateFormatter = SimpleDateFormat(
                    "MMM dd, yyyy",
                    LocalLocale.current.platformLocale
                )

                val recentDocs = reportsState.documents
                    .sortedByDescending { it.uploadedAt }
                    .take(3)

                if (recentDocs.isEmpty()) {
                    HorizEmptyReportsState(
                        modifier = Modifier.padding(vertical = 8.dp),
                        colorScheme = colorScheme
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colorScheme.outlineVariant)
                    ) {
                        recentDocs.forEachIndexed { index, doc ->
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(colorScheme.surface)
                                        .clickable {
                                            onNavigateToReportDetail(
                                                doc.documentName, doc.fileUrl
                                            )
                                        }
                                )
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
                                            imageVector = Icons.Default.MedicalInformation,
                                            contentDescription = null,
                                            tint = colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = doc.documentName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color = colorScheme.onBackground,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${
                                                dateFormatter.format(
                                                    Date(doc.uploadedAt)
                                                )
                                            } • ${doc.linkedMember}",
                                            fontSize = 13.sp,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (index < recentDocs.size - 1) {
                                HorizontalDivider(
                                    thickness = 1.dp, color = colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }

            is ReportsState.Error -> {
                HorizEmptyReportsState(
                    modifier = Modifier.padding(vertical = 8.dp),
                    colorScheme = colorScheme
                )
            }
        }
    }
}


@Composable
fun PharmacistSection(colorScheme: ColorScheme, contacts: List<ContactModel>) {
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

            val displayContacts =
                contacts.filter { it.imageResName == "doctor1" || it.imageResName == "doctor2" }
                    .sortedBy { it.imageResName }
                    .ifEmpty {
                        listOf(
                            ContactModel(
                                name = "Sawai Singh",
                                phone = ContactConfig.pharmacistPhones.sawaiSingh,
                                experience = "8 years experience", imageResName = "doctor1"
                            ),
                            ContactModel(
                                name = "Govind", phone = ContactConfig.pharmacistPhones.govind,
                                experience = "5 years experience", imageResName = "doctor2"
                            )
                        )
                    }

            displayContacts.forEach { contact ->
                val imageRes = when (contact.imageResName) {
                    "doctor1" -> R.drawable.doctor1
                    "doctor2" -> R.drawable.doctor2
                    "holding_flowers" -> R.drawable.holding_flowers
                    else -> R.drawable.holding_flowers
                }

                PharmacistCard(
                    name = contact.name,
                    specialty = contact.role,
                    experience = contact.experience,
                    imageRes = imageRes,
                    colorScheme = colorScheme,
                    onCallClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = "tel:${contact.phone}".toUri()
                        }
                        context.startActivity(intent)
                    }
                )
            }
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
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surface)
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        val totalWidth = maxWidth
        // Dedicate a reasonable static ratio to the image, e.g. 40% of the card width
        val imageWidth = totalWidth * 0.4f
        val textWidth = totalWidth - imageWidth

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Image touches left and bottom
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Doctor Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(imageWidth)
                    .fillMaxHeight()
            )

            // Details
            Column(
                modifier = Modifier
                    .width(textWidth)
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
                Box(
                    modifier = Modifier
                        .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { onCallClick() }
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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