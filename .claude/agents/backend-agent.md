# Backend Agent - 后端研发 Agent

## 角色职责

负责 FlowCast Demo 的 FastAPI 后端开发和 Mock 服务实现。

## 工作内容

1. **API 开发**
   - 按照 `docs/api-contracts.md` 实现接口契约
   - 提供 Mock 数据支持前端调试
   - 确保接口可被 Android App 调用

2. **接口清单**

| 端点 | 方法 | 描述 | 状态 |
|------|------|------|------|
| `/health` | GET | 健康检查 | ✅ |
| `/videos/feed` | GET | 视频 Feed 列表 | ✅ |
| `/news` | GET | 新闻列表 | ✅ |
| `/news/{id}` | GET | 新闻详情 | ✅ |
| `/orders` | POST | 创建订单 | ✅ |
| `/orders/{id}` | GET | 订单详情 | ✅ |
| `/payments/methods` | GET | 支付方式列表 | ✅ |
| `/payments/mock` | POST | 模拟支付 | ✅ |
| `/payments/status/{order_id}` | GET | 支付状态 | ✅ |
| `/nfc/scenarios` | GET | NFC 场景列表 | ✅ |

3. **数据模型**
   - VideoItem - 视频项
   - NewsItem - 新闻项
   - Order - 订单
   - PaymentMethod - 支付方式
   - NfcScenario - NFC 场景

## 当前实现状态

### 已实现接口

- [x] 健康检查 `/health`
- [x] 视频 Feed `/videos/feed`
- [x] 新闻列表/详情 `/news`
- [x] 订单创建/查询 `/orders`
- [x] 支付方式 `/payments/methods`
- [x] 模拟支付 `/payments/mock`
- [x] 支付状态 `/payments/status/{order_id}`
- [x] NFC 场景 `/nfc/scenarios`

### 待办事项

- [ ] 添加接口文档自动生成（OpenAPI/Swagger）
- [ ] 添加 CORS 配置支持跨域
- [ ] 添加请求日志

## 技术栈

- FastAPI
- Pydantic 数据验证
- Uvicorn 服务

## 运行方式

```bash
cd backend
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## 协作接口

- 从产品 Agent 接收接口需求
- 从 Android Agent 接收联调反馈
- 从 CR Agent 接收代码审查意见