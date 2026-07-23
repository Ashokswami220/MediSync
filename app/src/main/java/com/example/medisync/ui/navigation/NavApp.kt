package com.example.medisync.ui.navigation

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medisync.data.SettingsManager
import com.example.medisync.model.UserRole
import com.example.medisync.ui.components.UploadProgressToast
import com.example.medisync.ui.screens.admin.AdminHomeScreen
import com.example.medisync.ui.screens.admin.UploadDataDialog
import com.example.medisync.ui.screens.admin.UserDetailScreen
import com.example.medisync.ui.screens.admin.UserListScreen
import com.example.medisync.ui.screens.auth.AuthFlowScreen
import com.example.medisync.ui.screens.auth.AuthState
import com.example.medisync.ui.screens.auth.AuthViewModel
import com.example.medisync.ui.screens.auth.StandaloneLoginScreen
import com.example.medisync.ui.screens.common.AboutUsScreen
import com.example.medisync.ui.screens.common.EditProfileScreen
import com.example.medisync.ui.screens.common.ProfileState
import com.example.medisync.ui.screens.common.ProfileViewModel
import com.example.medisync.ui.screens.common.ReportDetailScreen
import com.example.medisync.ui.screens.common.SettingsScreen
import com.example.medisync.ui.screens.onboarding.CarouselScreen
import com.example.medisync.ui.screens.user.UserHomeScreen
import com.example.medisync.ui.screens.user.UserReportsScreen
import com.example.medisync.utils.UploadManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

const val ANIM_DURATION = 400
val ANIM_EASING = FastOutSlowInEasing

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavApp(
    startDestination: String = Routes.MAIN_TABS
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination

    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val cachedRoleString by settingsManager.userRoleFlow.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    // Prevent flicker while DataStore loads initial value
    if (cachedRoleString == null) {
        return
    }

    var activeRole by rememberSaveable { mutableStateOf<UserRole?>(null) }
    val currentRole =
        activeRole ?: runCatching { UserRole.valueOf(cachedRoleString!!) }.getOrDefault(
            UserRole.USER
        )

    val hazeState = remember { HazeState() }

    val authViewModel = koinViewModel<AuthViewModel>()
    val authState by authViewModel.authState.collectAsState()

    val profileViewModel = koinViewModel<ProfileViewModel>()
    val profileState by profileViewModel.profileState.collectAsState()

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val role = (profileState as ProfileState.Success).profile.role
            activeRole = role
            settingsManager.setUserRole(role.name)
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.checkInitialAuthState()
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.NeedsInfo) {
            if (currentRoute != Routes.LOGIN) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        } else if (authState is AuthState.Success) {
            profileViewModel.loadProfile()
        }
    }

    val navigateToDest = { route: String ->
        if (currentRoute != route) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }

    var selectedUserUid by rememberSaveable { mutableStateOf("") }
    val displayMembers = remember(profileState) {
        if (profileState is ProfileState.Success) {
            val user = (profileState as ProfileState.Success).profile
            val name = user.firstName.ifEmpty { "User" }
            listOf(name) + user.members.filter { it != name }
        } else {
            listOf("User")
        }
    }

    val bloodPressure = (profileState as? ProfileState.Success)?.profile?.bloodPressure ?: ""
    val bloodType = (profileState as? ProfileState.Success)?.profile?.bloodType ?: ""
    val bloodSugar = (profileState as? ProfileState.Success)?.profile?.bloodSugar ?: ""

    var selectedMember by rememberSaveable(displayMembers) { 
        mutableStateOf(displayMembers.firstOrNull() ?: "User") 
    }
    var selectedReportName by rememberSaveable { mutableStateOf("") }
    var selectedReportUrl by rememberSaveable { mutableStateOf("") }
    val uploadStatus by UploadManager.status.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                    )
                },
                exitTransition = {
                    if (targetState.destination.route == Routes.LOGIN) {
                        ExitTransition.None
                    } else {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                        )
                    }
                },
                popEnterTransition = {
                    if (initialState.destination.route == Routes.LOGIN) {
                        EnterTransition.None
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { -it / 3 },
                            animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                        )
                    }
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                    )
                }
            ) {

                // ================== ONBOARDING ==================
                composable(
                    route = Routes.CAROUSEL,
                    exitTransition = {
                        if (targetState.destination.route == Routes.AUTH_FLOW) {
                            slideOutVertically(
                                targetOffsetY = { height -> -height / 5 },
                                animationSpec = tween(600)
                            ) + fadeOut(tween(600))
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { width -> -width / 3 }, animationSpec = tween(
                                    ANIM_DURATION, easing = ANIM_EASING
                                )
                            )
                        }
                    },
                    popEnterTransition = {
                        if (initialState.destination.route == Routes.AUTH_FLOW) {
                            slideInVertically(
                                initialOffsetY = { height -> -height / 5 },
                                animationSpec = tween(600)
                            ) + fadeIn(tween(600))
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { width -> -width / 3 }, animationSpec = tween(
                                    ANIM_DURATION, easing = ANIM_EASING
                                )
                            )
                        }
                    }
                ) {
                    CarouselScreen(onNavigate = {
                        navigateToDest(Routes.AUTH_FLOW)
                    })
                }

                // ==================== AUTH ======================
                composable(
                    route = Routes.AUTH_FLOW,
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { height -> height }, animationSpec = tween(600)
                        )
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { height -> height }, animationSpec = tween(600)
                        )
                    },
                    exitTransition = {
                        slideOutVertically(
                            targetOffsetY = { height -> -height }, animationSpec = tween(600)
                        ) + fadeOut(tween(600))
                    }
                ) {
                    val context = LocalContext.current
                    val coroutineScope = rememberCoroutineScope()
                    val settingsManager = remember { SettingsManager(context) }

                    AuthFlowScreen(
                        onNavigateNext = {
                            coroutineScope.launch {
                                settingsManager.setOnboardingCompleted(true)
                            }
                            navigateToDest(Routes.MAIN_TABS)
                        }
                    )
                }

                // ================== MAIN TABS ===================
                composable(route = Routes.MAIN_TABS) {
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

                    val homeRoute =
                        if (currentRole == UserRole.ADMIN) Routes.ADMIN_HOME else Routes.USER_HOME
                    val isBottomBarTabButNotHome = currentTabRoute != homeRoute
                    BackHandler(enabled = isBottomBarTabButNotHome) {
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
                                        fadeIn(tween(ANIM_DURATION)) togetherWith fadeOut(
                                            tween(ANIM_DURATION)
                                        )
                                    },
                                    label = "tab_switch"
                                ) { targetRoute ->
                                    when (targetRoute) {
                                        Routes.ADMIN_HOME -> {
                                            if (currentRole == UserRole.ADMIN) AdminHomeScreen() else UserHomeScreen(
                                                onNavigateToReportDetail = { name, url ->
                                                    selectedReportName = name
                                                    selectedReportUrl = url
                                                    navigateToDest(Routes.REPORT_DETAIL)
                                                },
                                                selectedMember = selectedMember,
                                                onMemberSelected = { selectedMember = it },
                                                members = displayMembers,
                                                bloodPressure = bloodPressure,
                                                bloodType = bloodType,
                                                bloodSugar = bloodSugar
                                            )
                                        }

                                        Routes.USER_LIST -> {
                                            if (currentRole == UserRole.ADMIN) {
                                                UserListScreen(
                                                    onNavigateToUserDetail = { uid ->
                                                        selectedUserUid = uid
                                                        navigateToDest(Routes.USER_DETAIL)
                                                    }
                                                )
                                            } else {
                                                UserHomeScreen(
                                                    onNavigateToReportDetail = { name, url ->
                                                        selectedReportName = name
                                                        selectedReportUrl = url
                                                        navigateToDest(Routes.REPORT_DETAIL)
                                                    },
                                                    selectedMember = selectedMember,
                                                    onMemberSelected = { selectedMember = it },
                                                    members = displayMembers,
                                                    bloodPressure = bloodPressure,
                                                    bloodType = bloodType,
                                                    bloodSugar = bloodSugar
                                                )
                                            }
                                        }

                                        Routes.USER_HOME -> UserHomeScreen(
                                            onNavigateToReportDetail = { name, url ->
                                                selectedReportName = name
                                                selectedReportUrl = url
                                                navigateToDest(
                                                    Routes.REPORT_DETAIL
                                                )
                                            },
                                            selectedMember = selectedMember,
                                            onMemberSelected = { selectedMember = it },
                                            members = displayMembers,
                                            bloodPressure = bloodPressure,
                                            bloodType = bloodType,
                                            bloodSugar = bloodSugar
                                        )

                                        Routes.USER_REPORTS -> UserReportsScreen(
                                            onNavigateToReportDetail = { name, url ->
                                                selectedReportName = name
                                                selectedReportUrl = url
                                                navigateToDest(
                                                    Routes.REPORT_DETAIL
                                                )
                                            },
                                            selectedMember = selectedMember,
                                            onMemberSelected = { selectedMember = it },
                                            members = displayMembers,
                                            onNavigateToLogin = { navigateToDest(Routes.LOGIN) }
                                        )

                                        Routes.SETTINGS -> SettingsScreen(
                                            onNavigateToEditProfile = {
                                                navigateToDest(
                                                    Routes.EDIT_PROFILE
                                                )
                                            },
                                            onNavigateToAboutUs = { navigateToDest(Routes.ABOUT_US) },
                                            onNavigateToLogin = { navigateToDest(Routes.LOGIN) },
                                            onSignOut = {
                                                coroutineScope.launch {
                                                    settingsManager.setUserRole("USER")
                                                    activeRole = UserRole.USER
                                                }
                                                navController.navigate(Routes.LOGIN) {
                                                    popUpTo(navController.graph.id) {
                                                        inclusive = true
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            } // end of Box hazeSource
                        } // end of Scaffold content lambda

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
                    } // End of outer Box wrapping Scaffold
                }

                // ================== OTHER SCREENS =======================
                composable(route = Routes.USER_DETAIL) {
                    if (currentRole == UserRole.ADMIN) {
                        UserDetailScreen(
                            userUid = selectedUserUid,
                            onBackClick = { navController.popBackStack() },
                            onNavigateToReportDetail = { name, url ->
                                selectedReportName = name
                                selectedReportUrl = url
                                navigateToDest(
                                    Routes.REPORT_DETAIL
                                )
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }
                composable(route = Routes.REPORT_DETAIL) {
                    ReportDetailScreen(
                        reportName = selectedReportName,
                        fileUrl = selectedReportUrl,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(route = Routes.ABOUT_US) {
                     AboutUsScreen(onBackClick = { navController.popBackStack() })
                }
                composable(route = Routes.EDIT_PROFILE) {
                    EditProfileScreen(onBackClick = { navController.popBackStack() })
                }
                composable(
                    route = Routes.LOGIN,
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { height -> height }, animationSpec = tween(600)
                        )
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { height -> height }, animationSpec = tween(600)
                        )
                    },
                    exitTransition = {
                        slideOutVertically(
                            targetOffsetY = { height -> -height }, animationSpec = tween(600)
                        ) + fadeOut(tween(600))
                    }
                ) {
                    val context = LocalContext.current
                    val coroutineScope = rememberCoroutineScope()
                    val settingsManager = remember { SettingsManager(context) }

                    StandaloneLoginScreen(
                        viewModel = authViewModel,
                        onNavigateBack = {
                            if (navController.previousBackStackEntry == null) {
                                navController.navigate(Routes.MAIN_TABS) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            } else {
                                navController.popBackStack()
                            }
                        },
                        onLoginSuccess = {
                            coroutineScope.launch {
                                settingsManager.setOnboardingCompleted(true)
                            }
                            navigateToDest(Routes.MAIN_TABS)
                        }
                    )
                }
            }

            UploadProgressToast(
                uploadStatus = uploadStatus,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp),
                onViewClick = { uid ->
                    selectedUserUid = uid
                    navigateToDest(Routes.USER_DETAIL)
                }
            )
        }
    }
}
