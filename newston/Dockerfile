FROM openjdk:17-oracle
# 设置容器内工作目录（避免直接操作根目录）
WORKDIR /app

# 将本地编译生成的jar包复制到镜像中（建议重命名简化后续操作）
COPY target/newston-0.0.1-SNAPSHOT.jar /app/application.jar

# 声明应用监听端口（实际映射在运行容器时通过-p参数指定）
EXPOSE 8081

# 设置时区为上海时间
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 启动应用（使用数组形式避免shell解析问题）
CMD ["java", "-jar", "/app/application.jar"]