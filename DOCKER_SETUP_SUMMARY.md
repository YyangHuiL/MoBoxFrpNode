# MoBoxFrpNode Docker 镜像构建完成

## 📦 已创建的文件

### 核心文件
1. **Dockerfile** - Docker 镜像定义文件
   - 多阶段构建，优化镜像大小
   - 包含 Java 8 运行环境
   - 预装 tc (iproute2) 用于流量控制
   - 自动编译打包项目

2. **docker-entrypoint.sh** - 容器启动脚本
   - 自动检测网络接口
   - 初始化 tc 流量控制
   - 动态生成配置文件
   - 权限检查和环境验证

3. **docker-compose.yml** - Docker Compose 配置
   - 支持 .env 文件配置
   - Host 网络模式（支持 FRP 端口映射）
   - 数据持久化配置
   - 自动重启策略

### 配置文件
4. **.env.example** - 环境变量模板
5. **.dockerignore** - Docker 构建忽略文件

### 文档和脚本
6. **DOCKER_README.md** - 详细使用文档
7. **deploy.sh** - 一键部署脚本（Linux/Mac）

## 🚀 快速使用指南

### 方法一：使用一键部署脚本（推荐）

```bash
# 1. 给脚本添加执行权限
chmod +x deploy.sh

# 2. 运行部署脚本（会引导你配置）
./deploy.sh
```

### 方法二：手动部署

```bash
# 1. 复制并编辑配置文件
cp .env.example .env
nano .env  # 或使用其他编辑器

# 2. 修改以下配置（必须）
MOBOX_ADDRESS=http://你的主控地址:2026
MOBOX_NODE_ID=你的节点编号
MOBOX_NODE_AUTH=你的节点密码

# 3. 启动服务
docker-compose up -d

# 4. 查看日志
docker-compose logs -f
```

## ⚙️ 核心特性

### 1. 自动化网络接口检测
- 设置 `MOBOX_NETWORK=auto` 自动检测默认网络接口
- 支持手动指定网络接口（eth0, ens33, ens192 等）
- 启动时显示检测到的网络接口

### 2. TC 流量控制支持
- 自动初始化 HTB qdisc
- 支持动态限速
- 容器需要 NET_ADMIN 权限

### 3. 端口映射方案
- **Host 网络模式**（默认）：自动映射所有端口
- **Bridge 网络模式**：需手动配置端口范围

### 4. 配置灵活性
- 支持环境变量配置
- 支持 .env 文件
- 支持挂载自定义配置文件

### 5. 数据持久化
- 日志文件持久化到 `./data/logs`
- 依赖文件持久化到 `./data/dependency`

## 🔧 配置说明

### 必填环境变量
| 变量 | 说明 | 示例 |
|------|------|------|
| MOBOX_ADDRESS | 主控服务器地址 | http://192.168.1.100:2026 |
| MOBOX_NODE_ID | 节点编号 | node1 |
| MOBOX_NODE_AUTH | 节点密码 | mypassword123 |

### 可选环境变量
| 变量 | 说明 | 默认值 |
|------|------|--------|
| MOBOX_NETWORK | 网络接口 | auto |
| MOBOX_DEBUG | 调试模式 | false |
| MOBOX_SYSTEM_TYPE | 系统类型 | Linux |

## 📋 常用命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 查看日志
docker-compose logs -f

# 重启服务
docker-compose restart

# 查看状态
docker-compose ps

# 进入容器
docker-compose exec moboxfrp-node bash

# 查看网络接口
docker-compose exec moboxfrp-node ip link show

# 查看 tc 配置
docker-compose exec moboxfrp-node tc qdisc show

# 重新构建镜像
docker-compose build --no-cache
```

## 🐛 故障排查

### 问题 1: 网络接口检测失败
```bash
# 查看可用接口
docker exec moboxfrp-node ip link show

# 手动指定接口
# 编辑 .env 文件，设置 MOBOX_NETWORK=eth0
```

### 问题 2: tc 命令权限不足
```bash
# 确保 docker-compose.yml 中有：
privileged: true
# 或
cap_add:
  - NET_ADMIN
```

### 问题 3: 无法连接主控
```bash
# 测试网络连通性
docker exec moboxfrp-node ping -c 4 主控地址

# 检查配置
docker exec moboxfrp-node cat /opt/mossfrp/MoBoxFrp/config.yml
```

## 📊 目录结构

```
MoBoxFrpNode-master/
├── Dockerfile              # Docker 镜像定义
├── docker-compose.yml      # Docker Compose 配置
├── docker-entrypoint.sh    # 容器启动脚本
├── deploy.sh              # 一键部署脚本
├── .env.example           # 环境变量模板
├── .dockerignore          # Docker 忽略文件
├── DOCKER_README.md       # 详细文档
├── data/                  # 数据目录（自动创建）
│   ├── logs/             # 日志文件
│   └── dependency/       # 依赖文件
├── src/                   # 源代码
├── depend/                # 依赖库
└── pom.xml               # Maven 配置
```

## 🔐 安全建议

1. **修改默认密码**：不要使用示例中的密码
2. **限制网络访问**：使用防火墙规则限制访问
3. **定期更新**：及时更新镜像和依赖
4. **日志监控**：定期检查日志文件
5. **资源限制**：根据需要设置 CPU 和内存限制

## 📈 性能优化

### 资源限制配置
在 docker-compose.yml 中添加：
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

### 日志轮转
```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

## 🎯 下一步

1. ✅ 已完成 Docker 镜像配置
2. ✅ 已完成自动化部署脚本
3. ✅ 已完成文档编写
4. 📝 建议：在 Linux 服务器上测试部署
5. 📝 建议：根据实际端口需求调整端口映射

## 💡 提示

- 首次部署建议使用 `deploy.sh` 脚本，会自动引导配置
- 生产环境建议使用 `.env` 文件管理配置
- 网络接口建议使用 `auto` 自动检测
- 使用 `docker-compose logs -f` 实时查看日志
- 遇到问题请查看 `DOCKER_README.md` 详细文档

---

**作者**: MossCG  
**Docker 支持**: 2026-01-21  
**文档版本**: 1.0
