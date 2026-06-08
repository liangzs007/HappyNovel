export interface AdminBookRow {
  id: string
  title: string
  author: string
  sourceSite: string
  categories: string
  status: string
  publishStatus: string
  recommendationWeight: string
  latestChapterTitle: string
  updatedAt: string
}

export interface AdminBooksResult {
  books: AdminBookRow[]
  emptyText: string
}

export interface AdminSiteRow {
  id: string
  name: string
  baseDomain: string
  enabledStatus: string
  rateLimitLabel: string
  maxConcurrency: string
  lastFailureReason: string
}

export interface AdminTaskRow {
  id: string
  type: string
  status: string
  targetId: string
  retryCount: string
  failureReason: string
  duration: string
}

export interface AdminChapterRow {
  id: string
  order: string
  title: string
  crawlStatus: string
  cleanStatus: string
  translationStatus: string
  publishStatus: string
}

export interface AdminChaptersResult {
  chapters: AdminChapterRow[]
  emptyText: string
}

export interface AdminGlossaryTermRow {
  id: string
  sourceTerm: string
  translatedTerm: string
  type: string
  enabledStatus: string
  updatedAt: string
}

export interface AdminGlossaryResult {
  terms: AdminGlossaryTermRow[]
  emptyText: string
}

export interface AdminAuditRow {
  id: string
  actor: string
  action: string
  target: string
  summary: string
  createdAt: string
}

export interface AdminAuditResult {
  entries: AdminAuditRow[]
  emptyText: string
}

export interface AdminRecommendationRow {
  id: string
  name: string
  type: string
  boundBook: string
  sortWeight: string
  enabledStatus: string
}

export interface AdminRecommendationsResult {
  items: AdminRecommendationRow[]
  emptyText: string
}

export interface AdminComplianceResult {
  configCards: string[]
  complaints: AdminComplaintRow[]
  emptyText: string
}

export interface UpdateComplianceConfigRequest {
  privacyPolicyTitle: string
  privacyPolicyUrl: string
  termsTitle: string
  termsUrl: string
  adDisclosureEnabled: boolean
  adDisclosureText: string
}

export interface AdminComplaintRow {
  id: string
  source: string
  bookTitle: string
  chapterTitle: string
  status: string
  note: string
}

interface BackendBookSummary {
  id: string
  title: string
  author?: string
  status?: string
  latestChapterTitle?: string
  updatedAt?: string
}

interface BackendAdminBooksResponse {
  books: BackendBookSummary[]
  emptyText: string
}

interface BackendSiteConfig {
  id: string
  name: string
  baseDomain: string
  enabled: boolean
  rateLimitPerMinute: number
  maxConcurrency: number
}

interface BackendPipelineTask {
  id: string
  type: string
  status: string
  targetId: string
  retryCount: number
  failureReason?: string | null
}

interface BackendComplianceResponse {
  config: {
    privacyPolicyTitle: string
    privacyPolicyUrl: string
    termsTitle: string
    termsUrl: string
    adDisclosureEnabled: boolean
    adDisclosureText: string
  }
  complaints: AdminComplaintRow[]
  emptyText: string
}

interface BackendAdminChapterRow {
  id: string
  order: number
  title: string
  crawlStatus: string
  cleanStatus: string
  translationStatus: string
  publishStatus: string
}

interface BackendAdminChaptersResponse {
  chapters: BackendAdminChapterRow[]
  emptyText: string
}

interface BackendAdminGlossaryTermRow {
  id: string
  sourceTerm: string
  translatedTerm: string
  type: string
  enabledStatus: string
}

interface BackendAdminGlossaryResponse {
  terms: BackendAdminGlossaryTermRow[]
  emptyText: string
}

interface BackendAdminAuditResponse {
  entries: AdminAuditRow[]
  emptyText: string
}

interface BackendAdminRecommendationsResponse {
  items: AdminRecommendationRow[]
  emptyText: string
}

interface AdminApiOptions {
  baseUrl?: string
  fetcher?: typeof fetch
}

export function createAdminApi(options: AdminApiOptions = {}) {
  const baseUrl = options.baseUrl ?? ''
  const fetcher = options.fetcher ?? fetch

  return {
    async listBooks(limit = 50): Promise<AdminBooksResult> {
      const response = await fetcher(`${baseUrl}/api/admin/books?limit=${limit}`)
      if (!response.ok) {
        throw new Error(`后台书籍列表加载失败：${response.status}`)
      }
      const payload = await response.json() as BackendAdminBooksResponse
      return {
        emptyText: payload.emptyText,
        books: payload.books.map(toBookRow),
      }
    },

    async listSites(): Promise<AdminSiteRow[]> {
      const response = await fetcher(`${baseUrl}/api/admin/crawling/sites`)
      if (!response.ok) {
        throw new Error(`站点列表加载失败：${response.status}`)
      }
      const payload = await response.json() as BackendSiteConfig[]
      return payload.map(toSiteRow)
    },

    async listTasks(): Promise<AdminTaskRow[]> {
      const response = await fetcher(`${baseUrl}/api/admin/crawling/tasks`)
      if (!response.ok) {
        throw new Error(`任务列表加载失败：${response.status}`)
      }
      const payload = await response.json() as BackendPipelineTask[]
      return payload.map(toTaskRow)
    },

    async loadCompliance(): Promise<AdminComplianceResult> {
      const response = await fetcher(`${baseUrl}/api/admin/compliance`)
      if (!response.ok) {
        throw new Error(`合规配置加载失败：${response.status}`)
      }
      const payload = await response.json() as BackendComplianceResponse
      return toComplianceResult(payload)
    },

    async updateCompliance(request: UpdateComplianceConfigRequest): Promise<AdminComplianceResult> {
      const response = await fetcher(`${baseUrl}/api/admin/compliance`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
      })
      if (!response.ok) {
        throw new Error(`合规配置保存失败：${response.status}`)
      }
      const payload = await response.json() as BackendComplianceResponse
      return toComplianceResult(payload)
    },

    async listChapters(bookId = 'book-seed-1'): Promise<AdminChaptersResult> {
      const response = await fetcher(`${baseUrl}/api/admin/chapters?bookId=${bookId}`)
      if (!response.ok) {
        throw new Error(`章节列表加载失败：${response.status}`)
      }
      const payload = await response.json() as BackendAdminChaptersResponse
      return {
        emptyText: payload.emptyText,
        chapters: payload.chapters.map(toChapterRow),
      }
    },

    async listGlossaryTerms(): Promise<AdminGlossaryResult> {
      const response = await fetcher(`${baseUrl}/api/admin/glossary`)
      if (!response.ok) {
        throw new Error(`术语列表加载失败：${response.status}`)
      }
      const payload = await response.json() as BackendAdminGlossaryResponse
      return {
        emptyText: payload.emptyText,
        terms: payload.terms.map(toGlossaryTermRow),
      }
    },

    async listAuditLogs(): Promise<AdminAuditResult> {
      const response = await fetcher(`${baseUrl}/api/admin/audit`)
      if (!response.ok) {
        throw new Error(`审计日志加载失败：${response.status}`)
      }
      return await response.json() as BackendAdminAuditResponse
    },

    async listRecommendations(): Promise<AdminRecommendationsResult> {
      const response = await fetcher(`${baseUrl}/api/admin/recommendations`)
      if (!response.ok) {
        throw new Error(`分类推荐加载失败：${response.status}`)
      }
      return await response.json() as BackendAdminRecommendationsResponse
    },
  }
}

function toBookRow(book: BackendBookSummary): AdminBookRow {
  return {
    id: book.id,
    title: book.title,
    author: book.author ?? '-',
    sourceSite: '-',
    categories: '-',
    status: book.status ?? '-',
    publishStatus: '已发布',
    recommendationWeight: '0',
    latestChapterTitle: book.latestChapterTitle ?? '-',
    updatedAt: book.updatedAt ?? '-',
  }
}

function toSiteRow(site: BackendSiteConfig): AdminSiteRow {
  return {
    id: site.id,
    name: site.name,
    baseDomain: site.baseDomain,
    enabledStatus: site.enabled ? '启用' : '停用',
    rateLimitLabel: `${site.rateLimitPerMinute} 次/分钟`,
    maxConcurrency: String(site.maxConcurrency),
    lastFailureReason: '-',
  }
}

function toTaskRow(task: BackendPipelineTask): AdminTaskRow {
  return {
    id: task.id,
    type: task.type,
    status: task.status,
    targetId: task.targetId,
    retryCount: String(task.retryCount),
    failureReason: task.failureReason || '-',
    duration: '-',
  }
}

function toComplianceResult(payload: BackendComplianceResponse): AdminComplianceResult {
  return {
    configCards: [
      `隐私政策：${payload.config.privacyPolicyTitle}`,
      `服务条款：${payload.config.termsTitle}`,
      `广告披露：${payload.config.adDisclosureEnabled ? '已启用' : '未启用'}`,
    ],
    complaints: payload.complaints,
    emptyText: payload.emptyText,
  }
}

function toChapterRow(chapter: BackendAdminChapterRow): AdminChapterRow {
  return {
    id: chapter.id,
    order: String(chapter.order),
    title: chapter.title,
    crawlStatus: chapter.crawlStatus,
    cleanStatus: chapter.cleanStatus,
    translationStatus: chapter.translationStatus,
    publishStatus: chapter.publishStatus,
  }
}

function toGlossaryTermRow(term: BackendAdminGlossaryTermRow): AdminGlossaryTermRow {
  return {
    id: term.id,
    sourceTerm: term.sourceTerm,
    translatedTerm: term.translatedTerm,
    type: term.type,
    enabledStatus: term.enabledStatus,
    updatedAt: '-',
  }
}
