#!/bin/bash
# package.sh - 手动打包包含MossLib的JAR
set -e

# 清理并编译
mvn clean compile -DskipTests

# 创建临时目录
mkdir -p target/temp
cd target/temp

# 解压MossLib.jar
if [ -f "../../depend/MossLib.jar" ]; then
    jar xf ../../depend/MossLib.jar
else
    echo "Warning: depend/MossLib.jar not found, skipping..."
fi

# 复制编译的类文件
mkdir -p target/classes
cp -r ../../target/classes/* .

# 复制资源文件（包括MANIFEST.MF）
if [ -d "../../src/main/resources" ]; then
  cp -r ../../src/main/resources/* .
fi

# 创建JAR（使用已有的MANIFEST.MF）
if [ -f "META-INF/MANIFEST.MF" ]; then
  jar cfm ../MoBoxFrpNode.jar META-INF/MANIFEST.MF .
else
  # 如果没有MANIFEST.MF，创建一个
  echo "Manifest-Version: 1.0" > MANIFEST.MF
  echo "Main-Class: org.moboxlab.MoBoxFrpNode.Main" >> MANIFEST.MF
  jar cfm ../MoBoxFrpNode.jar MANIFEST.MF .
fi

# 回到项目根目录
cd ../..
echo "✅ JAR created at: target/MoBoxFrpNode.jar"

# Docker 打包流程
echo "🐳 开始构建 Docker 镜像 (Local Build)..."
docker build -t moboxfrp-node:latest .

echo "📦 准备发布包..."
DIST_NAME="MoBoxFrpNode-Offline"
DIST_DIR="target/$DIST_NAME"
rm -rf $DIST_DIR
mkdir -p $DIST_DIR

# 1. 导出镜像 (包含 Java环境 + 项目JAR + 系统工具)
echo "💾 导出镜像文件 (这可能需要几分钟)..."
docker save -o $DIST_DIR/moboxfrp-node.tar moboxfrp-node:latest

# 2. 复制部署脚本
cp deploy.sh $DIST_DIR/
chmod +x $DIST_DIR/deploy.sh

# 3. 生成 docker-compose.yml (运行时配置)
cat > $DIST_DIR/docker-compose.yml <<EOF
version: '3.8'

services:
  moboxfrp-node:
    image: moboxfrp-node:latest
    container_name: moboxfrp-node
    privileged: true
    network_mode: "host"
    environment:
      - MOBOX_ADDRESS=\${MOBOX_ADDRESS:-http://127.0.0.1:2026}
      - MOBOX_NODE_ID=\${MOBOX_NODE_ID:-node1}
      - MOBOX_NODE_AUTH=\${MOBOX_NODE_AUTH:-password}
      - MOBOX_NETWORK=\${MOBOX_NETWORK:-auto}
    volumes:
      - ./data/logs:/opt/mossfrp/MoBoxFrp/logs
      - ./data/dependency:/opt/mossfrp/MoBoxFrp/dependency
    restart: unless-stopped
EOF

# 打包为压缩文件
echo "🗜️ 压缩发布包..."
cd target
tar -czf release.tar.gz $DIST_NAME/
cd ..

echo "✅ 打包完成!"
echo "发布包位置: target/release.tar.gz"
echo "包含内容: "
ls -lh $DIST_DIR