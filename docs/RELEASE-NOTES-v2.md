# FlowCast Demo v2.0 发布说明

**发布日期**: 2026-05-14  
**版本**: v2.0.0  
**类型**: 内部 Demo 增强版

---

## 新增功能

### Android 端

| 功能 | 说明 | 状态 |
|------|------|------|
| 空态处理 | 视频/新闻/NFC 模块空数据时展示友好提示 | ✅ |
| 加载状态组件 | `LoadingState` 统一加载指示器 | ✅ |
| 空态组件 | `EmptyState` 统一空数据展示 | ✅ |

### 后端

| 功能 | 说明 | 状态 |
|------|------|------|
| 全局异常处理 | 统一错误响应格式 | ✅ |
| 请求验证 | 订单金额、商户信息验证 | ✅ |
| 错误码规范 | NEWS_NOT_FOUND, ORDER_NOT_FOUND 等 | ✅ |

---

## 文档更新

| 文档 | 版本 | 说明 |
|------|------|------|
| product-requirements-v2.md | v2.0 | 详细 PRD，含异常流程 |
| ui-acceptance-checklist-v2.md | v2.0 | 100+ 验收检查项 |
| api-contracts-detail.md | v1.0 | 完整 API 契约详解 |
| CHANGELOG.md | v1.0 | 需求变更日志 |

---

## 修复问题

| 问题 | 模块 | 修复内容 |
|------|------|----------|
| 缺少空态处理 | Android | 添加 EmptyState 组件 |
| 缺少加载态 | Android | 添加 LoadingState 组件 |
| 错误处理不统一 | Backend | 全局异常处理器 |
| 缺少请求验证 | Backend | 金额/商户信息验证 |

---

## 已知问题

| ID | 问题 | 模块 | 优先级 | 计划版本 |
|----|------|------|--------|----------|
| #001 | isLoading 状态未实际使用 | Android | P1 | v2.1 |
| #002 | 分享按钮无功能 | Android | P2 | v2.1 |
| #003 | 缺少 CORS 配置 | Backend | P1 | v2.1 |
| #004 | 错误码未常量化 | Backend | P2 | v2.2 |

---

## 升级指南

### Android

```bash
# 构建 Debug 版本
cd android
./gradlew assembleDebug
```

### Backend

```bash
# 启动服务
cd backend
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

---

## 验收状态

| 检查项 | 状态 |
|--------|------|
| P0 必过项 | 待测试 |
| P1 建议项 | 待测试 |
| 代码审查 | ✅ 通过 |
| 编译检查 | 待验证 |

---

## 下一步计划

### v2.1.0（下周）

- [ ] 修复 #001 #002 #003
- [ ] 添加真实视频源接入
- [ ] 完善测试覆盖率

### v3.0.0（下月）

- [ ] 真实 AI 新闻总结服务
- [ ] 真实 NFC 标签读写
- [ ] 支付状态回查机制

---

*发布人：Lead Agent*  
*审核人：CR Agent, Test Agent*