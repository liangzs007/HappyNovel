import React, { useState } from 'react'
import { createRoot } from 'react-dom/client'
import { adminNavigation, adminPages, dashboardMetrics, type AdminPageKey } from './adminModel'
import './styles.css'

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

function ManagementPage({ pageKey }: { pageKey: AdminPageKey }) {
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
            <tr>
              <td colSpan={page.tableColumns.length} className="empty-state">{page.emptyText}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  )
}

function App() {
  const [activePage, setActivePage] = useState<AdminPageKey>('dashboard')
  const page = adminPages[activePage]

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
        <ManagementPage pageKey={activePage} />
      </section>
    </main>
  )
}

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
