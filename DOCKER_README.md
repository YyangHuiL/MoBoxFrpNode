# MoBoxFrpNode Docker 部署指南

## 📋 简介

这是 MoBoxFrpNode 的 Docker 镜像，支持在 Linux 环境下运行，包含完整的流量控制（tc）功能。

## ✨ 特性

- ✅ 自动检测网络接口
- ✅ 支持 tc 流量控制
- ✅ 自动化配置生成
- ✅ 支持多个 FRP 实例
- ✅ 端口自动映射
- ✅ 数据持久化
- ✅ 环境变量配置

## 🚀 快速开始

### 方法一：使用 Docker Compose（推荐）

1. **修改配置**

编辑 `docker-compose.yml` 文件，修改以下环境变量：

```yaml
environment:
  - MOBOX_ADDRESS=http://your-master-server:2026  # 主控地址
  - MOBOX_NODE_ID=node1                           # 节点编号
  - MOBOX_NODE_AUTH=your_password_here            # 节点密码
```

2. **启动服务**

```bash
# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 方法二：使用 Docker 命令

1. **构建镜像**

```bash
docker build -t moboxfrp-node:latest .
```

2. **运行容器**

```bash
docker run -d \
  --name moboxfrp-node \
  --privileged \
  --network host \
  -e MOBOX_ADDRESS="http://your-master-server:2026" \
  -e MOBOX_NODE_ID="node1" \
  -e MOBOX_NODE_AUTH="your_password_here" \
  -e MOBOX_NETWORK="auto" \
  -v $(pwd)/data/logs:/opt/mossfrp/MoBoxFrp/logs \
  -v $(pwd)/data/dependency:/opt/mossfrp/MoBoxFrp/dependency \
  --restart unless-stopped \
  moboxfrp-node:latest
```

## ⚙️ 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| `MOBOX_ADDRESS` | 主控服务器地址 | `http://127.0.0.1:2026` | ✅ |
| `MOBOX_NODE_ID` | 节点编号 | `sz1` | ✅ |
| `MOBOX_NODE_AUTH` | 节点密码 | `123` | ✅ |
| `MOBOX_SYSTEM_TYPE` | 系统类型 | `Linux` | ❌ |
| `MOBOX_NETWORK` | 网络接口名称 | `auto` | ❌ |
| `MOBOX_DEBUG` | 调试模式 | `false` | ❌ |

### 网络接口配置

- **自动检测（推荐）**: 设置 `MOBOX_NETWORK=auto`，容器会自动检测默认网络接口
- **手动指定**: 设置为具体接口名，如 `eth0`、`ens33`、`ens192` 等

查看可用网络接口：
```bash
docker exec moboxfrp-node ip link show
```

## 🔧 网络模式

### Host 网络模式（推荐）

使用主机网络，FRP 可以直接使用主机的所有端口，无需手动映射：

```yaml
network_mode: "host"
```

**优点**：
- 端口映射自动化
- 性能最佳
- 配置简单

**缺点**：
- 容器与主机共享网络命名空间
- 端口冲突风险

### Bridge 网络模式

如果不能使用 host 网络，需要手动映射端口：

```yaml
ports:
  - "7000-7999:7000-7999"      # FRP 控制端口
  - "20000-30000:20000-30000"  # FRP 数据端口
```

⚠️ **注意**：需要根据实际使用的端口范围调整映射。

## 🔐 权限要求

容器需要 `NET_ADMIN` 权限以支持 tc 流量控制：

### 方法一：特权模式（简单但权限较大）
```yaml
privileged: true
```

### 方法二：精细权限控制（推荐）
```yaml
cap_add:
  - NET_ADMIN
  - SYS_ADMIN
```

## 📁 数据持久化

建议挂载以下目录：

```yaml
volumes:
  - ./data/logs:/opt/mossfrp/MoBoxFrp/logs              # 日志文件
  - ./data/dependency:/opt/mossfrp/MoBoxFrp/dependency  # 依赖文件
```

## 📊 日志查看

```bash
# 查看实时日志
docker-compose logs -f

# 查看最近 100 行日志
docker-compose logs --tail=100

# 查看容器内的日志文件
docker exec moboxfrp-node ls -lh /opt/mossfrp/MoBoxFrp/logs/
docker exec moboxfrp-node tail -f /opt/mossfrp/MoBoxFrp/logs/latest.log
```

## 🐛 故障排查

### 1. 网络接口检测失败

**问题**：容器启动时提示找不到网络接口

**解决方案**：
```bash
# 查看可用接口
docker exec moboxfrp-node ip link show

# 手动指定接口
docker-compose down
# 修改 docker-compose.yml 中的 MOBOX_NETWORK
docker-compose up -d
```

### 2. tc 命令权限不足

**问题**：日志中提示 tc 命令失败

**解决方案**：
- 确保容器以 `--privileged` 或 `--cap-add=NET_ADMIN` 运行
- 检查宿主机是否安装了 `iproute2` 包

### 3. 无法连接主控服务器

**问题**：节点无法连接到主控

**解决方案**：
```bash
# 检查网络连通性
docker exec moboxfrp-node ping -c 4 your-master-server

# 检查配置
docker exec moboxfrp-node cat /opt/mossfrp/MoBoxFrp/config.yml

# 查看详细日志
docker-compose logs -f
```

### 4. 端口映射问题

**问题**：FRP 隧道无法建立

**解决方案**：
- 使用 `network_mode: "host"` 模式
- 或确保所有需要的端口都已正确映射
- 检查防火墙规则

## 🔄 更新升级

```bash
# 停止容器
docker-compose down

# 拉取最新代码
git pull

# 重新构建镜像
docker-compose build --no-cache

# 启动新容器
docker-compose up -d
```

## 📝 配置文件示例

### 最小配置（docker-compose.yml）

```yaml
version: '3.8'
services:
  moboxfrp-node:
    build: .
    privileged: true
    network_mode: "host"
    environment:
      - MOBOX_ADDRESS=http://192.168.1.100:2026
      - MOBOX_NODE_ID=node1
      - MOBOX_NODE_AUTH=mypassword
    volumes:
      - ./data/logs:/opt/mossfrp/MoBoxFrp/logs
    restart: unless-stopped
```

### 完整配置（docker-compose.yml）

参见项目根目录的 `docker-compose.yml` 文件。

## 🛠️ 高级用法

### 自定义配置文件

如果需要完全自定义配置：

```yaml
volumes:
  - ./my-config.yml:/opt/mossfrp/MoBoxFrp/config.yml:ro
```

⚠️ **注意**：使用自定义配置文件时，环境变量将被忽略。

### 多节点部署

创建多个 docker-compose 文件或使用不同的容器名：

```bash
# 节点 1
docker run -d --name moboxfrp-node1 \
  -e MOBOX_NODE_ID=node1 \
  ... moboxfrp-node:latest

# 节点 2
docker run -d --name moboxfrp-node2 \
  -e MOBOX_NODE_ID=node2 \
  ... moboxfrp-node:latest
```

### 资源限制

```yaml
deploy:
  resources:
    limits:
      cpus: '2'
      memory: 1G
    reservations:
      cpus: '0.5'
      memory: 256M
```

## 📞 技术支持

- 项目地址：[MoBoxFrpNode](https://github.com/MossCG/MoBoxFrpNode)
- 问题反馈：提交 Issue

## 📄 许可证

请参考项目根目录的 LICENSE 文件。

---

**提示**：首次部署时，请仔细阅读本文档并根据实际情况调整配置。
