package com.example.medisync.ui.screens.admin

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.medisync.ui.navigation.HomeTopBar
import com.example.medisync.ui.screens.user.AnimatedSloganText
import com.example.medisync.ui.screens.user.HealthStatsGrid
import com.example.medisync.ui.screens.user.PharmacistSection
import com.example.medisync.ui.screens.user.PromotionCard

@Composable
fun AdminHomeScreen() {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val context = LocalContext.current

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
                    onCallUsClick = { /* No-op for admin for now */ },
                    onStatClick = { /* No-op */ },
                    context = context,
                    colorScheme = colorScheme,
                    bloodPressure = "",
                    bloodType = "",
                    bloodSugar = ""
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PharmacistSection(colorScheme = colorScheme)

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
    }
}