#!/bin/bash
set -e

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}MoBoxFrpNode Docker 启动脚本${NC}"
echo -e "${GREEN}========================================${NC}"

# 自动检测网络接口
detect_network_interface() {
    if [ "$MOBOX_NETWORK" = "auto" ]; then
        echo -e "${YELLOW}正在自动检测网络接口...${NC}"
        
        # 获取默认路由的网络接口
        DEFAULT_INTERFACE=$(ip route | grep default | awk '{print $5}' | head -n 1)
        
        if [ -z "$DEFAULT_INTERFACE" ]; then
            # 如果没有默认路由，尝试获取第一个非 lo 接口
            DEFAULT_INTERFACE=$(ip link show | grep -E "^[0-9]+:" | grep -v "lo:" | head -n 1 | awk -F': ' '{print $2}')
        fi
        
        if [ -z "$DEFAULT_INTERFACE" ]; then
            echo -e "${YELLOW}警告: 无法自动检测网络接口，使用 eth0${NC}"
            MOBOX_NETWORK="eth0"
        else
            MOBOX_NETWORK="$DEFAULT_INTERFACE"
            echo -e "${GREEN}检测到网络接口: $MOBOX_NETWORK${NC}"
        fi
    else
        echo -e "${GREEN}使用指定的网络接口: $MOBOX_NETWORK${NC}"
    fi
}

# 初始化 tc (traffic control)
init_tc() {
    echo -e "${YELLOW}正在初始化流量控制 (tc)...${NC}"
    
    # 检查网络接口是否存在
    if ! ip link show "$MOBOX_NETWORK" &> /dev/null; then
        echo -e "${YELLOW}警告: 网络接口 $MOBOX_NETWORK 不存在！${NC}"
        echo -e "${YELLOW}可用的网络接口:${NC}"
        ip link show
        exit 1
    fi
    
    # 清理可能存在的旧规则（忽略错误）
    tc qdisc del dev "$MOBOX_NETWORK" root 2>/dev/null || true
    
    # 初始化 HTB qdisc
    tc qdisc add dev "$MOBOX_NETWORK" root handle 1: htb default 1
    tc class add dev "$MOBOX_NETWORK" parent 1: classid 1:1 htb rate 10000mbit
    
    echo -e "${GREEN}流量控制初始化完成${NC}"
}

# 生成配置文件
generate_config() {
    echo -e "${YELLOW}正在生成配置文件...${NC}"
    
    cat > /opt/mossfrp/MoBoxFrp/config.yml <<EOF
# MoBoxFrp 节点配置文件
# Docker 自动生成

# 主控地址（结尾不要有多余斜杠）
address: "${MOBOX_ADDRESS}"

# 节点编号及密码
nodeID: "${MOBOX_NODE_ID}"
nodeAuth: "${MOBOX_NODE_AUTH}"

# 系统类型
# Windows or Linux
systemType: "${MOBOX_SYSTEM_TYPE}"
# 网络接口名称
network: "${MOBOX_NETWORK}"

#----------------------------------
# 是否显示调试信息
debug: ${MOBOX_DEBUG}
EOF

    echo -e "${GREEN}配置文件已生成${NC}"
    echo -e "${YELLOW}配置内容:${NC}"
    cat /opt/mossfrp/MoBoxFrp/config.yml
    echo ""
}

# 显示环境信息
show_environment() {
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}环境配置信息${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo -e "主控地址: ${YELLOW}$MOBOX_ADDRESS${NC}"
    echo -e "节点编号: ${YELLOW}$MOBOX_NODE_ID${NC}"
    echo -e "节点密码: ${YELLOW}$MOBOX_NODE_AUTH${NC}"
    echo -e "系统类型: ${YELLOW}$MOBOX_SYSTEM_TYPE${NC}"
    echo -e "网络接口: ${YELLOW}$MOBOX_NETWORK${NC}"
    echo -e "调试模式: ${YELLOW}$MOBOX_DEBUG${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
}

# 检查权限
check_permissions() {
    echo -e "${YELLOW}正在检查容器权限...${NC}"
    
    if ! tc qdisc show &> /dev/null; then
        echo -e "${YELLOW}警告: tc 命令可能无法正常工作${NC}"
        echo -e "${YELLOW}请确保容器以 --privileged 或 --cap-add=NET_ADMIN 运行${NC}"
    else
        echo -e "${GREEN}权限检查通过${NC}"
    fi
}

# 主流程
main() {
    # 检查权限
    check_permissions
    
    # 检测网络接口
    detect_network_interface
    
    # 显示环境信息
    show_environment
    
    # 初始化 tc
    if [ "$MOBOX_SYSTEM_TYPE" = "Linux" ]; then
        init_tc
    fi
    
    # 生成配置文件
    generate_config
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}启动 MoBoxFrpNode...${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    
    # 执行传入的命令
    exec "$@"
}

# 执行主流程
main
