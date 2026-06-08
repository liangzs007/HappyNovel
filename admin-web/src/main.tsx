import React, { useEffect, useState } from 'react'
import { createRoot } from 'react-dom/client'
import {
  createAdminApi,
  type AdminBookRow,
  type AdminBooksResult,
  type AdminComplianceResult,
  type AdminSiteRow,
  type AdminTaskRow,
} from './adminApi'
import {
  adminNavigation,
  adminPages,
  bookRowCells,
  complaintRowCells,
  dashboardMetrics,
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
}: {
  pageKey: AdminPageKey
  tableState: RemoteTableState
  complianceConfigCards: string[]
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
  const [sites, setSites] = useState<AdminSiteRow[] | null>(null)
  const [isSitesLoading, setSitesLoading] = useState(false)
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
