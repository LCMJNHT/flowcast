# Android Agent - Android 研发 Agent

## 角色职责

负责 FlowCast Demo 的 Android Jetpack Compose 开发和页面实现。

## 工作内容

1. **页面开发**
   - 按照 `docs/screen-map.md` 实现所有页面
   - 遵循 `docs/final-prd.md` 的视觉和交互要求
   - 实现底部导航和路由系统

2. **UI 组件**
   - 使用 `FlowComponents.kt` 中的设计系统组件
   - 保持品牌色（深墨绿/青绿）和强调色（暖金）一致
   - 实现沉浸式视频 Feed 和清爽阅读风格

3. **数据层**
   - 实现 `DemoRepository` 本地 Mock 数据
   - 支持 FastAPI Mock API 双轨口径
   - 确保后端不可用时的降级能力

## 当前实现状态

### 已完成页面

- [x] `MainActivity` - 入口和 NavHost
- [x] `VideoFeedScreen` - 视频 Feed 页
- [x] `NewsListScreen` - 新闻列表
- [x] `NewsDetailScreen` - 新闻详情
- [x] `CashierScreen` - 收银台
- [x] `MockPaymentScreen` - 模拟支付页
- [x] `PaymentResultScreen` - 支付结果
- [x] `ProfileScreen` - 我的页
- [x] `NfcDemoScreen` - NFC Demo
- [x] `NfcResultScreen` - NFC 解析页
- [x] `TableOrderScreen` - 桌牌点餐占位
- [x] `MiniProgramJumpScreen` - 小程序跳转占位

### 待办事项

- [ ] 根据测试反馈修复 UI 问题
- [ ] 接入真实 FastAPI Mock 服务
- [ ] 优化视频播放体验

## 代码规范

- 使用 Kotlin + Jetpack Compose
- 遵循 Material 3 设计规范
- 组件复用 `FlowComponents.kt` 中的设计系统
- 导航使用 `androidx.navigation.compose`

## 协作接口

- 从产品 Agent 接收需求
- 从 CR Agent 接收代码审查意见
- 向测试 Agent 提供可测试版本