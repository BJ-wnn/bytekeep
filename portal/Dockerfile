# 使用本地已下载的OpenJDK 8镜像
FROM docker.io/adoptopenjdk/openjdk8:alpine-slim


# 维护者信息
LABEL maintainer="yourname@example.com"

# 设置工作目录
WORKDIR /app

# 1. 先复制依赖目录lib到容器的/app/lib（与JAR包同级）
COPY target/libs /app/libs

# 将打包好的JAR文件复制到容器中
COPY target/*.jar app.jar

# 暴露端口（与Spring Boot应用配置的端口一致）
EXPOSE 8080

# 启动命令 - 针对JDK8优化了JVM参数
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]