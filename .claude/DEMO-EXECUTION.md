# 多 Agent 并行执行演示

## 执行场景：v2.1 版本需求开发

### 用户指令
"实现 v2.1 版本：添加视频播放功能和新闻频道筛选"

---

## Lead Agent 任务分解

```
任务分解完成:
├─ Product Agent: 分析 v2.1 需求，输出 PRD 更新
├─ Android Agent: 实现视频播放 UI 和新闻频道筛选
├─ Backend Agent: 实现视频播放 API 和新闻频道接口
├─ CR Agent: 审查代码质量
└─ Test Agent: 执行功能验收
```

---

## Subagent 创建与执行

### 阶段 1：产品分析（串行）

```
[Lead Agent] 创建 Product Subagent...
[Product Agent] 开始分析需求...
[Product Agent] 输出 PRD 更新建议:
  - 视频播放功能：添加播放/暂停按钮
  - 新闻频道筛选：添加频道选择器
  - 验收标准：P0 检查项更新
```

### 阶段 2：并行开发

```
[Lead Agent] 创建 Android Subagent...
[Lead Agent] 创建 Backend Subagent...
[Lead Agent] 两个 Subagent 并行执行中...
```

#### Android Subagent 执行

```
[Android Agent] 分析代码结构...
[Android Agent] 修改 FlowComponents.kt，添加播放控制组件
[Android Agent] 修改 MainActivity.kt，添加新闻频道筛选
[Android Agent] 输出:
  - 新增 VideoPlayerControls 组件
  - 新增 NewsChannelFilter 组件
```

#### Backend Subagent 执行

```
[Backend Agent] 分析 API 结构...
[Backend Agent] 修改 main.py，添加视频播放接口
[Backend Agent] 修改 main.py，添加新闻频道筛选
[Backend Agent] 输出:
  - 新增 GET /videos/{id}/play
  - 新增 GET /news?channel=xxx
```

### 阶段 3：代码审查（并行）

```
[Lead Agent] 创建 CR Subagent...
[CR Agent] 开始代码审查...
[CR Agent] 输出审查报告:
  - Android 代码：✅ 通过 (2 个 P2 建议)
  - Backend 代码：✅ 通过 (1 个 P1 修复)
```

### 阶段 4：功能验收（串行）

```
[Lead Agent] 创建 Test Subagent...
[Test Agent] 执行 P0 验收检查...
[Test Agent] 输出测试报告:
  - 视频播放功能：✅ 通过
  - 新闻频道筛选：✅ 通过
  - 回归测试：✅ 通过
```

---

## 执行汇总

```
[Lead Agent] 执行汇总报告
========================================
总任务数：5
完成：5
失败：0
成功率：100%
总耗时：8 分 30 秒

各 Agent 状态:
| Agent | 状态 | 输出物 |
|-------|------|--------|
| Product | ✅ | PRD v2.1 |
| Android | ✅ | 代码实现 |
| Backend | ✅ | API 实现 |
| CR | ✅ | 审查报告 |
| Test | ✅ | 测试报告 |

下一步行动:
1. 合并代码到主分支
2. 准备 v2.1 发布
```

---

## 实际 Agent 工具调用

以下是使用 `Agent` 工具的实际调用格式：

### 创建 Product Subagent

```
Agent(
  subagent_type="Explore",
  prompt="""
  分析 FlowCast v2.1 需求:
  1. 阅读 docs/product-requirements-v2.md
  2. 识别视频播放和新闻频道筛选的需求点
  3. 输出 PRD 更新建议

  输出格式:
  - 功能描述
  - 验收标准
  - 影响范围
  """
)
```

### 创建 Android Subagent

```
Agent(
  subagent_type="Explore",
  prompt="""
  实现 FlowCast v2.1 Android 端:
  1. 阅读 docs/product-requirements-v2.md v2.1 章节
  2. 实现 VideoPlayerControls 组件
  3. 实现 NewsChannelFilter 组件

  输出:
  - 修改的文件列表
  - 新增的组件
  - 依赖关系
  """
)
```

### 创建 Backend Subagent

```
Agent(
  subagent_type="Explore",
  prompt="""
  实现 FlowCast v2.1 后端 API:
  1. 添加 GET /videos/{id}/play 接口
  2. 添加 GET /news?channel=xxx 接口
  3. 更新 API 契约文档

  输出:
  - 新增的接口
  - 修改的文件
  - API 契约更新
  """
)
```

### 创建 CR Subagent

```
Agent(
  subagent_type="Explore",
  prompt="""
  审查 v2.1 代码:
  1. Android 代码规范检查
  2. Backend 代码规范检查
  3. 安全性检查
  4. 性能检查

  输出审查报告:
  - P0 阻断问题
  - P1 修复建议
  - P2 优化建议
  """
)
```

### 创建 Test Subagent

```
Agent(
  subagent_type="Explore",
  prompt="""
  执行 v2.1 功能验收:
  1. 视频播放功能 P0 检查
  2. 新闻频道筛选 P0 检查
  3. 回归测试

  输出测试报告:
  - 通过项
  - 失败项
  - 修复建议
  """
)
```

---

## 并行执行时序图

```
时间轴 →

阶段 1: Product Agent
[████████] Product

阶段 2: 并行开发
          [Android████████]
          [Backend████████] (并行)

阶段 3: CR Agent
                    [████████] CR

阶段 4: Test Agent
                              [████████] Test

总耗时：约 8-10 分钟
```

---

## 错误恢复示例

### 场景：Backend Subagent 超时

```
[Lead Agent] 检测 Backend Subagent 超时 (10 分钟)...
[Lead Agent] 重试 1/2...
[Lead Agent] Backend Subagent 仍然超时...
[Lead Agent] 重试 2/2...
[Lead Agent] Backend Subagent 完成 (简化模式)

[Lead Agent] 调整后续计划:
- Android Agent: 继续
- CR Agent: 仅审查 Android 代码
- Test Agent: 仅验收视频播放功能
```

---

## 输出物汇总

执行完成后生成的文件：

```
docs/
├── prd-v2.1.md                 # Product Agent 输出
├── release-notes-v2.1.md       # 发布说明

android/
├── app/src/main/java/.../
│   ├── VideoPlayerControls.kt  # Android Agent 输出
│   └── NewsChannelFilter.kt

backend/
├── app/
│   ├── main.py                 # Backend Agent 更新
│   └── api_v2.py               # 新增 API 模块

.claude/
├── reviews/
│   ├── code-review-v2.1.md     # CR Agent 输出
│   └── test-report-v2.1.md     # Test Agent 输出
└── execution-logs/
    └── v2.1-execution.log      # 执行日志
```

---

## 关键要点

1. **任务分解**: Lead Agent 将大任务分解为可管理的 Subagent 任务
2. **并行执行**: 独立任务并行执行，减少总耗时
3. **依赖管理**: 明确任务依赖关系，确保执行顺序
4. **错误恢复**: 超时重试机制，降级策略
5. **结果汇总**: 统一的输出格式和汇总报告

---

*演示版本：v1.0*  
*创建日期：2026-05-14*