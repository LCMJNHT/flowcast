import os
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Literal, Optional

import httpx
from fastapi import FastAPI, HTTPException, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field, ValidationError

app = FastAPI(
    title="FlowCast Demo API",
    description="Mock API for the FlowCast internal Android prototype.",
    version="0.1.0",
)


class ErrorDetail(BaseModel):
    code: str
    message: str
    details: Optional[dict] = None


class ErrorResponse(BaseModel):
    error: ErrorDetail


class Settings(BaseModel):
    data_mode: Literal["mock", "public", "hybrid"] = "hybrid"
    pexels_api_key: Optional[str] = None
    gnews_api_key: Optional[str] = None
    public_api_timeout_seconds: float = 5.0
    public_api_cache_ttl_seconds: int = 300


LOCAL_ENV_PATH = Path(__file__).resolve().parents[1] / ".env"


def _load_local_env() -> None:
    if not LOCAL_ENV_PATH.exists():
        return
    for raw_line in LOCAL_ENV_PATH.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip().strip("\"'"))


def get_settings() -> Settings:
    _load_local_env()
    mode = os.getenv("FLOWCAST_DATA_MODE", "hybrid").lower()
    if mode not in {"mock", "public", "hybrid"}:
        mode = "hybrid"
    return Settings(
        data_mode=mode,
        pexels_api_key=os.getenv("PEXELS_API_KEY") or None,
        gnews_api_key=os.getenv("GNEWS_API_KEY") or None,
        public_api_timeout_seconds=float(os.getenv("PUBLIC_API_TIMEOUT_SECONDS", "5")),
        public_api_cache_ttl_seconds=int(os.getenv("PUBLIC_API_CACHE_TTL_SECONDS", "300")),
    )


_PUBLIC_CACHE: dict[tuple[str, tuple[tuple[str, Any], ...]], tuple[float, Any]] = {}
PEXELS_VIDEO_SEARCH_URL = "https://api.pexels.com/v1/videos/search"


def _cache_key(provider: str, params: dict[str, Any]) -> tuple[str, tuple[tuple[str, Any], ...]]:
    return provider, tuple(sorted(params.items()))


def _cache_get(provider: str, params: dict[str, Any], ttl_seconds: int) -> Any:
    if ttl_seconds <= 0:
        return None
    cached = _PUBLIC_CACHE.get(_cache_key(provider, params))
    if not cached:
        return None
    created_at, value = cached
    if time.time() - created_at > ttl_seconds:
        return None
    return value


def _cache_set(provider: str, params: dict[str, Any], value: Any, ttl_seconds: int) -> None:
    if ttl_seconds > 0:
        _PUBLIC_CACHE[_cache_key(provider, params)] = (time.time(), value)


# 全局异常处理器
@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc: HTTPException):
    code = exc.detail if isinstance(exc.detail, str) else "HTTP_ERROR"
    details = exc.detail if isinstance(exc.detail, dict) else None
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": {
                "code": code,
                "message": ERROR_MESSAGES.get(code, code),
                **({"details": details} if details else {}),
            }
        }
    )


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc: RequestValidationError):
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "error": {
                "code": "INVALID_REQUEST",
                "message": "请求参数验证失败",
                "details": {"errors": exc.errors()}
            }
        }
    )


@app.exception_handler(Exception)
async def general_exception_handler(request, exc: Exception):
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "error": {
                "code": "INTERNAL_ERROR",
                "message": f"内部错误：{str(exc)}",
            }
        }
    )


class VideoItem(BaseModel):
    id: str
    title: str
    author: str
    source: str
    description: str
    cover_url: str
    play_url: str
    duration_seconds: int
    likes: str
    comments: str
    tags: list[str]


class NewsItem(BaseModel):
    id: str
    title: str
    summary: str
    source: str
    published_at: str
    key_points: list[str]
    source_url: str


class VideoFeedResponse(BaseModel):
    items: list[VideoItem]
    next_cursor: Optional[str] = None
    has_more: bool = False


class NewsListResponse(BaseModel):
    items: list[NewsItem]
    next_cursor: Optional[str] = None
    has_more: bool = False


class OrderCreateRequest(BaseModel):
    merchant_id: str = "M10001"
    amount: float = Field(default=88.0, gt=0)
    scene: str = "demo_cashier"
    payment_method: Optional[Literal["alipay", "wechat", "unionpay"]] = None


class Order(BaseModel):
    id: str
    merchant_id: str
    merchant_name: str
    amount: float
    status: Literal["pending", "success", "failed", "canceled"]
    scene: str
    payment_method: Optional[Literal["alipay", "wechat", "unionpay"]] = None
    cashier_scheme: str
    created_at: Optional[str] = None


class PaymentMethod(BaseModel):
    id: Literal["alipay", "wechat", "unionpay"]
    label: str
    enabled: bool = True
    description: str
    mock_scheme: str


class MockPaymentRequest(BaseModel):
    order_id: str
    method: Literal["alipay", "wechat", "unionpay"]
    status: Literal["success", "failed", "canceled"] = "success"


class PaymentStatus(BaseModel):
    order_id: str
    status: Literal["pending", "success", "failed", "canceled"]
    method: Optional[Literal["alipay", "wechat", "unionpay"]] = None
    amount: float


class NfcScenario(BaseModel):
    id: str
    type: Literal["payment", "order", "miniapp"]
    tag: str
    title: str
    description: str
    action: str
    scheme: str
    parsed_fields: dict[str, str]
    next_screen: str


VIDEOS = [
    VideoItem(
        id="v1",
        title="AI 产品晨报",
        author="FlowCast Lab",
        source="Public Video API placeholder",
        description="用第三方公开视频源模拟上下滑短视频体验。",
        cover_url="https://example.com/covers/ai-daily.jpg",
        play_url="https://example.com/videos/ai-daily.mp4",
        duration_seconds=42,
        likes="12.8k",
        comments="326",
        tags=["AI", "晨报", "企业效率"],
    ),
    VideoItem(
        id="v2",
        title="机器人咖啡店现场",
        author="Future Retail",
        source="Public Video API placeholder",
        description="后续接入 Pexels、Pixabay 或自建视频适配层。",
        cover_url="https://example.com/covers/robot-cafe.jpg",
        play_url="https://example.com/videos/robot-cafe.mp4",
        duration_seconds=58,
        likes="8.4k",
        comments="148",
        tags=["机器人", "零售", "现场"],
    ),
    VideoItem(
        id="v3",
        title="NFC 收银台演示",
        author="Demo Ops",
        source="Public Video API placeholder",
        description="展示触碰门店标签后进入 FlowCast Mock 收银台的完整链路。",
        cover_url="https://example.com/covers/nfc-cashier.jpg",
        play_url="https://example.com/videos/nfc-cashier.mp4",
        duration_seconds=36,
        likes="5.1k",
        comments="92",
        tags=["NFC", "支付", "Demo"],
    ),
    VideoItem(
        id="v4",
        title="智能门店巡检",
        author="Retail Ops",
        source="Public Video API placeholder",
        description="模拟门店摄像头巡检与 AI 异常提示，用于验证连续刷视频体验。",
        cover_url="https://example.com/covers/store-inspection.jpg",
        play_url="https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        duration_seconds=48,
        likes="6.7k",
        comments="173",
        tags=["零售", "巡检", "AI"],
    ),
    VideoItem(
        id="v5",
        title="边缘设备部署现场",
        author="Infra Lab",
        source="Public Video API placeholder",
        description="展示边缘设备、摄像头和内容推荐服务的部署链路。",
        cover_url="https://example.com/covers/edge-device.jpg",
        play_url="https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
        duration_seconds=52,
        likes="9.2k",
        comments="241",
        tags=["边缘计算", "部署", "视频流"],
    ),
    VideoItem(
        id="v6",
        title="移动收银动线优化",
        author="FlowCast Pay",
        source="Public Video API placeholder",
        description="用短视频演示移动收银、会员识别和小票回传的用户路径。",
        cover_url="https://example.com/covers/mobile-cashier.jpg",
        play_url="https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
        duration_seconds=39,
        likes="4.8k",
        comments="88",
        tags=["支付", "门店", "动线"],
    ),
    VideoItem(
        id="v7",
        title="一分钟理解推荐排序",
        author="Recommend Team",
        source="Public Video API placeholder",
        description="用 Demo 数据展示刷新、分页、去重和预加载如何提升刷视频流畅度。",
        cover_url="https://example.com/covers/recommend-ranking.jpg",
        play_url="https://storage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
        duration_seconds=60,
        likes="15.1k",
        comments="402",
        tags=["推荐", "分页", "预加载"],
    ),
    VideoItem(
        id="v8",
        title="会员运营快闪案例",
        author="Growth Desk",
        source="Public Video API placeholder",
        description="内容流中穿插会员权益与门店活动，用于验证业务转化入口。",
        cover_url="https://example.com/covers/member-growth.jpg",
        play_url="https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        duration_seconds=44,
        likes="7.6k",
        comments="129",
        tags=["会员", "运营", "活动"],
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
        type="payment",
        tag="NFC_PAY_MERCHANT",
        title="线下商户收款",
        description="触碰后进入指定商户的收银台。",
        action="open_cashier",
        scheme="flowcast://nfc/pay?merchantId=M10001&amount=20.00&scene=merchant_collect",
        parsed_fields={
            "merchantId": "M10001",
            "amount": "20.00",
            "scene": "merchant_collect",
        },
        next_screen="CashierScreen",
    ),
    NfcScenario(
        id="table",
        type="order",
        tag="NFC_TABLE_ORDER",
        title="门店桌牌点餐",
        description="触碰桌牌后绑定门店和桌号。",
        action="open_table_order",
        scheme="flowcast://nfc/order?storeId=S10001&tableId=T08&scene=table_order",
        parsed_fields={
            "storeId": "S10001",
            "tableId": "T08",
            "scene": "table_order",
        },
        next_screen="TableOrderScreen",
    ),
    NfcScenario(
        id="miniapp",
        type="miniapp",
        tag="NFC_MINIAPP_LAUNCH",
        title="碰一碰跳转小程序",
        description="解析平台、AppId 和页面路径。",
        action="launch_miniapp",
        scheme="flowcast://nfc/miniprogram?platform=wechat&appId=wx123&path=pages/index/index",
        parsed_fields={
            "platform": "wechat",
            "appId": "wx123",
            "path": "pages/index/index",
        },
        next_screen="MiniProgramJumpScreen",
    ),
]

PAYMENT_METHODS = [
    PaymentMethod(
        id="alipay",
        label="支付宝",
        description="Mock 支付宝收银台，默认可成功回调。",
        mock_scheme="flowcast://pay/mock/alipay",
    ),
    PaymentMethod(
        id="wechat",
        label="微信支付",
        description="Mock 微信支付收银台，用于演示取消和失败状态。",
        mock_scheme="flowcast://pay/mock/wechat",
    ),
    PaymentMethod(
        id="unionpay",
        label="银联云闪付",
        description="Mock 银联支付方式，用于补齐支付渠道列表。",
        mock_scheme="flowcast://pay/mock/unionpay",
    ),
]

ORDERS: dict[str, Order] = {
    "O10000": Order(
        id="O10000",
        merchant_id="M10001",
        merchant_name="FlowCast 线下门店",
        amount=20.0,
        status="pending",
        scene="merchant_collect",
        payment_method=None,
        cashier_scheme="flowcast://pay/cashier?orderId=O10000&amount=20.00",
    )
}


ERROR_MESSAGES = {
    "HTTP_ERROR": "HTTP request failed",
    "INTERNAL_ERROR": "Internal server error",
    "INVALID_AMOUNT": "Amount must be greater than zero",
    "INVALID_PAYMENT_METHOD": "Unsupported payment method",
    "INVALID_REQUEST": "Request validation failed",
    "INVALID_STATUS": "Unsupported payment status",
    "MISSING_MERCHANT": "Merchant id is required",
    "NEWS_NOT_FOUND": "News item not found",
    "ORDER_NOT_FOUND": "Order not found",
    "VIDEO_NOT_FOUND": "Video item not found",
}


def _bounded_limit(limit: int, default: int = 20, maximum: int = 50) -> int:
    if limit <= 0:
        return default
    return min(limit, maximum)


def _cursor_page(cursor: Optional[str]) -> int:
    if not cursor:
        return 1
    try:
        return max(int(cursor), 1)
    except ValueError:
        return 1


def _mock_paginate(items: list[Any], cursor: Optional[str], limit: int) -> tuple[list[Any], Optional[str], bool]:
    page = _cursor_page(cursor)
    size = _bounded_limit(limit, maximum=20)
    start = (page - 1) * size
    end = start + size
    page_items = items[start:end]
    has_more = end < len(items)
    return page_items, str(page + 1) if has_more else None, has_more


def _format_count(value: int) -> str:
    if value >= 1000:
        return f"{value / 1000:.1f}k"
    return str(value)


def _first_video_file(video_files: list[dict[str, Any]]) -> Optional[str]:
    if not video_files:
        return None
    sorted_files = sorted(
        video_files,
        key=lambda item: (item.get("width") or 0, item.get("height") or 0),
        reverse=True,
    )
    return sorted_files[0].get("link")


def _map_pexels_video(item: dict[str, Any]) -> VideoItem:
    user = item.get("user") or {}
    image = item.get("image") or ""
    return VideoItem(
        id=f"pexels-{item.get('id')}",
        title=item.get("url", "Pexels Video").rstrip("/").split("/")[-1].replace("-", " ").title(),
        author=user.get("name") or "Pexels Creator",
        source="Pexels",
        description=item.get("url") or "Pexels public video",
        cover_url=image,
        play_url=_first_video_file(item.get("video_files") or []) or item.get("url") or image,
        duration_seconds=int(item.get("duration") or 0),
        likes=_format_count(int(item.get("likes") or 0)),
        comments="0",
        tags=["Pexels", "Public Video"],
    )


def fetch_pexels_videos(query: str, cursor: Optional[str], limit: int, settings: Settings) -> VideoFeedResponse:
    page = _cursor_page(cursor)
    per_page = _bounded_limit(limit)
    params = {"query": query, "page": page, "per_page": per_page}
    cached = _cache_get("pexels", params, settings.public_api_cache_ttl_seconds)
    if cached:
        return cached

    response = httpx.get(
        PEXELS_VIDEO_SEARCH_URL,
        params=params,
        headers={"Authorization": settings.pexels_api_key or ""},
        timeout=settings.public_api_timeout_seconds,
        trust_env=False,
    )
    if response.is_error:
        response.raise_for_status()
    payload = response.json()
    items = [_map_pexels_video(item) for item in payload.get("videos", [])]
    total_results = int(payload.get("total_results") or len(items))
    has_more = page * per_page < total_results and bool(items)
    result = VideoFeedResponse(items=items, next_cursor=str(page + 1) if has_more else None, has_more=has_more)
    _cache_set("pexels", params, result, settings.public_api_cache_ttl_seconds)
    return result


GNEWS_CATEGORIES = {
    "general": "general",
    "world": "world",
    "business": "business",
    "technology": "technology",
    "entertainment": "entertainment",
    "sports": "sports",
    "science": "science",
    "health": "health",
    "ai": "technology",
    "tech": "technology",
    "全部": "general",
}


def _map_gnews_article(item: dict[str, Any], index: int) -> NewsItem:
    source = item.get("source") or {}
    source_name = source.get("name") or "GNews"
    title = item.get("title") or "Untitled"
    description = item.get("description") or item.get("content") or ""
    return NewsItem(
        id=f"gnews-{abs(hash(item.get('url') or title))}-{index}",
        title=title,
        summary=description,
        source=source_name,
        published_at=item.get("publishedAt") or "",
        key_points=[description] if description else [title],
        source_url=item.get("url") or source.get("url") or "",
    )


def fetch_gnews(channel: Optional[str], cursor: Optional[str], limit: int, settings: Settings) -> NewsListResponse:
    page = _cursor_page(cursor)
    size = _bounded_limit(limit)
    normalized_channel = (channel or "general").strip()
    category = GNEWS_CATEGORIES.get(normalized_channel.lower()) or GNEWS_CATEGORIES.get(normalized_channel)
    params: dict[str, Any] = {
        "apikey": settings.gnews_api_key,
        "lang": "en",
        "country": "us",
        "max": size,
        "page": page,
    }
    if category:
        endpoint = "https://gnews.io/api/v4/top-headlines"
        params["category"] = category
    else:
        endpoint = "https://gnews.io/api/v4/search"
        params["q"] = normalized_channel

    cache_params = {key: value for key, value in params.items() if key != "apikey"}
    cached = _cache_get("gnews", cache_params, settings.public_api_cache_ttl_seconds)
    if cached:
        return cached

    response = httpx.get(endpoint, params=params, timeout=settings.public_api_timeout_seconds, trust_env=False)
    if response.is_error:
        response.raise_for_status()
    payload = response.json()
    items = [_map_gnews_article(item, index) for index, item in enumerate(payload.get("articles", []))]
    total_articles = int(payload.get("totalArticles") or len(items))
    has_more = page * size < total_articles and bool(items)
    result = NewsListResponse(items=items, next_cursor=str(page + 1) if has_more else None, has_more=has_more)
    _cache_set("gnews", cache_params, result, settings.public_api_cache_ttl_seconds)
    return result


def get_video_feed(cursor: Optional[str], limit: int, query: str) -> VideoFeedResponse:
    settings = get_settings()
    if settings.data_mode != "mock" and settings.pexels_api_key:
        try:
            public_feed = fetch_pexels_videos(query, cursor, limit, settings)
            if public_feed.items:
                return public_feed
        except (httpx.HTTPError, TypeError, ValueError, ValidationError):
            pass
    items, next_cursor, has_more = _mock_paginate(VIDEOS, cursor, limit)
    return VideoFeedResponse(items=items, next_cursor=next_cursor, has_more=has_more)


def get_news_feed(channel: Optional[str], cursor: Optional[str], limit: int) -> NewsListResponse:
    settings = get_settings()
    if settings.data_mode != "mock" and settings.gnews_api_key:
        try:
            public_feed = fetch_gnews(channel, cursor, limit, settings)
            if public_feed.items:
                return public_feed
        except (httpx.HTTPError, TypeError, ValueError, ValidationError):
            pass
    items = NEWS
    if channel and channel not in {"全部", "general"}:
        lowered = channel.lower()
        filtered = [
            item
            for item in NEWS
            if lowered in item.title.lower()
            or lowered in item.summary.lower()
            or lowered in item.source.lower()
            or channel in item.title
            or channel in item.summary
            or channel in item.source
        ]
        items = filtered or NEWS
    page_items, next_cursor, has_more = _mock_paginate(items, cursor, limit)
    return NewsListResponse(items=page_items, next_cursor=next_cursor, has_more=has_more)


@app.get("/health")
def health() -> dict[str, str]:
    settings = get_settings()
    return {
        "status": "ok",
        "service": "flowcast-demo-api",
        "version": app.version,
        "mode": settings.data_mode,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }


@app.get("/videos/feed")
def video_feed(cursor: Optional[str] = None, limit: int = 20, query: str = "technology") -> VideoFeedResponse:
    return get_video_feed(cursor=cursor, limit=limit, query=query)


@app.get("/news")
def news_list(channel: Optional[str] = None, cursor: Optional[str] = None, limit: int = 20) -> NewsListResponse:
    return get_news_feed(channel=channel, cursor=cursor, limit=limit)


@app.get("/news/{news_id}")
def news_detail(news_id: str) -> NewsItem:
    for item in NEWS:
        if item.id == news_id:
            return item
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail="NEWS_NOT_FOUND",
    )


@app.post("/orders")
def create_order(payload: OrderCreateRequest) -> Order:
    # 验证金额
    if payload.amount <= 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="INVALID_AMOUNT",
        )
    # 验证商户信息
    if not payload.merchant_id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="MISSING_MERCHANT",
        )
    order_id = f"O{len(ORDERS) + 10000}"
    order = Order(
        id=order_id,
        merchant_id=payload.merchant_id,
        merchant_name="FlowCast 线下门店",
        amount=payload.amount,
        status="pending",
        scene=payload.scene,
        payment_method=payload.payment_method,
        cashier_scheme=f"flowcast://pay/cashier?orderId={order_id}&amount={payload.amount:.2f}",
        created_at=datetime.now(timezone.utc).isoformat(),
    )
    ORDERS[order_id] = order
    return order


@app.get("/orders/{order_id}")
def get_order(order_id: str) -> Order:
    if order_id not in ORDERS:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="ORDER_NOT_FOUND",
        )
    return ORDERS[order_id]


@app.get("/payments/methods")
def payment_methods() -> dict[str, list[PaymentMethod]]:
    return {"items": PAYMENT_METHODS}


@app.get("/payments/status/{order_id}")
def payment_status(order_id: str) -> PaymentStatus:
    if order_id not in ORDERS:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="ORDER_NOT_FOUND",
        )
    order = ORDERS[order_id]
    return PaymentStatus(
        order_id=order.id,
        status=order.status,
        method=order.payment_method,
        amount=order.amount,
    )


@app.post("/payments/mock")
def mock_payment(payload: MockPaymentRequest) -> Order:
    if payload.order_id not in ORDERS:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="ORDER_NOT_FOUND",
        )
    # 验证支付方式
    if payload.method not in ["alipay", "wechat", "unionpay"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="INVALID_PAYMENT_METHOD",
        )
    # 验证支付状态
    if payload.status not in ["success", "failed", "canceled"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="INVALID_STATUS",
        )
    order = ORDERS[payload.order_id]
    updated = order.model_copy(update={"status": payload.status, "payment_method": payload.method})
    ORDERS[payload.order_id] = updated
    return updated


@app.get("/nfc/scenarios")
def nfc_scenarios() -> dict[str, list[NfcScenario]]:
    return {"items": NFC_SCENARIOS}


@app.get("/videos/{video_id}/play")
def video_play(video_id: str) -> dict[str, str]:
    """获取视频播放地址"""
    for video in VIDEOS:
        if video.id == video_id:
            return {
                "video_id": video_id,
                "play_url": video.play_url,
                "cover_url": video.cover_url,
                "duration_seconds": str(video.duration_seconds),
            }
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail="VIDEO_NOT_FOUND",
    )


@app.get("/nfc/records")
def nfc_records() -> dict[str, list[dict]]:
    """获取 NFC 历史记录"""
    return {
        "items": [
            {
                "id": "nfc_1",
                "time": "2026-05-14 10:30:00",
                "type": "payment",
                "scene": "线下商户收款",
                "amount": 20.00,
                "merchant": "FlowCast 咖啡店",
            },
            {
                "id": "nfc_2",
                "time": "2026-05-14 09:15:00",
                "type": "order",
                "scene": "桌牌点餐",
                "store": "FlowCast Coffee",
                "table": "T08",
            },
            {
                "id": "nfc_3",
                "time": "2026-05-13 16:45:00",
                "type": "miniapp",
                "scene": "小程序跳转",
                "platform": "wechat",
            },
        ]
    }


@app.post("/payments/status/{order_id}/poll")
def payment_status_poll(order_id: str) -> PaymentStatus:
    """轮询支付状态"""
    if order_id not in ORDERS:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="ORDER_NOT_FOUND",
        )
    order = ORDERS[order_id]
    return PaymentStatus(
        order_id=order.id,
        status=order.status,
        method=order.payment_method,
        amount=order.amount,
    )
