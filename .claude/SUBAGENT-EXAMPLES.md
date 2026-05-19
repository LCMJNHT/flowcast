# Subagent 使用示例

本文档展示如何使用 `Agent` 工具创建 Subagent 并行执行任务。

---

## 示例 1：并行需求开发

**场景**: 根据 PRD 同时实现 Android 和后端代码

```bash
# 启动并行 Subagent
# Android Agent 和 Backend Agent 同时工作
```

### Lead Agent 执行流程

```
1. Lead Agent 接收任务："实现视频 Feed 空态处理"
   ↓
2. 分解任务:
   - Product Agent → 确认空态文案规范
   - Android Agent → 实现 EmptyState 组件
   - Backend Agent → 无需参与 (纯前端功能)
   - CR Agent → 审查代码质量
   ↓
3. 并行执行:
   [Product Agent] ─┐
   [Android Agent] ─┼→ 同时执行
   [CR Agent]     ─┘
   ↓
4. 汇总结果 → 输出完整报告
```

### 实际调用 (Claude Code 中)

```bash
# 启动 Product Subagent
Agent (subagent_type=Explore):
  prompt: |
    分析 docs/product-requirements-v2.md 中空态处理需求
    输出:
    1. 空态文案规范
    2. 空态展示时机
    3. 交互要求

# 启动 Android Subagent (并行)
Agent (subagent_type=Explore):
  prompt: |
    根据空态需求，实现 EmptyState 组件
    位置：android/app/src/main/java/com/flowcast/demo/ui/FlowComponents.kt
    要求:
    - 支持自定义 message
    - 支持可选的重试按钮
    - 符合 FlowCast 设计规范

# 启动 CR Subagent (并行)
Agent (subagent_type=Explore):
  prompt: |
    审查新实现的 EmptyState 组件:
    1. 代码规范检查
    2. 可访问性检查
    3. 性能检查
```

---

## 示例 2：Bug 修复流程

**场景**: 支付页面崩溃问题修复

### Lead Agent 工作流

```yaml
任务: 修复支付页面崩溃
分解:
  - Android Agent: 定位崩溃日志，修复 NullPointerException
  - Backend Agent: 检查支付接口返回数据
  - Test Agent: 验证支付流程
执行模式: 并行
```

### 实际调用

```bash
# 1. 启动 Android Subagent
Agent (subagent_type=Explore):
  prompt: |
    分析支付页面崩溃原因:
    1. 查看 MainActivity.kt PaymentResultScreen 相关代码
    2. 定位可能的空指针位置
    3. 提出修复方案

# 2. 启动 Backend Subagent (并行)
Agent (subagent_type=Explore):
  prompt: |
    检查支付接口 /payments/mock:
    1. 是否存在数据格式问题
    2. 错误响应是否规范
    3. 提出改进建议

# 3. 启动 Test Subagent (并行)
Agent (subagent_type=Explore):
  prompt: |
    执行支付流程测试:
    1. 支付成功流程
    2. 支付失败流程
    3. 支付取消流程
    输出测试报告
```

---

## 示例 3：功能验收流程

**场景**: v2.0 版本发布前验收

### Lead Agent 协调

```
用户指令："执行 v2.0 功能验收"

Lead Agent 执行:
1. 创建 Test Subagent → 执行 P0 验收检查
2. 创建 CR Subagent → 代码审查报告确认
3. 创建 Android Subagent → 修复验收发现的问题
4. 创建 Backend Subagent → API 契约验证

汇总输出:
- 验收通过率
- 阻塞问题列表
- 发布建议
```

---

## Subagent 输出模板

每个 Subagent 完成后的输出格式:

```markdown
## [Agent 名称] 执行报告

### 任务状态
- 状态：✅ 完成 / ❌ 失败 / ⏳ 进行中
- 耗时：X 分钟

### 执行内容
1. 具体工作项 1
2. 具体工作项 2

### 输出物
- 文件路径 1
- 文件路径 2

### 发现的问题
- 问题 1 (优先级)
- 问题 2 (优先级)

### 下一步建议
1. 建议 1
2. 建议 2
```

---

## 并行 vs 串行

| 场景 | 推荐模式 | 原因 |
|------|----------|------|
| 需求开发 | 并行 | 产品、研发可同时工作 |
| 代码审查 | 并行 | 静态分析、编译检查独立 |
| Bug 修复 | 并行 | 多 Agent 定位不同原因 |
| 功能验收 | 串行 | 依赖前序任务结果 |

---

## 最佳实践

### 1. 任务分解

```yaml
# ✅ 好的分解
任务: "实现 NFC 支付功能"
分解:
  - Android Agent: NFC 扫描 UI
  - Backend Agent: NFC 场景 API
  - Product Agent: NFC 业务流程

# ❌ 不好的分解
任务: "实现 NFC 支付功能"
分解:
  - Android Agent: 完成所有工作  # 任务过大
```

### 2. 依赖管理

```yaml
# ✅ 明确依赖
任务流:
  1. Product Agent → 输出 PRD
  2. Android Agent + Backend Agent → 并行实现 (依赖 PRD)
  3. CR Agent → 审查 (依赖代码实现)
  4. Test Agent → 验收 (依赖审查通过)

# ❌ 忽略依赖
任务流:
  1. Android Agent → 实现  # 没有 PRD 参考
  2. Test Agent → 测试   # 代码未完成
```

### 3. 超时处理

```yaml
超时策略:
  - Subagent 超时时间：10 分钟
  - 重试次数：2 次
  - 超时后：切换到简化模式或人工介入
```

---

*文档版本：v1.0*  
*维护者：Lead Agent*