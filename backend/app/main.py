from typing import Literal

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(
    title="FlowCast Demo API",
    description="Mock API for the FlowCast internal Android prototype.",
    version="0.1.0",
)


class VideoItem(BaseModel):
    id: str
    title: str
    author: str
    source: str
    description: str
    cover_url: str
    play_url: str
    likes: str
    comments: str


class NewsItem(BaseModel):
    id: str
    title: str
    summary: str
    source: str
    published_at: str
    key_points: list[str]
    source_url: str


class OrderCreateRequest(BaseModel):
    merchant_id: str = "M10001"
    amount: float = 88.0
    scene: str = "demo_cashier"


class Order(BaseModel):
    id: str
    merchant_id: str
    merchant_name: str
    amount: float
    status: Literal["pending", "success", "failed", "canceled"]
    cashier_scheme: str


class MockPaymentRequest(BaseModel):
    order_id: str
    method: Literal["alipay", "wechat", "unionpay"]
    status: Literal["success", "failed", "canceled"] = "success"


class NfcScenario(BaseModel):
    id: str
    title: str
    description: str
    scheme: str


VIDEOS = [
    VideoItem(
        id="v1",
        title="AI 产品晨报",
        author="FlowCast Lab",
        source="Public Video API placeholder",
        description="用第三方公开视频源模拟上下滑短视频体验。",
        cover_url="https://example.com/covers/ai-daily.jpg",
        play_url="https://example.com/videos/ai-daily.mp4",
        likes="12.8k",
        comments="326",
    ),
    VideoItem(
        id="v2",
        title="机器人咖啡店现场",
        author="Future Retail",
        source="Public Video API placeholder",
        description="后续接入 Pexels、Pixabay 或自建视频适配层。",
        cover_url="https://example.com/covers/robot-cafe.jpg",
        play_url="https://example.com/videos/robot-cafe.mp4",
        likes="8.4k",
        comments="148",
    ),
]

NEWS = [
    NewsItem(
        id="n1",
        title="多模态 AI 工具成为企业工作台入口",
        summary="近期多个 AI 平台加强了文档、图片、音视频理解能力，企业内部工作流成为主要落地场景。",
        source="AI Industry Mock",
        published_at="2026-05-14 09:30",
        key_points=[
            "企业场景更关注权限、审计和知识库接入。",
            "多模态输入降低了员工使用复杂工具的门槛。",
            "模型成本和响应速度仍是落地评估重点。",
        ],
        source_url="https://example.com/ai-enterprise-workbench",
    ),
    NewsItem(
        id="n2",
        title="公开视频 API 适合内部 Demo 快速验证内容流",
        summary="公开视频源可以降低版权和采集风险，并让客户端先验证播放、预加载和信息流交互。",
        source="Developer Mock",
        published_at="2026-05-14 10:15",
        key_points=[
            "客户端应请求自有后端，避免绑定具体第三方接口。",
            "后端适配层可以统一封面、播放地址和作者字段。",
            "第一版推荐用 Mock 数据保底。",
        ],
        source_url="https://example.com/public-video-api-demo",
    ),
]

NFC_SCENARIOS = [
    NfcScenario(
        id="merchant",
        title="线下商户收款",
        description="触碰后进入指定商户的收银台。",
        scheme="flowcast://nfc/pay?merchantId=M10001&amount=20.00&scene=merchant_collect",
    ),
    NfcScenario(
        id="table",
        title="门店桌牌点餐",
        description="触碰桌牌后绑定门店和桌号。",
        scheme="flowcast://nfc/order?storeId=S10001&tableId=T08&scene=table_order",
    ),
    NfcScenario(
        id="miniapp",
        title="碰一碰跳转小程序",
        description="解析平台、AppId 和页面路径。",
        scheme="flowcast://nfc/miniprogram?platform=wechat&appId=wx123&path=pages/index/index",
    ),
]

ORDERS: dict[str, Order] = {}


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.get("/videos/feed")
def video_feed() -> dict[str, object]:
    return {"items": VIDEOS, "next_cursor": "demo-next-page"}


@app.get("/news")
def news_list() -> dict[str, object]:
    return {"items": NEWS, "next_cursor": None}


@app.get("/news/{news_id}")
def news_detail(news_id: str) -> NewsItem:
    for item in NEWS:
        if item.id == news_id:
            return item
    raise HTTPException(status_code=404, detail="News item not found")


@app.post("/orders")
def create_order(payload: OrderCreateRequest) -> Order:
    order_id = f"O{len(ORDERS) + 10001}"
    order = Order(
        id=order_id,
        merchant_id=payload.merchant_id,
        merchant_name="FlowCast 线下门店",
        amount=payload.amount,
        status="pending",
        cashier_scheme=f"flowcast://pay/cashier?orderId={order_id}&amount={payload.amount:.2f}",
    )
    ORDERS[order_id] = order
    return order


@app.get("/orders/{order_id}")
def get_order(order_id: str) -> Order:
    if order_id not in ORDERS:
        raise HTTPException(status_code=404, detail="Order not found")
    return ORDERS[order_id]


@app.post("/payments/mock")
def mock_payment(payload: MockPaymentRequest) -> Order:
    if payload.order_id not in ORDERS:
        raise HTTPException(status_code=404, detail="Order not found")
    order = ORDERS[payload.order_id]
    updated = order.model_copy(update={"status": payload.status})
    ORDERS[payload.order_id] = updated
    return updated


@app.get("/nfc/scenarios")
def nfc_scenarios() -> dict[str, list[NfcScenario]]:
    return {"items": NFC_SCENARIOS}
