package com.flowcast.demo.data

data class VideoItem(
    val id: String,
    val title: String,
    val author: String,
    val source: String,
    val description: String,
    val likes: String,
    val comments: String,
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
            source = "Public Video API placeholder",
            description = "用第三方公开视频源模拟上下滑短视频体验。",
            likes = "12.8k",
            comments = "326",
        ),
        VideoItem(
            id = "v2",
            title = "机器人咖啡店现场",
            author = "Future Retail",
            source = "Public Video API placeholder",
            description = "后续接入 Pexels、Pixabay 或自建视频适配层。",
            likes = "8.4k",
            comments = "148",
        ),
        VideoItem(
            id = "v3",
            title = "一分钟看懂 NFC 支付",
            author = "Pay Demo Team",
            source = "Public Video API placeholder",
            description = "用于演示内容流到支付场景的联动。",
            likes = "5.1k",
            comments = "92",
        ),
    )

    val news = listOf(
        NewsItem(
            id = "n1",
            title = "多模态 AI 工具成为企业工作台入口",
            summary = "近期多个 AI 平台加强了文档、图片、音视频理解能力，企业内部工作流成为主要落地场景。",
            source = "AI Industry Mock",
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
            source = "Developer Mock",
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
            source = "Retail Tech Mock",
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
