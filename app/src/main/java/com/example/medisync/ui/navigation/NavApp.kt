package com.example.medisync.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medisync.model.UserRole
import com.example.medisync.ui.screens.onboarding.UserRoleDecideScreen
import com.example.medisync.ui.screens.admin.AdminHomeScreen
import com.example.medisync.ui.screens.admin.UploadDataScreen
import com.example.medisync.ui.screens.admin.UserListScreen
import com.example.medisync.ui.screens.admin.UserDetailScreen
import com.example.medisync.ui.screens.user.UserHomeScreen
import com.example.medisync.ui.screens.user.UserReportsScreen
import com.example.medisync.ui.screens.common.ReportDetailScreen
import com.example.medisync.ui.screens.common.ReportDetailScreen2
import com.example.medisync.ui.screens.common.SettingsScreen
import com.example.medisync.ui.screens.common.EditProfileScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

const val ANIM_DURATION = 400
val ANIM_EASING = FastOutSlowInEasing

@OptIn(ExperimentalMaterial3Api::class)
@android.annotation.SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavApp(
    startDestination: String = Routes.ROLE_DECIDE
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination
    
    var currentRole by rememberSaveable { mutableStateOf(UserRole.NONE) }
    val hazeState = remember { HazeState() }

    // List of screens that should show the bottom bar
    val bottomBarRoutes = listOf(
        Routes.ADMIN_HOME, Routes.USER_LIST, 
        Routes.SETTINGS, Routes.USER_HOME, Routes.USER_REPORTS
    )

    val navigateToDest = { route: String ->
        if (currentRoute != route) {
            if (route in bottomBarRoutes) {
                // Standard bottom nav behavior: pop up to the start destination (ROLE_DECIDE)
                // This keeps the stack depth at exactly 2 for all main tabs, perfectly avoiding bugs.
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            } else {
                // For sub-screens (like Upload Data), just push normally
                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
        }
    }
    val shouldShowBottomBar = currentRoute in bottomBarRoutes && currentRole != UserRole.NONE

    val homeRoute = if (currentRole == UserRole.ADMIN) Routes.ADMIN_HOME else Routes.USER_HOME
    // If we are on a bottom bar route that is NOT home, pressing back should go to home
    val isBottomBarTabButNotHome = currentRoute in bottomBarRoutes && currentRoute != homeRoute
    BackHandler(enabled = isBottomBarTabButNotHome) {
        navigateToDest(homeRoute)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                GlassNavBar(
                    role = currentRole,
                    currentRoute = currentRoute,
                    onNavigate = navigateToDest,
                    hazeState = hazeState
                )
            }
        }
    ) { _ ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    if (targetState.destination.route in bottomBarRoutes) {
                        fadeIn(animationSpec = tween(ANIM_DURATION))
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                        )
                    }
                },
                exitTransition = {
                    if (initialState.destination.route in bottomBarRoutes) {
                        fadeOut(animationSpec = tween(ANIM_DURATION))
                    } else {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                        )
                    }
                },
                popEnterTransition = {
                    if (targetState.destination.route in bottomBarRoutes) {
                        fadeIn(animationSpec = tween(ANIM_DURATION))
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { -it / 3 },
                            animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                        )
                    }
                },
                popExitTransition = {
                    if (initialState.destination.route in bottomBarRoutes) {
                        fadeOut(animationSpec = tween(ANIM_DURATION))
                    } else {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                        )
                    }
                }
            ) {
                // ================== ROLE DECISION ================
                composable(route = Routes.ROLE_DECIDE) {
                    UserRoleDecideScreen(
                        onRoleSelected = { role ->
                            currentRole = role
                            if (role == UserRole.ADMIN) {
                                navigateToDest(Routes.ADMIN_HOME)
                            } else {
                                navigateToDest(Routes.USER_HOME)
                            }
                        }
                    )
                }

                // ================== ONBOARDING ==================
                composable(route = Routes.CAROUSEL) {
                    // CarouselScreen(onNavigate = { navigateToDest(Routes.LANG_SELECTION) })
                }
                composable(route = Routes.LANG_SELECTION) {
                    // LangSelectionScreen(onNavigate = { navigateToDest(Routes.LOGIN) })
                }

                // ==================== AUTH ======================
                composable(route = Routes.LOGIN) {
                    // LoginScreen(...)
                }

                // ================== ADMIN =======================
                composable(route = Routes.ADMIN_HOME) {
                    AdminHomeScreen()
                }
                composable(route = Routes.UPLOAD_DATA) {
                    UploadDataScreen(onBackClick = { navController.popBackStack() })
                }
                composable(route = Routes.USER_LIST) {
                    UserListScreen(
                        onNavigateToUserDetail = { navigateToDest(Routes.USER_DETAIL) }
                    )
                }
                composable(route = Routes.USER_DETAIL) {
                    UserDetailScreen(
                        onBackClick = { navController.popBackStack() },
                        onNavigateToReportDetail = { navigateToDest(Routes.REPORT_DETAIL) }
                    )
                }

                // =================== USER =======================
                composable(route = Routes.USER_HOME) {
                    UserHomeScreen(onNavigateToReportDetail = { navigateToDest(Routes.REPORT_DETAIL) })
                }
                composable(route = Routes.USER_REPORTS) {
                    UserReportsScreen(onNavigateToReportDetail = { navigateToDest(Routes.REPORT_DETAIL_2) })
                }
                composable(route = Routes.REPORT_DETAIL) {
                    ReportDetailScreen(onBackClick = { navController.popBackStack() })
                }
                composable(route = Routes.REPORT_DETAIL_2) {
                    ReportDetailScreen2(onBackClick = { navController.popBackStack() })
                }

                // ================== COMMON ======================
                composable(route = Routes.ABOUT_US) {
                    // AboutUsScreen(onBackClick = { navController.popBackStack() })
                }
                composable(route = Routes.SETTINGS) {
                    SettingsScreen(onNavigateToEditProfile = { navigateToDest(Routes.EDIT_PROFILE) })
                }
                composable(route = Routes.EDIT_PROFILE) {
                    EditProfileScreen(onBackClick = { navController.popBackStack() })
                }
            }
        }
    }
}
