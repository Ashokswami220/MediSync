package com.example.medisync.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R

@Composable
fun NotLoggedInState(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 24.dp,
    buttonShapeDp: Dp = 100.dp,
    onNavigateToLogin: () -> Unit,
    colorScheme: ColorScheme
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
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
                    .padding(horizontal = horizontalPadding)
                    .height(240.dp)
                    .background(
                        Color.Black.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(32.dp)
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.person_with_doc),
                contentDescription = "Login Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .height(56.dp),
            shape = RoundedCornerShape(buttonShapeDp),
            colors = outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                1.dp, colorScheme.outlineVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(colorScheme.secondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Login to see reports", color = colorScheme.onSurface, fontSize = 16.sp
            )
        }
    }
}

@Composable
fun VertEmptyReportsState(
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_doc),
            contentDescription = "No reports",
            modifier = Modifier.size(110.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Reports available",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Try changing member",
            fontSize = 16.sp,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HorizEmptyReportsState(
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_doc),
            contentDescription = "No reports",
            modifier = Modifier.size(110.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No Reports available",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Try changing member",
                fontSize = 22.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
        }
    }
}
