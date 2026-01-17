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