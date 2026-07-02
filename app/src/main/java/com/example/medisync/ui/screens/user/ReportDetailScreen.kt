package com.example.medisync.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.medisync.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportName: String = "Comprehensive Metabolic Panel",
    reportTime: String = "Oct 24, 2026, 10:30 AM",
    reportType: String = "Lab Results",
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    var isFullScreen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Report",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White,
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { onBackClick() }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { /* TODO: Share */ }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.background)
        ) {
            // Report Info Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = reportName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reportTime,
                    fontSize = 16.sp,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = " • ",
                    fontSize = 16.sp,
                    color = colorScheme.outline
                )
                Text(
                    text = reportType,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))
            } // End of Header Column
            
            // Image Container without border/clip, takes full width
            ZoomableReportImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(colorScheme.background)
            )
            
            HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Page Navigation
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Page 1 of 1",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { /* TODO: Prev Page */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Page",
                            modifier = Modifier.size(24.dp),
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { /* TODO: Next Page */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Page",
                            modifier = Modifier.size(24.dp),
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Right: Download & Fullscreen
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { /* TODO: Download */ },
                        modifier = Modifier
                            .size(36.dp)
                            .background(colorScheme.secondary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    IconButton(
                        onClick = { isFullScreen = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Full Screen",
                            modifier = Modifier.size(20.dp),
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                            onClick = { isFullScreen = false },
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
    }
}

@Composable
fun ZoomableReportImage(
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .clipToBounds()
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
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ee9d0cc9a6ff0ce1775ac233da86d3f2),
            contentDescription = "Report Document",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}
