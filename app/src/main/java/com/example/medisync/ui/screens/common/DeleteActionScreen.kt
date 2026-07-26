package com.example.medisync.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

enum class DeleteActionMode {
    ACCOUNT, DATA
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeleteActionScreen(
    mode: DeleteActionMode,
    onComplete: (onResult: (Boolean) -> Unit) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var isProcessing by remember { mutableStateOf(false) }

    val title = if (mode == DeleteActionMode.ACCOUNT) "Delete account" else "Delete my data"
    val subtitle = if (mode == DeleteActionMode.ACCOUNT) {
        "If you proceed with the deletion of your account, you will lose access to all your synced medical reports, history, and preferences. This action is permanent and cannot be undone."
    } else {
        "If you proceed with the deletion of your data, all your synced medical reports, history, and preferences will be permanently wiped. This action cannot be undone."
    }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(4000.milliseconds)
            onComplete { success ->
                if (!success) {
                    isProcessing = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(all = 16.dp)
        ) {
            Text(
                text = title,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = subtitle,
                fontSize = 16.sp,
                color = colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
            
            if (isProcessing) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = colorScheme.secondary,
                    trackColor = colorScheme.outlineVariant,
                )
            }
        }

        Button(
            onClick = { isProcessing = true },
            enabled = !isProcessing,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.error,
                disabledContainerColor = colorScheme.error.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(100.dp)
        ) {
            Text(
                text = title,
                color = colorScheme.onError,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
