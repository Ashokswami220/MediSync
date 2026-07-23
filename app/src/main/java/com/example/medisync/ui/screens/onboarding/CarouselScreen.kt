package com.example.medisync.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun CarouselScreen(
    onNavigate: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        Triple(
            "Your Health,\nSecured",
            "Experience enterprise-grade security for your personal medical records. We prioritize your privacy above all else.",
            R.drawable.doctor_img1
        ), Triple(
            "All Records in\nOne Place",
            "Access your lab results, prescriptions, and imaging reports anytime, anywhere with a single tap.",
            R.drawable.doctor_img2
        ), Triple(
            "Share with\nYour Doctor",
            "Seamlessly and securely share your comprehensive health history with healthcare professionals.",
            R.drawable.doctor_img3
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Image Pager
        HorizontalPager(
            state = pagerState, modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(id = pages[page].third), contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
        }

        // Gradient Overlay matching CSS
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.30f),
                        0.5f to Color(0xFF0D1C2E).copy(alpha = 0.50f),
                        1.0f to Color(0xFF0D1C2E).copy(alpha = 0.95f)
                    )
                )
        )

        // 2. Skip Button
        TextButton(
            onClick = onNavigate, modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Skip", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp
            )
        }

        // 3. Bottom Content (Text, Indicators, Next Button)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(), horizontalAlignment = Alignment.Start
        ) {
            // Animated Text Content
            AnimatedContent(
                targetState = pagerState.targetPage, transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically(
                            animationSpec = tween(800)
                        ) { height -> height } + fadeIn(tween(800))).togetherWith(
                                slideOutVertically(
                                    animationSpec = tween(800)
                                ) { height -> -height } + fadeOut(tween(800)))
                    } else {
                        (slideInVertically(
                            animationSpec = tween(800)
                        ) { height -> -height } + fadeIn(tween(800))).togetherWith(
                                slideOutVertically(
                                    animationSpec = tween(800)
                                ) { height -> height } + fadeOut(tween(800)))
                    }
                }, label = "text_animation"
            ) { page ->
                Column {
                    Text(
                        text = pages[page].first, color = Color.White, fontSize = 34.sp,
                        fontWeight = FontWeight.Bold, lineHeight = 42.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = pages[page].second, color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp, lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isSelected) 32.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White
                                else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Next Button
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1, animationSpec = tween(800)
                            )
                        }
                    } else {
                        onNavigate()
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Next", fontSize = 18.sp, fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next"
                )
            }
        }
    }
}