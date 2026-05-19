# FlowCast Demo Final PRD

文档状态：终稿口径，用于内部评审、Android 实现验收和 FastAPI Mock 契约对齐。

设计来源：当前仓库未提供 `docs/final-ui-design.md`。本 PRD 以 `README.md`、`docs/product-outline.md`、`docs/screen-map.md`、`docs/api-contracts.md`、Android Compose 本地 Mock 和 backend FastAPI Mock 为准。

## 1. Product Goal

`闪阅 FlowCast` 是一个内部 Demo，用于验证“短视频 + AI 新闻 + Scheme 模拟支付 + NFC 场景跳转”的一体化产品形态。第一版优先确认视图、导航和演示链路，不追求真实生产级内容、推荐、支付和 NFC 能力。

核心目标：

- 让评审者能在一台 Android 手机上完整体验四条主路径：刷视频、看新闻总结、走收银台支付、触发 NFC 场景。
- 页面视觉具备内容消费类 App 的完整感，而不是纯技术样例。
- 所有数据源和业务逻辑允许先 Mock，但路由、接口命名、页面边界要保持后续可替换。
- 让后续真实视频源、AI 总结服务、支付渠道和 NFC Intent 接入时，不需要推翻当前信息架构。

## 2. Audience

- 内部产品、研发、设计评审人员。
- 后续负责 Android、FastAPI、测试和 CR 的实现者。
- 线下演示场景中的业务方观察者。
- 需要根据文档执行手工 UI 验收和冒烟测试的 QA/Agent。

## 3. Product Principles

- 视图优先：先确认页面结构、视觉密度和交互闭环。
- Mock 优先：视频、新闻、订单、支付、NFC 场景都允许使用本地或 FastAPI Mock 数据。
- 真实形态：页面、Scheme、接口和状态命名尽量接近真实产品。
- 可演示：每个核心入口都必须可点击、可返回、可解释。
- 可降级：后端不可用时，Android 仍应能通过本地 Mock 展示主要页面。
- 场景化：NFC、支付和 AI 新闻都要以业务任务呈现，避免只展示技术字段。
- 可验收：每个页面都要有明确的主信息、主动作、返回路径和异常/占位口径。

## 4. MVP Scope

### 4.1 In Scope

- Android Jetpack Compose 原型页面与导航。
- 本地 Mock 数据和 FastAPI Mock API 双轨口径。
- 视频 Feed 的沉浸式视觉、信息层级和互动入口占位。
- AI 新闻列表、详情、总结、关键点和原文链接展示。
- 收银台订单信息、支付方式选择、模拟支付页和支付结果页。
- NFC Demo 的三类场景：线下商户收款、门店桌牌点餐、小程序跳转。
- Scheme 展示、解析说明和下一步业务动作。
- 我的页作为 Demo 信息、能力概览和 NFC 工具入口。

### 4.2 Out of Scope

- 真实账号体系、用户画像、推荐算法和内容审核。
- 真实抖音/快手内容抓取或第三方公开视频 API Key 管理。
- 新闻爬虫、版权处理、真实 AI 总结任务调度。
- 真实支付 SDK、资金流、回调验签和商户结算。
- 真实 NFC Tag 写入工具、机型兼容性矩阵和线下压测。
- 生产级监控、埋点、灰度、风控和隐私合规闭环。

## 5. Information Architecture

底部导航保留四个一级入口：

```text
视频 / 新闻 / 收银台 / 我的
```

NFC 不作为一级 Tab。第一版通过“我的”里的 NFC Demo 入口演示，后续真实设备可由系统 NFC Intent 直接打开对应 Scheme。

页面关系：

```text
MainShell
  -> VideoFeed
  -> NewsList -> NewsDetail
  -> Cashier -> MockPayment -> PaymentResult
  -> Profile -> NfcDemo -> NfcResult
       -> merchant collection: Cashier
       -> table order: placeholder/action explanation
       -> mini program: placeholder/action explanation
```

说明：`docs/screen-map.md` 保留目标页名 `NfcSchemeResolveScreen`、`TableOrderScreen`、`MiniProgramJumpScreen`。当前 Android 实现中，NFC 解析由 `NfcResultScreen` 承载，桌牌点餐和小程序跳转以“下一步占位/动作说明”验收。

## 6. User Paths

### 6.1 短视频浏览

```text
打开 App
  -> 默认进入视频 Tab
  -> 浏览沉浸式视频 Feed
  -> 查看作者、标题、来源、点赞、评论、分享入口
```

验收重点：用户第一眼能理解这是短视频 Feed，而不是普通列表。

### 6.2 新闻阅读

```text
进入新闻 Tab
  -> 浏览 AI 新闻列表
  -> 点击新闻卡片
  -> 查看 AI 总结、关键点、原文链接
  -> 点击原文链接跳转外部浏览器或展示跳转意图
```

验收重点：列表高效可扫读，详情页突出“总结”而不是全文转载。

### 6.3 Scheme 模拟支付

```text
进入收银台 Tab
  -> 查看商户、订单金额、支付方式
  -> 选择模拟支付宝/微信/银联
  -> 跳转 MockPaymentScreen
  -> 点击成功/失败/取消
  -> 回到 PaymentResultScreen
```

验收重点：收银台可信、金额突出、支付方式清晰，结果状态明确。

### 6.4 NFC 场景演示

```text
进入我的
  -> 打开 NFC Demo
  -> 选择线下收款、桌牌点餐或小程序跳转
  -> 展示并解析对应 Scheme
  -> 跳转到收银台、点餐页或小程序跳转模拟页
```

验收重点：NFC 是业务场景入口，不是单纯技术字符串展示。

## 7. Page Requirements

### 7.1 Main Shell

- 默认首屏进入视频 Tab。
- 底部导航固定为 `视频 / 新闻 / 收银台 / 我的`，二级页面可按实现隐藏或保留底部栏，但返回路径必须清楚。
- App 关键页面露出 `闪阅 FlowCast` 品牌或当前模块名称。
- 不要求登录、定位、授权弹窗或真实账户状态。

### 7.2 视频 Feed

- 采用深色/沉浸式背景，视频卡接近全屏竖向比例。
- 单条内容至少包含标题、作者、描述、来源状态、点赞、评论、分享入口。
- 当前视频不可播放时，可使用封面/渐变/播放占位，但不能退化为纯文本列表。
- 数据源状态需要可解释，例如 `Public Video API placeholder`、`Mock`、`FastAPI`。

### 7.3 新闻列表与详情

- 新闻列表展示标题、摘要、来源、发布时间，标题层级高于摘要。
- 新闻详情优先展示 AI 总结和关键点，避免展示未授权全文转载。
- 原文链接可以是外部跳转、链接文本或模拟跳转反馈；第一版不要求内置 WebView。
- 新闻详情返回后，用户仍应处在新闻体验上下文。

### 7.4 收银台、模拟支付与结果

- 收银台展示商户、订单号、金额和收银台 Scheme。
- 支付方式至少包含模拟支付宝、模拟微信支付、模拟银联。
- 当前 Android 实现支持支付方式选中态，并通过统一主按钮进入对应 Mock 支付页。
- Mock 支付页展示渠道、金额、支付 Scheme，并提供成功、失败、取消三种结果动作。
- 支付结果页展示状态、订单/金额等关键信息和下一步入口；目标态可继续补齐支付方式、服务端回查刷新等信息。

### 7.5 NFC Demo 与解析

- NFC Demo 从我的页进入。
- 三类场景必须存在：线下商户收款、门店桌牌点餐、小程序跳转。
- 每个场景展示标题、业务说明和写入/触发的 Scheme。
- 解析页展示原始 Scheme，并说明业务动作和下一步。
- 线下收款场景可进入收银台；桌牌点餐和小程序跳转第一版允许以占位动作说明验收。
- 无法识别或缺少字段的 Scheme，目标态应展示错误状态，不应崩溃。

### 7.6 我的

- 展示 `闪阅 FlowCast`、内部 Demo 定位和能力概览。
- 提供 NFC Demo 主入口。
- 可放置未来入口占位，但不应喧宾夺主。

## 8. Core Interactions

- 底部导航切换必须保持轻量，不需要登录。
- 新闻卡片点击进入详情，详情页必须提供返回入口。
- 支付方式点击后应进入 Mock 支付页；若实现选中态，则主支付按钮使用当前选中的支付方式。
- 模拟支付页的成功、失败、取消必须能回到对应结果页。
- NFC 场景点击后应能看到原始 Scheme 和解析后的业务字段。
- 小程序跳转场景第一版可展示模拟跳转页，不要求真实打开微信或支付宝。
- 桌牌点餐场景第一版可展示模拟点餐页或占位动作说明，不要求真实菜单/购物车。
- 所有二级页面必须有返回或下一步动作，不能让评审者卡在死路由。

## 9. Data and API Expectations

- Android 第一版可先使用本地 Mock，后续接入 FastAPI Mock。
- FastAPI Mock 应保持现有接口方向：视频 Feed、新闻列表/详情、订单、模拟支付、NFC 场景。
- 视频源第一版允许使用公开视频 API 的模拟结果，不要求真实 Pexels/Pixabay API Key。
- 新闻总结第一版允许使用预置总结文本，不要求真实 AI 总结服务。
- 支付状态以 Mock 服务或本地状态为准，不接真实支付渠道。
- 后端本地开发 Base URL 为 `http://127.0.0.1:8000`。
- Android 可在后端不可用时继续使用 `DemoRepository` 本地数据。
- 接口契约以 `docs/api-contracts.md` 为准，至少覆盖：
  - `GET /videos/feed`
  - `GET /news`
  - `GET /news/{id}`
  - `POST /orders`
  - `GET /orders/{id}`
  - `GET /payments/methods`
  - `POST /payments/mock`
  - `GET /payments/status/{order_id}`
  - `GET /nfc/scenarios`

## 10. State Model

### 10.1 Payment Status

```text
pending -> success
pending -> failed
pending -> canceled
```

展示口径：

- `pending`：订单创建后或等待 Mock 支付动作。
- `success`：支付成功，给出积极反馈和返回收银台/继续浏览入口。
- `failed`：支付失败，允许返回收银台或重新支付。
- `canceled`：用户取消，状态与失败区分展示。

### 10.2 NFC Scenario Types

```text
payment -> open_cashier
order -> open_table_order
miniapp -> launch_miniapp
unsupported -> error state
```

字段口径：

- 收款：`merchantId`、`amount`、`scene`。
- 点餐：`storeId`、`tableId`、`scene`。
- 小程序：`platform`、`appId`、`path`。

## 11. Visual Direction

- 整体品牌：中文内容消费产品，不是后端接口样例或设置页集合。
- 主色：深墨绿/青绿作为品牌与动作色。
- 强调色：暖金用于金额、高价值信息或支付重点。
- 视频页：深色沉浸式，具备短视频消费感。
- 新闻/支付/我的/NFC：浅色、清爽、卡片化但克制，便于扫读和演示。
- 组件：卡片圆角约 8dp，主按钮清晰，技术 Scheme 用独立信息块展示。
- 文案：中文为主，可保留必要技术字段、接口名和 Scheme。

## 12. Quality Bar

- 页面不应出现无法解释的英文占位、空白大块或纯技术列表。
- 核心路径不得崩溃：视频、新闻详情、支付成功、支付失败/取消、NFC 收款。
- Mock 数据字段和 UI 展示要能相互对应。
- Scheme 不要求真实拉起外部 App，但必须能让评审者理解“从哪里来、解析成什么、下一步去哪”。
- backend 与 Android 的字段允许存在轻微命名差异，但文档必须说明目标契约。
- 后端不可用不阻断 Android 页面验收。

## 13. Demo Acceptance

内部 Demo 通过标准：

- Android App 能打开并完成四个一级 Tab 的浏览。
- 视频、新闻、支付、NFC 四条演示链路都能点击闭环。
- 页面风格与已确定的“内容消费感 + 一体化平台”终稿设计口径一致。
- 后端不可用时，核心页面仍能展示 Mock 内容。
- 评审者无需阅读代码即可理解每个模块的业务目的。
- `docs/ui-acceptance-checklist.md` 中 P0 项全部通过；P1 项无阻断演示的问题。

推荐演示脚本：

```text
打开 App -> 视频 Feed
  -> 新闻 Tab -> 新闻详情 -> 返回
  -> 收银台 -> 选择模拟微信支付 -> 支付成功 -> 返回收银台
  -> 我的 -> NFC Demo -> 线下商户收款 -> Scheme 解析 -> 进入收银台
  -> NFC Demo -> 桌牌点餐/小程序跳转 -> 查看占位动作说明
```
