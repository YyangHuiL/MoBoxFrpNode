# 构建阶段 - 使用 Maven + JDK 8
FROM maven:3.8-openjdk-8 AS builder

# 设置工作目录
WORKDIR /build

# 复制项目文件
COPY pom.xml .
COPY depend ./depend
COPY src ./src

# 按照 package.sh 的流程编译打包
RUN echo "开始编译项目..." && \
    # 1. 清理并编译（跳过测试）
    mvn clean compile -DskipTests && \
    # 2. 创建临时目录
    mkdir -p target/temp && \
    cd target/temp && \
    # 3. 解压 MossLib.jar
    jar xf ../../depend/MossLib.jar && \
    # 4. 复制编译的类文件
    cp -r ../../target/classes/* . && \
    # 5. 复制资源文件（包括 config.yml）
    if [ -d "../../src/main/resources" ]; then \
    cp -r ../../src/main/resources/* . ; \
    fi && \
    # 6. 创建 JAR（使用 MossLib 中的 MANIFEST.MF 或创建新的）
    if [ -f "META-INF/MANIFEST.MF" ]; then \
    jar cfm ../MoBoxFrpNode.jar META-INF/MANIFEST.MF . ; \
    else \
    echo "Manifest-Version: 1.0" > MANIFEST.MF && \
    echo "Main-Class: org.moboxlab.MoBoxFrpNode.Main" >> MANIFEST.MF && \
    jar cfm ../MoBoxFrpNode.jar MANIFEST.MF . ; \
    fi && \
    echo "JAR 打包完成: /build/target/MoBoxFrpNode.jar"

# 运行时镜像
FROM openjdk:8

# 安装必要的工具
RUN apt-get update && \
    apt-get install -y \
    iproute2 \
    iptables \
    net-tools \
    curl \
    wget \
    procps \
    && rm -rf /var/lib/apt/lists/*

# 创建工作目录
WORKDIR /opt/mossfrp

# 从构建阶段复制打包好的 JAR 文件
COPY --from=builder /build/target/MoBoxFrpNode.jar /opt/mossfrp/MoBoxFrpNode.jar

# 复制配置文件模板
COPY src/main/resources/config.yml /opt/mossfrp/config.yml.template

# 复制启动脚本
COPY docker-entrypoint.sh /opt/mossfrp/docker-entrypoint.sh
RUN chmod +x /opt/mossfrp/docker-entrypoint.sh

# 创建数据目录
RUN mkdir -p /opt/mossfrp/MoBoxFrp/logs /opt/mossfrp/MoBoxFrp/dependency

# 环境变量配置
ENV MOBOX_ADDRESS="http://127.0.0.1:2026" \
    MOBOX_NODE_ID="sz1" \
    MOBOX_NODE_AUTH="123" \
    MOBOX_SYSTEM_TYPE="Linux" \
    MOBOX_NETWORK="auto" \
    MOBOX_DEBUG="false"

# 暴露端口范围（根据实际需要调整）
# FRP 通常使用的端口范围
EXPOSE 7000-7999
EXPOSE 20000-30000

# 使用特权模式运行以支持 tc 命令
# 注意：需要在 docker run 时添加 --privileged 或 --cap-add=NET_ADMIN
ENTRYPOINT ["/opt/mossfrp/docker-entrypoint.sh"]
CMD ["java", "-jar", "/opt/mossfrp/MoBoxFrpNode.jar"]
