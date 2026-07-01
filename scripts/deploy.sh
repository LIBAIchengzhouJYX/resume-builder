#!/bin/bash
# ============================================================
# ResumeBuilder · Ubuntu 24.04.1 一键部署脚本
# 使用: sudo bash deploy.sh
# ============================================================
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PROJECT_DIR="/opt/resume-builder"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend"
LOG_DIR="/var/log/resume-builder"

# ---------- 配置变量（部署前修改这里）----------
GITHUB_CLIENT_ID="${GITHUB_CLIENT_ID:-your-github-client-id}"
GITHUB_CLIENT_SECRET="${GITHUB_CLIENT_SECRET:-your-github-client-secret}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-ResumeBuilder@2026!}"
MYSQL_DB_PASSWORD="${MYSQL_DB_PASSWORD:-ResumeBuilder@2026!}"
JWT_SECRET="${JWT_SECRET:-$(openssl rand -base64 48)}"
DOMAIN="${DOMAIN:-localhost}"

echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  ResumeBuilder · Ubuntu 24.04 部署脚本${NC}"
echo -e "${GREEN}============================================================${NC}"
echo ""

# ========== 0. 检查 root 权限 ==========
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}请使用 sudo 运行此脚本${NC}"
    exit 1
fi

# ========== 1. 系统更新 & 基础依赖 ==========
echo -e "${YELLOW}[1/8] 更新系统并安装基础依赖...${NC}"
apt-get update -qq
apt-get upgrade -y -qq
apt-get install -y -qq curl wget git unzip nginx certbot python3-certbot-nginx

# ========== 2. 安装 Java 17 ==========
echo -e "${YELLOW}[2/8] 安装 Java 17...${NC}"
if ! java -version 2>&1 | grep -q "17\."; then
    apt-get install -y -qq openjdk-17-jdk
fi
echo -e "${GREEN}  Java 版本: $(java -version 2>&1 | head -1)${NC}"

# ========== 3. 安装 Node.js 20 ==========
echo -e "${YELLOW}[3/8] 安装 Node.js 20...${NC}"
if ! node --version 2>/dev/null | grep -q "v20"; then
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
    apt-get install -y -qq nodejs
fi
echo -e "${GREEN}  Node 版本: $(node --version)${NC}"
echo -e "${GREEN}  npm 版本: $(npm --version)${NC}"

# ========== 4. 安装 MySQL 8 ==========
echo -e "${YELLOW}[4/8] 安装 MySQL 8...${NC}"
if ! mysql --version 2>/dev/null; then
    apt-get install -y -qq mysql-server
fi

# 启动 MySQL
systemctl enable mysql
systemctl start mysql

# 配置数据库
echo -e "${YELLOW}  配置 MySQL 数据库...${NC}"
mysql -u root <<SQL
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';
FLUSH PRIVILEGES;

CREATE DATABASE IF NOT EXISTS resume_builder
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'resume_user'@'localhost' IDENTIFIED BY '${MYSQL_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON resume_builder.* TO 'resume_user'@'localhost';
FLUSH PRIVILEGES;
SQL
echo -e "${GREEN}  MySQL 配置完成${NC}"

# ========== 5. 安装 wkhtmltopdf ==========
echo -e "${YELLOW}[5/8] 安装 wkhtmltopdf...${NC}"
apt-get install -y -qq wkhtmltopdf fonts-noto-cjk
echo -e "${GREEN}  wkhtmltopdf 安装完成${NC}"

# ========== 6. 构建后端 ==========
echo -e "${YELLOW}[6/8] 构建后端...${NC}"

# 创建项目目录
mkdir -p "$PROJECT_DIR" "$LOG_DIR"

# 项目根目录 = 脚本所在目录的上一级（scripts/ 的父目录）
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
echo -e "${YELLOW}  项目目录: $SCRIPT_DIR${NC}"
if [ -d "$SCRIPT_DIR/backend" ]; then
    echo -e "${YELLOW}  复制项目文件到 $PROJECT_DIR...${NC}"
    cp -r "$SCRIPT_DIR"/* "$PROJECT_DIR/"
fi

# 确保目录存在
cd "$PROJECT_DIR"
if [ ! -d "$BACKEND_DIR" ]; then
    echo -e "${RED}  错误: 找不到 backend 目录，请确保脚本在项目根目录运行${NC}"
    echo -e "${RED}  或先将项目文件放到 $PROJECT_DIR${NC}"
    exit 1
fi

# 更新 application.yml 配置
cat > "$BACKEND_DIR/src/main/resources/application.yml" <<YML
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/resume_builder?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: resume_user
    password: ${MYSQL_DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope:
              - read:user
              - user:email

  servlet:
    multipart:
      max-file-size: 5MB

server:
  port: 8080

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000
  pdf:
    wkhtmltopdf-path: /usr/bin/wkhtmltopdf
  frontend-url: https://${DOMAIN}

logging:
  level:
    com.resumebuilder: INFO
  file:
    path: ${LOG_DIR}
YML

# 构建项目（需要 Maven Wrapper）
cd "$BACKEND_DIR"
if [ ! -f "mvnw" ]; then
    echo -e "${YELLOW}  下载 Maven Wrapper...${NC}"
    mvn -N wrapper:wrapper -Dmaven=3.9.6 2>/dev/null || {
        # 如果没有 mvn，手动创建 mvnw
        curl -fsSL https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o /tmp/maven.tar.gz
        tar xzf /tmp/maven.tar.gz -C /tmp
        /tmp/apache-maven-3.9.6/bin/mvn -N wrapper:wrapper 2>/dev/null
    }
fi

chmod +x mvnw
./mvnw clean package -DskipTests -q
echo -e "${GREEN}  后端构建完成${NC}"

# 创建 systemd 服务
cat > /etc/systemd/system/resume-builder-api.service <<SVC
[Unit]
Description=ResumeBuilder API Server
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=${BACKEND_DIR}
ExecStart=/usr/bin/java -jar ${BACKEND_DIR}/target/resume-builder-1.0.0.jar
Restart=on-failure
RestartSec=10
Environment="MYSQL_DB_PASSWORD=${MYSQL_DB_PASSWORD}"
Environment="GITHUB_CLIENT_ID=${GITHUB_CLIENT_ID}"
Environment="GITHUB_CLIENT_SECRET=${GITHUB_CLIENT_SECRET}"
Environment="JWT_SECRET=${JWT_SECRET}"
Environment="DOMAIN=${DOMAIN}"

[Install]
WantedBy=multi-user.target
SVC

systemctl daemon-reload
systemctl enable resume-builder-api

# ========== 7. 构建前端 ==========
echo -e "${YELLOW}[7/8] 构建前端...${NC}"
cd "$FRONTEND_DIR"
npm install --silent
npm run build --silent
echo -e "${GREEN}  前端构建完成${NC}"

# ========== 8. 配置 Nginx ==========
echo -e "${YELLOW}[8/8] 配置 Nginx...${NC}"

cat > /etc/nginx/sites-available/resume-builder <<'NGX'
server {
    listen 80;
    server_name _;

    # 前端静态文件
    root /opt/resume-builder/frontend/dist;
    index index.html;

    # Gzip
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/javascript;
    gzip_min_length 1000;

    # 前端路由 (SPA)
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理到 Spring Boot
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
    }

    # OAuth2 回调代理
    location /oauth2 {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /login {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 健康检查
    location /health {
        access_log off;
        return 200 "OK";
    }
}
NGX

# 启用站点
rm -f /etc/nginx/sites-enabled/default
ln -sf /etc/nginx/sites-available/resume-builder /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx

# ========== 完成 ==========
# 启动后端服务
systemctl start resume-builder-api
systemctl reload nginx

echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  部署完成！${NC}"
echo -e "${GREEN}============================================================${NC}"
echo ""
echo -e "  访问地址:  ${YELLOW}http://$(hostname -I | awk '{print $1}')${NC}"
if [ "$DOMAIN" != "localhost" ]; then
    echo -e "  域名:      ${YELLOW}https://${DOMAIN}${NC}"
fi
echo ""
echo -e "  ${YELLOW}部署后需要手动完成的事项:${NC}"
echo -e "  1. 创建 GitHub OAuth App:"
echo -e "     Homepage URL:    http://${DOMAIN}"
echo -e "     Callback URL:    http://${DOMAIN}/login/oauth2/code/github"
echo ""
echo -e "  2. 编辑服务配置填入真实的 GitHub 凭据:"
echo -e "     sudo systemctl edit resume-builder-api"
echo -e "     添加:"
echo -e "     [Service]"
echo -e "     Environment=\"GITHUB_CLIENT_ID=你的真实ClientID\""
echo -e "     Environment=\"GITHUB_CLIENT_SECRET=你的真实ClientSecret\""
echo -e "     然后: sudo systemctl restart resume-builder-api"
echo ""
echo -e "  3. 如果使用域名, 配置 SSL 证书:"
echo -e "     sudo certbot --nginx -d ${DOMAIN}"
echo ""
echo -e "  常用命令:"
echo -e "    sudo systemctl status resume-builder-api   # 查看后端状态"
echo -e "    sudo journalctl -u resume-builder-api -f   # 查看后端日志"
echo -e "    sudo systemctl restart resume-builder-api  # 重启后端"
echo -e "    sudo nginx -t && sud