#!/bin/bash
# FlowCast Multi-Agent Runner
# 用于启动和管理多 Agent 协作任务

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示使用帮助
show_help() {
    echo "FlowCast Multi-Agent Runner"
    echo ""
    echo "用法：$0 <command> [options]"
    echo ""
    echo "命令:"
    echo "  run      启动 Agent 执行任务"
    echo "  status   查看 Agent 状态"
    echo "  clean    清理临时文件"
    echo "  help     显示帮助"
    echo ""
    echo "示例:"
    echo "  $0 run --agent android --task '实现空态处理'"
    echo "  $0 run --parallel android,backend --task '需求开发'"
    echo ""
}

# 启动单个 Agent
run_single_agent() {
    local agent_type=$1
    local task=$2

    log_info "启动 $agent_type Agent..."
    log_info "任务：$task"

    # 根据 Agent 类型执行不同逻辑
    case $agent_type in
        "android")
            log_info "Android Agent 开始工作..."
            # 执行 Android 相关任务
            ;;
        "backend")
            log_info "Backend Agent 开始工作..."
            # 执行后端相关任务
            ;;
        "product")
            log_info "Product Agent 开始工作..."
            # 执行产品相关任务
            ;;
        "cr")
            log_info "CR Agent 开始工作..."
            # 执行代码审查
            ;;
        "test")
            log_info "Test Agent 开始工作..."
            # 执行测试
            ;;
        *)
            log_error "未知的 Agent 类型：$agent_type"
            exit 1
            ;;
    esac
}

# 并行启动多个 Agent
run_parallel_agents() {
    local agents=$1
    local task=$2

    IFS=',' read -ra AGENT_ARRAY <<< "$agents"

    log_info "并行启动 ${#AGENT_ARRAY[@]} 个 Agent..."
    log_info "任务：$task"

    # 记录开始时间
    start_time=$(date +%s)

    # 并行执行
    pids=()
    for agent in "${AGENT_ARRAY[@]}"; do
        run_single_agent "$agent" "$task" &
        pids+=($!)
    done

    # 等待所有任务完成
    for pid in "${pids[@]}"; do
        wait $pid
    done

    # 计算耗时
    end_time=$(date +%s)
    elapsed=$((end_time - start_time))

    log_success "所有 Agent 完成任务，耗时：${elapsed}秒"
}

# 主入口
main() {
    if [ $# -eq 0 ]; then
        show_help
        exit 0
    fi

    local command=$1
    shift

    case $command in
        "run")
            # 解析参数
            agent_type=""
            task=""
            parallel=""

            while [[ $# -gt 0 ]]; do
                case $1 in
                    --agent)
                        agent_type="$2"
                        shift 2
                        ;;
                    --parallel)
                        parallel="$2"
                        shift 2
                        ;;
                    --task)
                        task="$2"
                        shift 2
                        ;;
                    *)
                        log_error "未知参数：$1"
                        exit 1
                        ;;
                esac
            done

            if [ -n "$parallel" ]; then
                run_parallel_agents "$parallel" "$task"
            elif [ -n "$agent_type" ]; then
                run_single_agent "$agent_type" "$task"
            else
                log_error "请指定 --agent 或 --parallel 参数"
                exit 1
            fi
            ;;
        "status")
            log_info "查看 Agent 状态..."
            # TODO: 实现状态查看
            ;;
        "clean")
            log_info "清理临时文件..."
            # TODO: 实现清理逻辑
            ;;
        "help")
            show_help
            ;;
        *)
            log_error "未知命令：$command"
            show_help
            exit 1
            ;;
    esac
}

main "$@"