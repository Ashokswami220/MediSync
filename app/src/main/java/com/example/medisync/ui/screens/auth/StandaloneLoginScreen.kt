package com.example.medisync.ui.screens.auth

import android.widget.Toast
import com.example.medisync.utils.GlobalToastManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StandaloneLoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var currentStep by remember { 
        mutableStateOf(if (viewModel.authState.value is AuthState.NeedsInfo) AuthStep.INFO else AuthStep.LOGIN) 
    }
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                GlobalToastManager.showToast(
                    message = "You have successfully logged in",
                    icon = Icons.AutoMirrored.Filled.Login
                )
                viewModel.resetState()
                onLoginSuccess()
            }

            is AuthState.NeedsInfo -> {
                currentStep = AuthStep.INFO
            }

            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG)
                    .show()
                viewModel.resetState()
            }

            is AuthState.LoggedOut -> {
                currentStep = AuthStep.LOGIN
                viewModel.resetState()
            }

            else -> {}
        }
    }

    BackHandler(enabled = true) {
        if (currentStep == AuthStep.INFO) {
            (context as? android.app.Activity)?.finishAffinity()
        } else {
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuthBackgroundShapes()

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.25f),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            (slideInVertically(
                                animationSpec = tween(800)
                            ) { height -> height } + fadeIn(tween(800)))
                                .togetherWith(
                                    slideOutVertically(
                                        animationSpec = tween(800)
                                    ) { height -> -height } + fadeOut(tween(800)))
                        },
                        label = "header_animation"
                    ) { step ->
                        Text(
                            text = step.title,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 44.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                StandaloneSheetsStack(
                    currentStep = currentStep,
                    onGoogleSignIn = { viewModel.signInWithGoogle() },
                    onCompleteProfile = { f, l, p -> viewModel.completeProfile(f, l, p) },
                    onLogout = { viewModel.signOut() },
                    isLoading = authState is AuthState.Loading
                )
            }

            // Optional close button or back button
            if (currentStep != AuthStep.INFO) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp)
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StandaloneSheetsStack(
    currentStep: AuthStep,
    onGoogleSignIn: () -> Unit,
    onCompleteProfile: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Login Sheet
        AnimatedVisibility(
            visible = currentStep.ordinal >= AuthStep.LOGIN.ordinal,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(600)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(600))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 16.dp
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    LoginSheet(
                        onGoogleSignIn = onGoogleSignIn,
                        isLoading = isLoading
                    )
                }
            }
        }

        // Info sheet
        AnimatedVisibility(
            visible = currentStep.ordinal >= AuthStep.INFO.ordinal,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(600)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(600))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 24.dp
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    InfoSheet(
                        onDone = onCompleteProfile,
                        onLogout = onLogout,
                        isLoading = isLoading
                    )
                }
            }
        }
    }
}
