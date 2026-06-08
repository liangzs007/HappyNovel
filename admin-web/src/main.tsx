import React from 'react'
import { createRoot } from 'react-dom/client'
import './styles.css'

function App() {
  return (
    <main className="shell">
      <aside className="sidebar">
        <h1>HappyNovel</h1>
        <nav>
          <a>仪表盘</a>
          <a>站点管理</a>
          <a>书籍管理</a>
          <a>任务管理</a>
        </nav>
      </aside>
      <section className="content">
        <header>
          <p>运营后台</p>
          <strong>小说平台 MVP</strong>
        </header>
        <div className="panel">
          <h2>工程骨架已创建</h2>
          <p>后续阶段将在这里实现站点、书籍、章节、术语和任务管理。</p>
        </div>
      </section>
    </main>
  )
}

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
