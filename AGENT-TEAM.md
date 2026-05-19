# FlowCast Demo Agent 团队

本文档描述参与 FlowCast Demo 开发的 Agent 团队结构和协作流程。

## 团队结构

```
                    ┌─────────────────┐
                    │   Lead Agent    │
                    │   (主 Agent)    │
                    └────────┬────────┘
                             │
           ┌─────────────────┼─────────────────┐
           │                 │                 │
    ┌──────┴──────┐   ┌──────┴──────┐   ┌──────┴──────┐
    │   Product   │   │  Research   │   │    Test     │
    │   Agent     │   │    & Dev    │   │   Agent     │
    │ (产品 Agent) │   │   Agent     │   │ (测试 Agent)│
    └─────────────┘   └──────┬──────┘   └─────────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
           ┌────────┴────────┐  ┌─────┴──────┐
           │   Android Agent │  │ Backend    │
           │ (Android 开发)   │  │ Agent      │
           │                 │  │ (后端开发)  │
           └─────────────────┘  └────────────┘
                             │
                    ┌────────┴────────┐
                    │    CR Agent     │
                    │ (代码审查/编译)  │
                    └─────────────────┘
```

## Agent 角色说明

### Lead Agent - 主 Agent
**职责**: 项目整体协调、进度把控、最终审批  
**工作**:
- 协调各 Agent 之间的协作
- 审批需求变更和技术决策
- 确认验收通过

### Product Agent - 产品 Agent
**职责**: 需求分析、PRD 完善、验收标准制定  
**工作**:
- 维护 `docs/final-prd.md`
- 更新 `docs/screen-map.md`
- 制定 `docs/ui-acceptance-checklist.md`

### Android Agent - Android 研发 Agent
**职责**: Jetpack Compose 页面开发和实现  
**工作**:
- 按照 screen-map 实现所有页面
- 遵循 PRD 的视觉和交互要求
- 维护 `DemoRepository` 本地 Mock

### Backend Agent - 后端研发 Agent
**职责**: FastAPI 后端开发和 Mock 服务  
**工作**:
- 按照 api-contracts 实现接口
- 提供 Mock 数据支持
- 确保 CORS 和跨域支持

### CR Agent - 代码审查 Agent
**职责**: 代码审查、编译检查、质量把控  
**工作**:
- 审查 Kotlin/Python 代码规范
- 确保编译通过
- 检查安全性和漏洞

### Test Agent - 测试 Agent
**职责**: 功能验收、UI 测试、质量验证  
**工作**:
- 执行 P0/P1 验收检查
- 运行演示脚本
- 输出测试报告

## 协作流程

```
┌─────────────────────────────────────────────────────────────┐
│                      需求开发流程                            │
└─────────────────────────────────────────────────────────────┘

  Product Agent           Research & Dev Agent
       │                        │
       │  1. 输出需求文档         │
       │───────────────────────>│
       │                        │
       │                        │ 2. 代码实现
       │                        │──────────┐
       │                        │          │
       │                        │<─────────┘
       │                        │
       │  3. 提交审查            │
       │<───────────────────────│
       │                        │
       v                        v
  Lead Agent              CR Agent
       │                        │
       │  4. 审批               │ 5. 审查报告
       │<───────────────────────│
       │
       │  6. 批准测试
       │───────────────────────> Test Agent
       │                        │
       │                        │ 7. 验收报告
       │<───────────────────────│
       │
       │  8. 最终审批通过
       v
  [发布/演示]
```

## 各 Agent 详细文档

- [产品 Agent](.claude/agents/product-agent.md)
- [Android Agent](.claude/agents/android-agent.md)
- [后端 Agent](.claude/agents/backend-agent.md)
- [CR Agent](.claude/agents/cr-agent.md)
- [测试 Agent](.claude/agents/test-agent.md)
- [主 Agent](.claude/agents/lead-agent.md)

## 使用方式

### 调用特定 Agent

当需要特定 Agent 执行任务时，使用相应的角色上下文：

```bash
# 产品需求讨论
@product-agent 请分析这个需求变更的影响

# Android 开发
@android-agent 请实现新的 NFC 页面

# 代码审查
@cr-agent 请审查这个 PR 的代码质量

# 测试验收
@test-agent 请执行 P0 验收检查
```

### 任务分配

Lead Agent 负责任务分配和优先级排序：

1. **P0**: 核心路径开发（视频、新闻、支付、NFC）
2. **P1**: 视觉优化和体验提升
3. **P2**: 扩展功能和文档完善

## 当前项目状态

| 模块 | 状态 | 负责人 |
|------|------|--------|
| 产品需求 | ✅ 完成 | Product Agent |
| Android 开发 | ✅ 完成 | Android Agent |
| 后端开发 | ✅ 完成 | Backend Agent |
| 代码审查 | ⏳ 待执行 | CR Agent |
| 功能测试 | ⏳ 待执行 | Test Agent |
| 最终验收 | ⏳ 待审批 | Lead Agent |

## 沟通机制

- **每日站会**: 各 Agent 汇报进度和阻塞
- **需求评审**: Product Agent 主导
- **代码审查**: CR Agent 主导
- **验收会议**: Test Agent 主导，Lead Agent 审批

## 决策记录

重要决策将记录在 `.claude/decisions/` 目录中，包括：
- 技术选型
- 架构变更
- 需求调整

---

*最后更新：2026-05-14*