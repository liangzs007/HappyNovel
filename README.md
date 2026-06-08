# HappyNovel

HappyNovel 是一个面向海外读者的中文网文 AI 翻译与 Android 阅读平台。

当前仓库处于 MVP 阶段，已规划以下子工程：

- `backend/`：Kotlin + Spring Boot 后端。
- `admin-web/`：React + TypeScript 中文运营后台。
- `android-app/`：Android 阅读 App。
- `deploy/`：本地开发环境配置。
- `docs/`：需求、架构、布局和实施计划文档。

## 本地开发环境

需要准备：

- JDK 21 或更高版本。
- Node.js 20 或更高版本。
- Docker 或兼容的容器运行环境。
- Android SDK，后续用于构建 `android-app/`。

## 启动基础服务

```bash
docker compose -f deploy/docker-compose.yml up -d
```

该命令会启动：

- PostgreSQL：`localhost:5432`
- Redis：`localhost:6379`

环境变量参考：

```bash
cp deploy/env.example .env
```

## 后端

```bash
cd backend
../gradlew test
../gradlew bootRun
```

健康检查：

```bash
curl http://localhost:8080/health
```

## Web 后台

```bash
cd admin-web
npm install
npm run dev
```

默认地址：

```text
http://localhost:5173
```

## Android App

```bash
cd android-app
../gradlew :app:assembleDebug
```

## 文档

核心文档：

- `docs/superpowers/specs/2026-06-08-novel-platform-mvp-design.md`
- `docs/superpowers/specs/2026-06-08-novel-platform-architecture-design.md`
- `docs/superpowers/specs/2026-06-08-admin-web-layout-design.md`
- `docs/superpowers/specs/2026-06-08-android-reader-layout-design.md`
- `docs/superpowers/plans/2026-06-08-novel-platform-mvp-implementation-plan.md`
