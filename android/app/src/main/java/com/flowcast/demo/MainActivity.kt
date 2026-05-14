package com.flowcast.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.flowcast.demo.data.DemoRepository
import com.flowcast.demo.data.NewsItem
import com.flowcast.demo.data.NfcScenario
import com.flowcast.demo.data.PaymentMethod
import com.flowcast.demo.data.VideoItem
import com.flowcast.demo.ui.FlowCastTheme

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

private data class TabItem(val route: String, val label: String)

private val tabs = listOf(
    TabItem("video", "视频"),
    TabItem("news", "新闻"),
    TabItem("cashier", "收银台"),
    TabItem("profile", "我的"),
)

@Composable
fun FlowCastApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route.orEmpty()
    val showBottomBar = tabs.any { currentRoute.startsWith(it.route) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
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
                            icon = { Text(tab.label.take(1)) },
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
            composable("video") { VideoFeedScreen() }
            composable("news") { NewsListScreen(navController) }
            composable(
                "news/{newsId}",
                arguments = listOf(navArgument("newsId") { type = NavType.StringType }),
            ) { entry ->
                val newsId = entry.arguments?.getString("newsId").orEmpty()
                NewsDetailScreen(
                    item = DemoRepository.news.first { it.id == newsId },
                    onBack = navController::popBackStack,
                )
            }
            composable("cashier") { CashierScreen(navController) }
            composable(
                "mockPay/{methodId}",
                arguments = listOf(navArgument("methodId") { type = NavType.StringType }),
            ) { entry ->
                val methodId = entry.arguments?.getString("methodId").orEmpty()
                MockPaymentScreen(
                    method = DemoRepository.paymentMethods.first { it.id == methodId },
                    navController = navController,
                )
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
                NfcResultScreen(
                    scenario = DemoRepository.nfcScenarios.first { it.id == scenarioId },
                    onCashier = { navController.navigate("cashier") },
                    onBack = navController::popBackStack,
                )
            }
        }
    }
}

@Composable
private fun VideoFeedScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101412)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(DemoRepository.videos) { video ->
            VideoCard(video)
        }
    }
}

@Composable
private fun VideoCard(video: VideoItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(640.dp)
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF1D6B5F), Color(0xFF171B1A), Color(0xFF0E1010)),
                ),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(18.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(video.title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Text("@${video.author}", color = Color(0xFFD8FFF4), fontWeight = FontWeight.SemiBold)
            Text(video.description, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(video.source, color = Color(0xFFBCD4CB), style = MaterialTheme.typography.labelMedium)
        }
        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Metric("赞", video.likes)
            Metric("评", video.comments)
            Metric("享", "分享")
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = 0.18f)) {
            Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color.White)
        }
        Text(value, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsListScreen(navController: NavController) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("AI 新闻") })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(DemoRepository.news) { item ->
                NewsCard(item, onClick = { navController.navigate("news/${item.id}") })
            }
        }
    }
}

@Composable
private fun NewsCard(item: NewsItem, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(item.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row {
                Text(item.source, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(12.dp))
                Text(item.publishedAt, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsDetailScreen(item: NewsItem, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("新闻总结") }, navigationIcon = { OutlinedButton(onClick = onBack) { Text("返回") } })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(item.summary, style = MaterialTheme.typography.bodyLarge)
            }
            item {
                Text("关键点", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                item.keyPoints.forEach { Text("• $it", modifier = Modifier.padding(top = 8.dp)) }
            }
            item {
                Text("原文链接", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(item.sourceUrl, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashierScreen(navController: NavController) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("收银台") })
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("FlowCast 线下门店", style = MaterialTheme.typography.titleMedium)
                    Text("订单号 O10001")
                    Text("¥88.00", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("flowcast://pay/cashier?orderId=O10001&amount=88.00")
                }
            }
            Text("选择支付方式", style = MaterialTheme.typography.titleMedium)
            DemoRepository.paymentMethods.forEach { method ->
                Button(
                    onClick = { navController.navigate("mockPay/${method.id}") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(method.name)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MockPaymentScreen(method: PaymentMethod, navController: NavController) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(method.name) })
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("模拟第三方支付页面", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(method.scheme)
            Button(onClick = { navController.navigate("paymentResult/success") }, modifier = Modifier.fillMaxWidth()) {
                Text("支付成功")
            }
            OutlinedButton(onClick = { navController.navigate("paymentResult/failed") }, modifier = Modifier.fillMaxWidth()) {
                Text("支付失败")
            }
            OutlinedButton(onClick = { navController.navigate("paymentResult/canceled") }, modifier = Modifier.fillMaxWidth()) {
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
        else -> "支付已取消"
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("Demo 版本后续会回查服务端订单状态。")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone) { Text("返回收银台") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(navController: NavController) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("我的") })
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("闪阅 FlowCast", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("内部 Demo：视图优先，底层逻辑后续逐步接入。")
            Button(onClick = { navController.navigate("nfc") }, modifier = Modifier.fillMaxWidth()) {
                Text("打开 NFC Demo")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NfcDemoScreen(navController: NavController) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("NFC Demo") })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(DemoRepository.nfcScenarios) { scenario ->
                Card(onClick = { navController.navigate("nfcResult/${scenario.id}") }, shape = RoundedCornerShape(8.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(scenario.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(scenario.description)
                        Text(scenario.scheme, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NfcResultScreen(
    scenario: NfcScenario,
    onCashier: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Scheme 解析") }, navigationIcon = { OutlinedButton(onClick = onBack) { Text("返回") } })
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(scenario.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(scenario.scheme)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(selected = true, onClick = {}, label = { Text("已解析") })
                FilterChip(selected = false, onClick = {}, label = { Text("NDEF URI") })
            }
            if (scenario.id == "merchant") {
                Button(onClick = onCashier, modifier = Modifier.fillMaxWidth()) {
                    Text("进入收银台")
                }
            } else {
                Text("这里后续接入点餐页或平台小程序跳转。")
            }
        }
    }
}
