# FlowCast 多 Agent 协作系统

> 一个基于 Claude Code 的多 Agent 并行执行框架，用于自动化需求开发、Bug 修复和功能验收。

---

## 系统概述

```
┌─────────────────────────────────────────────────────────────┐
│                    Lead Agent (主协调器)                      │
│  - 接收用户指令                                              │
│  - 任务分解与分配                                            │
│  - Subagent 创建与协调                                       │
│  - 结果汇总与决策                                            │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│  Product      │   │  Research     │   │  Test         │
│  Subagent     │   │  Subagent     │   │  Subagent     │
│  (需求分析)   │   │  (代码实现)   │   │  (功能验收)   │
└───────────────┘   └───────┬───────┘   └───────────────┘
                            │
                    ┌───────┴───────┐
                    │               │
            ┌───────────────┐ ┌───────────────┐
            │   Android     │ │   Backend     │
            │   Subagent    │ │   Subagent    │
            └───────────────┘ └───────────────┘
```

---

## Agent 角色

### Lead Agent（主协调器）

**职责**:
- 接收用户指令，分解为子任务
- 使用 `Agent` 工具创建 Subagent
- 协调 Subagent 之间的依赖关系
- 汇总所有 Subagent 的输出

**工具**:
- `Agent` - 创建 Subagent
- `TaskCreate` / `TaskUpdate` - 任务管理
- `Write` / `Read` - 文档读写

### Subagent 类型

| Agent | 专长 | 工具集 | 输出 |
|-------|------|--------|------|
| Product Agent | 需求分析 | Glob, Read, Write | PRD, 验收标准 |
| Android Agent | Compose 开发 | Glob, Read, Edit | Kotlin 代码 |
| Backend Agent | FastAPI 开发 | Glob, Read, Edit | Python 代码 |
| CR Agent | 代码审查 | Glob, Read, Grep | 审查报告 |
| Test Agent | 功能验收 | Read, Bash | 测试报告 |

---

## 使用方式

### 1. 需求开发

```bash
# 用户指令
"实现视频播放功能"

# Lead Agent 执行流程
1. 创建 Product Subagent → 分析需求，输出 PRD 更新
2. 创建 Android Subagent → 实现 VideoPlayerControls 组件
3. 创建 Backend Subagent → 实现视频播放 API
4. 创建 CR Subagent → 审查代码质量
5. 创建 Test Subagent → 执行功能验收
6. 汇总所有结果 → 输出完整报告
```

### 2. Bug 修复

```bash
# 用户指令
"支付页面崩溃了"

# Lead Agent 执行流程
1. 创建 Android Subagent → 分析崩溃日志
2. 创建 Backend Subagent → 检查支付接口
3. 根据诊断结果创建修复 Subagent
4. 创建 CR Subagent → 审查修复代码
5. 创建 Test Subagent → 回归测试
```

### 3. 代码审查

```bash
# 用户指令
"审查最新提交的代码"

# Lead Agent 执行流程
1. 创建 CR Subagent → 执行代码审查
2. 创建 Test Subagent → 执行编译检查
3. 汇总审查结果 → 输出审查报告
```

---

## Subagent 创建示例

### 创建 Product Subagent

```python
Agent(
    subagent_type="Explore",
    prompt="""
    分析 FlowCast 产品需求:
    1. 阅读 docs/product-requirements-v2.md
    2. 识别需要更新的功能点
    3. 输出 PRD 更新建议

    输出格式:
    - 功能描述
    - 验收标准
    - 影响范围
    """
)
```

### 创建 Android Subagent

```python
Agent(
    subagent_type="Explore",
    prompt="""
    实现 Android 空态处理:
    1. 阅读 docs/product-requirements-v2.md 空态章节
    2. 实现 EmptyState 组件
    3. 更新相关页面

    输出:
    - 修改的文件列表
    - 新增的组件
    - 依赖关系
    """
)
```

### 创建 CR Subagent

```python
Agent(
    subagent_type="Explore",
    prompt="""
    审查代码质量:
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

---

## 任务依赖管理

### 依赖图

```yaml
需求开发:
  阶段 1: Product Agent (需求分析)
  阶段 2: Android Agent + Backend Agent (并行开发)
  阶段 3: CR Agent (代码审查)
  阶段 4: Test Agent (功能验收)

Bug 修复:
  阶段 1: Android Agent + Backend Agent (并行诊断)
  阶段 2: 责任 Agent (修复实施)
  阶段 3: CR Agent (审查)
  阶段 4: Test Agent (回归测试)

代码审查:
  阶段 1: CR Agent (静态分析)
  阶段 2: Test Agent (编译检查)
```

### 并行策略

| 场景 | 并行组 | 说明 |
|------|--------|------|
| 需求开发 | Product → (Android + Backend) → CR → Test | 4 个阶段 |
| Bug 修复 | (Android + Backend) → CR → Test | 3 个阶段 |
| 代码审查 | CR + Test | 2 个阶段并行 |

---

## Subagent 输出模板

```markdown
## [Agent 名称] 执行报告

### 任务状态
- 状态：✅ 完成 / ❌ 失败 / ⏳ 进行中
- 耗时：X 分钟

### 执行内容
1. 具体工作项
2. 具体工作项

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

## Lead Agent 汇总模板

```markdown
## 任务汇总报告

### 各 Agent 状态
| Agent | 状态 | 输出物 |
|-------|------|--------|
| Product | ✅ | PRD v2 |
| Android | ✅ | 代码实现 |
| Backend | ✅ | API 实现 |
| CR | ✅ | 审查报告 |
| Test | ✅ | 测试报告 |

### 执行统计
- 总任务数：5
- 完成：5
- 失败：0
- 成功率：100%
- 总耗时：8 分 30 秒

### 下一步行动
1. 合并代码到主分支
2. 准备发布
```

---

## 错误处理

### 超时处理

```yaml
超时配置:
  默认超时：10 分钟
  重试次数：2
  降级策略:
    - 切换到简化模式
    - 人工介入
    - 跳过非关键任务
```

### 依赖失败处理

```yaml
场景：Product Agent 失败
处理:
  1. 重试 1/2
  2. 重试 2/2
  3. 降级：使用简化版 PRD
  4. 人工介入
```

---

## 监控指标

### 执行指标

| 指标 | 说明 | 目标值 |
|------|------|--------|
| 总任务数 | 创建的 Subagent 数量 | - |
| 完成任务数 | 成功完成的 Subagent 数量 | - |
| 失败任务数 | 失败的 Subagent 数量 | < 5% |
| 成功率 | 完成数 / 总数 | > 95% |
| 平均耗时 | Subagent 平均执行时间 | < 5 分钟 |

### 质量指标

| 指标 | 说明 | 目标值 |
|------|------|--------|
| 代码审查通过率 | 首次审查通过的代码比例 | > 90% |
| 测试通过率 | P0 测试通过比例 | 100% |
| Bug 数量 | 每版本 Bug 数量 | < 5 |

---

## 最佳实践

### 1. 任务分解原则

✅ **好的分解**
- 每个 Subagent 任务明确单一
- 依赖关系清晰
- 输出可验证

❌ **不好的分解**
- 任务过大（超过 3 个输出物）
- 依赖关系模糊
- 无法验证完成状态

### 2. 并行策略

| 场景 | 推荐模式 | 原因 |
|------|----------|------|
| 需求开发 | 部分并行 | Product → (Android + Backend) → CR → Test |
| Bug 修复 | 并行诊断 | Android + Backend 同时定位 |
| 代码审查 | 并行检查 | 静态分析 + 编译检查独立 |
| 功能验收 | 串行 | 依赖前序任务结果 |

### 3. 超时处理

```yaml
超时配置:
  默认超时：10 分钟
  重试次数：2
  降级策略:
    - 切换到简化模式
    - 人工介入
    - 跳过非关键任务
```

---

## 附录：文件结构

```
.claude/
├── AGENT-SYSTEM.md           # 系统架构
├── ORCHESTRATOR.md           # 编排器文档
├── SUBAGENT-EXAMPLES.md      # 使用示例
├── DEMO-EXECUTION.md         # 执行演示
├── README.md                 # Agent 团队说明
├── lead-agent.py             # Lead Agent 协调器
├── agents/                   # Agent 角色定义
│   ├── product-agent.md
│   ├── android-agent.md
│   ├── backend-agent.md
│   ├── cr-agent.md
│   ├── test-agent.md
│   └── lead-agent.md
├── reviews/                  # 审查报告
│   └── code-review-001.md
└── scripts/
    └── run-agent.sh          # Agent 启动脚本
```

---

*系统版本：v1.0*  
*创建日期：2026-05-14*  
*维护者：Lead Agent*