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

## MVP 验收脚本

本地可执行基础 smoke 验收：

```bash
./scripts/e2e_smoke_test.sh
```

该脚本会依次运行：

- 后端测试：`../gradlew test`
- 后台 Web API 测试与构建：`npm test -- --run src/adminApi.test.ts`、`npm run build`
- Android 阅读器单元测试与 Debug 构建：`:app:testDebugUnitTest`、`:app:assembleDebug`

如果已经启动后端服务，可额外开启 API smoke：

```bash
RUN_API_SMOKE=1 API_BASE_URL=http://localhost:8080 ./scripts/e2e_smoke_test.sh
```

JDBC 模式本地后端可以直接使用：

```bash
./scripts/run_backend_jdbc.sh
```

该脚本会默认启用内容、后台配置、审计、术语、发布控制和阅读事件的 JDBC 模式，并读取 `DATABASE_URL`、`DATABASE_USERNAME`、`DATABASE_PASSWORD`。

如需开启自动增量抓取调度，可设置：

```bash
CRAWLING_SCHEDULER_ENABLED=true CRAWLING_SCHEDULER_FIXED_DELAY_MS=60000 ./scripts/run_backend_jdbc.sh
```

调度器会按书籍来源的 `updateIntervalMinutes` 生成 `CRAWL_LATEST` 任务，并跳过已有待执行的同书籍来源任务。

API smoke 会检查首页、广告配置、合规配置、后台广告配置保存、匿名设备创建和阅读事件上报。

## 文档

核心文档：

- `docs/superpowers/specs/2026-06-08-novel-platform-mvp-design.md`
- `docs/superpowers/specs/2026-06-08-novel-platform-architecture-design.md`
- `docs/superpowers/specs/2026-06-08-admin-web-layout-design.md`
- `docs/superpowers/specs/2026-06-08-android-reader-layout-design.md`
- `docs/superpowers/plans/2026-06-08-novel-platform-mvp-implementation-plan.md`
