# ResumeBuilder — 开源简历制作系统

面向技术人才的在线简历制作工具。**Spring Boot 3 + Vue 3** 全栈架构，支持中英双语、6 套精美模板、Markdown 自由编辑、实时预览、GitHub OAuth 登录和高质量 PDF 导出。

## 特性

- **Markdown 自由编辑** — 描述类内容用 Markdown 排版，支持加粗、斜体、列表、标题、引用、代码等。工具栏辅助输入，内置实时预览。
- **6 套精选模板** — Modern / Minimal / Sidebar / Timeline / Classic / Bold，每套有独立的配色和排版风格。
- **中英双语** — 每个字段都可以填写中英两个版本，简历自动排版为双语对照格式，也可一键切换仅显示中文/英文。
- **自定义 CSS** — 开放 CSS 变量覆盖，打造独一无二的简历配色和字体。
- **高质量 PDF 导出** — wkhtmltopdf 引擎渲染，A4 标准排版，保留矢量文字和精细排版。
- **GitHub OAuth 登录** — 用 GitHub 账号一键登录，无需额外注册。
- **云端保存** — 数据存储在 MySQL，多设备同步，随时编辑。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 + Spring Security + OAuth2 |
| 数据库 | MySQL + Spring Data JPA |
| 认证 | GitHub OAuth2 + JWT |
| PDF 引擎 | wkhtmltopdf |
| 前端框架 | Vue 3 + Vite |
| UI / CSS | Tailwind CSS |
| 状态管理 | Pinia |
| Markdown | marked |

## 快速开始

### 前置要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- wkhtmltopdf（用于 PDF 导出）

### 1. 创建 GitHub OAuth App

1. 访问 [GitHub Developer Settings](https://github.com/settings/developers) → New OAuth App
2. Homepage URL: `http://localhost:8080`
3. Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
4. 记录 `Client ID` 和 `Client Secret`

### 2. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS resume_builder
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 3. 配置环境变量

```bash
# 必须设置
export GITHUB_CLIENT_ID=your-github-client-id
export GITHUB_CLIENT_SECRET=your-github-client-secret
export MYSQL_PASSWORD=your-mysql-password
export JWT_SECRET=your-jwt-secret-at-least-32-chars

# PDF 相关
export WKHTMLTOPDF_PATH=/usr/local/bin/wkhtmltopdf

# 前端地址（CORS 配置用）
export FRONTEND_URL=http://localhost:5173
```

### 4. 启动后端

```bash
cd backend
./mvnw spring-boot:run
```

后端运行在 `http://localhost:8080`。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，API 请求自动代理到后端。

### 6. 打开浏览器

访问 `http://localhost:5173`，点击 "GitHub 登录" 开始使用。

## Docker 部署

```bash
# 复制并编辑环境变量
cp .env.example .env
vim .env

# 启动服务
docker compose up -d

# 初始化数据库
docker compose exec backend mysql -u root -p resume_builder < sql/schema.sql
```

## 项目结构

```
resume-builder/
├── backend/
│   ├── src/main/java/com/resumebuilder/
│   │   ├── config/       # Security, JWT, AppConfig
│   │   ├── controller/   # REST API 控制器
│   │   ├── dto/          # 数据传输对象
│   │   ├── model/        # JPA 实体 (User, Resume)
│   │   ├── repository/   # Spring Data 仓库
│   │   └── service/      # 业务逻辑 + PDF 导出
│   ├── sql/              # 数据库初始化脚本
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── api/          # Axios 封装
│   │   ├── components/   # 可复用组件 (MarkdownEditor 等)
│   │   ├── router/       # Vue Router 配置
│   │   ├── stores/       # Pinia 状态管理
│   │   ├── styles/       # Tailwind + 全局样式
│   │   └── views/        # 页面 (Home, Editor, Dashboard, Preview)
│   ├── index.html
│   ├── package.json
│   ├── tailwind.config.js
│   └── vite.config.js
├── docker-compose.yml
├── .env.example
└── README.md
```

## API 概要

| Method | Path | 说明 |
|--------|------|------|
| GET | `/api/auth/me` | 获取当前用户 |
| GET | `/api/resumes` | 列出所有简历 |
| GET | `/api/resumes/:id` | 获取单个简历 |
| POST | `/api/resumes` | 创建简历 |
| PUT | `/api/resumes/:id` | 更新简历 |
| DELETE | `/api/resumes/:id` | 删除简历 |
| GET | `/api/export/pdf/:id` | 导出 PDF |

所有 `/api/*` 路径需要 `Authorization: Bearer <jwt_token>` 请求头。

## 贡献

欢迎提 Issue 和 PR！MIT 协议开源。

1. Fork 本仓库
2. 创建特性分支: `git checkout -b feature/amazing-feature`
3. 提交更改: `git commit -m 'feat: add amazing feature'`
4. 推送到分支: `git push origin feature/amazing-feature`
5. 提交 Pull Request

## License

MIT License. 详见 [LICENSE](./LICENSE) 文件。
