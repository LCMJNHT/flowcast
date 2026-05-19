package com.flowcast.demo

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.flowcast.demo.data.DemoRepository
import com.flowcast.demo.data.FlowCastRepository
import com.flowcast.demo.data.NewsItem
import com.flowcast.demo.data.NfcScenario
import com.flowcast.demo.data.PaymentMethod
import com.flowcast.demo.data.VideoItem
import com.flowcast.demo.ui.BrandGradient
import com.flowcast.demo.ui.FlowCastTheme
import com.flowcast.demo.ui.FlowChip
import com.flowcast.demo.ui.FlowColors
import com.flowcast.demo.ui.FlowPrimaryButton
import com.flowcast.demo.ui.SectionHeader
import com.flowcast.demo.ui.EmptyState
import com.flowcast.demo.ui.LoadingState
import com.flowcast.demo.ui.StatusPill
import com.flowcast.demo.ui.TechBlock

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowCastTheme {
                FlowCastApp()
            }
        }
    }
}

private data class TabItem(val route: String, val label: String, val icon: String)

private val tabs = listOf(
    TabItem("video", "视频", "▶"),
    TabItem("news", "新闻", "N"),
    TabItem("cashier", "收银台", "¥"),
    TabItem("profile", "我的", "我"),
)

private sealed interface ContentUiState<out T> {
    data object Loading : ContentUiState<Nothing>
    data class Success<T>(
        val data: T,
        val isFallback: Boolean = false,
        val message: String? = null,
    ) : ContentUiState<T>
    data class Empty(
        val isFallback: Boolean = false,
        val message: String? = null,
    ) : ContentUiState<Nothing>
    data class Error(val message: String) : ContentUiState<Nothing>
}

private data class VideoFeedState(
    val items: List<VideoItem> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val isFallback: Boolean = false,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
    val errorMessage: String? = null,
)

@Composable
fun FlowCastApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route.orEmpty()
    val showBottomBar = tabs.any { currentRoute.startsWith(it.route) }

    Scaffold(
        containerColor = FlowColors.Page,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute.startsWith(tab.route),
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(tab.label) },
                            icon = { Text(tab.icon, fontWeight = FontWeight.Bold) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "video",
            modifier = Modifier.padding(padding),
        ) {
            composable("video") { VideoFeedScreen(navController) }
            composable("news") { NewsListScreen(navController) }
            composable(
                "news/{newsId}",
                arguments = listOf(navArgument("newsId") { type = NavType.StringType }),
            ) { entry ->
                val newsId = entry.arguments?.getString("newsId").orEmpty()
                NewsDetailScreen(newsId = newsId, onBack = navController::popBackStack)
            }
            composable("cashier") { CashierScreen(navController) }
            composable(
                "mockPay/{methodId}",
                arguments = listOf(navArgument("methodId") { type = NavType.StringType }),
            ) { entry ->
                val methodId = entry.arguments?.getString("methodId").orEmpty()
                val method = DemoRepository.paymentMethods.firstOrNull { it.id == methodId }
                if (method == null) {
                    MissingRouteScreen(
                        title = "支付方式不可用",
                        message = "未找到 $methodId 对应的模拟支付渠道，请返回收银台重新选择。",
                        onBack = navController::popBackStack,
                    )
                } else {
                    MockPaymentScreen(
                        method = method,
                        navController = navController,
                    )
                }
            }
            composable(
                "paymentResult/{status}",
                arguments = listOf(navArgument("status") { type = NavType.StringType }),
            ) { entry ->
                PaymentResultScreen(
                    status = entry.arguments?.getString("status").orEmpty(),
                    onDone = { navController.navigate("cashier") },
                )
            }
            composable("profile") { ProfileScreen(navController) }
            composable("nfc") { NfcDemoScreen(navController) }
            composable(
                "nfcResult/{scenarioId}",
                arguments = listOf(navArgument("scenarioId") { type = NavType.StringType }),
            ) { entry ->
                val scenarioId = entry.arguments?.getString("scenarioId").orEmpty()
                val scenario = DemoRepository.nfcScenarios.firstOrNull { it.id == scenarioId }
                if (scenario == null) {
                    MissingRouteScreen(
                        title = "无法识别 NFC 场景",
                        message = "当前 Scheme 场景 $scenarioId 暂不支持，请返回 NFC Demo 重新选择。",
                        onBack = navController::popBackStack,
                    )
                } else {
                    NfcResultScreen(
                        scenario = scenario,
                        onCashier = { navController.navigate("cashier") },
                        onTableOrder = { navController.navigate("tableOrder") },
                        onMiniProgram = { navController.navigate("miniProgram") },
                        onBack = navController::popBackStack,
                    )
                }
            }
            composable("tableOrder") { TableOrderScreen(onBack = navController::popBackStack) }
            composable("miniProgram") { MiniProgramJumpScreen(onBack = navController::popBackStack) }
            composable("videoPlayer/{videoId}") { entry ->
                val videoId = entry.arguments?.getString("videoId").orEmpty()
                val video = FlowCastRepository.findVideo(videoId)
                if (video == null) {
                    MissingRouteScreen(
                        title = "视频不存在",
                        message = "未找到 ID 为 $videoId 的视频内容",
                        onBack = navController::popBackStack,
                    )
                } else {
                    VideoPlayerScreen(video = video, onBack = navController::popBackStack)
                }
            }
            composable("nfcHistory") { NfcHistoryScreen(onBack = navController::popBackStack) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VideoFeedScreen(navController: NavController) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }
    var feedState by remember { mutableStateOf(VideoFeedState()) }
    var reloadKey by remember { mutableStateOf(0) }
    var loadMoreKey by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { feedState.items.size })

    LaunchedEffect(reloadKey) {
        feedState = feedState.copy(
            isInitialLoading = feedState.items.isEmpty(),
            isRefreshing = feedState.items.isNotEmpty(),
            errorMessage = null,
            nextCursor = null,
            hasMore = false,
        )
        val result = FlowCastRepository.loadVideos(limit = 5)
        feedState = VideoFeedState(
            items = result.items.distinctBy { it.id },
            isInitialLoading = false,
            isRefreshing = false,
            isFallback = result.isFallback,
            nextCursor = result.nextCursor,
            hasMore = result.hasMore,
            errorMessage = result.errorMessage,
        )
        if (pagerState.currentPage > 0) {
            pagerState.scrollToPage(0)
        }
    }

    LaunchedEffect(loadMoreKey) {
        if (loadMoreKey == 0) return@LaunchedEffect
        val cursor = feedState.nextCursor ?: return@LaunchedEffect
        if (feedState.isLoadingMore || !feedState.hasMore) return@LaunchedEffect
        feedState = feedState.copy(isLoadingMore = true, errorMessage = null)
        val result = FlowCastRepository.loadVideos(cursor = cursor, limit = 5)
        if (result.isFallback && feedState.items.isNotEmpty()) {
            feedState = feedState.copy(
                isLoadingMore = false,
                isFallback = true,
                errorMessage = result.errorMessage ?: "加载更多失败",
            )
            return@LaunchedEffect
        }
        feedState = feedState.copy(
            items = (feedState.items + result.items).distinctBy { it.id },
            isLoadingMore = false,
            isFallback = feedState.isFallback || result.isFallback,
            nextCursor = result.nextCursor,
            hasMore = result.hasMore,
            errorMessage = result.errorMessage,
        )
    }

    LaunchedEffect(pagerState.currentPage, feedState.items, feedState.hasMore, feedState.nextCursor) {
        val activeVideo = feedState.items.getOrNull(pagerState.currentPage)
        if (activeVideo != null && activeVideo.playUrl.isNotBlank()) {
            player.stop()
            player.setMediaItem(MediaItem.fromUri(activeVideo.playUrl))
            player.prepare()
            player.playWhenReady = true
            player.play()
        } else {
            player.stop()
            player.clearMediaItems()
        }
        if (
            feedState.items.isNotEmpty() &&
            pagerState.currentPage >= feedState.items.lastIndex - 2 &&
            feedState.hasMore &&
            !feedState.isLoadingMore
        ) {
            loadMoreKey++
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowColors.VideoBlack),
    ) {
        when {
            feedState.isInitialLoading -> LoadingState(
                message = "视频加载中...",
                modifier = Modifier.align(Alignment.Center),
            )
            feedState.items.isEmpty() -> EmptyState(
                message = if (feedState.isFallback) "后端不可用，回退数据为空" else "暂无视频内容，稍后再来看看吧",
                modifier = Modifier.align(Alignment.Center),
                onRetry = { reloadKey++ },
            )
            else -> {
                VerticalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val video = feedState.items[it]
                    ImmersiveVideoPage(
                        video = video,
                        player = if (it == pagerState.currentPage) player else null,
                        isActive = it == pagerState.currentPage,
                        isLoadingMore = feedState.isLoadingMore && it == feedState.items.lastIndex,
                        loadError = if (it == feedState.items.lastIndex) feedState.errorMessage else null,
                        onRetryLoadMore = { loadMoreKey++ },
                        onOpenDetail = { navController.navigate("videoPlayer/${video.id}") },
                    )
                }
                if (feedState.isFallback) {
                    StatusPill(
                        "后端不可用，已显示回退数据",
                        FlowColors.Warning,
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 56.dp, end = 14.dp),
                    )
                }
                if (feedState.isRefreshing) {
                    StatusPill(
                        "刷新中",
                        FlowColors.Mint,
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 96.dp, end = 14.dp),
                    )
                }
            }
        }
        ChannelBar(
            onRefresh = { reloadKey++ },
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(FlowColors.VideoBlack.copy(alpha = 0.92f))
                .padding(horizontal = 14.dp, vertical = 13.dp),
        )
    }
}

@Composable
private fun ChannelBar(
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlowChip("推荐", selected = true, dark = true)
        FlowChip("AI", dark = true)
        FlowChip("零售", dark = true)
        FlowChip("NFC", dark = true)
        FlowChip("支付", dark = true)
        Spacer(Modifier.width(6.dp))
        StatusPill("手机端直连", FlowColors.Mint)
        if (onRefresh != null) {
            FlowChip("刷新", dark = true, onClick = onRefresh)
        }
    }
}

@Composable
private fun ImmersiveVideoPage(
    video: VideoItem,
    player: ExoPlayer?,
    isActive: Boolean,
    isLoadingMore: Boolean,
    loadError: String?,
    onRetryLoadMore: () -> Unit,
    onOpenDetail: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onOpenDetail),
    ) {
        AsyncImage(
            model = video.coverUrl,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        if (player != null) {
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        useController = false
                        this.player = player
                    }
                },
                update = { it.player = player },
                modifier = Modifier.matchParentSize(),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.06f),
                            Color.Black.copy(alpha = 0.82f),
                        ),
                    ),
                ),
        )
        Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(top = 84.dp, start = 16.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.34f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        ) {
            Text(
                "${video.durationSeconds}s · ${video.source}",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = CircleShape,
            color = Color.White.copy(alpha = if (isActive) 0.08f else 0.18f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        ) {
            Box(Modifier.size(62.dp), contentAlignment = Alignment.Center) {
                Text(if (isActive) "▶" else "待播", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp, bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VideoAction("赞", video.likes)
            VideoAction("评", video.comments)
            VideoAction("转", "分享")
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 18.dp, end = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(video.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("@${video.author}", color = FlowColors.Mint, fontWeight = FontWeight.SemiBold)
            Text(video.description, color = Color.White.copy(alpha = 0.9f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                video.tags.forEach { tag ->
                    FlowChip(tag, dark = true)
                }
            }
            when {
                isLoadingMore -> StatusPill("正在加载更多...", FlowColors.Mint)
                !loadError.isNullOrBlank() -> FlowChip("加载失败，点此重试", dark = true, onClick = onRetryLoadMore)
                isActive -> StatusPill("自动播放中", FlowColors.Success)
            }
        }
    }
}

@Composable
private fun VideoCard(video: VideoItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.62f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF255E5D),
                        FlowColors.VideoPanel,
                        FlowColors.VideoBlack,
                    ),
                ),
            )
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        AsyncImage(
            model = video.coverUrl,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.42f), Color.Black.copy(alpha = 0.86f)),
                    ),
                ),
        )
        Surface(
            modifier = Modifier.align(Alignment.TopStart),
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.24f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        ) {
            Text(
                "${video.durationSeconds}s · ${video.source}",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
        ) {
            Box(Modifier.size(74.dp), contentAlignment = Alignment.Center) {
                Text("▶", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VideoAction("赞", video.likes)
            VideoAction("评", video.comments)
            VideoAction("转", "分享")
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(video.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("@${video.author}", color = FlowColors.Mint, fontWeight = FontWeight.SemiBold)
            Text(video.description, color = Color.White.copy(alpha = 0.9f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                video.tags.forEach { tag ->
                    FlowChip(tag, dark = true)
                }
            }
        }
    }
}

@Composable
private fun VideoAction(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))) {
            Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Text(value, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

private val newsChannels = listOf("全部", "AI 产品", "开发者", "零售科技", "NFC", "支付")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsListScreen(navController: NavController) {
    var selectedChannel by remember { mutableStateOf("全部") }
    var uiState by remember { mutableStateOf<ContentUiState<List<NewsItem>>>(ContentUiState.Loading) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(selectedChannel, reloadKey) {
        uiState = ContentUiState.Loading
        val result = FlowCastRepository.loadNews(selectedChannel)
        uiState = when {
            result.items.isEmpty() -> ContentUiState.Empty(result.isFallback, result.errorMessage)
            else -> ContentUiState.Success(result.items, result.isFallback, result.errorMessage)
        }
    }

    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar("AI 新闻")
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                newsChannels.forEach { channel ->
                    FlowChip(channel, selected = channel == selectedChannel, onClick = { selectedChannel = channel })
                }
            }
            when (val content = uiState) {
                ContentUiState.Loading -> LoadingState(message = "新闻加载中...", modifier = Modifier.weight(1f))
                is ContentUiState.Empty -> EmptyState(
                    message = if (content.isFallback) "后端不可用，回退数据为空" else "暂无新闻内容",
                    modifier = Modifier.weight(1f),
                    onRetry = { reloadKey++ },
                )
                is ContentUiState.Error -> EmptyState(
                    message = content.message,
                    modifier = Modifier.weight(1f),
                    onRetry = { reloadKey++ },
                )
                is ContentUiState.Success -> {
                    if (content.isFallback) {
                        StatusPill("后端不可用，已显示回退数据", FlowColors.Warning)
                    }
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(content.data, key = { it.id }) { item ->
                            NewsCard(item, onClick = { navController.navigate("news/${item.id}") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(item: NewsItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = FlowColors.Card),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FlowChip(item.source)
                Text(item.publishedAt, style = MaterialTheme.typography.labelMedium, color = FlowColors.InkMuted)
            }
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
            Text(item.summary, style = MaterialTheme.typography.bodyMedium, color = FlowColors.InkMuted, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text("查看 AI 总结", color = FlowColors.Brand, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsDetailScreen(newsId: String, onBack: () -> Unit) {
    var sourceOpened by remember { mutableStateOf(false) }
    var uiState by remember { mutableStateOf<ContentUiState<NewsItem>>(ContentUiState.Loading) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(newsId, reloadKey) {
        uiState = ContentUiState.Loading
        val result = FlowCastRepository.loadNewsDetail(newsId)
        uiState = when (val item = result.item) {
            null -> ContentUiState.Error(result.errorMessage ?: "新闻不存在")
            else -> ContentUiState.Success(item, result.isFallback, result.errorMessage)
        }
    }

    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar("新闻总结", onBack = onBack)
        when (val content = uiState) {
            ContentUiState.Loading -> LoadingState(message = "新闻详情加载中...", modifier = Modifier.fillMaxSize())
            is ContentUiState.Empty -> EmptyState(content.message ?: "新闻为空", modifier = Modifier.fillMaxSize(), onRetry = { reloadKey++ })
            is ContentUiState.Error -> EmptyState(content.message, modifier = Modifier.fillMaxSize(), onRetry = { reloadKey++ })
            is ContentUiState.Success -> NewsDetailContent(
                item = content.data,
                sourceOpened = sourceOpened,
                isFallback = content.isFallback,
                onOpenSource = { sourceOpened = true },
            )
        }
    }
}

@Composable
private fun NewsDetailContent(
    item: NewsItem,
    sourceOpened: Boolean,
    isFallback: Boolean,
    onOpenSource: () -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (isFallback) {
                item {
                    StatusPill("后端不可用，已显示回退数据", FlowColors.Warning)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowChip("${item.source} · ${item.publishedAt}", selected = true)
                    Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
                }
            }
            item {
                InfoCard {
                    SectionHeader("AI 总结", "优先展示可复述的结论")
                    Spacer(Modifier.height(10.dp))
                    Text(item.summary, style = MaterialTheme.typography.bodyLarge, color = FlowColors.Ink)
                }
            }
            item {
                InfoCard {
                    SectionHeader("关键点")
                    Spacer(Modifier.height(8.dp))
                    item.keyPoints.forEachIndexed { index, point ->
                        KeyPoint(index + 1, point)
                    }
                }
            }
            item {
                InfoCard {
                    SectionHeader("原文链接", "Demo 环境模拟外部浏览器跳转")
                    Spacer(Modifier.height(10.dp))
                    TechBlock("原文 URL", item.sourceUrl)
                    Spacer(Modifier.height(12.dp))
                    FlowPrimaryButton(
                        text = if (sourceOpened) "已模拟打开原文" else "打开原文",
                        onClick = onOpenSource,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (sourceOpened) {
                item {
                    StatusPill("已发起外部链接跳转意图", FlowColors.Success)
                }
            }
        }
}

@Composable
private fun KeyPoint(index: Int, text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = CircleShape, color = FlowColors.BrandSoft) {
            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                Text(index.toString(), color = FlowColors.Brand, fontWeight = FontWeight.Bold)
            }
        }
        Text(text, modifier = Modifier.weight(1f), color = FlowColors.Ink)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashierScreen(navController: NavController) {
    var selectedMethodId by remember { mutableStateOf(DemoRepository.paymentMethods.first().id) }
    val selectedMethod = DemoRepository.paymentMethods.first { it.id == selectedMethodId }
    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar("收银台")
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = FlowColors.Card), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("FlowCast 线下门店", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("订单号 O10001 · 门店收银", color = FlowColors.InkMuted)
                            }
                            StatusPill("Mock 订单", FlowColors.Gold)
                        }
                        Text("¥88.00", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = FlowColors.Gold)
                        TechBlock("收银台 Scheme", "flowcast://pay/cashier?orderId=O10001&amount=88.00")
                    }
                }
            }
            item { SectionHeader("选择支付方式", "仅模拟支付结果，不接真实渠道") }
            items(DemoRepository.paymentMethods) { method ->
                PaymentMethodRow(
                    method = method,
                    selected = method.id == selectedMethodId,
                    onClick = { selectedMethodId = method.id },
                )
            }
            item {
                FlowPrimaryButton(
                    text = "使用${selectedMethod.name}支付",
                    onClick = { navController.navigate("mockPay/${selectedMethod.id}") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(method: PaymentMethod, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) FlowColors.BrandSoft else Color.White,
        border = BorderStroke(1.dp, if (selected) FlowColors.Brand else Color(0xFFE2EAE6)),
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(8.dp), color = if (selected) FlowColors.Brand else FlowColors.BrandSoft) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Text(method.name.takeLast(2), color = if (selected) Color.White else FlowColors.Brand, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(method.name, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
                Text(method.scheme, color = FlowColors.InkMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(if (selected) "已选择" else "选择", color = FlowColors.Brand, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MockPaymentScreen(method: PaymentMethod, navController: NavController) {
    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar(method.name, onBack = { navController.popBackStack() })
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = FlowColors.Card)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatusPill("模拟第三方支付页", FlowColors.Blue)
                    Text(method.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
                    Text("¥88.00", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = FlowColors.Gold)
                    Text("订单号 O10001", color = FlowColors.InkMuted, fontWeight = FontWeight.SemiBold)
                    Text("当前页面只验证 Scheme 跳转与结果回传链路。", color = FlowColors.InkMuted)
                    TechBlock("支付 Scheme", method.scheme)
                }
            }
            FlowPrimaryButton("支付成功", onClick = { navController.navigate("paymentResult/success") }, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = { navController.navigate("paymentResult/failed") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Text("支付失败")
            }
            OutlinedButton(onClick = { navController.navigate("paymentResult/canceled") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Text("取消支付")
            }
        }
    }
}

@Composable
private fun PaymentResultScreen(status: String, onDone: () -> Unit) {
    val title = when (status) {
        "success" -> "支付成功"
        "failed" -> "支付失败"
        "processing" -> "支付处理中"
        else -> "支付已取消"
    }
    val color = when (status) {
        "success" -> FlowColors.Success
        "failed" -> FlowColors.Error
        "processing" -> FlowColors.Blue
        else -> FlowColors.Warning
    }
    val message = when (status) {
        "success" -> "订单已完成，Demo 会回查服务端订单状态。"
        "failed" -> "支付渠道返回失败，可回到收银台重新选择方式。"
        "processing" -> "当前订单等待渠道回调，稍后可刷新状态。"
        else -> "用户取消了本次模拟支付，可重新发起。"
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowColors.Page)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.12f)) {
            Box(Modifier.size(92.dp), contentAlignment = Alignment.Center) {
                Text(if (status == "success") "✓" else "!", color = color, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
        Spacer(Modifier.height(8.dp))
        Text(message, color = FlowColors.InkMuted)
        Spacer(Modifier.height(18.dp))
        InfoCard {
            KeyValueRow("订单号", "O10001")
            KeyValueRow("金额", "¥88.00")
            KeyValueRow("支付方式", "模拟渠道")
        }
        Spacer(Modifier.height(24.dp))
        FlowPrimaryButton("返回收银台", onClick = onDone, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(navController: NavController) {
    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar("我的")
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandGradient, RoundedCornerShape(8.dp))
                        .padding(18.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("闪阅 FlowCast", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        StatusPill("内部 Demo · v2.1.0", Color.White)
                        Text("内容流、AI 新闻、模拟支付与 NFC 业务跳转闭环。", color = Color.White.copy(alpha = 0.86f))
                    }
                }
            }
            item {
                InfoCard {
                    SectionHeader("能力概览")
                    Spacer(Modifier.height(12.dp))
                    CapabilityRow("内容流", "公开视频源占位，保留后端适配空间")
                    CapabilityRow("AI 新闻", "列表、详情、总结与关键点层级")
                    CapabilityRow("模拟支付", "Scheme 发起和结果页闭环")
                    CapabilityRow("NFC", "场景 Scheme 解析与业务动作")
                }
            }
            item {
                FlowPrimaryButton("打开 NFC Demo", onClick = { navController.navigate("nfc") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                FlowPrimaryButton("NFC 记录", onClick = { navController.navigate("nfcHistory") }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CapabilityRow(title: String, desc: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Surface(shape = RoundedCornerShape(8.dp), color = FlowColors.BrandSoft) {
            Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                Text(title.take(1), color = FlowColors.Brand, fontWeight = FontWeight.Bold)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = FlowColors.Ink, fontWeight = FontWeight.Bold)
            Text(desc, color = FlowColors.InkMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NfcDemoScreen(navController: NavController) {
    val scenarios = DemoRepository.nfcScenarios
    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar("NFC Demo", onBack = { navController.popBackStack() })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                InfoCard {
                    SectionHeader("碰一碰触发", "模拟 NDEF URI 读取后的业务分发")
                    Spacer(Modifier.height(10.dp))
                    Text("选择一个线下标签场景，查看原始 Scheme、解析字段和下一步动作。", color = FlowColors.InkMuted)
                }
            }
            if (scenarios.isEmpty()) {
                item {
                    EmptyState(
                        message = "暂无 NFC 场景",
                        modifier = Modifier.padding(vertical = 32.dp),
                    )
                }
            } else {
                item { SectionHeader("选择场景") }
                items(scenarios) { scenario ->
                    NfcScenarioCard(scenario, onClick = { navController.navigate("nfcResult/${scenario.id}") })
                }
            }
        }
    }
}

@Composable
private fun NfcScenarioCard(scenario: NfcScenario, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = FlowColors.Card),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowChip("NFC")
                FlowChip(scenarioTag(scenario.id), selected = true)
            }
            Text(scenario.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
            Text(scenario.description, color = FlowColors.InkMuted)
            TechBlock("写入 Scheme", scenario.scheme)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NfcResultScreen(
    scenario: NfcScenario,
    onCashier: () -> Unit,
    onTableOrder: () -> Unit,
    onMiniProgram: () -> Unit,
    onBack: () -> Unit,
) {
    val fields = parseSchemeFields(scenario.scheme)
    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar("Scheme 解析", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = FlowColors.Card)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusPill("已解析 · NDEF URI", FlowColors.Success)
                        Text(scenario.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
                        Text(scenario.description, color = FlowColors.InkMuted)
                        TechBlock("原始 Scheme", scenario.scheme)
                    }
                }
            }
            item {
                InfoCard {
                    SectionHeader("解析字段")
                    Spacer(Modifier.height(8.dp))
                    fields.forEach { (key, value) ->
                        KeyValueRow(key, value)
                    }
                }
            }
            item {
                InfoCard {
                    SectionHeader("业务动作", "根据 Scheme host/path 分发到对应流程")
                    Spacer(Modifier.height(10.dp))
                    Text(businessAction(scenario), style = MaterialTheme.typography.titleMedium, color = FlowColors.Ink, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(actionDescription(scenario), color = FlowColors.InkMuted)
                }
            }
            item {
                when (scenario.id) {
                    "merchant" -> FlowPrimaryButton("进入收银台", onClick = onCashier, modifier = Modifier.fillMaxWidth())
                    "table" -> FlowPrimaryButton("进入桌牌点餐", onClick = onTableOrder, modifier = Modifier.fillMaxWidth())
                    "miniapp" -> FlowPrimaryButton("模拟小程序跳转", onClick = onMiniProgram, modifier = Modifier.fillMaxWidth())
                    else -> TechBlock("无法识别", "当前 Scheme 暂不支持，请返回 NFC Demo 重新选择。")
                }
            }
        }
    }
}

@Composable
private fun TableOrderScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar("桌牌点餐", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                InfoCard {
                    StatusPill("NFC 桌牌", FlowColors.Mint)
                    Spacer(Modifier.height(12.dp))
                    Text("FlowCast Coffee · T08 桌", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
                    Spacer(Modifier.height(8.dp))
                    Text("已从 NFC Scheme 恢复 storeId 与 tableId，第一版展示点餐入口占位。", color = FlowColors.InkMuted)
                }
            }
            item {
                InfoCard {
                    SectionHeader("推荐菜单")
                    Spacer(Modifier.height(8.dp))
                    KeyValueRow("冰拿铁", "¥26")
                    KeyValueRow("海盐芝士卷", "¥18")
                    KeyValueRow("门店桌号", "S10001 / T08")
                }
            }
        }
    }
}

@Composable
private fun MiniProgramJumpScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar("小程序跳转", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                InfoCard {
                    StatusPill("模拟跳转", FlowColors.Blue)
                    Spacer(Modifier.height(12.dp))
                    Text("微信小程序", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
                    Spacer(Modifier.height(8.dp))
                    Text("Demo 不真实拉起外部平台，仅展示 platform、appId 与 path 的解析结果。", color = FlowColors.InkMuted)
                }
            }
            item {
                InfoCard {
                    SectionHeader("跳转参数")
                    Spacer(Modifier.height(8.dp))
                    KeyValueRow("platform", "wechat")
                    KeyValueRow("appId", "wx123")
                    KeyValueRow("path", "pages/index/index")
                }
            }
        }
    }
}

@Composable
private fun MissingRouteScreen(title: String, message: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar(title, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            InfoCard {
                StatusPill("无法继续", FlowColors.Warning)
                Spacer(Modifier.height(12.dp))
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
                Spacer(Modifier.height(8.dp))
                Text(message, color = FlowColors.InkMuted)
                Spacer(Modifier.height(16.dp))
                FlowPrimaryButton("返回上一页", onClick = onBack, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlowTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBack != null) {
                OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(start = 8.dp)) {
                    Text("返回")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = FlowColors.Page),
    )
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = FlowColors.Card),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, modifier = Modifier.width(86.dp), color = FlowColors.InkMuted, style = MaterialTheme.typography.bodySmall)
        Text(value, modifier = Modifier.weight(1f), color = FlowColors.Ink, fontWeight = FontWeight.SemiBold)
    }
}

private fun parseSchemeFields(scheme: String): List<Pair<String, String>> {
    val query = scheme.substringAfter("?", missingDelimiterValue = "")
    val pairs = query
        .split("&")
        .filter { it.contains("=") }
        .map {
            val key = it.substringBefore("=")
            val value = it.substringAfter("=")
            key to value
        }
    return listOf("scheme" to scheme.substringBefore("?")) + pairs
}

private fun scenarioTag(id: String): String = when (id) {
    "merchant" -> "收款"
    "table" -> "点餐"
    else -> "小程序"
}

private fun businessAction(scenario: NfcScenario): String = when (scenario.id) {
    "merchant" -> "打开指定商户收银台"
    "table" -> "打开门店桌牌点餐"
    else -> "跳转平台小程序"
}

private fun actionDescription(scenario: NfcScenario): String = when (scenario.id) {
    "merchant" -> "携带 merchantId、amount 与 scene 参数，进入支付金额确认与方式选择。"
    "table" -> "携带 storeId 与 tableId，后续可恢复桌台、菜单和购物车上下文。"
    else -> "携带 platform、appId 与 path，后续按平台开放规则发起跳转。"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoPlayerScreen(video: VideoItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val player = remember(video.playUrl) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            if (video.playUrl.isNotBlank()) {
                setMediaItem(MediaItem.fromUri(video.playUrl))
                prepare()
                play()
            }
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    Column(Modifier.fillMaxSize().background(FlowColors.VideoBlack)) {
        FlowTopBar("视频播放", onBack = onBack)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            if (video.playUrl.isBlank()) {
                Text("暂无播放地址", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                AndroidView(
                    factory = { viewContext ->
                        PlayerView(viewContext).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                            useController = true
                            this.player = player
                        }
                    },
                    update = { it.player = player },
                    modifier = Modifier.fillMaxSize(),
                )
                StatusPill("播放中", FlowColors.Success, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(video.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            item {
                Text("@${video.author} · ${video.source} · ${video.durationSeconds}s", color = FlowColors.Mint, fontWeight = FontWeight.SemiBold)
            }
            item {
                Text(video.description, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    video.tags.forEach { tag ->
                        FlowChip(tag, dark = true, onClick = {})
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowChip("赞 ${video.likes}", dark = true, onClick = {})
                    FlowChip("评 ${video.comments}", dark = true, onClick = {})
                    FlowChip("分享", dark = true, onClick = {})
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NfcHistoryScreen(onBack: () -> Unit) {
    val history = remember {
        listOf(
            mapOf("time" to "2026-05-14 10:30", "scene" to "线下商户收款", "amount" to "¥20.00"),
            mapOf("time" to "2026-05-14 09:15", "scene" to "桌牌点餐", "table" to "T08"),
            mapOf("time" to "2026-05-13 16:45", "scene" to "小程序跳转", "platform" to "wechat"),
        )
    }
    Column(Modifier.fillMaxSize().background(FlowColors.Page)) {
        FlowTopBar("NFC 记录", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                InfoCard {
                    SectionHeader("历史记录", "最近 7 天的 NFC 触碰记录")
                }
            }
            items(history.size) { index ->
                val record = history[index]
                Card(
                    onClick = {},
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = FlowColors.Card),
                    elevation = CardDefaults.cardElevation(1.dp),
                ) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(record["scene"]!!, fontWeight = FontWeight.Bold, color = FlowColors.Ink)
                            Text(record["time"]!!, color = FlowColors.InkMuted, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(record.values.drop(2).joinToString(" ") { "$it" }, color = FlowColors.Brand, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
