# FlowCast 任务编排器

这是 FlowCast 多 Agent 系统的核心编排逻辑，展示如何使用 `Agent` 工具创建 Subagent。

---

## 编排器使用方式

```bash
# 用法
./orchestrator.sh <任务类型> [参数]

# 示例
./orchestrator.sh develop "实现 NFC 支付功能"
./orchestrator.sh review "审查最新提交"
./orchestrator.sh test "执行 P0 验收"
```

---

## 任务类型定义

### 1. 需求开发任务

```yaml
任务类型：develop
输入：需求描述
分解:
  - Product Agent: 分析需求，输出 PRD 更新
  - Android Agent: 实现 Android 代码
  - Backend Agent: 实现后端 API
  - CR Agent: 代码审查
  - Test Agent: 功能验收
输出:
  - 更新的 PRD 文档
  - 代码实现
  - 审查报告
  - 测试报告
```

### 2. Bug 修复任务

```yaml
任务类型：fix
输入：Bug 描述
分解:
  - Android Agent / Backend Agent: 定位问题
  - CR Agent: 审查修复方案
  - Test Agent: 回归测试
输出:
  - Bug 修复
  - 测试通过报告
```

### 3. 代码审查任务

```yaml
任务类型：review
输入：审查范围
分解:
  - CR Agent: 代码审查
  - Test Agent: 回归测试
输出:
  - 审查报告
  - 修复建议
```

---

## 实际调用示例 (Claude Code)

### 示例 1：需求开发

```
用户：实现视频 Feed 的空态处理

Lead Agent 执行:
1. Agent (subagent_type=Explore, prompt="分析 PRD 中空态处理需求") → Product Agent
2. Agent (subagent_type=Explore, prompt="实现 EmptyState 组件") → Android Agent
3. Agent (subagent_type=Explore, prompt="审查 EmptyState 代码") → CR Agent
4. Agent (subagent_type=Explore, prompt="验证空态展示") → Test Agent
```

### 示例 2：Bug 修复

```
用户：支付页面崩溃了

Lead Agent 执行:
1. Agent (subagent_type=Explore, prompt="分析支付页面崩溃原因") → Android Agent
2. Agent (subagent_type=Explore, prompt="检查支付接口数据") → Backend Agent
3. Agent (subagent_type=Explore, prompt="审查修复代码") → CR Agent
```

---

## Subagent 输出模板

每个 Subagent 完成后应输出：

```markdown
## [Agent 名称] 执行报告

### 任务状态
- 状态：✅ 完成
- 耗时：2 分 30 秒

### 执行内容
1. 具体工作项
2. 具体工作项

### 输出物
- 文件路径

### 阻塞问题
- 无 (或具体问题)

### 下一步建议
1. 建议 1
```

---

## 编排逻辑

```python
def orchestrate(task_type, task_description):
    """编排多 Agent 执行"""

    if task_type == "develop":
        # 1. 产品分析
        product_result = create_subagent("product", task_description)

        # 2. 并行开发 (依赖产品输出)
        android_result = create_subagent("android", product_result.spec)
        backend_result = create_subagent("backend", product_result.api_spec)

        # 3. 代码审查
        cr_result = create_subagent("cr", "审查新代码")

        # 4. 功能测试
        test_result = create_subagent("test", "P0 验收检查")

        return summarize([
            product_result,
            android_result,
            backend_result,
            cr_result,
            test_result,
        ])

    elif task_type == "fix":
        # Bug 修复流程
        # ...
        pass
```

---

## 并行策略

| 任务类型 | 并行组 | 说明 |
|----------|--------|------|
| 需求开发 | Product → (Android + Backend) → CR → Test | 4 个阶段 |
| Bug 修复 | (Android + Backend) → CR → Test | 3 个阶段 |
| 代码审查 | CR → Test | 2 个阶段 |
| 功能验收 | Test | 1 个阶段 |

---

## 错误处理

```yaml
错误类型:
  Subagent 超时:
    策略：重试 2 次，每次超时 10 分钟
    降级：切换到简化模式

  Subagent 输出不完整:
    策略：要求补充输出
    降级：人工介入

  依赖任务失败:
    策略：重新分配任务
    降级：跳过依赖，继续执行
```

---

## 监控指标

```yaml
执行指标:
  - 总任务数
  - 完成任务数
  - 失败任务数
  - 平均耗时
  - 成功率

质量指标:
  - 代码审查通过率
  - 测试通过率
  - Bug 数量
```

---

*文档版本：v1.0*  
*维护者：Lead Agent*