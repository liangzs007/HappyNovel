# 小说平台 MVP 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** 按阶段实现海外网文平台 MVP，从工程骨架到内容抓取、AI 翻译、中文后台、Android 阅读器和基础合规能力。

**架构：** 采用分阶段交付。先建立可运行的多模块工程和数据模型，再实现后端内容管线，随后接入 Web 后台和 Android App，最后补齐广告配置、合规配置和端到端验证。每个阶段都应能独立测试并提交。

**技术栈：** Kotlin、Spring Boot、PostgreSQL、Redis、Flyway、React、TypeScript、Android Kotlin、Jetpack Compose、Room、DataStore、Retrofit 或 Ktor Client、OpenAI API、Google AdMob。

---

## 1. 阶段拆分

MVP 规格覆盖多个独立子系统，不能作为一个单体任务执行。实施拆分为以下阶段：

1. **阶段一：仓库与工程骨架**
   建立后端、后台、Android 和文档目录结构，配置基础构建、环境变量和本地开发说明。

2. **阶段二：后端数据模型与基础 API**
   建立 PostgreSQL 数据模型、Flyway 迁移、Spring Boot 模块边界、后台认证、审计日志和基础 App API。

3. **阶段三：抓取、清洗与质检管线**
   实现站点配置、指定书籍来源、抓取任务、清洗任务、质量检测和异常章节状态流。

4. **阶段四：OpenAI 翻译与术语表**
   实现翻译供应商抽象、OpenAI Provider、术语表、分块翻译、成本记录、失败重试和自动发布。

5. **阶段五：中文 Web 后台**
   实现站点管理、书籍管理、章节管理、任务管理、术语表管理、分类推荐和合规发布控制。

6. **阶段六：Android 阅读器**
   实现首页、分类、详情、目录、阅读器、书架、本地进度、阅读设置、预加载和简单缓存。

7. **阶段七：广告、合规与端到端验收**
   接入 AdMob 配置通道，补齐隐私政策、服务条款、版权投诉记录、下架能力，并完成本地端到端验收。

## 2. 推荐仓库结构

```text
.
├── AGENT.md
├── README.md
├── backend/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── src/main/kotlin/
│   ├── src/main/resources/
│   └── src/test/kotlin/
├── admin-web/
│   ├── package.json
│   ├── src/
│   └── vite.config.ts
├── android-app/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── app/
├── deploy/
│   ├── docker-compose.yml
│   └── env.example
└── docs/
    └── superpowers/
        ├── specs/
        └── plans/
```

职责边界：

- `backend/`：Spring Boot 后端、任务管线、数据库迁移、App API、后台 API。
- `admin-web/`：中文 Web 后台。
- `android-app/`：Android 阅读 App。
- `deploy/`：本地开发所需的 PostgreSQL、Redis 和环境变量样例。
- `docs/`：需求、设计、实施计划和后续架构文档。

## 3. 阶段一：仓库与工程骨架

**交付物：**

- 后端 Spring Boot 工程可以启动。
- Web 后台 Vite 工程可以启动。
- Android App 工程可以编译。
- 本地 PostgreSQL 和 Redis 可以通过 `docker-compose` 启动。
- README 提供本地启动说明。

**建议任务：**

- [ ] 创建 `backend/` Kotlin + Spring Boot 工程。
- [ ] 创建 `admin-web/` React + TypeScript + Vite 工程。
- [ ] 创建 `android-app/` Kotlin + Jetpack Compose 工程。
- [ ] 创建 `deploy/docker-compose.yml`，包含 PostgreSQL 和 Redis。
- [ ] 创建 `deploy/env.example`，定义数据库、Redis、OpenAI、AdMob 和后台管理员配置项。
- [ ] 更新 `README.md`，写明本地启动顺序。
- [ ] 分别运行后端测试、Web 构建和 Android 构建。
- [ ] 提交：`chore: scaffold MVP project structure`。

## 4. 阶段二：后端数据模型与基础 API

**交付物：**

- 数据库迁移覆盖书籍、章节、译文、站点、任务、术语、管理员、审计日志、匿名设备和广告配置。
- 后端模块边界清晰。
- 管理员可以登录。
- App 可以获取首页、分类、详情、目录和章节内容的 mock 或种子数据。

**核心表：**

- `site_config`
- `book`
- `book_source`
- `chapter`
- `chapter_raw_content`
- `chapter_clean_content`
- `chapter_translation`
- `taxonomy_category`
- `taxonomy_tag`
- `book_tag`
- `glossary_term`
- `pending_glossary_term`
- `pipeline_task`
- `admin_user`
- `audit_log`
- `anonymous_device`
- `reading_event`
- `ad_config`
- `compliance_config`
- `copyright_complaint`

**建议任务：**

- [ ] 添加 Flyway 迁移，创建核心表和索引。
- [ ] 添加 Kotlin entity、repository 和 service 基础结构。
- [ ] 实现后台管理员登录接口。
- [ ] 实现审计日志写入服务。
- [ ] 实现 App 首页列表 API。
- [ ] 实现 App 分类列表 API。
- [ ] 实现 App 书籍详情 API。
- [ ] 实现 App 章节目录 API。
- [ ] 实现 App 章节内容 API。
- [ ] 添加后端集成测试，验证迁移、查询和基础 API。
- [ ] 提交：`feat: add backend data model and base APIs`。

## 5. 阶段三：抓取、清洗与质检管线

**交付物：**

- 后台可配置站点解析规则。
- 后台可录入指定小说 URL。
- 可手动触发整书抓取、最新章节抓取和指定章节重抓。
- 定时任务可按书籍更新频率执行增量检查。
- 原始内容、清洗内容和异常状态分开保存。

**建议任务：**

- [ ] 实现站点配置 CRUD。
- [ ] 实现书籍来源 URL 管理。
- [ ] 实现抓取任务创建和状态流转。
- [ ] 实现站点请求限速和同站点并发控制。
- [ ] 实现章节列表解析接口。
- [ ] 实现章节正文解析接口。
- [ ] 实现原始内容保存。
- [ ] 实现清洗服务，处理 HTML、广告段、导航文本和无关链接。
- [ ] 实现章节标题标准化和段落结构化。
- [ ] 实现重复章节、疑似缺章、正文长度、乱码比例和广告残留检测。
- [ ] 实现异常章节 `needs_review` 和 `blocked` 状态。
- [ ] 添加抓取和清洗服务测试。
- [ ] 提交：`feat: add crawling and cleaning pipeline`。

## 6. 阶段四：OpenAI 翻译与术语表

**交付物：**

- 目标语言可配置，MVP 启用英文。
- 每本书可以维护术语表和人名表。
- 翻译任务通过抽象 Provider 执行。
- OpenAI 翻译支持分块、合并、失败重试和成本记录。
- 翻译完成后自动进入可发布状态。

**建议任务：**

- [ ] 定义 `TranslationProvider` 接口。
- [ ] 实现 `OpenAITranslationProvider`。
- [ ] 实现翻译语言配置。
- [ ] 实现术语表 CRUD。
- [ ] 实现待确认术语列表。
- [ ] 实现章节分块策略。
- [ ] 实现翻译 prompt 构造，包含术语表和段落结构要求。
- [ ] 实现译文合并和段落结构保存。
- [ ] 实现 token 和成本元数据记录。
- [ ] 实现翻译失败重试和手动重翻译。
- [ ] 实现译文自动发布。
- [ ] 添加 Provider mock 测试和翻译流程集成测试。
- [ ] 提交：`feat: add OpenAI translation pipeline`。

## 7. 阶段五：中文 Web 后台

**交付物：**

- 中文后台可操作 MVP 所需内容和任务。
- 后台能查看任务状态和失败原因。
- 后台能下架书籍、隐藏章节、重抓、重新清洗和重翻译。

**建议任务：**

- [ ] 实现后台登录页。
- [ ] 实现后台基础布局、侧边栏和路由。
- [ ] 实现仪表盘。
- [ ] 实现站点管理页。
- [ ] 实现书籍管理页。
- [ ] 实现章节管理页。
- [ ] 实现术语表管理页。
- [ ] 实现任务管理页。
- [ ] 实现分类与推荐配置页。
- [ ] 实现合规与发布控制页。
- [ ] 添加前端单元测试和关键页面 smoke test。
- [ ] 提交：`feat: add Chinese admin console`。

## 8. 阶段六：Android 阅读器

**交付物：**

- App 默认英文 UI。
- 游客无需登录即可浏览和阅读。
- 本地书架、阅读进度、阅读设置和最近阅读可用。
- 阅读器支持基础设置、预加载和简单缓存。

**建议任务：**

- [ ] 配置 Android 工程、Compose、导航和主题。
- [ ] 实现 API Client。
- [ ] 实现本地 Room 数据库。
- [ ] 实现 DataStore 阅读设置。
- [ ] 实现首页。
- [ ] 实现分类页。
- [ ] 实现书籍详情页。
- [ ] 实现章节目录页。
- [ ] 实现阅读器页面。
- [ ] 实现字体大小、行距、日夜模式和背景色设置。
- [ ] 实现本地书架。
- [ ] 实现阅读进度保存。
- [ ] 实现当前章和下一章预加载。
- [ ] 实现最近章节简单缓存。
- [ ] 添加 ViewModel 测试和基础 UI 测试。
- [ ] 提交：`feat: add Android reader MVP`。

## 9. 阶段七：广告、合规与端到端验收

**交付物：**

- App 可以读取后端广告配置。
- App 预留 AdMob 广告位。
- 后台可配置隐私政策、服务条款和广告披露。
- 后台可记录版权投诉。
- 本地环境跑通端到端流程。

**建议任务：**

- [ ] 实现后端广告配置 API。
- [ ] 在 Android App 接入 AdMob SDK。
- [ ] 实现阅读器底部广告位或章节间广告位。
- [ ] 实现章节切换插屏频率配置。
- [ ] 实现后台广告配置页。
- [ ] 实现合规配置保存和读取。
- [ ] 实现版权投诉记录管理。
- [ ] 编写端到端验收脚本或手动验收清单。
- [ ] 使用测试小说跑通：录入来源、抓取、清洗、翻译、发布、App 阅读。
- [ ] 提交：`feat: add ads compliance and end-to-end validation`。

## 10. 验收顺序

建议按以下顺序验收：

1. 后端、数据库、Redis、本地环境可启动。
2. 后端基础数据模型和 App API 可用。
3. 后台可录入站点和书籍来源。
4. 抓取和清洗管线可生成合格章节。
5. OpenAI 翻译可生成英文译文。
6. 译文可自动发布到 App。
7. Android App 可完成浏览、详情、目录、阅读、书架和缓存。
8. 后台可完成下架、隐藏、重抓、重新清洗和重翻译。
9. 广告配置和合规配置可从后台影响 App。
10. 使用 1 本测试小说完成完整闭环。

## 11. 当前不执行的事项

以下事项不进入 MVP 实施计划：

- 真实大规模生产部署。
- CI/CD。
- 线上监控和自动备份。
- 个性化推荐。
- 多角色权限完整实现。
- 人工润色审核流。
- 付费、订阅或去广告。
- Web 阅读站。
- 作者后台。

## 12. 下一步

完成本总计划后，应先选择阶段一开始实施。阶段一完成并提交后，再为阶段二编写更细的任务计划或直接按本计划展开。
