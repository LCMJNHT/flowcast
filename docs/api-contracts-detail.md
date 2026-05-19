# FlowCast Demo API 契约详解

> 本文档详细描述每个 API 的请求/响应格式、错误码和边界情况处理。

---

## 1. 健康检查

### `GET /health`

**用途**: 检查后端服务是否可用

**响应**:
```json
{
  "status": "ok",
  "service": "flowcast-demo-api",
  "version": "0.1.0",
  "mode": "hybrid",
  "timestamp": "2026-05-14T01:30:00+00:00"
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 是 | 服务状态，ok/error |
| service | string | 是 | 服务名称 |
| version | string | 是 | 版本号 |
| mode | string | 是 | 运行模式，mock/public/hybrid，默认 hybrid |
| timestamp | string | 是 | ISO8601 时间戳 |

**错误处理**:

| HTTP 状态码 | 说明 |
|-------------|------|
| 200 | 服务正常 |
| 503 | 服务不可用 |

---

## 2. 视频 Feed

### `GET /videos/feed`

**用途**: 获取视频列表

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cursor | string | 否 | 分页游标，首屏不传 |
| limit | int | 否 | 每页数量，默认 20，最大 50 |
| query | string | 否 | Pexels 搜索词，默认 technology |

**响应**:
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

**数据源**:

- `FLOWCAST_DATA_MODE=mock`: 固定返回本地 Mock 数据。
- `FLOWCAST_DATA_MODE=public|hybrid`: 如果配置 `PEXELS_API_KEY`，服务端调用 Pexels `https://api.pexels.com/v1/videos/search`，通过 `Authorization` header 传 key。
- 无 key、第三方超时/失败、第三方返回空列表时，自动回退本地 Mock 数据。

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| items | array | 视频列表 |
| items[].id | string | 视频 ID |
| items[].title | string | 视频标题 |
| items[].author | string | 作者名称 |
| items[].source | string | 数据源说明 |
| items[].description | string | 视频描述 |
| items[].cover_url | string | 封面图 URL |
| items[].play_url | string | 播放地址 |
| items[].duration_seconds | int | 时长（秒） |
| items[].likes | string | 点赞数（格式化） |
| items[].comments | string | 评论数（格式化） |
| items[].tags | array | 标签列表 |
| next_cursor | string | 下一页游标 |
| has_more | boolean | 是否还有更多 |

**错误处理**:

| HTTP 状态码 | 错误码 | 说明 |
|-------------|--------|------|
| 200 | - | 成功 |
| 500 | VIDEO_FEED_ERROR | 视频源服务异常 |

**边界情况**:

- 视频列表为空：返回 `{"items": [], "next_cursor": null, "has_more": false}`
- 封面图加载失败：客户端使用默认渐变色背景
- 播放地址无效：客户端展示播放占位图标

---

## 3. 新闻模块

### `GET /news`

**用途**: 获取新闻列表

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| channel | string | 否 | 频道筛选，不传返回全部 |
| cursor | string | 否 | 分页游标 |
| limit | int | 否 | 每页数量，默认 20，最大 50 |

**响应**:
```json
{
  "items": [
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
  ],
  "next_cursor": null,
  "has_more": false
}
```

**数据源**:

- `FLOWCAST_DATA_MODE=mock`: 固定返回本地 Mock 数据。
- `FLOWCAST_DATA_MODE=public|hybrid`: 如果配置 `GNEWS_API_KEY`，已知频道走 GNews `top-headlines`，自定义频道走 GNews `search`，通过 `apikey` 参数传 key。
- 无 key、第三方超时/失败、第三方返回空列表时，自动回退本地 Mock 数据。

### `GET /news/{id}`

**用途**: 获取新闻详情

**响应**:
```json
{
  "id": "n1",
  "title": "多模态 AI 工具成为企业工作台入口",
  "summary": "近期多个 AI 平台加强了文档、图片、音视频理解能力。",
  "source": "AI Industry Mock",
  "published_at": "2026-05-14 09:30",
  "key_points": [
    "企业场景更关注权限、审计和知识库接入。",
    "多模态输入降低了员工使用复杂工具的门槛。",
    "模型成本和响应速度仍是落地评估重点。"
  ],
  "source_url": "https://example.com/ai-enterprise-workbench",
  "word_count": 1200,
  "read_time_minutes": 3
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| key_points | array | 关键点列表，至少 3 条 |
| source_url | string | 原文链接 |
| word_count | int | 原文字数（可选） |
| read_time_minutes | int | 阅读时长（可选） |

**错误处理**:

| HTTP 状态码 | 错误码 | 说明 |
|-------------|--------|------|
| 200 | - | 成功 |
| 404 | NEWS_NOT_FOUND | 新闻不存在 |
| 500 | NEWS_SERVICE_ERROR | 新闻服务异常 |

---

## 4. 订单模块

### `POST /orders`

**用途**: 创建订单

**请求**:
```json
{
  "merchant_id": "M10001",
  "merchant_name": "FlowCast 线下门店",
  "amount": 88.0,
  "scene": "demo_cashier",
  "payment_method": "alipay"
}
```

**请求字段**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| merchant_id | string | 是 | 商户 ID |
| merchant_name | string | 否 | 商户名称 |
| amount | number | 是 | 订单金额，必须>0 |
| scene | string | 否 | 场景标识 |
| payment_method | string | 否 | 支付方式 |

**响应**:
```json
{
  "id": "O10001",
  "merchant_id": "M10001",
  "merchant_name": "FlowCast 线下门店",
  "amount": 88.0,
  "status": "pending",
  "scene": "demo_cashier",
  "payment_method": null,
  "cashier_scheme": "flowcast://pay/cashier?orderId=O10001&amount=88.00",
  "created_at": "2026-05-14T10:00:00+00:00"
}
```

**错误处理**:

| HTTP 状态码 | 错误码 | 说明 |
|-------------|--------|------|
| 201 | - | 创建成功 |
| 400 | INVALID_AMOUNT | 金额无效 |
| 400 | MISSING_MERCHANT | 缺少商户信息 |
| 500 | ORDER_CREATE_FAILED | 订单创建失败 |

### `GET /orders/{id}`

**用途**: 获取订单详情

**响应**:
```json
{
  "id": "O10001",
  "merchant_id": "M10001",
  "merchant_name": "FlowCast 线下门店",
  "amount": 88.0,
  "status": "pending",
  "payment_method": null,
  "cashier_scheme": "flowcast://pay/cashier?orderId=O10001&amount=88.00"
}
```

**订单状态枚举**:

| 状态 | 说明 |
|------|------|
| pending | 待支付 |
| success | 支付成功 |
| failed | 支付失败 |
| canceled | 已取消 |

**错误处理**:

| HTTP 状态码 | 错误码 | 说明 |
|-------------|--------|------|
| 200 | - | 成功 |
| 404 | ORDER_NOT_FOUND | 订单不存在 |

---

## 5. 支付模块

### `GET /payments/methods`

**用途**: 获取支付方式列表

**响应**:
```json
{
  "items": [
    {
      "id": "alipay",
      "label": "支付宝",
      "enabled": true,
      "description": "Mock 支付宝收银台，默认可成功回调。",
      "mock_scheme": "flowcast://pay/mock/alipay",
      "icon_url": "https://example.com/icons/alipay.png"
    },
    {
      "id": "wechat",
      "label": "微信支付",
      "enabled": true,
      "description": "Mock 微信支付收银台。",
      "mock_scheme": "flowcast://pay/mock/wechat",
      "icon_url": "https://example.com/icons/wechat.png"
    },
    {
      "id": "unionpay",
      "label": "银联云闪付",
      "enabled": true,
      "description": "Mock 银联支付方式。",
      "mock_scheme": "flowcast://pay/mock/unionpay",
      "icon_url": "https://example.com/icons/unionpay.png"
    }
  ]
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 支付方式 ID |
| label | string | 展示名称 |
| enabled | boolean | 是否可用 |
| description | string | 描述说明 |
| mock_scheme | string | Mock Scheme |
| icon_url | string | 图标 URL |

### `POST /payments/mock`

**用途**: 模拟支付

**请求**:
```json
{
  "order_id": "O10001",
  "method": "wechat",
  "status": "success"
}
```

**请求字段**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| order_id | string | 是 | 订单 ID |
| method | string | 是 | 支付方式 |
| status | string | 是 | 支付结果状态 |

**status 枚举值**: `success` | `failed` | `canceled`

**响应**:
```json
{
  "order_id": "O10001",
  "status": "success",
  "method": "wechat",
  "amount": 88.0,
  "paid_at": "2026-05-14T10:05:00+00:00",
  "transaction_id": "T202605141005001"
}
```

**错误处理**:

| HTTP 状态码 | 错误码 | 说明 |
|-------------|--------|------|
| 200 | - | 成功 |
| 400 | INVALID_STATUS | 无效的状态值 |
| 404 | ORDER_NOT_FOUND | 订单不存在 |
| 500 | PAYMENT_PROCESS_FAILED | 支付处理失败 |

### `GET /payments/status/{order_id}`

**用途**: 查询支付状态

**响应**:
```json
{
  "order_id": "O10001",
  "status": "success",
  "method": "wechat",
  "amount": 88.0
}
```

**错误处理**:

| HTTP 状态码 | 错误码 | 说明 |
|-------------|--------|------|
| 200 | - | 成功 |
| 404 | ORDER_NOT_FOUND | 订单不存在 |

---

## 6. NFC 模块

### `GET /nfc/scenarios`

**用途**: 获取 NFC 场景列表

**响应**:
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
    },
    {
      "id": "table",
      "type": "order",
      "tag": "NFC_TABLE_ORDER",
      "title": "门店桌牌点餐",
      "description": "触碰桌牌后绑定门店和桌号。",
      "action": "open_table_order",
      "scheme": "flowcast://nfc/order?storeId=S10001&tableId=T08&scene=table_order",
      "parsed_fields": {
        "storeId": "S10001",
        "tableId": "T08",
        "scene": "table_order"
      },
      "next_screen": "TableOrderScreen"
    },
    {
      "id": "miniapp",
      "type": "miniapp",
      "tag": "NFC_MINIAPP_LAUNCH",
      "title": "碰一碰跳转小程序",
      "description": "解析平台、AppId 和页面路径。",
      "action": "launch_miniapp",
      "scheme": "flowcast://nfc/miniprogram?platform=wechat&appId=wx123&path=pages/index/index",
      "parsed_fields": {
        "platform": "wechat",
        "appId": "wx123",
        "path": "pages/index/index"
      },
      "next_screen": "MiniProgramJumpScreen"
    }
  ]
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 场景 ID |
| type | string | 场景类型：payment/order/miniapp |
| tag | string | NFC 标签标识 |
| title | string | 场景标题 |
| description | string | 场景描述 |
| action | string | 业务动作 |
| scheme | string | 完整 Scheme |
| parsed_fields | object | 解析后的字段 |
| next_screen | string | 下一个页面 |

**错误处理**:

| HTTP 状态码 | 错误码 | 说明 |
|-------------|--------|------|
| 200 | - | 成功 |
| 500 | NFC_SERVICE_ERROR | NFC 服务异常 |

---

## 7. 通用错误响应格式

### 错误响应结构

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "错误描述信息",
    "details": {
      "field": "具体字段说明"
    }
  }
}
```

### 通用错误码表

| 错误码 | HTTP 状态码 | 说明 |
|--------|-------------|------|
| INVALID_REQUEST | 422 | 请求参数无效 |
| NEWS_NOT_FOUND | 404 | 新闻不存在 |
| ORDER_NOT_FOUND | 404 | 订单不存在 |
| VIDEO_NOT_FOUND | 404 | 视频不存在 |
| INVALID_AMOUNT | 400 | 金额无效 |
| INVALID_PAYMENT_METHOD | 400 | 支付方式无效 |
| INVALID_STATUS | 400 | 支付状态无效 |
| INTERNAL_ERROR | 500 | 内部错误 |

---

## 8. 降级策略

### 8.0 服务端公共数据降级

`/videos/feed` 和 `/news` 在缺少 API key、第三方接口超时、第三方接口失败或第三方返回空列表时，由服务端自动回退到本地 Mock 数据，响应仍为 200。

### 8.1 后端不可用

当后端服务不可用时，Android 客户端应：

1. 自动切换到本地 Mock 数据
2. 展示"数据源状态：本地 Mock"提示
3. 记录日志以便后续排查
4. 不阻断用户核心流程

### 8.2 部分接口失败

当部分接口失败时：

1. 视频接口失败 → 展示本地 Mock 视频列表
2. 新闻接口失败 → 展示本地 Mock 新闻列表
3. 支付接口失败 → 使用本地 Mock 支付流程
4. NFC 接口失败 → 使用本地 Mock 场景列表

### 8.3 数据格式异常

当响应数据格式异常时：

1. 尝试使用默认值填充缺失字段
2. 无法解析时展示占位数据
3. 记录异常日志
4. 不展示原始错误信息给用户

---

*文档版本：v1.0*  
*最后更新：2026-05-14*  
*维护者：Backend Agent*
