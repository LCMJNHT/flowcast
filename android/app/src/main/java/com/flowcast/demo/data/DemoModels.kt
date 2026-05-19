package com.flowcast.demo.data

data class VideoItem(
    val id: String,
    val title: String,
    val author: String,
    val source: String,
    val description: String,
    val coverUrl: String,
    val playUrl: String,
    val durationSeconds: Int,
    val likes: String,
    val comments: String,
    val tags: List<String>,
)

data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val publishedAt: String,
    val keyPoints: List<String>,
    val sourceUrl: String,
)

data class PaymentMethod(
    val id: String,
    val name: String,
    val scheme: String,
)

data class NfcScenario(
    val id: String,
    val title: String,
    val description: String,
    val scheme: String,
)

object DemoRepository {
    val videos = listOf(
        VideoItem(
            id = "v1",
            title = "AI 产品晨报",
            author = "FlowCast Lab",
            source = "公开视频源 Mock",
            description = "用第三方公开视频源模拟上下滑短视频体验。",
            coverUrl = "https://example.com/covers/ai-daily.jpg",
            playUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            durationSeconds = 42,
            likes = "12.8k",
            comments = "326",
            tags = listOf("AI", "晨报", "效率"),
        ),
        VideoItem(
            id = "v2",
            title = "机器人咖啡店现场",
            author = "Future Retail",
            source = "公开视频源 Mock",
            description = "后续接入 Pexels、Pixabay 或自建视频适配层。",
            coverUrl = "https://example.com/covers/robot-cafe.jpg",
            playUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            durationSeconds = 58,
            likes = "8.4k",
            comments = "148",
            tags = listOf("零售", "机器人", "现场"),
        ),
        VideoItem(
            id = "v3",
            title = "一分钟看懂 NFC 支付",
            author = "Pay Demo Team",
            source = "公开视频源 Mock",
            description = "用于演示内容流到支付场景的联动。",
            coverUrl = "https://example.com/covers/nfc-cashier.jpg",
            playUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            durationSeconds = 36,
            likes = "5.1k",
            comments = "92",
            tags = listOf("NFC", "支付", "Demo"),
        ),
        VideoItem(
            id = "v4",
            title = "智能门店巡检",
            author = "Retail Ops",
            source = "公开视频源 Mock",
            description = "模拟门店摄像头巡检与 AI 异常提示，用于验证连续刷视频体验。",
            coverUrl = "https://example.com/covers/store-inspection.jpg",
            playUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            durationSeconds = 48,
            likes = "6.7k",
            comments = "173",
            tags = listOf("零售", "巡检", "AI"),
        ),
        VideoItem(
            id = "v5",
            title = "边缘设备部署现场",
            author = "Infra Lab",
            source = "公开视频源 Mock",
            description = "展示边缘设备、摄像头和内容推荐服务的部署链路。",
            coverUrl = "https://example.com/covers/edge-device.jpg",
            playUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            durationSeconds = 52,
            likes = "9.2k",
            comments = "241",
            tags = listOf("边缘计算", "部署", "视频流"),
        ),
        VideoItem(
            id = "v6",
            title = "移动收银动线优化",
            author = "FlowCast Pay",
            source = "公开视频源 Mock",
            description = "用短视频演示移动收银、会员识别和小票回传的用户路径。",
            coverUrl = "https://example.com/covers/mobile-cashier.jpg",
            playUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            durationSeconds = 39,
            likes = "4.8k",
            comments = "88",
            tags = listOf("支付", "门店", "动线"),
        ),
        VideoItem(
            id = "v7",
            title = "一分钟理解推荐排序",
            author = "Recommend Team",
            source = "公开视频源 Mock",
            description = "用 Demo 数据展示刷新、分页、去重和预加载如何提升刷视频流畅度。",
            coverUrl = "https://example.com/covers/recommend-ranking.jpg",
            playUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            durationSeconds = 60,
            likes = "15.1k",
            comments = "402",
            tags = listOf("推荐", "分页", "预加载"),
        ),
        VideoItem(
            id = "v8",
            title = "会员运营快闪案例",
            author = "Growth Desk",
            source = "公开视频源 Mock",
            description = "内容流中穿插会员权益与门店活动，用于验证业务转化入口。",
            coverUrl = "https://example.com/covers/member-growth.jpg",
            playUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            durationSeconds = 44,
            likes = "7.6k",
            comments = "129",
            tags = listOf("会员", "运营", "活动"),
        ),
    )

    val news = listOf(
        NewsItem(
            id = "n1",
            title = "多模态 AI 工具成为企业工作台入口",
            summary = "近期多个 AI 平台加强了文档、图片、音视频理解能力，企业内部工作流成为主要落地场景。",
            source = "AI 行业 Mock",
            publishedAt = "2026-05-14 09:30",
            keyPoints = listOf(
                "企业场景更关注权限、审计和知识库接入。",
                "多模态输入降低了员工使用复杂工具的门槛。",
                "模型成本和响应速度仍是落地评估重点。",
            ),
            sourceUrl = "https://example.com/ai-enterprise-workbench",
        ),
        NewsItem(
            id = "n2",
            title = "公开视频 API 适合内部 Demo 快速验证内容流",
            summary = "公开视频源可以降低版权和采集风险，并让客户端先验证播放、预加载和信息流交互。",
            source = "开发者 Mock",
            publishedAt = "2026-05-14 10:15",
            keyPoints = listOf(
                "客户端应请求自有后端，避免绑定具体第三方接口。",
                "后端适配层可以统一封面、播放地址和作者字段。",
                "第一版推荐用 Mock 数据保底。",
            ),
            sourceUrl = "https://example.com/public-video-api-demo",
        ),
        NewsItem(
            id = "n3",
            title = "NFC 碰一碰正在扩展到点餐和小程序跳转",
            summary = "NFC 标签可以写入业务 Scheme，适合收款、桌牌点餐、会员页和小程序入口。",
            source = "零售科技 Mock",
            publishedAt = "2026-05-14 11:05",
            keyPoints = listOf(
                "Android 可通过 NDEF 读取 URI 或文本记录。",
                "支付结果仍需要服务端状态确认。",
                "小程序跳转要遵守目标平台开放规则。",
            ),
            sourceUrl = "https://example.com/nfc-mini-program",
        ),
    )

    val paymentMethods = listOf(
        PaymentMethod("alipay", "模拟支付宝", "flowcast://pay/mock/alipay?orderId=O10001"),
        PaymentMethod("wechat", "模拟微信支付", "flowcast://pay/mock/wechat?orderId=O10001"),
        PaymentMethod("unionpay", "模拟银联", "flowcast://pay/mock/unionpay?orderId=O10001"),
    )

    val nfcScenarios = listOf(
        NfcScenario(
            id = "merchant",
            title = "线下商户收款",
            description = "触碰后进入指定商户的收银台。",
            scheme = "flowcast://nfc/pay?merchantId=M10001&amount=20.00&scene=merchant_collect",
        ),
        NfcScenario(
            id = "table",
            title = "门店桌牌点餐",
            description = "触碰桌牌后绑定门店和桌号。",
            scheme = "flowcast://nfc/order?storeId=S10001&tableId=T08&scene=table_order",
        ),
        NfcScenario(
            id = "miniapp",
            title = "碰一碰跳转小程序",
            description = "解析平台、AppId 和页面路径。",
            scheme = "flowcast://nfc/miniprogram?platform=wechat&appId=wx123&path=pages/index/index",
        ),
    )
}
