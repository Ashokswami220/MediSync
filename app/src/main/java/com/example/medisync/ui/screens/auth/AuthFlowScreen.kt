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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                viewModel.resetState()
                onNavigateNext()
            }
            is AuthState.NeedsInfo -> {
                currentStep = AuthStep.INFO
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    BackHandler(enabled = currentStep != AuthStep.LANGUAGE) {
        currentStep = when (currentStep) {
            AuthStep.INFO -> AuthStep.LOGIN
            AuthStep.LOGIN -> AuthStep.LANGUAGE
            AuthStep.LANGUAGE -> AuthStep.LANGUAGE
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
                    onNavigateNext = onNavigateNext,
                    onGoogleSignIn = { viewModel.signInWithGoogle() },
                    onCompleteProfile = { f, l, p -> viewModel.completeProfile(f, l, p) },
                    isLoading = authState is AuthState.Loading
                )
            }

            // Skip Button
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

@Composable
fun AuthSheetsStack(
    currentStep: AuthStep,
    onStepChange: (AuthStep) -> Unit,
    onNavigateNext: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onCompleteProfile: (String, String, String) -> Unit,
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
                color = MaterialTheme.colorScheme.surface,
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
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = 16.dp
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    LoginSheet(
                        onNext = { onStepChange(AuthStep.INFO) },
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
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 24.dp
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    InfoSheet(
                        onDone = onCompleteProfile,
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
            Text("Next", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun LanguageCard(
    modifier: Modifier = Modifier, title: String, subtitle: String, iconText: String,
    isSelected: Boolean, onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
    val bgColor = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f) else Color.White

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
            Text(iconText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text Content
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
fun LoginSheet(onNext: () -> Unit, onGoogleSignIn: () -> Unit, isLoading: Boolean) {
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Phone Number Input
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Phone Number") },
            leadingIcon = {
                Text(
                    "+91", modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.secondary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom 4-digit OTP Input
        Text("Enter OTP", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = otp,
                onValueChange = { if (it.length <= 4) otp = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                decorationBox = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(4) { index ->
                            val char = when {
                                index >= otp.length -> ""
                                else -> otp[index].toString()
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .border(
                                        1.dp,
                                        if (otp.length == index) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(32.dp)
                                    )
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { /* Request OTP */ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Get OTP", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
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
                "Continue", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "or", modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("G", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Sign in with Google", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
        }
    }
}

@Composable
fun InfoSheet(onDone: (String, String, String) -> Unit, isLoading: Boolean) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("First Name") },
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
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Last Name") },
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
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Phone Number") },
            leadingIcon = {
                Text(
                    "+91", modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onDone(firstName, lastName, phoneNumber) },
            enabled = !isLoading && firstName.isNotBlank() && phoneNumber.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(
                "Continue", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
            )
        }
    }
}
