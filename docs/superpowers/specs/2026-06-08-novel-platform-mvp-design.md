# Novel Platform MVP Design

## 1. Project Goal

Build an operations-ready MVP for an overseas web novel reading product.

The first phase creates a complete content pipeline:

1. Operators configure a small set of public Chinese novel sites and specific novel URLs.
2. The backend crawls chapters manually or on a schedule.
3. The system cleans, structures, and quality-checks chapter text.
4. OpenAI translates approved chapters into English with book-specific glossary support.
5. Translated chapters are automatically published to an Android reading app.
6. The Android app serves overseas Google Play users with free reading and ads.

The Web admin console is for internal operators and uses a Chinese UI. The Android app defaults to an English UI and reads translated English content. App UI language and novel content language are managed separately.

## 2. MVP Scope

The MVP supports:

- 1-3 configured public novel sites.
- 10-30 manually added novels.
- Chinese source content translated to English.
- Android reader app plus Web admin console.
- Kotlin/Spring Boot backend.
- PostgreSQL storage.
- Redis-assisted scheduling, queueing, or caching if needed.
- OpenAI as the first translation provider through an abstract provider interface.
- Local development deployment, with Docker, environment variables, and database migrations reserved for future production deployment.

The MVP does not include:

- Full-web search crawling.
- Automatic discovery of new novels.
- Personalized recommendations.
- Paid chapters, subscriptions, or paid ad removal.
- Full role-based admin workflows.
- Full human editing workflow.
- Web reading site.
- Author portal.
- Comments, community, audio reading, or complex page-turn animations.
- Full CI/CD, monitoring, backups, or production operations.

## 3. Content Source and Crawling

Content comes from specified public novel sites and specified novel URLs. Operators add each source URL manually in the admin console and bind it to a site configuration.

Site configuration includes:

- Site name.
- Base domain.
- Request headers.
- Character encoding.
- Rate limit.
- Max concurrency.
- Chapter list parsing rule.
- Chapter body parsing rule.
- Ad filtering rules.
- Enabled or disabled state.

Crawler triggers:

- Manual full-book crawl.
- Manual latest-chapter crawl.
- Manual recrawl for a selected chapter.
- Scheduled incremental checks based on each book's configured update interval.

Crawler tasks record:

- Task type.
- Target site, book, and optional chapter.
- Status.
- Started and finished timestamps.
- Duration.
- Retry count.
- Failure reason.
- Number of chapters found or updated.

The crawler limits same-site concurrency and request frequency to reduce blocking risk.

## 4. Text Cleaning and Quality Checks

The system stores raw fetched HTML or text before cleaning.

Cleaning includes:

- Removing HTML.
- Removing scripts.
- Removing ad paragraphs.
- Removing navigation text.
- Removing unrelated links.
- Normalizing chapter titles.
- Saving body text as structured paragraphs.

Quality checks include:

- Duplicate chapter detection.
- Suspected missing chapter sequence.
- Body too short or too long.
- High garbled-text ratio.
- Residual ad keyword detection.

Chapters that pass quality checks move to the pending translation state.

Abnormal chapters move to `needs_review` or `blocked` and are not translated automatically. Admin users can inspect the raw content and cleaned body, then trigger recrawl, reclean, or manual status changes.

## 5. AI Translation and Glossary

Translation uses a provider abstraction. The MVP implements OpenAI first.

The translation provider interface must allow future providers without changing business task flow.

Translation tasks record:

- Provider.
- Model.
- Source language.
- Target language.
- Book and chapter.
- Input token count.
- Output token count.
- Estimated cost.
- Status.
- Retry count.
- Failure reason.

Target languages are configurable, but MVP only enables English.

Each book has its own glossary. Glossary entries include:

- Chinese source term.
- English translated term.
- Type.
- Description.
- Enabled state.

Glossary term types include:

- Person.
- Place.
- Organization.
- Cultivation method or skill.
- Item.
- Title or form of address.
- Other.

Chapter translation requirements:

- Translate by chapter.
- Split long chapters by paragraph when needed.
- Preserve paragraph structure.
- Preserve translated chapter title.
- Include the book glossary in translation context.
- Keep names, places, organizations, skills, and special terms consistent across chapters.

The system may detect suspected new proper nouns and add them to a pending glossary confirmation list. Operators can confirm, edit, or ignore these suggestions.

Translated chapters move to publishable state. The MVP auto-publishes translated chapters by default, while preserving admin controls for hiding a chapter, taking down a book, and triggering retranslation.

## 6. Web Admin Console

The Web admin console uses a Chinese UI.

MVP authentication uses a single administrator account. The data model and backend structure reserve future role and permission support.

Critical admin actions create audit logs, including:

- Site configuration changes.
- Book import.
- Manual crawl trigger.
- Glossary changes.
- Book or chapter takedown.
- Retranslation trigger.
- Publication state changes.

Admin modules:

### Dashboard

Shows:

- Book count.
- Chapter count.
- Today's crawl task count.
- Translation task count.
- Failed task count.
- Abnormal chapter count.
- Pending glossary term count.

### Site Management

Supports:

- Create and edit site configurations.
- Configure parser and filtering rules.
- Configure rate limits and concurrency.
- Enable or disable sites.
- Test parsing against a selected URL and inspect parsed output.

### Book Management

Supports:

- Add a novel source URL.
- Maintain title, author, cover, description, categories, tags, serialization state, update frequency, publication state, recommendation weight, and ad switch.
- Trigger full recrawl, latest-chapter crawl, and full-book retranslation.
- Take down, hide, or republish a book.

### Chapter Management

Supports:

- List chapters.
- Inspect crawl, cleaning, translation, and publication states.
- View raw fetched content.
- View cleaned source body.
- View English translated body.
- Hide a chapter.
- Recrawl a chapter.
- Reclean a chapter.
- Retranslate a chapter.

### Glossary Management

Supports:

- Maintain book-level glossary entries.
- Add, edit, disable, and import terms.
- Process pending glossary suggestions.

### Task Management

Supports:

- Inspect crawler, cleaning, and translation queues.
- View task state, failure reason, duration, and retry count.
- Retry or cancel tasks.

### Category and Recommendation Management

Supports:

- Maintain categories, tags, recommendation slots, and ordering weights.
- Configure list sorting by update time, read count, favorite count, and admin weight.

### Compliance and Publication Control

Supports:

- Book and chapter takedown.
- Book and chapter hiding.
- Delete markers.
- Copyright complaint records.
- Privacy policy URL.
- Terms of service URL.
- Advertising disclosure text.

## 7. Android App

The Android app targets overseas Google Play readers.

MVP app characteristics:

- Kotlin.
- Jetpack Compose.
- English UI by default.
- UI i18n structure reserved.
- No forced login.
- Guest reading support.
- Local bookshelf, reading progress, reading settings, and recent reading.
- Backend anonymous device ID support.
- Future Google login and anonymous data merge reserved.

Main screens:

### Home

Shows recommendation, new books, latest updates, and popular lists.

Sorting is based on backend rules, including:

- Update time.
- Read count.
- Favorite count.
- Admin recommendation weight.

### Categories

Allows browsing by genre, tag, and serialization state. Supports pagination, empty state, and error retry.

### Book Detail

Shows:

- Cover.
- Title.
- Author.
- Description.
- Categories and tags.
- Serialization state.
- Latest chapter.
- Update time.

Actions:

- Add to bookshelf.
- Start reading.
- Open chapter catalog.

### Chapter Catalog

Shows chapter list, update time, and local read state. Users can open any available chapter.

### Reader

Supports:

- Chapter body reading.
- Previous and next chapter.
- Catalog entry.
- Font size setting.
- Line height setting.
- Day and night mode.
- Background color setting.
- Reading progress persistence.
- Current and next chapter preloading.

Complex page-turn animations are out of MVP scope.

### Bookshelf

Shows saved novels, recent reading progress, and latest chapter hints. MVP storage is local. Login-based sync is reserved.

### Cache

Supports simple offline caching for recently read books, prioritizing the current chapter, adjacent chapters, or a small recent chapter window. Full-book download is out of MVP scope.

### Ads

The Android app integrates Google AdMob.

Supported ad behavior:

- Chapter-between ads or bottom reader ad placement.
- Optional interstitial ads on chapter switching.
- Interstitial frequency controlled by backend config.
- Ad enablement controlled by book, language, or region.

Paid ad removal, subscriptions, and paid chapters are out of MVP scope.

## 8. Backend API and Core Modules

The backend uses Kotlin and Spring Boot.

Core modules:

### Content Module

Manages books, chapters, categories, tags, covers, descriptions, publication states, recommendation weights, read counts, and favorite counts.

### Crawling Module

Manages site configuration, source URLs, crawler tasks, parsing, rate limits, concurrency, incremental updates, and retry behavior.

### Cleaning and Quality Module

Stores raw content and cleaned content. Runs ad cleaning, paragraph structuring, title normalization, duplicate checks, garbled-text checks, and abnormal chapter marking.

### Translation Module

Manages translation languages, translation tasks, OpenAI provider, glossary, pending glossary terms, translation chunking, translated-text merging, cost tracking, and retry behavior.

### Publication Module

Controls whether books and chapters are visible to the Android app. Supports auto-publication, takedown, hiding, and republishing after retranslation.

### App API

Provides:

- Home lists.
- Category lists.
- Book detail.
- Chapter catalog.
- Chapter content.
- Ad config.
- Anonymous device initialization.
- Reading behavior reporting.

### Admin API

Provides:

- Site management.
- Book management.
- Chapter management.
- Task management.
- Glossary management.
- Category and recommendation configuration.
- Publication control.
- Compliance configuration.
- Admin login.
- Audit logs.

### User and Device Module

MVP uses anonymous device ID. The backend records only necessary reading behavior and error logs.

Future support is reserved for:

- User table.
- Device table.
- Bookshelf sync.
- Reading progress sync.
- Google login binding.
- Anonymous data merge.

## 9. Main Data Flow

1. Operator configures a site and adds a novel URL.
2. Operator manually triggers crawling, or a schedule triggers incremental crawling.
3. The crawler fetches chapter list and chapter bodies.
4. The backend stores raw fetched content.
5. The cleaning module creates structured chapter content.
6. Quality checks mark chapters as translatable or abnormal.
7. Translatable chapters create English translation tasks.
8. The OpenAI provider translates chapters with glossary context.
9. The system stores translated chapters with paragraph structure.
10. Translated chapters are automatically published.
11. The Android app fetches published books, catalogs, and chapter content through App API.
12. Reading behavior is reported to the backend for counts, ranking, and future recommendation support.

## 10. Data and Index Requirements

The data model must separately preserve:

- Raw fetched content.
- Cleaned source content.
- Translated content.
- Book publication state.
- Chapter publication state.
- Crawling state.
- Cleaning state.
- Translation state.

Indexes should support:

- Book by publication state, language, category, tag, update time, and recommendation weight.
- Chapter by book, chapter order, publication state, and update time.
- Translation by chapter, language, provider, and state.
- Tasks by type, state, priority, created time, and retry count.
- Audit logs by actor, action, target, and created time.

Task tables must support:

- Status.
- Retry count.
- Failure reason.
- Duration.
- Provider metadata.
- Cost metadata.

## 11. Compliance Requirements

MVP includes basic compliance preparation:

- Privacy policy URL.
- Terms of service URL.
- Advertising disclosure.
- Anonymous device ID only before login.
- Minimal data collection.
- Book and chapter takedown.
- Book and chapter hiding.
- Delete markers.
- Copyright complaint records and status.
- Source URL and crawl timestamp tracking.

The project must explicitly recognize that crawling, translating, and publishing public-site novels can create copyright and Google Play takedown risk if content is not authorized. Before real public release, content authorization or a stronger complaint and takedown process should be prepared.

## 12. Acceptance Criteria

MVP is accepted when:

1. Admin can configure 1-3 novel sites and specified novel URLs.
2. The system can manually and periodically crawl chapters.
3. Crawl tasks record state, timing, retry count, and failure reason.
4. Crawled content can be cleaned, structured, and quality checked.
5. Abnormal chapters do not enter automatic translation.
6. Each book can maintain a glossary and person-name table.
7. OpenAI translation runs by chapter, preserves paragraph structure, and records cost metadata.
8. English translations can auto-publish to the app.
9. Admin can take down a book, hide a chapter, recrawl, reclean, and retranslate.
10. Android app supports home, categories, detail, catalog, reader, and bookshelf.
11. Reader supports font size, line height, day/night mode, background color, progress saving, preloading, and simple cache.
12. Android app supports AdMob placements and receives backend ad config.
13. Local development can run backend, database, Web admin, and Android app debugging.

## 13. Later Roadmap

### Phase 2

- Google login.
- Bookshelf and progress sync.
- More target languages.
- More site configurations.
- Translation quality improvements.

### Phase 3

- Human editing and review workflow.
- Role and permission management.
- Operations reporting.
- Recommendation slot management.
- Production deployment.
- Monitoring and backups.

### Phase 4

- Personalized recommendation.
- Subscription or ad removal.
- Web reading site.
- Author or content partner portal.
