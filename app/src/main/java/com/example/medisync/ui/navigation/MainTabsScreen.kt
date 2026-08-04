package com.example.medisync.ui.navigation

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.example.medisync.model.UserRole
import com.example.medisync.ui.components.GlassNavBar
import com.example.medisync.ui.screens.admin.AdminHomeScreen
import com.example.medisync.ui.screens.admin.UploadDataDialog
import com.example.medisync.ui.screens.admin.UserListScreen
import com.example.medisync.ui.screens.common.DeleteActionMode
import com.example.medisync.ui.screens.common.ProfileViewModel
import com.example.medisync.ui.screens.common.SettingsScreen
import com.example.medisync.ui.screens.user.UserHomeScreen
import com.example.medisync.ui.screens.user.UserReportsScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainTabsScreen(
    currentRole: UserRole,
    profileViewModel: ProfileViewModel,
    displayMembers: List<String>,
    bloodPressure: String,
    bloodType: String,
    bloodSugar: String,
    selectedHomeMember: String,
    onHomeMemberSelected: (String) -> Unit,
    selectedReportMember: String,
    onReportMemberSelected: (String) -> Unit,
    onNavigateToReportDetail: (name: String, url: String) -> Unit,
    onNavigateToUserDetail: (uid: String) -> Unit,
    onNavigateToUserProfile: (uid: String) -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToDeleteAction: (mode: DeleteActionMode) -> Unit,
    onNavigateToAboutUs: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val hazeState = remember { HazeState() }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    var currentTabRoute by rememberSaveable {
        mutableStateOf(
            if (currentRole == UserRole.ADMIN) Routes.ADMIN_HOME else Routes.USER_HOME
        )
    }
    var showUploadDialog by rememberSaveable { mutableStateOf(false) }
    var uploadButtonCenter by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(currentRole) {
        if (currentRole == UserRole.USER && (currentTabRoute == Routes.ADMIN_HOME || currentTabRoute == Routes.USER_LIST)) {
            currentTabRoute = Routes.USER_HOME
        } else if (currentRole == UserRole.ADMIN && (currentTabRoute == Routes.USER_HOME || currentTabRoute == Routes.USER_REPORTS)) {
            currentTabRoute = Routes.ADMIN_HOME
        }
    }

    val homeRoute = if (currentRole == UserRole.ADMIN) Routes.ADMIN_HOME else Routes.USER_HOME
    val isBottomBarTabButNotHome = currentTabRoute != homeRoute
    BackHandler(enabled = isBottomBarTabButNotHome) {
        if (currentTabRoute == Routes.USER_REPORTS) {
            onReportMemberSelected("All")
        }
        currentTabRoute = homeRoute
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                GlassNavBar(
                    role = currentRole,
                    currentRoute = currentTabRoute,
                    onNavigate = { route ->
                        if (route == Routes.UPLOAD_DATA) {
                            showUploadDialog = !showUploadDialog
                        } else {
                            if (route != Routes.USER_REPORTS && currentTabRoute == Routes.USER_REPORTS) {
                                onReportMemberSelected("All")
                            }
                            currentTabRoute = route
                        }
                    },
                    hazeState = hazeState,
                    onUploadButtonPositioned = { uploadButtonCenter = it }
                )
            }
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            ) {
                AnimatedContent(
                    targetState = currentTabRoute,
                    transitionSpec = {
                        fadeIn(tween(ANIM_DURATION)) togetherWith fadeOut(tween(ANIM_DURATION))
                    },
                    label = "tab_switch"
                ) { targetRoute ->
                    when (targetRoute) {
                        Routes.ADMIN_HOME -> {
                            if (currentRole == UserRole.ADMIN) AdminHomeScreen() else UserHomeScreen(
                                onNavigateToReportDetail = onNavigateToReportDetail,
                                onNavigateToReports = { currentTabRoute = Routes.USER_REPORTS },
                                selectedMember = selectedHomeMember,
                                onMemberSelected = onHomeMemberSelected,
                                members = displayMembers,
                                bloodPressure = bloodPressure,
                                bloodType = bloodType,
                                bloodSugar = bloodSugar,
                                onNavigateToLogin = onNavigateToLogin,
                                onRefreshProfile = { profileViewModel.loadProfile() }
                            )
                        }

                        Routes.USER_LIST -> {
                            if (currentRole == UserRole.ADMIN) {
                                UserListScreen(
                                    onNavigateToUserDetail = onNavigateToUserDetail,
                                    onNavigateToUserProfile = onNavigateToUserProfile
                                )
                            } else {
                                UserHomeScreen(
                                    onNavigateToReportDetail = onNavigateToReportDetail,
                                    onNavigateToReports = { currentTabRoute = Routes.USER_REPORTS },
                                    selectedMember = selectedHomeMember,
                                    onMemberSelected = onHomeMemberSelected,
                                    members = displayMembers,
                                    bloodPressure = bloodPressure,
                                    bloodType = bloodType,
                                    bloodSugar = bloodSugar,
                                    onNavigateToLogin = onNavigateToLogin,
                                    onRefreshProfile = { profileViewModel.loadProfile() }
                                )
                            }
                        }

                        Routes.USER_HOME -> UserHomeScreen(
                            onNavigateToReportDetail = onNavigateToReportDetail,
                            onNavigateToReports = { currentTabRoute = Routes.USER_REPORTS },
                            selectedMember = selectedHomeMember,
                            onMemberSelected = onHomeMemberSelected,
                            members = displayMembers,
                            bloodPressure = bloodPressure,
                            bloodType = bloodType,
                            bloodSugar = bloodSugar,
                            onNavigateToLogin = onNavigateToLogin,
                            onRefreshProfile = { profileViewModel.loadProfile() }
                        )

                        Routes.USER_REPORTS -> UserReportsScreen(
                            onNavigateToReportDetail = onNavigateToReportDetail,
                            selectedMember = selectedReportMember,
                            onMemberSelected = onReportMemberSelected,
                            members = displayMembers,
                            onNavigateToLogin = onNavigateToLogin
                        )

                        Routes.SETTINGS -> SettingsScreen(
                            onNavigateToEditProfile = onNavigateToEditProfile,
                            onNavigateToDeleteAction = onNavigateToDeleteAction,
                            onNavigateToAboutUs = onNavigateToAboutUs,
                            onNavigateToLogin = onNavigateToLogin,
                            onSignOut = onSignOut
                        )
                    }
                }
            }
        }

        // Upload Dialog Overlay
        AnimatedVisibility(
            visible = showUploadDialog,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            UploadDataDialog(
                onDismiss = { showUploadDialog = false },
                buttonCenter = uploadButtonCenter
            )
        }
    }
}
