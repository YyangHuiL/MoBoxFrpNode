#!/bin/bash

# MoBoxFrpNode Docker 快速部署脚本
# 使用方法: ./deploy.sh

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker 是否安装
check_docker() {
    print_info "检查 Docker 环境..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    
    print_success "Docker 环境检查通过"
}

# 创建必要的目录
create_directories() {
    print_info "创建数据目录..."
    mkdir -p data/logs
    mkdir -p data/dependency
    print_success "数据目录创建完成"
}

# 配置向导
configure() {
    print_info "开始配置向导..."
    echo ""
    
    # 读取主控地址
    read -p "请输入主控地址 (例如: http://192.168.1.100:2026): " MASTER_ADDRESS
    if [ -z "$MASTER_ADDRESS" ]; then
        print_error "主控地址不能为空"
        exit 1
    fi
    
    # 读取节点编号
    read -p "请输入节点编号 (例如: node1): " NODE_ID
    if [ -z "$NODE_ID" ]; then
        print_error "节点编号不能为空"
        exit 1
    fi
    
    # 读取节点密码
    read -sp "请输入节点密码: " NODE_AUTH
    echo ""
    if [ -z "$NODE_AUTH" ]; then
        print_error "节点密码不能为空"
        exit 1
    fi
    
    # 读取网络接口
    read -p "请输入网络接口名称 (留空自动检测): " NETWORK_INTERFACE
    if [ -z "$NETWORK_INTERFACE" ]; then
        NETWORK_INTERFACE="auto"
    fi
    
    # 读取调试模式
    read -p "是否启用调试模式? (y/N): " DEBUG_MODE
    if [[ "$DEBUG_MODE" =~ ^[Yy]$ ]]; then
        DEBUG_MODE="true"
    else
        DEBUG_MODE="false"
    fi
    
    # 生成 .env 文件
    print_info "生成配置文件..."
    cat > .env <<EOF
# MoBoxFrpNode 配置文件
# 自动生成于 $(date)

# 主控地址
MOBOX_ADDRESS=${MASTER_ADDRESS}

# 节点编号
MOBOX_NODE_ID=${NODE_ID}

# 节点密码
MOBOX_NODE_AUTH=${NODE_AUTH}

# 系统类型
MOBOX_SYSTEM_TYPE=Linux

# 网络接口
MOBOX_NETWORK=${NETWORK_INTERFACE}

# 调试模式
MOBOX_DEBUG=${DEBUG_MODE}
EOF
    
    print_success "配置文件已生成: .env"
    echo ""
}

# 构建镜像或导入镜像
build_image() {
    if [ -f "moboxfrp-node.tar" ]; then
        print_info "检测到离线镜像文件，开始导入..."
        docker load -i moboxfrp-node.tar
        print_success "镜像导入完成"
    else
        print_info "未检测到离线镜像，开始构建 Docker 镜像..."
        # 检查是有 build 上下文 (Dockerfile)
        if [ -f "Dockerfile" ]; then
            docker-compose build
            print_success "镜像构建完成"
        else
            print_warning "未找到 Dockerfile 且未找到离线镜像，尝试直接启动(可能拉取远程镜像)..."
        fi
    fi
}

# 启动服务
start_service() {
    print_info "启动 MoBoxFrpNode 服务..."
    docker-compose up -d
    print_success "服务启动成功"
}

# 显示状态
show_status() {
    echo ""
    print_info "服务状态:"
    docker-compose ps
    echo ""
    print_info "查看日志命令:"
    echo "  docker-compose logs -f"
    echo ""
    print_info "停止服务命令:"
    echo "  docker-compose down"
    echo ""
}

# 主函数
main() {
    echo -e "${GREEN}"
    echo "=========================================="
    echo "  MoBoxFrpNode Docker 快速部署脚本"
    echo "=========================================="
    echo -e "${NC}"
    
    # 检查 Docker
    check_docker
    
    # 创建目录
    create_directories
    
    # 配置向导
    if [ ! -f .env ]; then
        configure
    else
        print_warning "检测到已存在 .env 文件"
        read -p "是否重新配置? (y/N): " RECONFIG
        if [[ "$RECONFIG" =~ ^[Yy]$ ]]; then
            configure
        else
            print_info "使用现有配置"
        fi
    fi
    
    # 构建镜像
    build_image
    
    # 启动服务
    start_service
    
    # 显示状态
    show_status
    
    print_success "部署完成！"
}

# 运行主函数
main
