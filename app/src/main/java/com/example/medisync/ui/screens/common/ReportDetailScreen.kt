package com.example.medisync.ui.screens.common

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.medisync.R
import com.example.medisync.model.UserRole
import com.example.medisync.repo.DocumentRepository
import com.example.medisync.utils.GlobalToastManager
import com.example.medisync.utils.HapticHelper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportName: String = "Report",
    fileUrl: String = "",
    onBackClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val profileViewModel: ProfileViewModel = koinViewModel()
    val profileState by profileViewModel.profileState.collectAsState()
    val documentRepository: DocumentRepository = koinInject()

    var isFullScreen by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }

    val isPdf = fileUrl.contains(".pdf", ignoreCase = true)

    var localPdfFile by remember { mutableStateOf<File?>(null) }
    var renderedPageUri by remember { mutableStateOf<String?>(null) }
    var isLoadingPdf by remember { mutableStateOf(false) }

    var hasTrackedAnalytics by remember { mutableStateOf(false) }
    LaunchedEffect(fileUrl, profileState) {
        android.util.Log.d(
            "MediSync", "Tracking check: fileUrl='${
                fileUrl.take(
                    15
                )
            }...', hasTracked=$hasTrackedAnalytics, profileState=$profileState"
        )
        if (fileUrl.isNotEmpty() && !hasTrackedAnalytics && profileState is ProfileState.Success) {
            val role = (profileState as ProfileState.Success).profile.role
            android.util.Log.d("MediSync", "Tracking role: $role")
            if (role != UserRole.ADMIN) {
                val res = documentRepository.incrementReportOpenCount()
                android.util.Log.d("MediSync", "Tracking incremented result: $res")
            }
            hasTrackedAnalytics = true
        }
    }

    LaunchedEffect(fileUrl) {
        if (fileUrl.isEmpty()) return@LaunchedEffect

        val timeoutJob = launch {
            delay(5000.milliseconds)
            withContext(Main) {
                GlobalToastManager.showToast(
                    message = "Taking longer than usual. Please wait...",
                    icon = Icons.Default.Info
                )
            }
        }

        if (isPdf) {
            withContext(IO) {
                try {
                    val safeUrl = if (fileUrl.startsWith("http://")) fileUrl.replace(
                        "http://", "https://"
                    ) else fileUrl
                    val savedReportsDir = File(context.filesDir, "saved_reports").apply { mkdirs() }
                    val pdfFile = File(savedReportsDir, "report_${fileUrl.hashCode()}.pdf")

                    if (!pdfFile.exists()) {
                        isLoadingPdf = true
                        val request = okhttp3.Request.Builder()
                            .url(safeUrl)
                            .header("User-Agent", "Mozilla/5.0")
                            .build()
                        val client = OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .build()
                        val response = client.newCall(request)
                            .execute()

                        if (!response.isSuccessful) {
                            throw IOException(
                                "Failed to download PDF: HTTP ${response.code}"
                            )
                        }

                        response.body?.byteStream()
                            ?.use { input ->
                                pdfFile.outputStream()
                                    .use { output ->
                                        input.copyTo(output)
                                    }
                            }
                    }

                    if (pdfFile.length() == 0L) {
                        pdfFile.delete()
                        throw IOException("Downloaded PDF is empty")
                    }

                    localPdfFile = pdfFile

                    val fileDescriptor = ParcelFileDescriptor.open(
                        pdfFile, ParcelFileDescriptor.MODE_READ_ONLY
                    )
                    val renderer = PdfRenderer(fileDescriptor)
                    totalPages = renderer.pageCount
                    renderer.close()
                    fileDescriptor.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isLoadingPdf = false
        }
        timeoutJob.cancel()
    }


    @OptIn(ExperimentalFoundationApi::class)
    val pagerState =
        rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage + 1
    }

    val displayUrl = if (isPdf) {
        renderedPageUri ?: ""
    } else {
        if (fileUrl.startsWith("http://")) fileUrl.replace("http://", "https://") else fileUrl
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable {
                                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                                    onBackClick()
                                }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = stringResource(R.string.back),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .wrapContentWidth()
                        ) {
                            Text(
                                text = reportName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                color = Color.White,
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.surfaceContainerLowest
                    )
                )
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.surfaceContainerLowest)
        ) {

            if (isLoadingPdf) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                    )
                }
            } else if (isPdf && localPdfFile != null) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(colorScheme.background)
                ) { page ->
                    PdfPageImage(
                        pdfFile = localPdfFile,
                        pageIndex = page,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                ZoomableReportImage(
                    fileUrl = displayUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(colorScheme.background)
                )
            }

            HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Controls
            ReportDetailBottomControls(
                originalFileUrl = fileUrl,
                localPdfFile = localPdfFile,
                reportName = reportName,
                isPdf = isPdf,
                currentPage = currentPage,
                totalPages = totalPages,
                onPreviousPage = {
                    if (isPdf) {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    } else if (currentPage > 1) {
                        currentPage--
                    }
                },
                onNextPage = {
                    if (isPdf) {
                        if (pagerState.currentPage < totalPages - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    } else if (currentPage < totalPages) {
                        currentPage++
                    }
                },
                onFullScreenClick = { isFullScreen = true }
            )

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
                        if (isPdf && localPdfFile != null) {
                            @OptIn(ExperimentalFoundationApi::class)
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                PdfPageImage(
                                    pdfFile = localPdfFile,
                                    pageIndex = page,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            ZoomableReportImage(
                                fileUrl = displayUrl,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

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
                                contentDescription = stringResource(R.string.exit_full_screen),
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
fun ReportDetailBottomControls(
    originalFileUrl: String,
    localPdfFile: File?,
    reportName: String,
    isPdf: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onFullScreenClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Page Navigation
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    onPreviousPage()
                },
                enabled = currentPage > 1,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.previous_page),
                    modifier = Modifier.size(24.dp),
                    tint = if (currentPage > 1) colorScheme.onSurfaceVariant else colorScheme.onSurfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            }
            Text(
                text = if (isPdf) "Page $currentPage of $totalPages" else "1/1",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    onNextPage()
                },
                enabled = isPdf,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.next_page),
                    modifier = Modifier.size(24.dp),
                    tint = if (isPdf) colorScheme.onSurfaceVariant else colorScheme.onSurfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            }
        }

        // Right: Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    if (originalFileUrl.isNotEmpty()) {
                        GlobalToastManager.showToast(
                            message = "Preparing file for sharing...",
                            icon = Icons.Default.Share
                        )
                        coroutineScope.launch {
                            try {
                                val extension = if (isPdf) ".pdf" else ".jpg"
                                val finalFileName = if (reportName.endsWith(
                                        extension, ignoreCase = true
                                    )
                                ) reportName else "$reportName$extension"

                                val sharedDir = File(context.cacheDir, "shared_reports")
                                    .apply { mkdirs() }
                                val tempFile = File(sharedDir, finalFileName)

                                withContext(IO) {
                                    if (isPdf && localPdfFile != null && localPdfFile.exists()) {
                                        localPdfFile.inputStream()
                                            .use { input ->
                                                tempFile.outputStream()
                                                    .use { output ->
                                                        input.copyTo(output)
                                                    }
                                            }
                                    } else {
                                        val safeUrl = if (originalFileUrl.startsWith(
                                                "http://"
                                            )
                                        ) originalFileUrl.replace(
                                            "http://", "https://"
                                        ) else originalFileUrl
                                        URL(safeUrl)
                                            .openStream()
                                            .use { input ->
                                                tempFile.outputStream()
                                                    .use { output ->
                                                        input.copyTo(output)
                                                    }
                                            }
                                    }
                                }

                                val uri = getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    tempFile
                                )

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = if (extension.equals(
                                            ".pdf", ignoreCase = true
                                        )
                                    ) "application/pdf" else "image/*"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, reportName)
                                    putExtra(Intent.EXTRA_TEXT, reportName)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Report"))
                            } catch (_: Exception) {
                                GlobalToastManager.showToast(
                                    message = "Failed to prepare file",
                                    icon = Icons.Default.ErrorOutline
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.share),
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    if (originalFileUrl.isNotEmpty()) {
                        try {
                            val downloadManager = context.getSystemService(
                                Context.DOWNLOAD_SERVICE
                            ) as DownloadManager

                            val safeOriginalUrl =
                                if (originalFileUrl.startsWith("http://")) originalFileUrl.replace(
                                    "http://", "https://"
                                ) else originalFileUrl
                            val extension = if (isPdf) ".pdf" else ".jpg"
                            val finalFileName = if (reportName.endsWith(
                                    extension, ignoreCase = true
                                )
                            ) reportName else "$reportName$extension"
                            val sanitizedFileName = finalFileName.replace("/", "_")
                                .replace("\\", "_")

                            val request =
                                DownloadManager.Request(safeOriginalUrl.toUri())
                                    .setTitle(reportName)
                                    .setDescription("Downloading $reportName")
                                    .setNotificationVisibility(
                                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                                    )
                                    .setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS,
                                        sanitizedFileName
                                    )
                                    .setAllowedOverMetered(true)
                                    .setAllowedOverRoaming(true)

                            downloadManager.enqueue(request)
                            GlobalToastManager.showToast(
                                message = "Downloading $reportName...",
                                icon = Icons.Default.Download
                            )
                        } catch (e: Exception) {
                            GlobalToastManager.showToast(
                                message = "Failed to download: ${e.localizedMessage}",
                                icon = Icons.Default.ErrorOutline
                            )
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.secondary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.download_or_view_original),
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = {
                    HapticHelper.trigger(context, HapticHelper.Type.LIGHT)
                    onFullScreenClick()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = stringResource(R.string.full_screen),
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ZoomableReportImage(
    fileUrl: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { componentSize = it }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()

                            scale = (scale * zoom).coerceIn(1f, 5f)

                            if (scale > 1f) {
                                val maxX = (componentSize.width * (scale - 1)) / 2f
                                val maxY = (componentSize.height * (scale - 1)) / 2f

                                val originalX = offset.x
                                offset = Offset(
                                    x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                    y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                )

                                // Consume event to prevent Pager from scrolling, unless we hit the horizontal edge
                                val hitHorizontalEdge =
                                    (offset.x == originalX && pan.x != 0f && zoom == 1f)
                                if (!hitHorizontalEdge) {
                                    event.changes.forEach {
                                        if (it.positionChanged()) it.consume()
                                    }
                                }
                            } else {
                                offset = Offset.Zero
                                if (zoom != 1f) {
                                    event.changes.forEach {
                                        if (it.positionChanged()) it.consume()
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(fileUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.report_document),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            loading = {
                LaunchedEffect(Unit) {
                    delay(5000.milliseconds)
                    GlobalToastManager.showToast(
                        message = "Taking longer than usual. Please wait...",
                        icon = Icons.Default.Info
                    )
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator(
                        modifier = Modifier.size(60.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                    )
                }
            },
            error = {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = stringResource(R.string.error),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.document_is_missing_or_corrupt),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.please_contact_support),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PdfPageImage(
    pdfFile: File?,
    pageIndex: Int, // 0-indexed
    modifier: Modifier = Modifier
) {
    var renderedPageUri by remember(pdfFile, pageIndex) { mutableStateOf<String?>(null) }
    var isLoading by remember(pdfFile, pageIndex) { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(pdfFile, pageIndex) {
        if (pdfFile != null && pdfFile.exists()) {
            isLoading = true
            withContext(IO) {
                try {
                    val pageFile = File(
                        context.cacheDir,
                        "${pdfFile.nameWithoutExtension}_page_${pageIndex + 1}.png"
                    )

                    if (pageFile.exists()) {
                        renderedPageUri = Uri.fromFile(pageFile)
                            .toString()
                        isLoading = false
                        return@withContext
                    }

                    val fileDescriptor = ParcelFileDescriptor.open(
                        pdfFile, ParcelFileDescriptor.MODE_READ_ONLY
                    )
                    val renderer = PdfRenderer(fileDescriptor)

                    val safePageIndex = pageIndex.coerceIn(0, renderer.pageCount - 1)
                    val page = renderer.openPage(safePageIndex)

                    var renderWidth = page.width * 2
                    var renderHeight = page.height * 2
                    val maxDim = 2500f
                    if (renderWidth > maxDim || renderHeight > maxDim) {
                        val scale = (maxDim / renderWidth).coerceAtMost(maxDim / renderHeight)
                        renderWidth = (renderWidth * scale).toInt()
                        renderHeight = (renderHeight * scale).toInt()
                    }

                    val bitmap = createBitmap(renderWidth, renderHeight)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(AndroidColor.WHITE)

                    page.render(
                        bitmap, null, null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                    page.close()
                    renderer.close()
                    fileDescriptor.close()

                    pageFile.outputStream()
                        .use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    bitmap.recycle()

                    renderedPageUri = Uri.fromFile(pageFile)
                        .toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator(
                color = MaterialTheme.colorScheme.secondary,
                polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
            )
        }
    } else {
        ZoomableReportImage(
            fileUrl = renderedPageUri ?: "",
            modifier = modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}
