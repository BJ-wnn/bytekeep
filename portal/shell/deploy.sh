#!/bin/bash
# 项目部署脚本：打包 -> 构建Docker镜像 -> 启动容器
# 使用方法：在项目根目录执行 ./shell/deploy.sh

# 配置参数
PROJECT_NAME="portal"                  # 项目名称
APP_NAME="spring-web-${PROJECT_NAME}"  # 应用名称
IMAGE_TAG="1.0"                        # 镜像版本号
CONTAINER_NAME="${APP_NAME}-container" # 容器名称
PORT_MAPPING="8080:8080"               # 端口映射（宿主机:容器）
MAIN_CLASS="org.wnn.portal.PortalApplication" # 主类全路径

# 颜色定义
RED="\033[31m"
GREEN="\033[32m"
YELLOW="\033[33m"
BLUE="\033[34m"
RESET="\033[0m"

# 日志函数
info() {
    echo -e "${BLUE}[INFO] $1${RESET}"
}

success() {
    echo -e "${GREEN}[SUCCESS] $1${RESET}"
}

warning() {
    echo -e "${YELLOW}[WARNING] $1${RESET}"
}

error() {
    echo -e "${RED}[ERROR] $1${RESET}"
    exit 1
}

# 检查工作目录（确保在项目根目录执行）
check_workdir() {
    if [ ! -f "pom.xml" ]; then
        error "请在项目根目录执行此脚本（需存在pom.xml文件）"
    fi
}

# 清理并打包项目
package_project() {
    info "开始打包项目..."
    if mvn clean package -Ptest -DskipTests; then
        success "项目打包完成"
    else
        error "项目打包失败，请检查Maven配置和依赖"
    fi
}

# 检查打包结果
check_package_result() {
    info "检查打包结果..."
    JAR_FILE=$(find target -maxdepth 1 -name "*.jar" ! -name "*.original" | head -n 1)
    if [ -z "$JAR_FILE" ]; then
        error "未在target目录找到可执行JAR文件"
    fi

    if [ ! -d "target/libs" ]; then
        error "未在target目录找到libs依赖目录，请检查Maven配置"
    fi

    success "打包结果检查通过"
}

# 构建Docker镜像
build_image() {
    info "开始构建Docker镜像: ${APP_NAME}:${IMAGE_TAG}..."
    if docker build -t ${APP_NAME}:${IMAGE_TAG} .; then
        success "Docker镜像构建完成"
    else
        error "Docker镜像构建失败"
    fi
}

# 停止并删除旧容器
clean_old_container() {
    info "清理旧容器..."
    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        warning "发现旧容器${CONTAINER_NAME}，正在停止并删除..."
        docker stop ${CONTAINER_NAME} > /dev/null
        docker rm ${CONTAINER_NAME} > /dev/null
        success "旧容器已清理"
    else
        info "无旧容器需要清理"
    fi
}

# 启动新容器
start_container() {
    info "启动新容器: ${CONTAINER_NAME}..."
    if docker run --network inner-net -d -p ${PORT_MAPPING} --name ${CONTAINER_NAME} ${APP_NAME}:${IMAGE_TAG} --logging.config=classpath:logback-spring.xml ; then
        success "容器启动成功！访问地址: http://localhost:${PORT_MAPPING%:*}"
        info "查看日志命令: docker logs -f ${CONTAINER_NAME}"
    else
        error "容器启动失败"
    fi
}

# 主流程
main() {
    echo "=============================================="
    echo "          开始部署 ${PROJECT_NAME} 项目          "
    echo "=============================================="

    check_workdir
    package_project
    check_package_result
    build_image
    clean_old_container
    start_container

    echo "=============================================="
    echo "          部署流程已完成                       "
    echo "=============================================="
}

# 执行主流程
main
