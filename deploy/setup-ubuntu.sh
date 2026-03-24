#!/bin/bash

################################################################################
# Ubuntu 服务器初始化脚本
# 功能：
#   1. 安装 SSH 服务并配置 root 登录
#   2. 安装 Docker 和 Docker Compose
#   3. 配置国内镜像源
################################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否以 root 用户运行
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用 root 用户运行此脚本 (sudo ./setup.sh)"
        exit 1
    fi
}

# 更新软件包列表
update_packages() {
    log_info "更新软件包列表..."
    apt-get update
}

################################################################################
# 1. 安装和配置 SSH 服务
################################################################################
setup_ssh() {
    log_info "开始安装和配置 SSH 服务..."
    
    # 安装 openssh-server
    apt-get install -y openssh-server
    
    # 备份原始配置文件
    cp /etc/ssh/sshd_config /etc/ssh/sshd_config.bak
    
    # 修改 SSH 配置允许 root 登录
    sed -i 's/#PermitRootLogin prohibit-root/PermitRootLogin yes/' /etc/ssh/sshd_config
    sed -i 's/PermitRootLogin prohibit-root/PermitRootLogin yes/' /etc/ssh/sshd_config
    
    # 如果没有 PermitRootLogin 配置项，则添加
    if ! grep -q "^PermitRootLogin" /etc/ssh/sshd_config; then
        echo "PermitRootLogin yes" >> /etc/ssh/sshd_config
    fi
    
    # 配置密码认证
    sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config
    sed -i 's/PasswordAuthentication no/PasswordAuthentication yes/' /etc/ssh/sshd_config
    
    # 如果没有 PasswordAuthentication 配置项，则添加
    if ! grep -q "^PasswordAuthentication" /etc/ssh/sshd_config; then
        echo "PasswordAuthentication yes" >> /etc/ssh/sshd_config
    fi
    
    # 重启 SSH 服务
    systemctl restart sshd
    systemctl enable sshd
    
    log_info "SSH 服务已安装并配置完成"
    log_warn "请设置 root 用户密码：passwd root"
}

################################################################################
# 2. 安装 Docker
################################################################################
install_docker() {
    log_info "开始安装 Docker..."
    
    # 卸载旧版本（如果存在）
    apt-get remove -y docker docker-engine docker.io containerd runc || true
    
    # 安装必要的依赖
    apt-get install -y \
        apt-transport-https \
        ca-certificates \
        curl \
        gnupg \
        lsb-release
    
    # 添加 Docker 官方 GPG 密钥
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    
    # 设置稳定的仓库
    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
      $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    # 更新软件包索引
    apt-get update
    
    # 安装 Docker Engine
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    
    # 启动 Docker
    systemctl start docker
    systemctl enable docker
    
    log_info "Docker 安装完成"
}

################################################################################
# 3. 配置 Docker 国内镜像源
################################################################################
configure_docker_mirrors() {
    log_info "配置 Docker 国内镜像源..."
    
    # 创建或修改 daemon.json 文件
    mkdir -p /etc/docker
    
    # 定义国内镜像源列表
    cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.126.com",
    "https://mirror.baidubce.com",
    "https://ccr.ccs.tencentyun.com",
    "https://registry.cn-hangzhou.aliyuncs.com",
    "https://dockerhub.icu",
    "https://docker.1panel.live"
  ],
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  }
}
EOF
    
    # 重载 systemd 守护进程
    systemctl daemon-reload
    
    # 重启 Docker
    systemctl restart docker
    
    log_info "Docker 镜像源配置完成"
}

################################################################################
# 4. 安装 Docker Compose 独立版本
################################################################################
install_docker_compose() {
    log_info "安装 Docker Compose..."
    
    # 下载 Docker Compose（使用国内镜像）
    DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
    mkdir -p $DOCKER_CONFIG/cli-plugins
    
    # 从 GitHub 中国镜像下载
    curl -SL https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose
    
    # 设置执行权限
    chmod +x /usr/local/bin/docker-compose
    
    # 创建软链接
    ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
    
    log_info "Docker Compose 安装完成"
}

################################################################################
# 5. 验证安装
################################################################################
verify_installation() {
    log_info "开始验证安装..."
    
    # 验证 SSH 服务
    log_info "验证 SSH 服务..."
    if systemctl is-active --quiet sshd; then
        log_info "✓ SSH 服务运行正常"
    else
        log_error "✗ SSH 服务运行异常"
    fi
    
    # 验证 Docker
    log_info "验证 Docker..."
    if docker --version; then
        log_info "✓ Docker 安装成功"
    else
        log_error "✗ Docker 安装失败"
    fi
    
    # 验证 Docker 服务状态
    if systemctl is-active --quiet docker; then
        log_info "✓ Docker 服务运行正常"
    else
        log_error "✗ Docker 服务运行异常"
    fi
    
    # 验证 Docker Compose
    log_info "验证 Docker Compose..."
    if docker-compose --version; then
        log_info "✓ Docker Compose 安装成功"
    else
        log_error "✗ Docker Compose 安装失败"
    fi
    
    # 验证 Docker 镜像源
    log_info "验证 Docker 镜像源配置..."
    echo "Docker 信息："
    docker info | grep -A 10 "Registry Mirrors"
    
    # 测试镜像拉取
    log_info "测试镜像拉取（使用 hello-world）..."
    if docker pull hello-world; then
        log_info "✓ Docker 镜像源工作正常"
        docker images hello-world
    else
        log_warn "⚠ Docker 镜像拉取测试失败，请检查网络连接"
    fi
    
    # 清理测试镜像
    docker rmi hello-world 2>/dev/null || true
}

################################################################################
# 6. 配置 Docker 非 root 用户访问（可选）
################################################################################
configure_docker_user() {
    log_info "配置 Docker 非 root 用户访问..."
    
    # 获取当前登录用户（如果不是 root）
    if [ -n "$SUDO_USER" ] && [ "$SUDO_USER" != "root" ]; then
        USER_NAME=$SUDO_USER
    else
        log_warn "未检测到 sudo 用户，跳过 Docker 用户组配置"
        return
    fi
    
    # 创建 docker 用户组（如果不存在）
    groupadd docker 2>/dev/null || true
    
    # 将用户添加到 docker 组
    usermod -aG docker $USER_NAME
    
    log_info "已将用户 $USER_NAME 添加到 docker 组"
    log_warn "请注销并重新登录后，即可不使用 sudo 运行 docker 命令"
}

################################################################################
# 主函数
################################################################################
main() {
    log_info "=========================================="
    log_info "Ubuntu 服务器初始化脚本"
    log_info "=========================================="
    
    # 检查 root 权限
    check_root
    
    # 更新软件包
    update_packages
    
    # 安装 SSH
    setup_ssh
    
    # 安装 Docker
    install_docker
    
    # 配置镜像源
    configure_docker_mirrors
    
    # 安装 Docker Compose
    install_docker_compose
    
    # 配置用户权限
#    configure_docker_user
    
    # 验证安装
    verify_installation
    
    log_info "=========================================="
    log_info "安装完成！"
    log_info "=========================================="
    log_warn "重要提醒："
    log_warn "1. 请设置 root 密码：passwd root"
    log_warn "2. 如要使用非 root 用户运行 docker，请重新登录"
    log_warn "3. 建议配置防火墙规则以确保安全"
    log_info "=========================================="
}

# 执行主函数
main
