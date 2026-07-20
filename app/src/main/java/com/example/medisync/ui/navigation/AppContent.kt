package com.example.medisync.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.example.medisync.R
import com.example.medisync.ui.components.MemberSwitcher
import com.example.medisync.model.UserRole
import com.example.medisync.utils.HapticHelper
import com.google.firebase.auth.FirebaseAuth
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.HazeColorEffect

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
    onUploadButtonPositioned: (androidx.compose.ui.geometry.Offset) -> Unit = {},
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
            val dist = kotlin.math.abs(x - tabCenter)
            if (dist < nearestDist) {
                nearestDist = dist
                nearestIdx = idx
            }
        }
        return nearestIdx
    }

    // ── Pill transition stretch/bulge effect ──
    val distanceToTarget = kotlin.math.abs(pillX - snapTargetX)
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
            .padding(start = 16.dp, end = 16.dp, bottom = 28.dp, top = 12.dp)
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
                        val center = coords.localToRoot(androidx.compose.ui.geometry.Offset(coords.size.width / 2f, coords.size.height / 2f))
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

@Composable
fun UserHomeTopBar(
    modifier: Modifier = Modifier,
    scrollFraction: Float = 0f,
    onBellClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    // Smooth the scroll fraction for animation
    val fraction by animateFloatAsState(
        targetValue = scrollFraction.coerceIn(0f, 1f), label = "scrollFraction"
    )

    // Top bar height interpolation
    val expandedHeight = 180.dp
    val collapsedHeight = 110.dp
    val currentHeight = lerp(expandedHeight, collapsedHeight, fraction)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(currentHeight)
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(colorScheme.secondary)
    ) {
        // Background Image (Doctor SVG) stays below content
        Image(
            painter = painterResource(R.drawable.doctor),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Color overlay that becomes solid secondary color when collapsed
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.secondary.copy(alpha = fraction))
        )

        // Gradient Scrim to ensure buttons and text are perfectly visible over the SVG
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f * (1f - fraction)),
                            Color.Black.copy(alpha = 0.8f * (1f - fraction))
                        )
                    )
                )
        )

        // Bottom Content Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: BalaJiMedic
            Text(
                text = "BalaJiMedic",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            // Right: Go To, Person (Ashok), Bell
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Person
                var selectedMember by remember { mutableStateOf("Ashok") }
                val currentUser = FirebaseAuth.getInstance().currentUser
                val isLoggedIn = currentUser != null

                if (isLoggedIn) {
                    MemberSwitcher(
                        selectedMember = selectedMember,
                        onMemberSelected = { selectedMember = it }
                    )
                }

                // Bell
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onBellClick()
                        }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications", tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String = "BalaJiMedic",
    showName: Boolean = true,
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onSearchActiveChange: (Boolean) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    showSearchIcon: Boolean = true,
    extraActions: @Composable RowScope.() -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(colorScheme.background)
    ) {
        // Bottom Content Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = if (isSearchActive) Arrangement.Start else Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearchActive) {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                // Search Bar
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Back",
                    tint = colorScheme.onBackground,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp)
                        .clickable {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onSearchActiveChange(false)
                        }
                )
                Spacer(modifier = Modifier.width(16.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search...", color = colorScheme.onSurfaceVariant) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = colorScheme.onBackground,
                        unfocusedTextColor = colorScheme.onBackground
                    ),
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable {
                                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                    onSearchQueryChange("")
                                }
                            )
                        }
                    }
                )
            } else {
                // Left: Title
                Text(
                    text = title,
                    color = colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                // Right: Person (conditional), Search/Bell
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (showName) {
                        var selectedMember by remember { mutableStateOf("Ashok") }
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val isLoggedIn = currentUser != null

                        if (isLoggedIn) {
                            MemberSwitcher(
                                selectedMember = selectedMember,
                                onMemberSelected = { selectedMember = it }
                            )
                        }
                    }

                    extraActions()

                    // Action button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable {
                                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                if (showSearchIcon) onSearchActiveChange(true)
                            }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (showSearchIcon) Icons.Default.Search else Icons.Default.Notifications,
                            contentDescription = if (showSearchIcon) "Search" else "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (isSearchActive) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = colorScheme.outlineVariant
            )
        }
    }
}