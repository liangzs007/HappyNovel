import React, { useEffect, useState } from 'react'
import { createRoot } from 'react-dom/client'
import { createAdminApi, type AdminBooksResult } from './adminApi'
import { adminNavigation, adminPages, bookRowCells, dashboardMetrics, type AdminPageKey } from './adminModel'
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
  booksResult,
  isBooksLoading,
  booksError,
}: {
  pageKey: AdminPageKey
  booksResult: AdminBooksResult | null
  isBooksLoading: boolean
  booksError: string | null
}) {
  const page = adminPages[pageKey]
  const isBooksPage = pageKey === 'books'
  const bookRows = booksResult?.books ?? []

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
            {isBooksPage && isBooksLoading ? (
              <tr>
                <td colSpan={page.tableColumns.length} className="empty-state">正在加载书籍列表...</td>
              </tr>
            ) : null}
            {isBooksPage && booksError ? (
              <tr>
                <td colSpan={page.tableColumns.length} className="empty-state">{booksError}</td>
              </tr>
            ) : null}
            {isBooksPage && !isBooksLoading && !booksError && bookRows.length > 0
              ? bookRows.map((book) => (
                <tr key={book.id}>
                  {bookRowCells(book).map((cell, index) => <td key={`${book.id}-${index}`}>{cell}</td>)}
                </tr>
              ))
              : null}
            {!isBooksPage || (!isBooksLoading && !booksError && bookRows.length === 0) ? (
              <tr>
                <td colSpan={page.tableColumns.length} className="empty-state">
                  {isBooksPage ? booksResult?.emptyText ?? page.emptyText : page.emptyText}
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </div>
    </section>
  )
}

function App() {
  const [activePage, setActivePage] = useState<AdminPageKey>('dashboard')
  const [booksResult, setBooksResult] = useState<AdminBooksResult | null>(null)
  const [isBooksLoading, setBooksLoading] = useState(false)
  const [booksError, setBooksError] = useState<string | null>(null)
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
          booksResult={booksResult}
          isBooksLoading={isBooksLoading}
          booksError={booksError}
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
