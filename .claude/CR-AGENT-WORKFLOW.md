# CR Agent 工作流程

## 问题说明

之前的 CR Agent 在代码审查时存在一个问题：**代码未编译就输出审查报告**。

这导致审查报告说"通过"，但实际代码有编译错误。

---

## 正确的 CR Agent 工作流程

### 1. 编译检查（必须）

```bash
# Android 编译检查
cd android
./gradlew compileDebugKotlin --no-daemon

# 后端语法检查
cd backend
python -m py_compile app/main.py
```

### 2. 代码规范检查

```bash
# Android Kotlin 代码规范
# 使用 ktlint 或 detekt
./gradlew ktlintCheck

# Backend Python 代码规范
# 使用 flake8 或 pylint
flake8 backend/app/
```

### 3. 审查输出

CR Agent 应该输出：

```markdown
## 编译检查

✅ Android: BUILD SUCCESSFUL
✅ Backend: 语法检查通过

## 代码审查

### 优点
- ...

### 问题
- P0: ...
- P1: ...
- P2: ...

### 修复建议
1. ...
```

---

## 多 Agent 系统中的 CR Agent

### 执行时序

```
1. Android Agent 完成代码实现
         ↓
2. CR Agent 触发
   ├─ 编译检查 (必须)
   ├─ 规范检查
   └─ 输出报告
         ↓
3. Test Agent 触发（依赖 CR 通过）
```

### 关键检查点

| 检查项 | 方法 | 阻断级别 |
|--------|------|----------|
| 编译通过 | `./gradlew compileDebugKotlin` | P0 |
| 后端语法 | `python -m py_compile` | P0 |
| 代码规范 | ktlint/flake8 | P1 |
| 单元测试 | `./gradlew test` | P1 |

---

## CR Agent 自动化脚本

```bash
#!/bin/bash
# .claude/scripts/cr-agent.sh

set -e

echo "=== CR Agent 开始执行 ==="

# 1. Android 编译
echo "[1/4] Android 编译检查..."
cd android
./gradlew compileDebugKotlin --no-daemon
if [ $? -ne 0 ]; then
    echo "❌ Android 编译失败"
    exit 1
fi
echo "✅ Android 编译通过"

# 2. 后端语法检查
echo "[2/4] Python 语法检查..."
cd ../backend
python -m py_compile app/main.py
if [ $? -ne 0 ]; then
    echo "❌ Python 语法检查失败"
    exit 1
fi
echo "✅ Python 语法通过"

# 3. 代码规范检查
echo "[3/4] 代码规范检查..."
# TODO: 添加 ktlint/flake8

# 4. 输出报告
echo "[4/4] 生成审查报告..."
# TODO: 输出详细报告

echo "=== CR Agent 执行完成 ==="
```

---

## 本次修复内容

### 问题 1: 缺少导入

```kotlin
// 修复前
import androidx.compose.foundation.BorderStroke

// 修复后
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable  // 新增
import androidx.compose.foundation.layout.size  // 新增
```

### 问题 2: CircularProgressIndicator 调用

```kotlin
// 修复前
androidx.compose.material3.CircularProgressIndicator(
    modifier = Modifier.size(40.dp),
    ...
)

// 修复后
CircularProgressIndicator(
    modifier = Modifier.size(40.dp),
    ...
)
```

### 问题 3: 编译验证

```bash
# 修复前
BUILD FAILED in 12s
e: Unresolved reference 'clickable'
e: Unresolved reference 'size'

# 修复后
BUILD SUCCESSFUL in 12s
```

---

## CR Agent 输出模板

```markdown
## CR Agent 审查报告

### 编译检查
- Android: ✅ 通过 / ❌ 失败
- Backend: ✅ 通过 / ❌ 失败

### 代码审查
#### Android
- 新增文件：...
- 修改文件：...
- 代码规范：...

#### Backend
- 新增文件：...
- 修改文件：...
- 代码规范：...

### 问题列表
| 优先级 | 问题 | 位置 | 建议 |
|--------|------|------|------|
| P0 | ... | ... | ... |
| P1 | ... | ... | ... |
| P2 | ... | ... | ... |

### 审查结论
✅ 通过 / ❌ 不通过

---
*CR Agent v2.1*
```

---

*文档版本：v1.0*  
*维护者：CR Agent*