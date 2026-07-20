package com.example.medisync.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.ui.platform.LocalContext
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.example.medisync.R
import com.example.medisync.utils.GlobalToastManager

enum class AuthStep(val title: String) {
    LANGUAGE("Choose an\nLanguage"),
    LOGIN("Login"),
    INFO("Enter\ninformation")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthFlowScreen(
    onNavigateNext: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var currentStep by remember { mutableStateOf(AuthStep.LANGUAGE) }
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
                onNavigateNext()
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

    BackHandler(enabled = currentStep != AuthStep.LANGUAGE) {
        if (currentStep == AuthStep.INFO) {
            (context as? android.app.Activity)?.finishAffinity()
        } else {
            currentStep = when (currentStep) {
                AuthStep.INFO -> AuthStep.LOGIN
                AuthStep.LOGIN -> AuthStep.LANGUAGE
                AuthStep.LANGUAGE -> AuthStep.LANGUAGE
            }
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

                // Bottom Sheets Container
                AuthSheetsStack(
                    currentStep = currentStep,
                    onStepChange = { currentStep = it },
                    onGoogleSignIn = { viewModel.signInWithGoogle() },
                    onCompleteProfile = { f, l, p -> viewModel.completeProfile(f, l, p) },
                    onLogout = { viewModel.signOut() },
                    isLoading = authState is AuthState.Loading
                )
            }

            // Skip Button (Only visible on LOGIN sheet)
            if (currentStep == AuthStep.LOGIN) {
                TextButton(
                    onClick = onNavigateNext,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Skip",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AuthSheetsStack(
    currentStep: AuthStep,
    onStepChange: (AuthStep) -> Unit,
    onGoogleSignIn: () -> Unit,
    onCompleteProfile: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Language Sheet
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(600)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(600))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    LangSelectionSheet(onNext = { onStepChange(AuthStep.LOGIN) })
                }
            }
        }

        // 2. Login Sheet
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

        // 3. Info sheet
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

@Composable
fun AuthBackgroundShapes() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Top Area
        val topPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height * 0.45f)
            quadraticTo(width * 0.5f, height * 0.55f, 0f, height * 0.4f)
            close()
        }
        drawPath(topPath, secondaryColor)

        // Left Circle
        drawCircle(
            color = primaryColor,
            radius = width * 0.4f,
            center = Offset(-width * 0.1f, height * 0.25f)
        )

        // Right Curve
        val rightPath = Path().apply {
            moveTo(width, height * 0.2f)
            quadraticTo(width * 0.6f, height * 0.3f, width * 0.65f, height * 0.6f)
            lineTo(width, height * 0.6f)
            close()
        }
        drawPath(rightPath, tertiaryColor)
    }
}

@Composable
fun LangSelectionSheet(onNext: () -> Unit) {
    var selectedLanguage by remember { mutableStateOf("English") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LanguageCard(
                modifier = Modifier.fillMaxWidth(),
                title = "English",
                subtitle = "Select English as your primary language",
                iconText = "A",
                isSelected = selectedLanguage == "English",
                onClick = { selectedLanguage = "English" }
            )
            LanguageCard(
                modifier = Modifier.fillMaxWidth(),
                title = "हिंदी",
                subtitle = "अपनी प्राथमिक भाषा के रूप में हिंदी चुनें",
                iconText = "अ",
                isSelected = selectedLanguage == "Hindi",
                onClick = { selectedLanguage = "Hindi" }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(
                "Done", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LanguageCard(
    modifier: Modifier = Modifier, title: String, subtitle: String, iconText: String,
    isSelected: Boolean, onClick: () -> Unit
) {
    val borderColor =
        if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
    val bgColor =
        if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f) else Color.White

    Row(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                iconText, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Arrow Icon
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Select",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LoginSheet(onGoogleSignIn: () -> Unit, isLoading: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Color.Black.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(32.dp)
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.login_svg2),
                contentDescription = "Login Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            border = BorderStroke(
                1.dp, MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "G", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Sign in with Google", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp
            )
        }
    }
}

@Composable
fun InfoSheet(onDone: (String, String, String) -> Unit, onLogout: () -> Unit, isLoading: Boolean) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var showErrorFirstName by remember { mutableStateOf(false) }
    var showErrorLastName by remember { mutableStateOf(false) }
    var showErrorPhone by remember { mutableStateOf(false) }

    val continueShake = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun triggerShake(animatable: Animatable<Float, AnimationVector1D>) {
        coroutineScope.launch {
            for (i in 0..2) {
                animatable.animateTo(15f, animationSpec = tween(50))
                animatable.animateTo(-15f, animationSpec = tween(50))
            }
            animatable.animateTo(0f, animationSpec = tween(50))
        }
    }

    val email = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown Email"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "You are signed in as",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = email,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Log out",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = {
                        firstName = it
                        showErrorFirstName = false
                    },
                    isError = showErrorFirstName,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("First Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.secondary
                    )
                )
                if (showErrorFirstName) {
                    Text("Enter first name", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = lastName,
                    onValueChange = {
                        lastName = it
                        showErrorLastName = false
                    },
                    isError = showErrorLastName,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Last Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.secondary
                    )
                )
                if (showErrorLastName) {
                    Text("Enter last name", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.length <= 10) phoneNumber = it
                showErrorPhone = false
            },
            isError = showErrorPhone,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Phone Number") },
            leadingIcon = {
                Text(
                    "+91", modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.secondary
            )
        )
        if (showErrorPhone) {
            Text(
                text = if (phoneNumber.isEmpty()) "Enter phone number" else "Enter correct number",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                var hasError = false
                if (firstName.isBlank()) {
                    showErrorFirstName = true
                    hasError = true
                }
                if (lastName.isBlank()) {
                    showErrorLastName = true
                    hasError = true
                }
                if (phoneNumber.length != 10) {
                    showErrorPhone = true
                    hasError = true
                }
                
                if (hasError) {
                    triggerShake(continueShake)
                } else {
                    onDone(firstName, lastName, phoneNumber)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = continueShake.value.dp)
                .height(56.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(
                "Continue", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
