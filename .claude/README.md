# FlowCast 多 Agent 协作系统

## 快速开始

### 1. 系统结构

```
.claude/
├── AGENT-SYSTEM.md           # 系统架构说明
├── ORCHESTRATOR.md           # 任务编排器文档
├── SUBAGENT-EXAMPLES.md      # Subagent 使用示例
├── lead-agent.py             # Lead Agent 协调器
├── agents/                   # Agent 角色定义
│   ├── product-agent.md      # 产品 Agent
│   ├── android-agent.md      # Android 研发 Agent
│   ├── backend-agent.md      # 后端研发 Agent
│   ├── cr-agent.md           # 代码审查 Agent
│   ├── test-agent.md         # 测试 Agent
│   └── lead-agent.md         # 主 Agent
├── reviews/                  # 审查报告
│   └── code-review-001.md
└── scripts/
    └── run-agent.sh          # Agent 启动脚本
```

### 2. Agent 角色

| Agent | 职责 | 专长领域 |
|-------|------|----------|
| Product Agent | 产品需求分析 | PRD、验收标准、用户路径 |
| Android Agent | Android 开发 | Jetpack Compose、Kotlin |
| Backend Agent | 后端开发 | FastAPI、Python |
| CR Agent | 代码审查 | 代码质量、安全审查 |
| Test Agent | 功能测试 | UI 验收、冒烟测试 |
| Lead Agent | 协协调 | 任务分解、结果汇总 |

### 3. 使用方式

#### 方式 A：文档化 Agent（轻量级）

每个 Agent 是一个 Markdown 文件，人工切换上下文执行：

```bash
# 1. 查看 Agent 定义
cat .claude/agents/android-agent.md

# 2. 人工执行 Android Agent 任务
# (根据文档定义手动执行)

# 3. 输出结果到审查报告
echo "Android Agent 完成" >> .claude/reviews/execution-001.md
```

#### 方式 B：真正的多 Agent 并行（使用 Agent 工具）

使用 Claude Code 的 `Agent` 工具创建 Subagent 并行执行：

```
# Lead Agent 执行流程

1. 创建 Product Subagent
   Agent(subagent_type=Explore, prompt="分析需求...")

2. 并行创建 Android + Backend Subagent
   Agent(subagent_type=Explore, prompt="实现 Android...")
   Agent(subagent_type=Explore, prompt="实现后端...")

3. 创建 CR Subagent
   Agent(subagent_type=Explore, prompt="审查代码...")

4. 创建 Test Subagent
   Agent(subagent_type=Explore, prompt="功能验收...")

5. 汇总所有结果
```

### 4. 实际调用示例

#### 示例 1：需求开发

```bash
# 用户指令
"根据 PRD 实现视频 Feed 空态处理"

# Lead Agent 执行 (伪代码)
lead_agent = LeadAgent()

# 阶段 1：产品分析 (串行)
product_result = lead_agent.create_subagent(
    type="product",
    task="分析 PRD 中空态处理需求"
)

# 阶段 2：代码实现 (并行)
android_result = lead_agent.create_subagent(
    type="android",
    task="实现 EmptyState 组件",
    depends_on=product_result
)

cr_result = lead_agent.create_subagent(
    type="cr",
    task="审查 EmptyState 代码",
    depends_on=android_result
)

# 阶段 3：功能验收 (串行)
test_result = lead_agent.create_subagent(
    type="test",
    task="验证空态展示"
)

# 汇总输出
lead_agent.summarize()
```

#### 示例 2：Bug 修复

```bash
# 用户指令
"修复支付页面崩溃问题"

# Lead Agent 执行
lead_agent = LeadAgent()

# 并行诊断
android_diag = lead_agent.create_subagent(
    type="android",
    task="分析支付页面崩溃日志"
)

backend_diag = lead_agent.create_subagent(
    type="backend",
    task="检查支付接口数据"
)

# 等待诊断结果
wait_for(android_diag, backend_diag)

# 根据诊断结果分配修复任务
if android_diag.root_cause:
    fix_result = lead_agent.create_subagent(
        type="android",
        task=f"修复：{android_diag.root_cause}"
    )

# 审查和测试
lead_agent.create_subagent(type="cr", task="审查修复代码")
lead_agent.create_subagent(type="test", task="回归测试")
```

### 5. Subagent 输出格式

```markdown
## [Agent 名称] 执行报告

### 任务状态
- 状态：✅ 完成 / ❌ 失败 / ⏳ 进行中
- 耗时：X 分钟

### 执行内容
1. 工作项 1
2. 工作项 2

### 输出物
- 文件路径 1
- 文件路径 2

### 发现的问题
- 问题 1 (P1)
- 问题 2 (P2)

### 下一步建议
1. 建议 1
2. 建议 2
```

### 6. 依赖管理

```yaml
任务依赖图:

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

### 7. 监控指标

```yaml
执行指标:
  总任务数：5
  完成任务数：5
  失败任务数：0
  成功率：100%
  平均耗时：5 分钟

质量指标:
  代码审查通过率：95%
  测试通过率：100%
  Bug 数量：0
```

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

## 附录：Agent 工具调用格式

```python
# Python SDK (示例)
from claude import Agent

# 创建 Subagent
subagent = Agent(
    subagent_type="Explore",
    prompt="分析需求文档，输出 PRD 更新建议",
    model="claude-sonnet-4-6",
)

# 等待结果
result = subagent.run()
print(result.output)
```

```bash
# Bash 脚本调用
./orchestrator.sh develop "实现 NFC 支付功能"
```

---

*系统版本：v1.0*  
*创建日期：2026-05-14*  
*维护者：Lead Agent*