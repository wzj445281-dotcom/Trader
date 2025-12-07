# Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# 预下载依赖，利用缓存加速构建
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn -q -DskipTests package

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
# 复制构建好的 jar 包
COPY --from=build /app/target/*.jar app.jar

# 创建上传目录，防止报错
RUN mkdir -p /app/uploads

# 设置时区为上海，方便日志查看
ENV TZ=Asia/Shanghai

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]