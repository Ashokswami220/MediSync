package com.example.medisync.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.medisync.R

@Composable
fun Modifier.graphPaperBackground(
    gridSize: Dp = 32.dp
): Modifier {
    val lineColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    val backgroundColor = MaterialTheme.colorScheme.background

    return this
        .background(backgroundColor)
        .drawBehind {
            val sizePx = gridSize.toPx()
            val width = size.width
            val height = size.height

            // Draw vertical lines
            var x = 0f
            while (x < width) {
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.5f
                )
                x += sizePx
            }

            // Draw horizontal lines
            var y = 0f
            while (y < height) {
                drawLine(
                    color = lineColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.5f
                )
                y += sizePx
            }
        }
}


@Composable
fun AboutUsScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.graphPaperBackground(),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.Start
            ) {
                val shapeColor = MaterialTheme.colorScheme.primary
                val shapeSize = 110.dp
                val spacing = 16.dp

                // Top Section (Profile Box + Text)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(shapeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "F",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Ashok swami",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Developer & designer",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Grid of shapes matching the design exactly
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column
                    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                        // Top Left: Triangle
                        Icon(
                            painter = painterResource(id = R.drawable.triangle),
                            contentDescription = "Triangle",
                            tint = shapeColor,
                            modifier = Modifier.size(shapeSize).scale(1.3f).pressableScale { }
                        )

                        // Bottom Left: Pill
                        Icon(
                            painter = painterResource(id = R.drawable.pill),
                            contentDescription = "Pill",
                            tint = shapeColor,
                            modifier = Modifier.size(shapeSize).scale(1.3f).pressableScale { }
                        )
                    }

                    // Middle Column
                    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                        // Top Middle: Fan
                        Icon(
                            painter = painterResource(id = R.drawable.fan),
                            contentDescription = "Fan",
                            tint = shapeColor,
                            modifier = Modifier.size(shapeSize).scale(1.3f).pressableScale { }
                        )

                        // Bottom Middle: 6-sided Cookie
                        Icon(
                            painter = painterResource(id = R.drawable.cookie_6),
                            contentDescription = "Cookie",
                            tint = shapeColor,
                            modifier = Modifier.size(shapeSize).scale(1.3f).pressableScale { }
                        )
                    }

                    // Right Column
                    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                        // Middle & Bottom Right: Tall Pill/Rectangle
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(shapeSize * 2 + spacing)
                                .pressableScale(targetScale = 0.95f) { }
                                .clip(RoundedCornerShape(40.dp))
                                .background(shapeColor)
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.pressableScale(
    targetScale: Float = 0.93f,
    onClick: () -> Unit = {}
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        label = "button_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}
