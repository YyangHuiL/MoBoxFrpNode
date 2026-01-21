# Docker 镜像构建验证指南

## ✅ 构建流程说明

本 Docker 镜像完全按照 `package.sh` 的打包流程构建：

### 构建阶段（Builder Stage）
使用 `maven:3.8-openjdk-8` 镜像，包含：
- **Maven 3.8**：用于项目构建
- **OpenJDK 8**：满足项目的 JDK 8 要求

### 打包步骤（与 package.sh 一致）
1. ✅ `mvn clean compile -DskipTests` - 清理并编译项目
2. ✅ `mkdir -p target/temp` - 创建临时目录
3. ✅ `jar xf depend/MossLib.jar` - 解压 MossLib.jar
4. ✅ `cp target/classes/*` - 复制编译的类文件
5. ✅ `cp src/main/resources/*` - 复制资源文件
6. ✅ `jar cfm MoBoxFrpNode.jar META-INF/MANIFEST.MF` - 创建最终 JAR

### 运行时阶段（Runtime Stage）
使用 `openjdk:8-jre-slim` 镜像，包含：
- **JRE 8**：运行 Java 应用
- **iproute2**：提供 tc 命令（流量控制）
- **其他工具**：iptables, net-tools, curl, wget, procps

## 🧪 本地测试构建

### 1. 测试构建镜像

```bash
# 进入项目目录
cd /path/to/MoBoxFrpNode-master

# 构建镜像（会显示详细输出）
docker build -t moboxfrp-node:test .

# 查看构建的镜像
docker images | grep moboxfrp-node
```

### 2. 验证 JAR 文件

```bash
# 创建临时容器查看 JAR
docker run --rm moboxfrp-node:test ls -lh /opt/mossfrp/

# 验证 JAR 是否可执行
docker run --rm moboxfrp-node:test java -jar /opt/mossfrp/MoBoxFrpNode.jar --help
```

### 3. 测试启动脚本

```bash
# 测试网络接口检测
docker run --rm --privileged \
  -e MOBOX_ADDRESS=http://test:2026 \
  -e MOBOX_NODE_ID=test \
  -e MOBOX_NODE_AUTH=test \
  moboxfrp-node:test \
  bash -c "ip link show"

# 测试 tc 命令
docker run --rm --privileged \
  moboxfrp-node:test \
  bash -c "tc qdisc show"
```

### 4. 完整功能测试

```bash
# 创建测试配置
cat > .env.test <<EOF
MOBOX_ADDRESS=http://your-test-server:2026
MOBOX_NODE_ID=test-node
MOBOX_NODE_AUTH=test-password
MOBOX_NETWORK=auto
MOBOX_DEBUG=true
EOF

# 使用测试配置启动
docker run -d \
  --name moboxfrp-test \
  --privileged \
  --network host \
  --env-file .env.test \
  moboxfrp-node:test

# 查看启动日志
docker logs -f moboxfrp-test

# 进入容器检查
docker exec -it moboxfrp-test bash

# 停止并删除测试容器
docker stop moboxfrp-test
docker rm moboxfrp-test
```

## 🔍 验证清单

### 构建验证
- [ ] Docker 镜像构建成功
- [ ] JAR 文件存在于 `/opt/mossfrp/MoBoxFrpNode.jar`
- [ ] JAR 文件大小合理（应包含 MossLib）
- [ ] 配置文件模板存在

### 运行时验证
- [ ] JRE 8 可用：`java -version`
- [ ] tc 命令可用：`tc -V`
- [ ] 网络接口可检测：`ip link show`
- [ ] 启动脚本可执行

### 功能验证
- [ ] 自动检测网络接口
- [ ] 配置文件正确生成
- [ ] tc 初始化成功
- [ ] 应用正常启动
- [ ] 日志正常输出

## 📊 预期输出

### 构建成功输出
```
Step 1/XX : FROM maven:3.8-openjdk-8 AS builder
...
开始编译项目...
[INFO] Compiling XX source files to /build/target/classes
...
JAR 打包完成: /build/target/MoBoxFrpNode.jar
...
Successfully built xxxxxxxxxx
Successfully tagged moboxfrp-node:latest
```

### 启动成功输出
```
==========================================
MoBoxFrpNode Docker 启动脚本
==========================================
正在检查容器权限...
权限检查通过
正在自动检测网络接口...
检测到网络接口: eth0
==========================================
环境配置信息
==========================================
主控地址: http://your-server:2026
节点编号: node1
节点密码: ********
系统类型: Linux
网络接口: eth0
调试模式: false
==========================================
正在初始化流量控制 (tc)...
流量控制初始化完成
正在生成配置文件...
配置文件已生成
==========================================
启动 MoBoxFrpNode...
==========================================
欢迎使用MoBoxFrp~这里是节点哦~
软件版本: X.X ...
```

## 🐛 常见构建问题

### 问题 1: Maven 依赖下载失败
```bash
# 解决方案：使用国内镜像
# 在 pom.xml 中添加阿里云镜像（如果需要）
```

### 问题 2: MossLib.jar 找不到
```bash
# 检查文件是否存在
ls -lh depend/MossLib.jar

# 确保 .dockerignore 没有排除 depend 目录
```

### 问题 3: JAR 文件无法执行
```bash
# 检查 MANIFEST.MF 中的 Main-Class
docker run --rm moboxfrp-node:test \
  jar xf /opt/mossfrp/MoBoxFrpNode.jar META-INF/MANIFEST.MF && \
  cat META-INF/MANIFEST.MF
```

### 问题 4: tc 命令不可用
```bash
# 确保使用 --privileged 或 --cap-add=NET_ADMIN
docker run --rm --privileged moboxfrp-node:test tc -V
```

## 📝 构建优化建议

### 1. 使用构建缓存
```bash
# 首次构建
docker build -t moboxfrp-node:latest .

# 后续构建（利用缓存）
docker build -t moboxfrp-node:latest .

# 强制重新构建
docker build --no-cache -t moboxfrp-node:latest .
```

### 2. 多平台构建
```bash
# 构建 AMD64 和 ARM64 版本
docker buildx build --platform linux/amd64,linux/arm64 \
  -t moboxfrp-node:latest .
```

### 3. 减小镜像大小
当前配置已经使用：
- ✅ 多阶段构建（builder + runtime）
- ✅ slim 基础镜像
- ✅ 清理 apt 缓存
- ✅ .dockerignore 排除不必要文件

## 🎯 生产环境部署检查

部署前确认：
- [ ] 已修改默认密码
- [ ] 已配置正确的主控地址
- [ ] 已测试网络连通性
- [ ] 已配置数据持久化
- [ ] 已设置重启策略
- [ ] 已配置日志轮转
- [ ] 已进行安全加固

## 📞 获取帮助

如果遇到问题：
1. 查看构建日志：`docker build` 输出
2. 查看运行日志：`docker logs -f container_name`
3. 进入容器调试：`docker exec -it container_name bash`
4. 查看详细文档：`DOCKER_README.md`

---

**最后更新**: 2026-01-21  
**Docker 版本要求**: Docker 20.10+, Docker Compose 1.29+
