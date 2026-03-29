#!/bin/bash

# ============================================================================
# Jenkins 后端构建脚本
# 用途：在 Jenkins 流水线中构建 Spring Boot 项目
# ============================================================================

set -e  # 遇到错误立即退出

# 配置变量
PROJECT_NAME="djblog"
BACKEND_DIR="backend"
VERSION="1.0.0"

echo "=========================================="
echo "开始构建 ${PROJECT_NAME} 后端服务"
echo "=========================================="

# 检查 Java 环境
echo "[1/4] 检查 Java 环境..."
java -version
if [ $? -ne 0 ]; then
    echo "错误：未找到 Java 环境，请确保已安装 JDK 21"
    exit 1
fi

# 检查 Maven 环境
echo "[2/4] 检查 Maven 环境..."
mvn -version
if [ $? -ne 0 ]; then
    echo "错误：未找到 Maven，请确保已安装 Maven 3.6+"
    exit 1
fi

# 清理旧的构建产物
echo "[3/4] 清理旧的构建产物..."
cd ${BACKEND_DIR}
mvn clean
rm -rf target/*.jar

# Maven 构建
echo "[4/4] 执行 Maven 构建..."
mvn clean package -DskipTests -B

# 验证构建结果
if [ -f "target/${PROJECT_NAME}-${VERSION}.jar" ]; then
    echo "=========================================="
    echo "✓ 构建成功!"
    echo "  JAR 文件：${BACKEND_DIR}/target/${PROJECT_NAME}-${VERSION}.jar"
    echo "=========================================="
    
    # 显示文件大小
    ls -lh target/${PROJECT_NAME}-${VERSION}.jar
else
    echo "=========================================="
    echo "✗ 构建失败：未找到 JAR 文件"
    echo "=========================================="
    exit 1
fi
