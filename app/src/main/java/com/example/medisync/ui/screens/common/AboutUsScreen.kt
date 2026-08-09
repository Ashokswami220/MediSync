package com.example.medisync.ui.screens.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.medisync.R
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.IntOffset
import com.example.medisync.data.local.ContactConfig
import kotlin.random.Random
import kotlin.math.roundToInt


@Composable
fun AboutUsScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FallingShapesBackground(
                modifier = Modifier.blur(7.dp)
            )
            
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 20.dp, top = paddingValues.calculateTopPadding(), bottom = 16.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Use BoxWithConstraints to calculate sizes dynamically based on screen width
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 440.dp),
                contentAlignment = Alignment.Center
            ) {
                val horizontalPadding = 20.dp
                val spacing = 16.dp
                val availableWidth = maxWidth - (horizontalPadding * 2)
                val shapeSize = (availableWidth - spacing * 2) / 3.364f
                val rightColumnWidth = shapeSize * 1.364f

                val shapeColor = Color.Black.copy(alpha = 0.5f)
                val iconColor = MaterialTheme.colorScheme.surface
                val uriHandler = LocalUriHandler.current
                // Scale icon sizes proportionally (original: 40.dp at shapeSize=110.dp)
                val iconSize = shapeSize * (40f / 110f)
                val githubIconSize = shapeSize * (80f / 110f)

                Column(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.Start
                ) {
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
                            CustomAIcon()
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

                    // Grid of shapes - uses .size() exactly like the original so pressableScale works perfectly
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalAlignment = Alignment.Top
                    ) {

                        // Left Column
                        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                            // Top Left: Fan (X)
                            Box(
                                modifier = Modifier
                                    .size(shapeSize)
                                    .pressableScale { uriHandler.openUri(ContactConfig.socialLinks.twitter) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.fan_left),
                                    contentDescription = "Fan",
                                    tint = shapeColor,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(1.2f)
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_twitter_x),
                                    contentDescription = "X",
                                    tint = iconColor,
                                    modifier = Modifier.size(iconSize)
                                )
                            }

                            // Bottom Left: Pill (Mail)
                            Box(
                                modifier = Modifier
                                    .size(shapeSize)
                                    .pressableScale { uriHandler.openUri(ContactConfig.socialLinks.email) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.pill),
                                    contentDescription = "Pill",
                                    tint = shapeColor,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(1.3f)
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Mail",
                                    tint = iconColor,
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                        }

                        // Middle Column
                        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                            // Top Middle: Triangle (LinkedIn)
                            Box(
                                modifier = Modifier
                                    .size(shapeSize)
                                    .pressableScale { uriHandler.openUri(ContactConfig.socialLinks.linkedin) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.triangle),
                                    contentDescription = "Triangle",
                                    tint = shapeColor,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(1.3f)
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_linkedin),
                                    contentDescription = "LinkedIn",
                                    tint = iconColor,
                                    modifier = Modifier.size(iconSize)
                                )
                            }

                            // Bottom Middle: 6-sided Cookie (Instagram)
                            Box(
                                modifier = Modifier
                                    .size(shapeSize)
                                    .pressableScale { uriHandler.openUri(ContactConfig.socialLinks.instagram) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.cookie_6),
                                    contentDescription = "Cookie",
                                    tint = shapeColor,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(1.3f)
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_instagram),
                                    contentDescription = "Instagram",
                                    tint = iconColor,
                                    modifier = Modifier.size(iconSize)
                                )
                            }
                        }

                        // Right Column
                        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
                            // Middle & Bottom Right: Tall Pill/Rectangle (GitHub)
                            Box(
                                modifier = Modifier
                                    .width(rightColumnWidth)
                                    .height(shapeSize * 2 + spacing)
                                    .pressableScale(targetScale = 0.95f) { uriHandler.openUri(
                                        ContactConfig.socialLinks.github) }
                                    .clip(RoundedCornerShape(40.dp))
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_github),
                                    contentDescription = "GitHub",
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(githubIconSize)
                                )
                            }
                        }
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

@Composable
fun CustomAIcon(modifier: Modifier = Modifier) {
    val surface = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier.size(54.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val topPoint = Offset(size.width / 2, 4.dp.toPx())
            val bottomLeft = Offset(4.dp.toPx(), size.height - 4.dp.toPx())
            val bottomRight = Offset(size.width - 4.dp.toPx(), size.height - 4.dp.toPx())

            drawLine(
                color = surface,
                start = topPoint,
                end = bottomLeft,
                strokeWidth = strokeWidth,
                cap = Round
            )
            drawLine(
                color = surface,
                start = topPoint,
                end = bottomRight,
                strokeWidth = strokeWidth,
                cap = Round
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.syringe),
            contentDescription = "Syringe Crossbar",
            tint = surface,
            modifier = Modifier
                .requiredSize(64.dp)
                .padding(top = 7.dp)
                .rotate(25f)
        )
    }
}

@Composable
fun FallingShapesBackground(modifier: Modifier = Modifier) {
    val drawables = listOf(
        R.drawable.fan_left,
        R.drawable.syringe,
        R.drawable.pill,
        R.drawable.triangle,
        R.drawable.cookie_6,
        R.drawable.ghost
    )

    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Pre-calculate randomized state for each shape
    val shapes = remember(secondaryColor) {
        val secondaryIndex = Random.nextInt(5)
        List(5) { index ->
            val drawableId = drawables.random()
            val duration = Random.nextInt(7000, 15000)
            val startXPercent = Random.nextFloat()
            // Random start offset in time
            val startDelayMillis = Random.nextInt(0, 15000)
            val size = Random.nextInt(24, 44).dp
            
            // Only one shape is the secondary color
            val isSecondary = (index == secondaryIndex)
            val baseColor = if (isSecondary) secondaryColor else Color.Black
            val shapeColor = baseColor.copy(alpha = Random.nextFloat() * 0.2f + 0.1f) // vary alpha slightly
            
            ShapeParams(drawableId, duration, startXPercent, startDelayMillis, size, shapeColor)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerHeight = maxHeight

        val density = LocalDensity.current
        val heightPx = with(density) { containerHeight.toPx() }

        val infiniteTransition = rememberInfiniteTransition(label = "falling_shapes")

        shapes.forEach { shape ->
            val yProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(shape.duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(offsetMillis = shape.startDelayMillis, offsetType = StartOffsetType.FastForward)
                ),
                label = "y_progress_${shape.duration}"
            )

            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(shape.duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation_${shape.duration}"
            )

            // yProgress 0 to 1 means from -shape.size to containerHeight + shape.size
            val sizePx = with(density) { shape.size.toPx() }
            val yPosPx = -sizePx + yProgress * (heightPx + sizePx * 2)

            Box(
                modifier = Modifier
                    .size(shape.size)
                    .offset {
                        IntOffset(
                            x = (shape.startXPercent * (constraints.maxWidth - sizePx)).roundToInt(),
                            y = yPosPx.roundToInt()
                        )
                    }
                    .graphicsLayer {
                        rotationZ = rotation
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = shape.drawableId),
                    contentDescription = null,
                    tint = shape.shapeColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

data class ShapeParams(
    val drawableId: Int,
    val duration: Int,
    val startXPercent: Float,
    val startDelayMillis: Int,
    val size: androidx.compose.ui.unit.Dp,
    val shapeColor: Color
)
