# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci
COPY . .
# 这一步会生成 dist 目录
RUN npm run build

# Production stage
FROM nginx:alpine
# 复制构建好的静态文件
COPY --from=build /app/dist /usr/share/nginx/html
# 复制自定义的 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]