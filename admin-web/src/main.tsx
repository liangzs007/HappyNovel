import React, { useEffect, useState } from 'react'
import { createRoot } from 'react-dom/client'
import {
  createAdminApi,
  type AdminAuditResult,
  type AdminAuditRow,
  type AdminBookRow,
  type AdminBooksResult,
  type AdminChapterRow,
  type AdminChaptersResult,
  type AdminComplianceResult,
  type AdminGlossaryResult,
  type AdminGlossaryTermRow,
  type AdminRecommendationRow,
  type AdminRecommendationsResult,
  type AdminSiteRow,
  type AdminTaskRow,
  type CreateSiteRequest,
  type CreateGlossaryTermRequest,
} from './adminApi'
import {
  adminNavigation,
  adminPages,
  auditRowCells,
  bookRowCells,
  chapterRowCells,
  complaintRowCells,
  dashboardMetrics,
  glossaryRowCells,
  recommendationRowCells,
  siteRowCells,
  taskRowCells,
  type AdminPageKey,
} from './adminModel'
import './styles.css'

const adminApi = createAdminApi()

function LoginPanel() {
  return (
    <section className="login-panel" aria-label="后台登录">
      <div>
        <p className="eyebrow">管理员入口</p>
        <h2>HappyNovel 运营后台</h2>
      </div>
      <form>
        <label>
          账号
          <input value="admin" readOnly />
        </label>
        <label>
          密码
          <input value="********" readOnly type="password" />
        </label>
        <button type="button">登录</button>
      </form>
    </section>
  )
}

function Dashboard() {
  return (
    <div className="dashboard-grid">
      {dashboardMetrics.map((metric) => (
        <article className={`metric metric-${metric.tone}`} key={metric.label}>
          <span>{metric.label}</span>
          <strong>{metric.value}</strong>
        </article>
      ))}
    </div>
  )
}

function ManagementPage({
  pageKey,
  tableState,
  complianceConfigCards,
  siteCreateForm,
  glossaryCreateForm,
}: {
  pageKey: AdminPageKey
  tableState: RemoteTableState
  complianceConfigCards: string[]
  siteCreateForm?: React.ReactNode
  glossaryCreateForm?: React.ReactNode
}) {
  const page = adminPages[pageKey]

  return (
    <section className="page-panel">
      <div className="page-heading">
        <div>
          <h2>{page.title}</h2>
          <p>{page.description}</p>
        </div>
        {page.primaryAction ? <button type="button">{page.primaryAction}</button> : null}
      </div>
      {pageKey === 'dashboard' ? <Dashboard /> : null}
      {pageKey === 'compliance' && complianceConfigCards.length > 0 ? (
        <div className="compliance-summary">
          {complianceConfigCards.map((card) => <span key={card}>{card}</span>)}
        </div>
      ) : null}
      {pageKey === 'sites' ? siteCreateForm : null}
      {pageKey === 'glossary' ? glossaryCreateForm : null}
      <div className="filters">
        <input placeholder="关键词搜索" />
        <select aria-label="状态筛选">
          <option>全部状态</option>
          <option>启用</option>
          <option>停用</option>
        </select>
        <button type="button">查询</button>
        <button type="button" className="secondary">重置</button>
      </div>
      <div className="table-shell">
        <table>
          <thead>
            <tr>
              {page.tableColumns.map((column) => <th key={column}>{column}</th>)}
            </tr>
          </thead>
          <tbody>
            {tableState.isLoading ? (
              <tr>
                <td colSpan={page.tableColumns.length} className="empty-state">{tableState.loadingText}</td>
              </tr>
            ) : null}
            {tableState.error ? (
              <tr>
                <td colSpan={page.tableColumns.length} className="empty-state">{tableState.error}</td>
              </tr>
            ) : null}
            {!tableState.isLoading && !tableState.error && tableState.rows.length > 0
              ? tableState.rows.map((row) => (
                <tr key={row.id}>
                  {row.cells.map((cell, index) => <td key={`${row.id}-${index}`}>{cell}</td>)}
                </tr>
              ))
              : null}
            {!tableState.isLoading && !tableState.error && tableState.rows.length === 0 ? (
              <tr>
                <td colSpan={page.tableColumns.length} className="empty-state">{tableState.emptyText}</td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </div>
    </section>
  )
}

function SiteQuickCreateForm({
  isSubmitting,
  onSubmit,
}: {
  isSubmitting: boolean
  onSubmit: (request: CreateSiteRequest) => void
}) {
  const [form, setForm] = useState({
    name: '',
    baseDomain: '',
    rateLimitPerMinute: '30',
    maxConcurrency: '2',
    chapterListSelector: '.chapter-list a',
    chapterBodySelector: '.chapter-content',
    adBlocklist: '请收藏本站,最新网址',
  })

  return (
    <form
      className="quick-form quick-form-sites"
      onSubmit={(event) => {
        event.preventDefault()
        onSubmit({
          name: form.name,
          baseDomain: form.baseDomain,
          rateLimitPerMinute: Number(form.rateLimitPerMinute) || 30,
          maxConcurrency: Number(form.maxConcurrency) || 1,
          chapterListSelector: form.chapterListSelector,
          chapterBodySelector: form.chapterBodySelector,
          adBlocklist: form.adBlocklist.split(',').map((item) => item.trim()).filter(Boolean),
        })
      }}
    >
      <label>
        站点名称
        <input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
      </label>
      <label>
        基础域名
        <input value={form.baseDomain} onChange={(event) => setForm({ ...form, baseDomain: event.target.value })} />
      </label>
      <label>
        目录选择器
        <input
          value={form.chapterListSelector}
          onChange={(event) => setForm({ ...form, chapterListSelector: event.target.value })}
        />
      </label>
      <label>
        正文选择器
        <input
          value={form.chapterBodySelector}
          onChange={(event) => setForm({ ...form, chapterBodySelector: event.target.value })}
        />
      </label>
      <label>
        限速
        <input
          inputMode="numeric"
          value={form.rateLimitPerMinute}
          onChange={(event) => setForm({ ...form, rateLimitPerMinute: event.target.value })}
        />
      </label>
      <label>
        并发
        <input
          inputMode="numeric"
          value={form.maxConcurrency}
          onChange={(event) => setForm({ ...form, maxConcurrency: event.target.value })}
        />
      </label>
      <label>
        广告词
        <input value={form.adBlocklist} onChange={(event) => setForm({ ...form, adBlocklist: event.target.value })} />
      </label>
      <button type="submit" disabled={isSubmitting || !form.name || !form.baseDomain}>
        {isSubmitting ? '保存中' : '保存站点'}
      </button>
    </form>
  )
}

function GlossaryQuickCreateForm({
  isSubmitting,
  onSubmit,
}: {
  isSubmitting: boolean
  onSubmit: (request: CreateGlossaryTermRequest) => void
}) {
  const [form, setForm] = useState<CreateGlossaryTermRequest>({
    bookId: 'book-seed-1',
    sourceTerm: '',
    translatedTerm: '',
    type: 'PERSON',
    description: '',
  })

  return (
    <form
      className="quick-form"
      onSubmit={(event) => {
        event.preventDefault()
        onSubmit(form)
      }}
    >
      <label>
        书籍 ID
        <input
          value={form.bookId}
          onChange={(event) => setForm({ ...form, bookId: event.target.value })}
        />
      </label>
      <label>
        中文原词
        <input
          value={form.sourceTerm}
          onChange={(event) => setForm({ ...form, sourceTerm: event.target.value })}
        />
      </label>
      <label>
        英文译名
        <input
          value={form.translatedTerm}
          onChange={(event) => setForm({ ...form, translatedTerm: event.target.value })}
        />
      </label>
      <label>
        类型
        <select
          value={form.type}
          onChange={(event) => setForm({ ...form, type: event.target.value })}
        >
          <option value="PERSON">人物</option>
          <option value="PLACE">地点</option>
          <option value="ORGANIZATION">组织</option>
          <option value="SKILL">技能</option>
          <option value="ITEM">物品</option>
          <option value="TITLE">称谓</option>
          <option value="OTHER">其他</option>
        </select>
      </label>
      <label>
        备注
        <input
          value={form.description}
          onChange={(event) => setForm({ ...form, description: event.target.value })}
        />
      </label>
      <button type="submit" disabled={isSubmitting || !form.sourceTerm || !form.translatedTerm}>
        {isSubmitting ? '保存中' : '保存术语'}
      </button>
    </form>
  )
}

interface RemoteTableRow {
  id: string
  cells: string[]
}

interface RemoteTableState {
  rows: RemoteTableRow[]
  isLoading: boolean
  loadingText: string
  error: string | null
  emptyText: string
}

const idleTableState = (emptyText: string): RemoteTableState => ({
  rows: [],
  isLoading: false,
  loadingText: '正在加载...',
  error: null,
  emptyText,
})

function App() {
  const [activePage, setActivePage] = useState<AdminPageKey>('dashboard')
  const [booksResult, setBooksResult] = useState<AdminBooksResult | null>(null)
  const [isBooksLoading, setBooksLoading] = useState(false)
  const [booksError, setBooksError] = useState<string | null>(null)
  const [chaptersResult, setChaptersResult] = useState<AdminChaptersResult | null>(null)
  const [isChaptersLoading, setChaptersLoading] = useState(false)
  const [chaptersError, setChaptersError] = useState<string | null>(null)
  const [glossaryResult, setGlossaryResult] = useState<AdminGlossaryResult | null>(null)
  const [isGlossaryLoading, setGlossaryLoading] = useState(false)
  const [isGlossarySubmitting, setGlossarySubmitting] = useState(false)
  const [glossaryError, setGlossaryError] = useState<string | null>(null)
  const [auditResult, setAuditResult] = useState<AdminAuditResult | null>(null)
  const [isAuditLoading, setAuditLoading] = useState(false)
  const [auditError, setAuditError] = useState<string | null>(null)
  const [recommendationsResult, setRecommendationsResult] = useState<AdminRecommendationsResult | null>(null)
  const [isRecommendationsLoading, setRecommendationsLoading] = useState(false)
  const [recommendationsError, setRecommendationsError] = useState<string | null>(null)
  const [sites, setSites] = useState<AdminSiteRow[] | null>(null)
  const [isSitesLoading, setSitesLoading] = useState(false)
  const [isSiteSubmitting, setSiteSubmitting] = useState(false)
  const [sitesError, setSitesError] = useState<string | null>(null)
  const [tasks, setTasks] = useState<AdminTaskRow[] | null>(null)
  const [isTasksLoading, setTasksLoading] = useState(false)
  const [tasksError, setTasksError] = useState<string | null>(null)
  const [compliance, setCompliance] = useState<AdminComplianceResult | null>(null)
  const [isComplianceLoading, setComplianceLoading] = useState(false)
  const [complianceError, setComplianceError] = useState<string | null>(null)
  const page = adminPages[activePage]

  useEffect(() => {
    if (activePage !== 'books' || booksResult) {
      return
    }

    setBooksLoading(true)
    setBooksError(null)
    adminApi.listBooks()
      .then(setBooksResult)
      .catch(() => setBooksError('书籍列表加载失败，请检查后端服务。'))
      .finally(() => setBooksLoading(false))
  }, [activePage, booksResult])

  useEffect(() => {
    if (activePage !== 'chapters' || chaptersResult) {
      return
    }

    setChaptersLoading(true)
    setChaptersError(null)
    adminApi.listChapters()
      .then(setChaptersResult)
      .catch(() => setChaptersError('章节列表加载失败，请检查后端服务。'))
      .finally(() => setChaptersLoading(false))
  }, [activePage, chaptersResult])

  useEffect(() => {
    if (activePage !== 'glossary' || glossaryResult) {
      return
    }

    setGlossaryLoading(true)
    setGlossaryError(null)
    adminApi.listGlossaryTerms()
      .then(setGlossaryResult)
      .catch(() => setGlossaryError('术语列表加载失败，请检查后端服务。'))
      .finally(() => setGlossaryLoading(false))
  }, [activePage, glossaryResult])

  useEffect(() => {
    if (activePage !== 'audit' || auditResult) {
      return
    }

    setAuditLoading(true)
    setAuditError(null)
    adminApi.listAuditLogs()
      .then(setAuditResult)
      .catch(() => setAuditError('审计日志加载失败，请检查后端服务。'))
      .finally(() => setAuditLoading(false))
  }, [activePage, auditResult])

  useEffect(() => {
    if (activePage !== 'recommendations' || recommendationsResult) {
      return
    }

    setRecommendationsLoading(true)
    setRecommendationsError(null)
    adminApi.listRecommendations()
      .then(setRecommendationsResult)
      .catch(() => setRecommendationsError('分类推荐加载失败，请检查后端服务。'))
      .finally(() => setRecommendationsLoading(false))
  }, [activePage, recommendationsResult])

  useEffect(() => {
    if (activePage !== 'sites' || sites) {
      return
    }

    setSitesLoading(true)
    setSitesError(null)
    adminApi.listSites()
      .then(setSites)
      .catch(() => setSitesError('站点列表加载失败，请检查后端服务。'))
      .finally(() => setSitesLoading(false))
  }, [activePage, sites])

  useEffect(() => {
    if (activePage !== 'tasks' || tasks) {
      return
    }

    setTasksLoading(true)
    setTasksError(null)
    adminApi.listTasks()
      .then(setTasks)
      .catch(() => setTasksError('任务列表加载失败，请检查后端服务。'))
      .finally(() => setTasksLoading(false))
  }, [activePage, tasks])

  useEffect(() => {
    if (activePage !== 'compliance' || compliance) {
      return
    }

    setComplianceLoading(true)
    setComplianceError(null)
    adminApi.loadCompliance()
      .then(setCompliance)
      .catch(() => setComplianceError('合规配置加载失败，请检查后端服务。'))
      .finally(() => setComplianceLoading(false))
  }, [activePage, compliance])

  const tableState = createTableState({
    pageKey: activePage,
    pageEmptyText: page.emptyText,
    booksResult,
    isBooksLoading,
    booksError,
    chaptersResult,
    isChaptersLoading,
    chaptersError,
    glossaryResult,
    isGlossaryLoading,
    glossaryError,
    auditResult,
    isAuditLoading,
    auditError,
    recommendationsResult,
    isRecommendationsLoading,
    recommendationsError,
    sites,
    isSitesLoading,
    sitesError,
    tasks,
    isTasksLoading,
    tasksError,
    compliance,
    isComplianceLoading,
    complianceError,
  })

  return (
    <main className="shell">
      <aside className="sidebar">
        <h1>HappyNovel</h1>
        <nav aria-label="后台导航">
          {adminNavigation.map((item) => (
            <button
              className={item.key === activePage ? 'active' : ''}
              key={item.key}
              type="button"
              onClick={() => setActivePage(item.key)}
            >
              {item.label}
            </button>
          ))}
        </nav>
      </aside>
      <section className="content">
        <header className="topbar">
          <div>
            <p>运营后台</p>
            <strong>{page.title}</strong>
          </div>
          <span>管理员：admin</span>
        </header>
        <LoginPanel />
        <ManagementPage
          pageKey={activePage}
          tableState={tableState}
          complianceConfigCards={compliance?.configCards ?? []}
          siteCreateForm={(
            <SiteQuickCreateForm
              isSubmitting={isSiteSubmitting}
              onSubmit={(request) => {
                setSiteSubmitting(true)
                setSitesError(null)
                adminApi.createSite(request)
                  .then((site) => setSites((current) => [...(current ?? []), site]))
                  .catch(() => setSitesError('站点保存失败，请检查域名和解析规则。'))
                  .finally(() => setSiteSubmitting(false))
              }}
            />
          )}
          glossaryCreateForm={(
            <GlossaryQuickCreateForm
              isSubmitting={isGlossarySubmitting}
              onSubmit={(request) => {
                setGlossarySubmitting(true)
                setGlossaryError(null)
                adminApi.createGlossaryTerm(request)
                  .then(setGlossaryResult)
                  .catch(() => setGlossaryError('术语保存失败，请检查书籍 ID 和后端服务。'))
                  .finally(() => setGlossarySubmitting(false))
              }}
            />
          )}
        />
      </section>
    </main>
  )
}

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)

function createTableState({
  pageKey,
  pageEmptyText,
  booksResult,
  isBooksLoading,
  booksError,
  chaptersResult,
  isChaptersLoading,
  chaptersError,
  glossaryResult,
  isGlossaryLoading,
  glossaryError,
  auditResult,
  isAuditLoading,
  auditError,
  recommendationsResult,
  isRecommendationsLoading,
  recommendationsError,
  sites,
  isSitesLoading,
  sitesError,
  tasks,
  isTasksLoading,
  tasksError,
  compliance,
  isComplianceLoading,
  complianceError,
}: {
  pageKey: AdminPageKey
  pageEmptyText: string
  booksResult: AdminBooksResult | null
  isBooksLoading: boolean
  booksError: string | null
  chaptersResult: AdminChaptersResult | null
  isChaptersLoading: boolean
  chaptersError: string | null
  glossaryResult: AdminGlossaryResult | null
  isGlossaryLoading: boolean
  glossaryError: string | null
  auditResult: AdminAuditResult | null
  isAuditLoading: boolean
  auditError: string | null
  recommendationsResult: AdminRecommendationsResult | null
  isRecommendationsLoading: boolean
  recommendationsError: string | null
  sites: AdminSiteRow[] | null
  isSitesLoading: boolean
  sitesError: string | null
  tasks: AdminTaskRow[] | null
  isTasksLoading: boolean
  tasksError: string | null
  compliance: AdminComplianceResult | null
  isComplianceLoading: boolean
  complianceError: string | null
}): RemoteTableState {
  if (pageKey === 'books') {
    return {
      rows: (booksResult?.books ?? []).map((book: AdminBookRow) => ({
        id: book.id,
        cells: bookRowCells(book),
      })),
      isLoading: isBooksLoading,
      loadingText: '正在加载书籍列表...',
      error: booksError,
      emptyText: booksResult?.emptyText ?? pageEmptyText,
    }
  }

  if (pageKey === 'chapters') {
    return {
      rows: (chaptersResult?.chapters ?? []).map((chapter: AdminChapterRow) => ({
        id: chapter.id,
        cells: chapterRowCells(chapter),
      })),
      isLoading: isChaptersLoading,
      loadingText: '正在加载章节列表...',
      error: chaptersError,
      emptyText: chaptersResult?.emptyText ?? pageEmptyText,
    }
  }

  if (pageKey === 'glossary') {
    return {
      rows: (glossaryResult?.terms ?? []).map((term: AdminGlossaryTermRow) => ({
        id: term.id,
        cells: glossaryRowCells(term),
      })),
      isLoading: isGlossaryLoading,
      loadingText: '正在加载术语列表...',
      error: glossaryError,
      emptyText: glossaryResult?.emptyText ?? pageEmptyText,
    }
  }

  if (pageKey === 'audit') {
    return {
      rows: (auditResult?.entries ?? []).map((entry: AdminAuditRow) => ({
        id: entry.id,
        cells: auditRowCells(entry),
      })),
      isLoading: isAuditLoading,
      loadingText: '正在加载审计日志...',
      error: auditError,
      emptyText: auditResult?.emptyText ?? pageEmptyText,
    }
  }

  if (pageKey === 'recommendations') {
    return {
      rows: (recommendationsResult?.items ?? []).map((item: AdminRecommendationRow) => ({
        id: item.id,
        cells: recommendationRowCells(item),
      })),
      isLoading: isRecommendationsLoading,
      loadingText: '正在加载分类推荐...',
      error: recommendationsError,
      emptyText: recommendationsResult?.emptyText ?? pageEmptyText,
    }
  }

  if (pageKey === 'sites') {
    return {
      rows: (sites ?? []).map((site) => ({
        id: site.id,
        cells: siteRowCells(site),
      })),
      isLoading: isSitesLoading,
      loadingText: '正在加载站点列表...',
      error: sitesError,
      emptyText: pageEmptyText,
    }
  }

  if (pageKey === 'tasks') {
    return {
      rows: (tasks ?? []).map((task) => ({
        id: task.id,
        cells: taskRowCells(task),
      })),
      isLoading: isTasksLoading,
      loadingText: '正在加载任务列表...',
      error: tasksError,
      emptyText: pageEmptyText,
    }
  }

  if (pageKey === 'compliance') {
    return {
      rows: (compliance?.complaints ?? []).map((complaint) => ({
        id: complaint.id,
        cells: complaintRowCells(complaint),
      })),
      isLoading: isComplianceLoading,
      loadingText: '正在加载合规配置...',
      error: complianceError,
      emptyText: compliance?.emptyText ?? pageEmptyText,
    }
  }

  return idleTableState(pageEmptyText)
}
