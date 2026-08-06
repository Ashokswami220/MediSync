package com.example.medisync.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.example.medisync.R
import com.example.medisync.utils.HapticHelper
import com.google.firebase.auth.FirebaseAuth

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
    selectedMember: String = "User",
    onMemberSelected: (String) -> Unit = {},
    members: List<String> = emptyList(),
    showAllOption: Boolean = false,
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
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val isLoggedIn = currentUser != null

                        if (isLoggedIn) {
                            val displayMembers = if (showAllOption) {
                                listOf("All") + members
                            } else {
                                members
                            }
                            MemberSwitcher(
                                selectedMember = selectedMember,
                                onMemberSelected = onMemberSelected,
                                members = displayMembers
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

@Composable
fun HomeTopBar(
    modifier: Modifier = Modifier,
    scrollFraction: Float = 0f,
    onBellClick: () -> Unit = {},
    selectedMember: String = "User",
    onMemberSelected: (String) -> Unit = {},
    members: List<String> = emptyList(),
    showAllOption: Boolean = false,
    showMemberSwitcher: Boolean = true
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
            alignment = Alignment.TopCenter,
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
                val currentUser = FirebaseAuth.getInstance().currentUser
                val isLoggedIn = currentUser != null

                if (isLoggedIn && showMemberSwitcher) {
                    val displayMembers = if (showAllOption) {
                        listOf("All") + members
                    } else {
                        members
                    }
                    MemberSwitcher(
                        selectedMember = selectedMember,
                        onMemberSelected = onMemberSelected,
                        members = displayMembers
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
