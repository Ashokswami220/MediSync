package com.example.medisync.ui.screens.admin

import android.content.Context
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.Spring.StiffnessMedium
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medisync.R
import com.example.medisync.ui.screens.user.openMedicalCoordinates
import com.example.medisync.utils.HapticHelper

@Composable
fun AdminStatsGrid(
    onCallUsClick: () -> Unit,
    onTotalUsersClick: () -> Unit,
    onReportsOpenedClick: () -> Unit,
    context: Context,
    colorScheme: ColorScheme,
    reportsOpenedCount: Long,
    totalUsersCount: Int,
    unclaimedPreRegCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant)
    ) {
        val syringeInteractionSource = remember { MutableInteractionSource() }
        val isSyringePressed by syringeInteractionSource.collectIsPressedAsState()
        val syringeOffsetY by animateDpAsState(
            targetValue = if (isSyringePressed) (-6).dp else 0.dp,
            animationSpec = spring(
                dampingRatio = DampingRatioMediumBouncy,
                stiffness = StiffnessMedium
            ), label = "syringeAnimation"
        )

        // Top Info Row (Identical to User View)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Call Us Cell (38%)
            Box(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .clickable {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onCallUsClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 8.dp)
                ) {
                    Text(
                        stringResource(R.string.call_us),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Call,
                        contentDescription = stringResource(R.string.call_us),
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colorScheme.outlineVariant)
            )

            // Medical Cell (38%)
            Box(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .clickable {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        openMedicalCoordinates(context)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.go_to),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            Icons.Default.Directions,
                            contentDescription = stringResource(R.string.directions),
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.medical),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colorScheme.outlineVariant)
            )

            // Empty Cell for SVG (24%)
            Box(
                modifier = Modifier
                    .weight(0.24f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .clickable(
                        interactionSource = syringeInteractionSource,
                        indication = LocalIndication.current
                    ) {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.syringe),
                    contentDescription = stringResource(R.string.syringe),
                    modifier = Modifier
                        .size(36.dp)
                        .offset { IntOffset(0, syringeOffsetY.roundToPx()) }
                        .rotate(-20f)
                )
            }
        }

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)

        // Reports Opened Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    onClick = {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onReportsOpenedClick()
                    }
                )
                .background(colorScheme.surface)
        ) {
            // Left Content
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 24.dp, bottom = 24.dp)
            ) {
                // 1. Icon + Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primaryContainer.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Visibility, contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.reports_opened), fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2. Huge Number + Label
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        reportsOpenedCount.toString(), fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.total_views), fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // SVG Image (Bottom Right)
            Image(
                painter = painterResource(id = R.drawable.blood_test),
                contentDescription = null,
                alignment = Alignment.BottomEnd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(160.dp) // Maintain SVG scale width
            )
        }

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Total Users Cell
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .clickable {
                        HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                        onTotalUsersClick()
                    }
            ) {
                Column(modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    colorScheme.secondaryContainer.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.People, contentDescription = null,
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.total_users), fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        totalUsersCount.toString(), fontSize = 36.sp, fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        lineHeight = 36.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(colorScheme.outlineVariant)
            )

            // Joined Today Cell
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colorScheme.errorContainer.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PersonAdd, contentDescription = null,
                                tint = colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.pre_reg_users),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        unclaimedPreRegCount.toString(), fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        lineHeight = 36.sp
                    )
                }
            }
        }
    }
}
