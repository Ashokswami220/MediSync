package com.example.medisync.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.model.UserRole
import com.example.medisync.ui.navigation.Routes
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import kotlin.math.abs

data class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun GlassNavBar(
    modifier: Modifier = Modifier,
    role: UserRole,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    hazeState: HazeState,
    onUploadButtonPositioned: (Offset) -> Unit = {},
) {
    val separatedItem = if (role == UserRole.ADMIN) {
        NavItem(Routes.UPLOAD_DATA, Icons.Default.Upload, "Upload")
    } else null

    val items = when (role) {
        UserRole.ADMIN -> listOf(
            NavItem(Routes.ADMIN_HOME, Icons.Default.Home, "Home"),
            NavItem(Routes.USER_LIST, Icons.AutoMirrored.Filled.List, "List"),
            NavItem(Routes.SETTINGS, Icons.Default.Settings, "Settings")
        )

        UserRole.USER -> listOf(
            NavItem(Routes.USER_HOME, Icons.Default.Home, "Home"),
            NavItem(Routes.USER_REPORTS, Icons.Default.Assessment, "Reports"),
            NavItem(Routes.SETTINGS, Icons.Default.Settings, "Settings")
        )

        else -> emptyList()
    }

    if (items.isEmpty()) return

    // Keep onNavigate always fresh inside pointerInput (which doesn't recompose)
    val currentOnNavigate by rememberUpdatedState(onNavigate)

    val barShape = RoundedCornerShape(percent = 50)
    val density = LocalDensity.current

    // Track each tab's measured position, width, and height
    val tabOffsets = remember { mutableStateMapOf<Int, Float>() }
    val tabWidths = remember { mutableStateMapOf<Int, Float>() }
    val tabHeights = remember { mutableStateMapOf<Int, Float>() }

    // Current selected index
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }

    // Drag state
    var isDragging by remember { mutableStateOf(false) }
    var hoverIndex by remember { mutableIntStateOf(selectedIndex) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var touchOffsetInPill by remember { mutableFloatStateOf(0f) }

    // Sync hoverIndex when navigation completes from outside
    LaunchedEffect(selectedIndex) {
        if (!isDragging) {
            hoverIndex = selectedIndex
        }
    }

    // The target index for the pill is ALWAYS the hoverIndex
    val targetIdx = hoverIndex
    val snapTargetX = tabOffsets[targetIdx] ?: 0f
    val targetW = tabWidths[targetIdx] ?: 0f

    val targetX = if (isDragging) dragOffsetX else snapTargetX

    val pillX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = if (isDragging) 400f else 800f
        ),
        label = "pill_x"
    )
    val animatedW by animateFloatAsState(
        targetValue = targetW,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = if (isDragging) 400f else 800f
        ),
        label = "pill_w"
    )

    fun getNearestTabIndex(x: Float): Int {
        var nearestIdx = 0
        var nearestDist = Float.MAX_VALUE
        tabOffsets.forEach { (idx, offsetX) ->
            val tabCenter = offsetX + (tabWidths[idx] ?: 0f) / 2f
            val dist = abs(x - tabCenter)
            if (dist < nearestDist) {
                nearestDist = dist
                nearestIdx = idx
            }
        }
        return nearestIdx
    }

    // ── Pill transition stretch/bulge effect ──
    val distanceToTarget = abs(pillX - snapTargetX)
    val isTransitioning = isDragging || distanceToTarget > 2f
    val pillScale by animateFloatAsState(
        targetValue = if (isTransitioning) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "pill_scale"
    )



    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 12.dp)
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            // ── Background Layer (Clipped) ──
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(barShape)
                    // ── Haze backdrop blur ──
                    .hazeEffect(state = hazeState) {
                        blurEffect {
                            blurRadius = 25.dp
                            noiseFactor = 0.15f
                            colorEffects = listOf(
                                HazeColorEffect.tint(Color(0xFF1C1C1E).copy(alpha = 0.55f))
                            )
                        }
                    }
                    // ── Top highlight ──
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 60f
                        )
                    )
                    // ── Chromatic border ──
                    .border(
                        width = 0.75.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color(0xFFAEC6FF).copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.08f),
                                Color(0xFFFFD6F0).copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.20f)
                            )
                        ),
                        shape = barShape
                    )
            )

            // ── Inner Content (Not Clipped) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // ── Sliding pill highlight (drawn behind tabs) ──
                if (animatedW > 0f) {
                    val pillWidthDp = with(density) { animatedW.toDp() }
                    val pillOffsetDp = with(density) { pillX.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = pillOffsetDp)
                            .width(pillWidthDp)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = pillScale
                                scaleY = pillScale
                            }
                            .clip(RoundedCornerShape(percent = 50))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
                            .border(
                                width = 0.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.28f),
                                        Color.White.copy(alpha = 0.06f)
                                    )
                                ),
                                shape = RoundedCornerShape(percent = 50)
                            )
                    )
                }

                // ── Tab items row with drag gesture ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(items.size) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    val currentPillWidth = tabWidths[targetIdx] ?: 0f
                                    if (offset.x >= pillX && offset.x <= pillX + currentPillWidth) {
                                        isDragging = true
                                        touchOffsetInPill = offset.x - pillX
                                        dragOffsetX = pillX
                                        hoverIndex = getNearestTabIndex(offset.x)
                                    }
                                },
                                onDragEnd = {
                                    if (isDragging) {
                                        isDragging = false
                                        currentOnNavigate(items[hoverIndex].route)
                                    }
                                },
                                onDragCancel = {
                                    if (isDragging) {
                                        isDragging = false
                                        hoverIndex = selectedIndex
                                    }
                                },
                                onHorizontalDrag = { change, _ ->
                                    if (isDragging) {
                                        val rawOffset = change.position.x - touchOffsetInPill
                                        val minOffset = tabOffsets[0] ?: 0f
                                        val maxOffset = tabOffsets[items.lastIndex] ?: 0f

                                        dragOffsetX = if (rawOffset < minOffset) {
                                            minOffset - (minOffset - rawOffset) * 0.15f
                                        } else if (rawOffset > maxOffset) {
                                            maxOffset + (rawOffset - maxOffset) * 0.15f
                                        } else {
                                            rawOffset
                                        }

                                        hoverIndex = getNearestTabIndex(change.position.x)
                                    }
                                }
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val selected = index == selectedIndex

                        // ── Content brightness ──
                        val contentAlpha = 1f

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned { coords ->
                                    tabOffsets[index] = coords.positionInParent().x
                                    tabWidths[index] = coords.size.width.toFloat()
                                    tabHeights[index] = coords.size.height.toFloat()
                                }
                                .clip(RoundedCornerShape(percent = 50))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onNavigate(item.route) }
                                )
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.offset(y = 2.dp)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = Color.White.copy(alpha = contentAlpha),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = item.label,
                                    color = Color.White.copy(alpha = contentAlpha),
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.offset(y = (-4).dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Separated Action Button ──
        if (separatedItem != null) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxHeight()
                    .onGloballyPositioned { coords ->
                        val center = coords.localToRoot(
                            Offset(coords.size.width / 2f, coords.size.height / 2f)
                        )
                        onUploadButtonPositioned(center)
                    }
                    .clip(CircleShape)
                    .hazeEffect(state = hazeState) {
                        blurEffect {
                            blurRadius = 25.dp
                            noiseFactor = 0.15f
                            colorEffects = listOf(
                                HazeColorEffect.tint(Color(0xFF1C1C1E).copy(alpha = 0.55f))
                            )
                        }
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 60f
                        )
                    )
                    .border(
                        width = 0.75.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color(0xFFAEC6FF).copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.08f),
                                Color(0xFFFFD6F0).copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.20f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onNavigate(separatedItem.route) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val contentAlpha = 1f
                Icon(
                    imageVector = separatedItem.icon,
                    contentDescription = separatedItem.label,
                    tint = Color.White.copy(alpha = contentAlpha),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
