# FlowCast 多 Agent 协作系统

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Lead Agent (主协调器)                    │
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
│  (需求)       │   │  (研发)       │   │  (测试)       │
└───────────────┘   └───────┬───────┘   └───────────────┘
                            │
                    ┌───────┴───────┐
                    │               │
            ┌───────────────┐ ┌───────────────┐
            │   Android     │ │   Backend     │
            │   Subagent    │ │   Subagent    │
            └───────────────┘ └───────────────┘
```

## Agent 角色定义

### Lead Agent (主协调器)

**职责**:
- 接收用户指令，分解为子任务
- 创建并管理 Subagent
- 协调 Subagent 之间的依赖关系
- 汇总结果并输出最终决策

**工具权限**:
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

## Subagent 创建规范

### 创建 Product Subagent

```
Agent 工具参数:
- subagent_type: Explore (需求分析)
- prompt: 分析需求文档，输出 PRD 更新建议
- run_in_background: false
```

### 创建 Research Subagent

```
Agent 工具参数:
- subagent_type: Explore (代码探索)
- prompt: 探索代码结构，定位需要修改的文件
- run_in_background: false
```

### 创建 Test Subagent

```
Agent 工具参数:
- subagent_type: Explore (测试验收)
- prompt: 执行 P0 验收检查，输出测试报告
- run_in_background: true
```

## 任务流转

```
用户指令
    ↓
Lead Agent 解析
    ↓
┌───────────────────────────────────┐
│ 任务分解                          │
│ 1. 产品需求分析 → Product Agent   │
│ 2. 代码实现 → Android/Backend     │
│ 3. 代码审查 → CR Agent            │
│ 4. 功能验收 → Test Agent          │
└───────────────────────────────────┘
    ↓
Subagent 并行执行
    ↓
结果汇总 → Lead Agent
    ↓
输出最终结果
```

## 依赖管理

```yaml
任务依赖图:
  需求开发:
    - 前置: Product Agent 输出 PRD
    - 并行: Android Agent + Backend Agent
    - 后置: CR Agent 审查 → Test Agent 验收

代码审查:
    - 前置: 代码实现完成
    - 并行: 静态分析 + 编译检查
    - 后置: 输出审查报告

功能验收:
    - 前置: 代码审查通过
    - 串行: P0 检查 → P1 检查
    - 后置: 验收报告
```

## 通信协议

### Subagent 输出格式

```markdown
## [Agent 名称] 执行报告

### 任务状态
- 状态：完成/进行中/失败
- 耗时：X 分钟

### 输出物
- 文件 1
- 文件 2

### 阻塞问题
- 问题 1（如有）
```

### Lead Agent 汇总格式

```markdown
## 任务汇总报告

### 各 Agent 状态
| Agent | 状态 | 输出物 |
|-------|------|--------|
| Product | ✅ | PRD v2 |
| Android | ✅ | 代码实现 |
| CR | ⏳ | 审查中 |

### 下一步行动
1. 等待 CR Agent 完成
2. 启动 Test Agent
```

## 错误处理

| 错误类型 | 处理策略 |
|----------|----------|
| Subagent 超时 | 重试或切换到简化模式 |
| Subagent 输出不完整 | 要求补充或人工介入 |
| 依赖任务失败 | 重新分配或调整计划 |

## 使用示例

### 示例 1：需求开发

```bash
# 用户指令
"根据 PRD 实现视频 Feed 空态处理"

# Lead Agent 执行
1. 创建 Product Agent → 确认空态文案
2. 创建 Android Agent → 实现 EmptyState 组件
3. 创建 CR Agent → 审查代码质量
4. 创建 Test Agent → 验证空态展示
```

### 示例 2：Bug 修复

```bash
# 用户指令
"修复支付页面崩溃问题"

# Lead Agent 执行
1. 创建 Android Agent → 定位崩溃原因
2. 创建 CR Agent → 审查修复方案
3. 创建 Test Agent → 验证支付流程
```

---

*系统版本：v1.0*  
*维护者：Lead Agent*