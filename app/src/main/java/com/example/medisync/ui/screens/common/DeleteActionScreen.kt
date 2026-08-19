package com.example.medisync.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.utils.GlobalToastManager
import com.example.medisync.utils.HapticHelper
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

enum class DeleteActionMode {
    ACCOUNT, DATA, REPORTS
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeleteActionScreen(
    mode: DeleteActionMode,
    profileViewModel: ProfileViewModel = koinViewModel(),
    onAccountDeleted: () -> Unit,
    onDataDeleted: () -> Unit,
    onReportsDeleted: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val title = when (mode) {
        DeleteActionMode.ACCOUNT -> "Delete account"
        DeleteActionMode.DATA -> "Delete my data"
        DeleteActionMode.REPORTS -> "Clear local reports"
    }
    val subtitle = when (mode) {
        DeleteActionMode.ACCOUNT -> "If you proceed with the deletion of your account, you will lose access to all your synced medical reports, history, and preferences. This action is permanent and cannot be undone."
        DeleteActionMode.DATA -> "If you proceed with the deletion of your data, all your synced medical reports, history, and preferences will be permanently wiped. This action cannot be undone."
        DeleteActionMode.REPORTS -> "This will remove downloaded reports from your device to free up space. Your reports will NOT be deleted from the database and you can view them again anytime."
    }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            when (mode) {
                DeleteActionMode.ACCOUNT -> {
                    profileViewModel.deleteAccount { success, msg ->
                        if (success) {
                            GlobalToastManager.showToast(msg, Icons.Default.Delete)
                            onAccountDeleted()
                        } else {
                            GlobalToastManager.showToast(msg, Icons.Default.Error)
                            isProcessing = false
                        }
                    }
                }
                DeleteActionMode.DATA -> {
                    profileViewModel.deleteData { success, msg ->
                        if (success) {
                            GlobalToastManager.showToast(msg, Icons.Default.Delete)
                            onDataDeleted()
                        } else {
                            GlobalToastManager.showToast(msg, Icons.Default.Error)
                            isProcessing = false
                        }
                    }
                }
                DeleteActionMode.REPORTS -> {
                    kotlinx.coroutines.delay(800.milliseconds) // slight delay for visual effect
                    val reportsDir = java.io.File(context.filesDir, "saved_reports")
                    if (reportsDir.exists()) {
                        reportsDir.listFiles()?.forEach { it.delete() }
                    }
                    GlobalToastManager.showToast("Local reports cleared", Icons.Default.Delete)
                    onReportsDeleted()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = colorScheme.secondary,
                    trackColor = colorScheme.outlineVariant,
                )
            }
        }

        Button(
            onClick = {
                HapticHelper.trigger(context, HapticHelper.Type.HEAVY)
                isProcessing = true
            },
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
