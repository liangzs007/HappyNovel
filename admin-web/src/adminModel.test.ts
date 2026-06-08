import { describe, expect, it } from 'vitest'
import { adminNavigation, adminPages, dashboardMetrics } from './adminModel'

describe('admin console model', () => {
  it('defines all MVP navigation entries in Chinese', () => {
    expect(adminNavigation.map((item) => item.label)).toEqual([
      '仪表盘',
      '站点管理',
      '书籍管理',
      '章节管理',
      '术语表',
      '任务管理',
      '分类推荐',
      '合规发布',
      '审计日志',
    ])
  })

  it('defines dashboard metrics for operations status', () => {
    expect(dashboardMetrics.map((metric) => metric.label)).toContain('失败任务')
    expect(dashboardMetrics.map((metric) => metric.label)).toContain('待确认术语')
  })

  it('defines table columns and empty states for management pages', () => {
    expect(adminPages.sites.tableColumns).toContain('站点名称')
    expect(adminPages.books.tableColumns).toContain('发布状态')
    expect(adminPages.chapters.tableColumns).toContain('翻译状态')
    expect(adminPages.tasks.tableColumns).toContain('失败原因')
    expect(adminPages.compliance.emptyText).toBe('暂无版权投诉记录')
  })
})
