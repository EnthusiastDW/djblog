#!/bin/bash

# ============================================================================
# Jenkins 前端构建脚本
# 用途：在 Jenkins 流水线中构建 Vue.js 前端项目
# ============================================================================

set -e  # 遇到错误立即退出

# 配置变量
PROJECT_NAME="frontend"
FRONTEND_DIR="frontend"

echo "=========================================="
echo "开始构建 ${PROJECT_NAME} 前端应用"
echo "=========================================="

# 检查 Node.js 环境
echo "[1/4] 检查 Node.js 环境..."
node -v
if [ $? -ne 0 ]; then
    echo "错误：未找到 Node.js，请确保已安装 Node.js 20+"
    exit 1
fi

# 检查 npm 环境
echo "[2/4] 检查 npm 环境..."
npm -v
if [ $? -ne 0 ]; then
    echo "错误：未找到 npm"
    exit 1
fi

# 进入前端目录
cd ${FRONTEND_DIR}

# 清理旧的构建产物
echo "[3/4] 清理旧的构建产物..."
rm -rf node_modules dist
rm -f package-lock.json

# 安装依赖
echo "[4/4] 安装依赖并构建..."
npm ci --legacy-peer-deps
npm run build

# 验证构建结果
if [ -d "dist" ]; then
    cd ..
    echo "=========================================="
    echo "✓ 构建成功!"
    echo "  构建目录：${FRONTEND_DIR}/dist"
    echo "=========================================="
    
    # 显示构建产物信息
    echo "构建产物大小:"
    du -sh ${FRONTEND_DIR}/dist
else
    cd ..
    echo "=========================================="
    echo "✗ 构建失败：未找到 dist 目录"
    echo "=========================================="
    exit 1
fi
