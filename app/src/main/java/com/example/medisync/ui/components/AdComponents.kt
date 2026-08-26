package com.example.medisync.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import android.graphics.Color as AndroidColor

object AdConfig {
    const val TEST_NATIVE_AD = "ca-app-pub-3940256099942544/2247696110"
    const val REAL_COMPACT_AD = "ca-app-pub-6093923293882696/6146928299"
    const val REAL_RICH_AD = "ca-app-pub-6093923293882696/6122877560"

    // Toggle this to false before generating production release!
    const val IS_TEST_MODE = true

    val compactAdId: String
        get() = if (IS_TEST_MODE) TEST_NATIVE_AD else REAL_COMPACT_AD

    val richAdId: String
        get() = if (IS_TEST_MODE) TEST_NATIVE_AD else REAL_RICH_AD
}

@Composable
fun rememberNativeAd(context: Context, adUnitId: String): NativeAd? {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    android.util.Log.e(
                        "AdMob", "Ad failed to load: ${error.message} (Code: ${error.code})"
                    )
                    nativeAd = null
                }
            })
            .build()
        adLoader.loadAd(
            AdRequest.Builder()
                .build()
        )
    }

    DisposableEffect(nativeAd) {
        onDispose {
            nativeAd?.destroy()
        }
    }

    return nativeAd
}

@SuppressLint("SetTextI18n")
@Composable
fun CompactNativeAd(
    adUnitId: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    textColor: Color = Color.Black
) {
    val context = LocalContext.current
    val nativeAd = rememberNativeAd(context, adUnitId)

    if (nativeAd != null) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AndroidView(
                factory = { ctx ->
                    val adView = NativeAdView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setBackgroundColor(backgroundColor.toArgb())
                    }

                    // Root Layout
                    val rootLayout = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(16, 16, 16, 16)
                    }

                    // Icon
                    val iconView = ImageView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(120, 120)
                            .apply {
                                setMargins(0, 0, 24, 0)
                            }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    // Text Column
                    val textColumn = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    // Headline
                    val headlineView = TextView(ctx).apply {
                        textSize = 14f
                        setTextColor(textColor.toArgb())
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        maxLines = 1
                        ellipsize = android.text.TextUtils.TruncateAt.END
                    }

                    // Body
                    val bodyView = TextView(ctx).apply {
                        textSize = 12f
                        setTextColor(textColor.toArgb())
                        alpha = 0.7f
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                    }

                    // Ad Badge
                    val adBadge = TextView(ctx).apply {
                        text = ctx.getString(com.example.medisync.R.string.medisync_ad_badge)
                        textSize = 10f
                        setTextColor(AndroidColor.WHITE)
                        setBackgroundColor("#FFCC00".toColorInt())
                        setPadding(8, 2, 8, 2)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                            .apply {
                                setMargins(0, 8, 0, 0)
                            }
                    }

                    textColumn.addView(headlineView)
                    textColumn.addView(bodyView)
                    textColumn.addView(adBadge)

                    // CTA Button
                    val ctaView = Button(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                            .apply {
                                gravity = Gravity.CENTER_VERTICAL
                            }
                        textSize = 12f
                        setPadding(24, 0, 24, 0)
                    }

                    rootLayout.addView(iconView)
                    rootLayout.addView(textColumn)
                    rootLayout.addView(ctaView)

                    adView.addView(rootLayout)

                    // Register Views
                    adView.iconView = iconView
                    adView.headlineView = headlineView
                    adView.bodyView = bodyView
                    adView.callToActionView = ctaView

                    adView
                },
                update = { adView ->
                    (adView.iconView as? ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
                    (adView.headlineView as? TextView)?.text = nativeAd.headline
                    (adView.bodyView as? TextView)?.text = nativeAd.body
                    (adView.callToActionView as? Button)?.text = nativeAd.callToAction

                    adView.setNativeAd(nativeAd)
                }
            )
        }
    }
}

@Composable
fun RichNativeAd(
    adUnitId: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    textColor: Color = Color.Black
) {
    val context = LocalContext.current
    val nativeAd = rememberNativeAd(context, adUnitId)

    if (nativeAd != null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            AndroidView(
                factory = { ctx ->
                    val adView = NativeAdView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setBackgroundColor(backgroundColor.toArgb())
                    }

                    // Root Layout
                    val rootLayout = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        setPadding(16, 16, 16, 16)
                    }

                    // Header Row (Icon + Headline + Ad Badge)
                    val headerRow = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val iconView = ImageView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(80, 80)
                            .apply {
                                setMargins(0, 0, 16, 0)
                            }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    val textColumn = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    val headlineView = TextView(ctx).apply {
                        textSize = 16f
                        setTextColor(textColor.toArgb())
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }

                    val adBadge = TextView(ctx).apply {
                        text = ctx.getString(com.example.medisync.R.string.medisync_sponsored_badge)
                        textSize = 10f
                        setTextColor(textColor.toArgb())
                        alpha = 0.6f
                    }

                    textColumn.addView(headlineView)
                    textColumn.addView(adBadge)

                    headerRow.addView(iconView)
                    headerRow.addView(textColumn)

                    // Media View
                    val mediaView = MediaView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            400 // approx 150dp height in px, will let compose scale it
                        )
                            .apply {
                                setMargins(0, 24, 0, 24)
                                weight = 1f // Take up middle space
                            }
                    }

                    // Footer Row (Body + CTA)
                    val footerRow = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val bodyView = TextView(ctx).apply {
                        textSize = 14f
                        setTextColor(textColor.toArgb())
                        alpha = 0.8f
                        maxLines = 2
                        ellipsize = android.text.TextUtils.TruncateAt.END
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                            .apply {
                                setMargins(0, 0, 16, 0)
                            }
                    }

                    val ctaView = Button(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    footerRow.addView(bodyView)
                    footerRow.addView(ctaView)

                    rootLayout.addView(headerRow)
                    rootLayout.addView(mediaView)
                    rootLayout.addView(footerRow)

                    adView.addView(rootLayout)

                    // Register Views
                    adView.iconView = iconView
                    adView.headlineView = headlineView
                    adView.bodyView = bodyView
                    adView.mediaView = mediaView
                    adView.callToActionView = ctaView

                    adView
                },
                update = { adView ->
                    (adView.iconView as? ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
                    (adView.headlineView as? TextView)?.text = nativeAd.headline
                    (adView.bodyView as? TextView)?.text = nativeAd.body
                    (adView.callToActionView as? Button)?.text = nativeAd.callToAction
                    adView.mediaView?.mediaContent = nativeAd.mediaContent

                    adView.setNativeAd(nativeAd)
                }
            )
        }
    }
}
