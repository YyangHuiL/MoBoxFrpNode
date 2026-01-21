#!/bin/bash
# package.sh - 手动打包包含MossLib的JAR

# 清理并编译
mvn clean compile -DskipTests

# 创建临时目录
mkdir -p target/temp
cd target/temp

# 解压MossLib.jar
jar xf ../../depend/MossLib.jar

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
  echo "Main-Class: your.main.Class" >> MANIFEST.MF
  jar cfm ../MoBoxFrpNode.jar MANIFEST.MF .
fi

# 回到项目根目录
cd ../..
echo "✅ JAR created at: target/MoBoxFrpNode.jar"

# Docker 打包流程
echo "🐳 开始构建 Docker 镜像..."
docker build -t moboxfrp-node:latest .

echo "📦 准备发布包..."
DIST_DIR="target/dist"
rm -rf $DIST_DIR
mkdir -p $DIST_DIR

# 导出镜像
echo "💾 导出镜像文件 (这可能需要一点时间)..."
docker save -o $DIST_DIR/moboxfrp-node.tar moboxfrp-node:latest

# 复制部署脚本和配置
cp deploy.sh $DIST_DIR/
cp docker-compose.yml $DIST_DIR/

# 处理 docker-compose.yml 去除 build 部分，适配离线部署
# 这里简单地创建一个新的 docker-compose.yml 仅用于运行
cat > $DIST_DIR/docker-compose.yml <<EOF
version: '3.8'

services:
  moboxfrp-node:
    image: moboxfrp-node:latest
    container_name: moboxfrp-node
    privileged: true
    network_mode: "host"
    environment:
      - MOBOX_ADDRESS=\${MOBOX_ADDRESS:-http://your-master-server:2026}
      - MOBOX_NODE_ID=\${MOBOX_NODE_ID:-node1}
      - MOBOX_NODE_AUTH=\${MOBOX_NODE_AUTH:-your_password_here}
      - MOBOX_SYSTEM_TYPE=\${MOBOX_SYSTEM_TYPE:-Linux}
      - MOBOX_NETWORK=\${MOBOX_NETWORK:-auto}
      - MOBOX_DEBUG=\${MOBOX_DEBUG:-false}
    volumes:
      - ./data/logs:/opt/mossfrp/MoBoxFrp/logs
      - ./data/dependency:/opt/mossfrp/MoBoxFrp/dependency
    restart: unless-stopped
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
EOF

# 打包为压缩文件
echo "🗜️ 压缩发布包..."
cd target
tar -czf release.tar.gz dist/
cd ..

echo "✅ 打包完成!"
echo "发布包位置: target/release.tar.gz"
echo "部署方法:"
echo "1. 上传 target/release.tar.gz 到服务器"
echo "2. 解压: tar -xzf release.tar.gz"
echo "3. 进入目录: cd dist"
echo "4. 运行: ./deploy.sh"