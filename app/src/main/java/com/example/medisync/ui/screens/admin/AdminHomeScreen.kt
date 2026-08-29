package com.example.medisync.ui.screens.admin

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.medisync.ui.components.HomeTopBar
import com.example.medisync.ui.components.sheets.CallUsBottomSheet
import com.example.medisync.ui.components.sheets.ReportsOpenedBottomSheet
import com.example.medisync.ui.components.sheets.TotalUsersBottomSheet
import com.example.medisync.ui.screens.common.ConfigViewModel
import com.example.medisync.ui.screens.user.AnimatedSloganText
import com.example.medisync.ui.screens.user.DoctorSection
import com.example.medisync.ui.screens.user.PromotionCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen() {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val context = LocalContext.current
    val configViewModel: ConfigViewModel = koinViewModel()
    val appConfig by configViewModel.appConfig.collectAsState()

    val adminHomeViewModel: AdminHomeViewModel = koinViewModel()
    val totalUsers by adminHomeViewModel.totalUsers.collectAsState()
    val usersJoinedToday by adminHomeViewModel.usersJoinedToday.collectAsState()
    val unclaimedPreRegUsers by adminHomeViewModel.unclaimedPreRegUsers.collectAsState()
    val reportsOpenedCount by adminHomeViewModel.reportsOpenedCount.collectAsState()
    val reportsOpenedTodayCount by adminHomeViewModel.reportsOpenedTodayCount.collectAsState()
    val totalUploadedReportsCount by adminHomeViewModel.totalUploadedReportsCount.collectAsState()

    val collapseRangePx = with(density) { 70.dp.toPx() }
    val scrollFraction by remember {
        derivedStateOf {
            (scrollState.value / collapseRangePx).coerceIn(0f, 1f)
        }
    }

    var showCallUsSheet by remember { mutableStateOf(false) }
    var showJoinedTodaySheet by remember { mutableStateOf(false) }
    var showReportsOpenedSheet by remember { mutableStateOf(false) }

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
                AdminStatsGrid(
                    onCallUsClick = { showCallUsSheet = true },
                    onTotalUsersClick = { showJoinedTodaySheet = true },
                    onReportsOpenedClick = { showReportsOpenedSheet = true },
                    context = context,
                    colorScheme = colorScheme,
                    reportsOpenedCount = reportsOpenedCount,
                    totalUsersCount = totalUsers,
                    unclaimedPreRegCount = unclaimedPreRegUsers
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            DoctorSection(colorScheme = colorScheme, contacts = appConfig.contacts)

            Spacer(modifier = Modifier.height(48.dp))

            PromotionCard()

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedSloganText(scrollState = scrollState)
        }

        HomeTopBar(
            modifier = Modifier.align(Alignment.TopCenter),
            scrollFraction = scrollFraction,
            onBellClick = { },
            showMemberSwitcher = false
        )

        if (showCallUsSheet) {
            CallUsBottomSheet(
                onDismissRequest = { showCallUsSheet = false },
                onCallClick = { number ->
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:$number".toUri()
                    }
                    context.startActivity(intent)
                    showCallUsSheet = false
                }
            )
        }

        if (showJoinedTodaySheet) {
            TotalUsersBottomSheet(
                totalUsers = totalUsers,
                usersJoinedToday = usersJoinedToday,
                colorScheme = colorScheme,
                onDismissRequest = { showJoinedTodaySheet = false }
            )
        }

        if (showReportsOpenedSheet) {
            ReportsOpenedBottomSheet(
                reportsOpenedCount = reportsOpenedCount,
                reportsOpenedTodayCount = reportsOpenedTodayCount,
                totalUploadedReportsCount = totalUploadedReportsCount,
                colorScheme = colorScheme,
                onDismissRequest = { showReportsOpenedSheet = false }
            )
        }
    }
}