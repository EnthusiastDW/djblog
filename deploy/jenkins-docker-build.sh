#!/bin/bash

# ============================================================================
# Jenkins Docker 镜像构建与推送脚本
# 用途：构建 Docker 镜像并推送到镜像仓库
# ============================================================================

set -e  # 遇到错误立即退出

# 获取脚本所在目录的绝对路径
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 配置变量（根据实际环境修改）
DOCKER_REGISTRY="${DOCKER_REGISTRY:-docker.io}"        # Docker 镜像仓库地址
DOCKER_USERNAME="${DOCKER_USERNAME}"                    # Docker 仓库用户名
DOCKER_PASSWORD="${DOCKER_PASSWORD}"                    # Docker 仓库密码
IMAGE_PREFIX="${IMAGE_PREFIX:-djblog}"                  # 镜像名称前缀
VERSION="${VERSION:-latest}"                            # 镜像版本标签

echo "=========================================="
echo "Docker 镜像构建与推送"
echo "项目根目录：${PROJECT_ROOT}"
echo "=========================================="

# 检查 Docker 环境
echo "[1/6] 检查 Docker 环境..."
docker -v
if [ $? -ne 0 ]; then
    echo "错误：未找到 Docker，请确保已安装 Docker"
    exit 1
fi

# 登录 Docker 仓库
echo "[2/6] 登录 Docker 仓库..."
if [ -n "${DOCKER_USERNAME}" ] && [ -n "${DOCKER_PASSWORD}" ]; then
    echo "${DOCKER_PASSWORD}" | docker login --username ${DOCKER_USERNAME} --password-stdin ${DOCKER_REGISTRY}
    echo "✓ Docker 登录成功"
else
    echo "警告：未配置 DOCKER_USERNAME 或 DOCKER_PASSWORD，跳过登录步骤"
fi

# 构建后端镜像
echo "[3/6] 构建后端 Docker 镜像..."
cd "${PROJECT_ROOT}"
docker build -f deploy/Dockerfile.backend \
    -t ${DOCKER_REGISTRY}/${IMAGE_PREFIX}/backend:${VERSION} \
    -t ${DOCKER_REGISTRY}/${IMAGE_PREFIX}/backend:latest \
    .

echo "✓ 后端镜像构建成功"

# 构建前端镜像
echo "[4/6] 构建前端 Docker 镜像..."
docker build -f deploy/Dockerfile.frontend \
    -t ${DOCKER_REGISTRY}/${IMAGE_PREFIX}/frontend:${VERSION} \
    -t ${DOCKER_REGISTRY}/${IMAGE_PREFIX}/frontend:latest \
    .

echo "✓ 前端镜像构建成功"

# 列出构建的镜像
echo "[5/6] 查看构建的镜像..."
docker images | grep ${IMAGE_PREFIX}

# 推送镜像到仓库
echo "[6/6] 推送 Docker 镜像..."
if [ -n "${DOCKER_USERNAME}" ] && [ -n "${DOCKER_PASSWORD}" ]; then
    echo "推送后端镜像..."
    docker push ${DOCKER_REGISTRY}/${IMAGE_PREFIX}/backend:${VERSION}
    docker push ${DOCKER_REGISTRY}/${IMAGE_PREFIX}/backend:latest
    
    echo "推送前端镜像..."
    docker push ${DOCKER_REGISTRY}/${IMAGE_PREFIX}/frontend:${VERSION}
    docker push ${DOCKER_REGISTRY}/${IMAGE_PREFIX}/frontend:latest
    
    echo "=========================================="
    echo "✓ 镜像推送成功!"
    echo "  后端镜像：${DOCKER_REGISTRY}/${IMAGE_PREFIX}/backend:${VERSION}"
    echo "  前端镜像：${DOCKER_REGISTRY}/${IMAGE_PREFIX}/frontend:${VERSION}"
    echo "=========================================="
else
    echo "=========================================="
    echo "✓ 镜像构建完成 (未推送)"
    echo "  后端镜像：${IMAGE_PREFIX}/backend:${VERSION}"
    echo "  前端镜像：${IMAGE_PREFIX}/frontend:${VERSION}"
    echo "=========================================="
fi
