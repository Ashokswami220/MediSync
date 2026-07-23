package com.example.medisync.ui.screens.common

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.medisync.data.SettingsManager
import com.example.medisync.data.local.ContactConfig
import com.example.medisync.repo.AuthRepository
import com.example.medisync.ui.components.AppearanceBottomSheet
import com.example.medisync.ui.components.DeleteAccountDialog
import com.example.medisync.ui.components.DeleteDataDialog
import com.example.medisync.ui.components.LanguageBottomSheet
import com.example.medisync.ui.navigation.TopBar
import com.example.medisync.ui.theme.LocalAppearance
import com.example.medisync.utils.GlobalToastManager
import com.example.medisync.utils.HapticHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToAboutUs: () -> Unit = {},
    onSignOut: () -> Unit = {},
    profileViewModel: ProfileViewModel = koinViewModel()
) {
    val authRepo: AuthRepository = koinInject()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val profileState by profileViewModel.profileState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLoggedIn = currentUser != null

    var name by remember { mutableStateOf("Guest") }
    var number by remember { mutableStateOf("Tap to login") }
    var email by remember { mutableStateOf("") }

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val p = (profileState as ProfileState.Success).profile
            name = "${p.firstName} ${p.lastName}".trim()
            number = p.phoneNumber
            email = currentUser?.email ?: ""
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            profileViewModel.loadProfile()
        } else {
            name = "Guest"
            number = "Tap to login"
        }
    }

    val (showDeleteDataDialog, setShowDeleteDataDialog) = remember { mutableStateOf(false) }
    val (showDeleteAccountDialog, setShowDeleteAccountDialog) = remember { mutableStateOf(false) }

    var showAppearanceSheet by remember { mutableStateOf(false) }
    var currentAppearance by LocalAppearance.current
    val settingsManager = remember { SettingsManager(context) }
    val isHaptic by settingsManager.hapticsFlow.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()

    var showLanguageSheet by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf("English") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        TopBar(
            title = "Settings",
            showName = false,
            showSearchIcon = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Profile Card (Blueprint Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
                    .border(1.dp, colorScheme.outlineVariant)
                    .clickable {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        if (isLoggedIn) {
                            onNavigateToEditProfile()
                        } else {
                            onNavigateToLogin()
                        }
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(colorScheme.secondary.copy(alpha = 0.2f))
                            .border(1.dp, colorScheme.outlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isLoggedIn) name else "Guest",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isLoggedIn) number else "Tap to login to access profile",
                            fontSize = 15.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                        if (isLoggedIn && email.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = email,
                                fontSize = 13.sp,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Edit Profile",
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "APP PREFERENCES",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Preferences Group
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
                    .border(1.dp, colorScheme.outlineVariant)
            ) {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "App Language",
                    value = currentLanguage,
                    onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        showLanguageSheet = true
                    }
                )
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Appearance",
                    value = currentAppearance,
                    onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        showAppearanceSheet = true
                    }
                )
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                SettingsSwitchItem(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Feedback",
                    checked = isHaptic,
                    onCheckedChange = { checked ->
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        coroutineScope.launch { settingsManager.setHapticsEnabled(checked) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SUPPORT",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Support Group
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
                    .border(1.dp, colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .diagonalHatch(
                                colorScheme.outlineVariant.copy(alpha = 0.5f), spacing = 6.dp
                            )
                            .clickable {
                                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                val intent = Intent(
                                    Intent.ACTION_VIEW,

                                    ContactConfig.socialLinks.telegram.toUri()
                                )
                                try {
                                    context.startActivity(intent)
                                } catch (_: ActivityNotFoundException) {
                                    // Handle missing browser
                                }
                            }
                            .padding(16.dp), contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Email, contentDescription = "Contact Us",
                                tint = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Contact Us", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                    VerticalDivider(color = colorScheme.outlineVariant)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .diagonalHatch(
                                colorScheme.outlineVariant.copy(alpha = 0.5f), spacing = 6.dp
                            )
                            .clickable {
                                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            }
                            .padding(16.dp), contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Star, contentDescription = "Rate App",
                                tint = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Rate App", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                    VerticalDivider(color = colorScheme.outlineVariant)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .diagonalHatch(
                                colorScheme.outlineVariant.copy(alpha = 0.5f), spacing = 6.dp
                            )
                            .clickable {
                                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            }
                            .padding(16.dp), contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Share, contentDescription = "Share",
                                tint = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Share", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .diagonalHatch(
                            colorScheme.outlineVariant.copy(alpha = 0.5f), spacing = 6.dp
                        )
                ) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About Us",
                        showArrow = true,
                        onClick = { 
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onNavigateToAboutUs()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DATA & ACCOUNT",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Danger Zone Group
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface)
                    .border(1.dp, colorScheme.outlineVariant)
            ) {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Delete My Data",
                    titleColor = if (isLoggedIn) colorScheme.error else colorScheme.onSurfaceVariant,
                    showArrow = false,
                    onClick = {
                        if (isLoggedIn) {
                            HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                            setShowDeleteDataDialog(true)
                        }
                    }
                )
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                SettingsItem(
                    icon = Icons.Default.PersonRemove,
                    title = "Delete My Account",
                    titleColor = if (isLoggedIn) colorScheme.error else colorScheme.onSurfaceVariant,
                    showArrow = false,
                    onClick = {
                        if (isLoggedIn) {
                            HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                            setShowDeleteAccountDialog(true)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoggedIn) {
                OutlinedButton(
                    onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                        coroutineScope.launch {
                            authRepo.signOut()
                            onSignOut()
                            GlobalToastManager.showToast(
                                message = "You have Logged out successfully",
                                icon = Icons.AutoMirrored.Filled.Logout
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(1.dp, colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showLanguageSheet) {
            LanguageBottomSheet(
                currentLanguage = currentLanguage,
                onDismissRequest = { showLanguageSheet = false },
                onLanguageSelected = { language ->
                    currentLanguage = language
                    showLanguageSheet = false
                }
            )
        }

        if (showAppearanceSheet) {
            AppearanceBottomSheet(
                currentAppearance = currentAppearance,
                onDismiss = { showAppearanceSheet = false },
                onAppearanceSelected = { appearance ->
                    currentAppearance = appearance
                }
            )
        }

        if (showDeleteDataDialog) {
            DeleteDataDialog(
                onConfirm = {
                    profileViewModel.deleteData {
                        // Data deleted
                    }
                },
                onDismiss = { setShowDeleteDataDialog(false) }
            )
        }

        if (showDeleteAccountDialog) {
            DeleteAccountDialog(
                onConfirm = {
                    profileViewModel.deleteAccount {
                        GlobalToastManager.showToast(
                            message = "Your Account has been deleted",
                            icon = Icons.Default.Delete
                        )
                        onSignOut()
                    }
                },
                onDismiss = { setShowDeleteAccountDialog(false) }
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    showArrow: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = titleColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.secondary,
            )
        )
    }
}


fun Modifier.diagonalHatch(
    color: Color, strokeWidth: Dp = 1.dp,
    spacing: Dp = 6.dp
) = this.drawBehind {
    val strokeWidthPx = strokeWidth.toPx()
    val spacingPx = spacing.toPx()

    clipRect {
        var x = -size.height
        while (x < size.width) {
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x + size.height, size.height),
                strokeWidth = strokeWidthPx
            )
            x += spacingPx
        }
    }
}