# FlowCast Demo API Contracts

用途：为 Android Demo 提供可替换的 Mock 服务契约。Android 首版可使用本地 `DemoRepository`，但字段含义应尽量与本文件保持一致。

Base URL for local development:

```text
http://127.0.0.1:8000
```

## Health

```http
GET /health
```

Response:

```json
{
  "status": "ok",
  "service": "flowcast-demo-api",
  "version": "0.1.0",
  "mode": "hybrid",
  "timestamp": "2026-05-14T01:30:00+00:00"
}
```

`mode` comes from `FLOWCAST_DATA_MODE` and supports `mock`, `public`, and
`hybrid` (default).

## Videos

```http
GET /videos/feed?cursor=1&limit=20&query=technology
```

Returns a vertical video feed. In `public` or `hybrid` mode the backend calls
Pexels `https://api.pexels.com/v1/videos/search` with the `Authorization`
header when `PEXELS_API_KEY` is configured. Missing keys, provider errors,
timeouts, empty provider payloads, and `mock` mode return the local mock feed.

Response shape:

```json
{
  "items": [
    {
      "id": "v1",
      "title": "AI 产品晨报",
      "author": "FlowCast Lab",
      "source": "Public Video API placeholder",
      "description": "用第三方公开视频源模拟上下滑短视频体验。",
      "cover_url": "https://example.com/covers/ai-daily.jpg",
      "play_url": "https://example.com/videos/ai-daily.mp4",
      "duration_seconds": 42,
      "likes": "12.8k",
      "comments": "326",
      "tags": ["AI", "晨报", "企业效率"]
    }
  ],
  "next_cursor": "2",
  "has_more": true
}
```

## News

```http
GET /news?channel=ai&cursor=1&limit=20
GET /news/{id}
```

The list endpoint returns titles and summaries. In `public` or `hybrid` mode the
backend calls GNews `top-headlines` for known channels such as `ai`,
`technology`, `business`, `science`, `sports`, and `health`, or GNews `search`
for custom channels. `GNEWS_API_KEY` is sent as the `apikey` query parameter.
Missing keys, provider errors, timeouts, empty provider payloads, and `mock`
mode return the local mock news list. The detail endpoint returns AI-style
summaries, key points, and the original source URL for mock/detail items.

Item fields:

```json
{
  "id": "n1",
  "title": "多模态 AI 工具成为企业工作台入口",
  "summary": "近期多个 AI 平台加强了文档、图片、音视频理解能力。",
  "source": "AI Industry Mock",
  "published_at": "2026-05-14 09:30",
  "key_points": [
    "企业场景更关注权限、审计和知识库接入。"
  ],
  "source_url": "https://example.com/ai-enterprise-workbench"
}
```

List response wrapper:

```json
{
  "items": [],
  "next_cursor": null,
  "has_more": false
}
```

## Orders

```http
POST /orders
GET /orders/{id}
```

Creates and reads demo orders.

Create request:

```json
{
  "merchant_id": "M10001",
  "amount": 88.0,
  "scene": "demo_cashier",
  "payment_method": "alipay"
}
```

Order response:

```json
{
  "id": "O10001",
  "merchant_id": "M10001",
  "merchant_name": "FlowCast 线下门店",
  "amount": 88.0,
  "status": "pending",
  "scene": "demo_cashier",
  "payment_method": "alipay",
  "cashier_scheme": "flowcast://pay/cashier?orderId=O10001&amount=88.00"
}
```

Allowed order status values:

```text
pending / success / failed / canceled
```

## Payment

```http
GET /payments/methods
POST /payments/mock
GET /payments/status/{order_id}
```

Updates a mock order payment status.

Payment methods response:

```json
{
  "items": [
    {
      "id": "alipay",
      "label": "支付宝",
      "enabled": true,
      "description": "Mock 支付宝收银台，默认可成功回调。",
      "mock_scheme": "flowcast://pay/mock/alipay"
    }
  ]
}
```

Mock payment request:

```json
{
  "order_id": "O10001",
  "method": "wechat",
  "status": "success"
}
```

Allowed method values:

```text
alipay / wechat / unionpay
```

Allowed mock status values:

```text
success / failed / canceled
```

Payment status response:

```json
{
  "order_id": "O10001",
  "status": "success",
  "method": "wechat",
  "amount": 88.0
}
```

## NFC

```http
GET /nfc/scenarios
```

Returns demo NFC Schemes for merchant collection, table ordering, and mini-program jumping.

Response shape:

```json
{
  "items": [
    {
      "id": "merchant",
      "type": "payment",
      "tag": "NFC_PAY_MERCHANT",
      "title": "线下商户收款",
      "description": "触碰后进入指定商户的收银台。",
      "action": "open_cashier",
      "scheme": "flowcast://nfc/pay?merchantId=M10001&amount=20.00&scene=merchant_collect",
      "parsed_fields": {
        "merchantId": "M10001",
        "amount": "20.00",
        "scene": "merchant_collect"
      },
      "next_screen": "CashierScreen"
    }
  ]
}
```

Allowed NFC type values:

```text
payment / order / miniapp
```

Allowed action values:

```text
open_cashier / open_table_order / launch_miniapp
```

## Demo Schemes

```text
flowcast://pay/cashier?orderId=O10001&amount=88.00
flowcast://pay/mock/alipay?orderId=O10001
flowcast://pay/mock/wechat?orderId=O10001
flowcast://pay/mock/unionpay?orderId=O10001
flowcast://pay/result?orderId=O10001&status=success

flowcast://nfc/pay?merchantId=M10001&amount=20.00&scene=merchant_collect
flowcast://nfc/order?storeId=S10001&tableId=T08&scene=table_order
flowcast://nfc/miniprogram?platform=wechat&appId=wx123&path=pages/index/index
```

## Error Shape

All backend errors use the same envelope:

```json
{
  "error": {
    "code": "NEWS_NOT_FOUND",
    "message": "News item not found",
    "details": {
      "errors": []
    }
  }
}
```

`details` is optional and is mainly present on request validation errors.

## Public Provider Environment

```bash
export FLOWCAST_DATA_MODE=hybrid
export PEXELS_API_KEY=your_pexels_key
export GNEWS_API_KEY=your_gnews_key
export PUBLIC_API_TIMEOUT_SECONDS=5
export PUBLIC_API_CACHE_TTL_SECONDS=300
```
