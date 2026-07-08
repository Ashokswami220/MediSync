package com.example.medisync.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.medisync.R
import com.example.medisync.utils.HapticHelper

@Composable
fun ReportDetailScreen2(
    reportName: String = "Comprehensive Metabolic Panel",
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var isFullScreen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .reportGridBackground()
    ) {
        // Full screen zoomable image
        FullscreenZoomableImage(
            modifier = Modifier.fillMaxSize()
        )

        // Floating Control Panel at the bottom
        ReportDetailBottomBar(
            reportName = reportName,
            onBackClick = onBackClick,
            onFullScreenClick = {
                HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                isFullScreen = true
            }
        )
    }

    if (isFullScreen) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                ZoomableReportImage(
                    modifier = Modifier.fillMaxSize()
                )

                // Fullscreen Exit Button
                IconButton(
                    onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        isFullScreen = false
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Full Screen",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FullscreenZoomableImage(
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            // No clipToBounds() here, so when scale > 1, it draws over the padding
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        val maxX = (size.width * (scale - 1)) / 2f
                        val maxY = (size.height * (scale - 1)) / 2f
                        offset = Offset(
                            x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                            y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                        )
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.ee9d0cc9a6ff0ce1775ac233da86d3f2),
            contentDescription = "Report Document",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .padding(top = 120.dp, start = 12.dp, end = 12.dp)
                .fillMaxWidth()
                .wrapContentHeight(unbounded = true)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(
                    4.dp
                )
        )
    }
}

@Composable
fun Modifier.reportGridBackground(): Modifier {
    val bgColor = MaterialTheme.colorScheme.background
    val gridColorBase = MaterialTheme.colorScheme.secondary
    return this.drawBehind {
        drawRect(color = bgColor)
        val gridSizePx = 32.dp.toPx()
        val gridColor = gridColorBase.copy(alpha = 0.15f)

        var x = 0f
        while (x < size.width) {
            drawLine(
                color = gridColor, start = Offset(x, 0f), end = Offset(x, size.height),
                strokeWidth = 2f
            )
            x += gridSizePx
        }
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor, start = Offset(0f, y), end = Offset(size.width, y),
                strokeWidth = 2f
            )
            y += gridSizePx
        }
    }
}

@Composable
fun BoxScope.ReportDetailBottomBar(
    reportName: String,
    onBackClick: () -> Unit,
    onFullScreenClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp) // padding for nav bar area
            .navigationBarsPadding()
            .fillMaxWidth()
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Top Row: Report Name & Full Screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reportName,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onFullScreenClick() }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Full Screen",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))

            // Bottom Row: Actions & Dividers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                            onBackClick()
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Back",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(16.dp),
                    color = Color.Black.copy(alpha = 0.2f)
                )

                // Pagination
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { HapticHelper.trigger(context, HapticHelper.Type.LIGHT) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous",
                            tint = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "1/1",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { HapticHelper.trigger(context, HapticHelper.Type.LIGHT) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next",
                            tint = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                VerticalDivider(
                    modifier = Modifier.height(16.dp),
                    color = Color.Black.copy(alpha = 0.2f)
                )

                // Share
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { HapticHelper.trigger(context, HapticHelper.Type.LIGHT) }
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(16.dp),
                    color = Color.Black.copy(alpha = 0.2f)
                )

                // Download
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { HapticHelper.trigger(context, HapticHelper.Type.LIGHT) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
