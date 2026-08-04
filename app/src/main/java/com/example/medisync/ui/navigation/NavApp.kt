package com.example.medisync.ui.navigation

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medisync.data.SettingsManager
import com.example.medisync.model.MemberVitals
import com.example.medisync.model.UserRole
import com.example.medisync.ui.components.UploadProgressToast
import com.example.medisync.ui.screens.admin.UserDetailScreen
import com.example.medisync.ui.screens.admin.UserProfileScreen
import com.example.medisync.ui.screens.auth.AuthFlowScreen
import com.example.medisync.ui.screens.auth.AuthState
import com.example.medisync.ui.screens.auth.AuthViewModel
import com.example.medisync.ui.screens.auth.StandaloneLoginScreen
import com.example.medisync.ui.screens.common.AboutUsScreen
import com.example.medisync.ui.screens.common.DeleteActionMode
import com.example.medisync.ui.screens.common.DeleteActionScreen
import com.example.medisync.ui.screens.common.EditProfileScreen
import com.example.medisync.ui.screens.common.ProfileState
import com.example.medisync.ui.screens.common.ProfileViewModel
import com.example.medisync.ui.screens.common.ReportDetailScreen
import com.example.medisync.ui.screens.onboarding.CarouselScreen
import com.example.medisync.utils.UploadManager
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

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

    val settingsManager = koinInject<SettingsManager>()
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

    var selectedHomeMember by rememberSaveable {
        mutableStateOf("User")
    }
    var selectedReportMember by rememberSaveable {
        mutableStateOf("All")
    }

    LaunchedEffect(displayMembers) {
        if (selectedReportMember != "All" && selectedReportMember !in displayMembers) {
            selectedReportMember = "All"
        }
        if (selectedHomeMember !in displayMembers) {
            selectedHomeMember = displayMembers.firstOrNull() ?: "User"
        }
    }

    val currentProfile = (profileState as? ProfileState.Success)?.profile
    val mainUserName = currentProfile?.firstName?.ifEmpty { "User" } ?: "User"
    val isMainUser = selectedHomeMember == mainUserName
    
    val currentVitals = if (isMainUser) {
        MemberVitals(
            bloodType = currentProfile?.bloodType ?: "",
            bloodPressure = currentProfile?.bloodPressure ?: "",
            bloodSugar = currentProfile?.bloodSugar ?: ""
        )
    } else {
        currentProfile?.memberVitals?.get(selectedHomeMember) ?: MemberVitals()
    }

    val bloodPressure = currentVitals.bloodPressure
    val bloodType = currentVitals.bloodType
    val bloodSugar = currentVitals.bloodSugar

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
                    if (targetState.destination.route == Routes.MAIN_TABS) {
                        fadeIn(animationSpec = tween(ANIM_DURATION))
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                        )
                    }
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
                    } else if (targetState.destination.route == Routes.MAIN_TABS) {
                        fadeIn(animationSpec = tween(ANIM_DURATION))
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
                        when (targetState.destination.route) {
                            Routes.AUTH_FLOW -> {
                                slideOutVertically(
                                    targetOffsetY = { height -> -height / 5 },
                                    animationSpec = tween(600)
                                ) + fadeOut(tween(600))
                            }

                            Routes.MAIN_TABS -> {
                                slideOutHorizontally(
                                    targetOffsetX = { -it / 3 },
                                    animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                                )
                            }

                            else -> {
                                slideOutHorizontally(
                                    targetOffsetX = { width -> -width / 3 }, animationSpec = tween(
                                        ANIM_DURATION, easing = ANIM_EASING
                                    )
                                )
                            }
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
                        if (targetState.destination.route == Routes.MAIN_TABS) {
                            slideOutHorizontally(
                                targetOffsetX = { -it / 3 },
                                animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                            )
                        } else {
                            slideOutVertically(
                                targetOffsetY = { height -> -height }, animationSpec = tween(600)
                            ) + fadeOut(tween(600))
                        }
                    }
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    val settingsManager = koinInject<SettingsManager>()

                    AuthFlowScreen(
                        onNavigateNext = {
                            coroutineScope.launch {
                                settingsManager.setOnboardingCompleted(true)
                            }
                            navController.navigate(Routes.MAIN_TABS) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                }

                // ================== MAIN TABS ===================
                composable(route = Routes.MAIN_TABS) {
                    MainTabsScreen(
                        currentRole = currentRole,
                        profileViewModel = profileViewModel,
                        displayMembers = displayMembers,
                        bloodPressure = bloodPressure,
                        bloodType = bloodType,
                        bloodSugar = bloodSugar,
                        selectedHomeMember = selectedHomeMember,
                        onHomeMemberSelected = { selectedHomeMember = it },
                        selectedReportMember = selectedReportMember,
                        onReportMemberSelected = { selectedReportMember = it },
                        onNavigateToReportDetail = { name, url ->
                            selectedReportName = name
                            selectedReportUrl = url
                            navigateToDest(Routes.REPORT_DETAIL)
                        },
                        onNavigateToUserDetail = { uid ->
                            selectedUserUid = uid
                            navigateToDest(Routes.USER_DETAIL)
                        },
                        onNavigateToUserProfile = { uid ->
                            selectedUserUid = uid
                            navigateToDest(Routes.ADMIN_USER_PROFILE)
                        },
                        onNavigateToEditProfile = { navigateToDest(Routes.EDIT_PROFILE) },
                        onNavigateToDeleteAction = { mode ->
                            navigateToDest("${Routes.DELETE_ACTION}/${mode.name}")
                        },
                        onNavigateToAboutUs = { navigateToDest(Routes.ABOUT_US) },
                        onSignOut = {
                            coroutineScope.launch {
                                settingsManager.setUserRole("USER")
                                activeRole = UserRole.USER
                            }
                            authViewModel.signOut()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = { navigateToDest(Routes.LOGIN) }
                    )
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
                            },
                            onTopBarClick = {
                                navigateToDest(Routes.ADMIN_USER_PROFILE)
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }
                composable(route = Routes.ADMIN_USER_PROFILE) {
                    if (currentRole == UserRole.ADMIN) {
                        UserProfileScreen(
                            userUid = selectedUserUid,
                            onBackClick = { navController.popBackStack() }
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
                    route = "${Routes.DELETE_ACTION}/{mode}",
                    arguments = listOf(navArgument("mode") { type = NavType.StringType })
                ) { backStackEntry ->
                    val modeString = backStackEntry.arguments?.getString("mode") ?: "ACCOUNT"
                    val mode = try {
                        DeleteActionMode.valueOf(modeString)
                    } catch (_: Exception) {
                        DeleteActionMode.ACCOUNT
                    }
                    DeleteActionScreen(
                        mode = mode,
                        onAccountDeleted = {
                            coroutineScope.launch {
                                settingsManager.setUserRole("USER")
                            }
                            authViewModel.signOut()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        },
                        onDataDeleted = {
                            navController.popBackStack()
                        }
                    )
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
                        if (targetState.destination.route == Routes.MAIN_TABS) {
                            slideOutHorizontally(
                                targetOffsetX = { -it / 3 },
                                animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                            )
                        } else {
                            slideOutVertically(
                                targetOffsetY = { height -> -height }, animationSpec = tween(600)
                            ) + fadeOut(tween(600))
                        }
                    }
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    val settingsManager = koinInject<SettingsManager>()

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
                            navController.navigate(Routes.MAIN_TABS) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
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
