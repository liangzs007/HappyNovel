import { describe, expect, it } from 'vitest'
import {
  adminNavigation,
  adminPages,
  bookRowCells,
  chapterRowCells,
  complaintRowCells,
  dashboardMetrics,
  glossaryRowCells,
  siteRowCells,
  taskRowCells,
} from './adminModel'

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

  it('maps admin book rows to Chinese table cells', () => {
    expect(bookRowCells({
      id: 'book-admin-1',
      title: 'Dragon Gate',
      author: 'Happy Novel Team',
      sourceSite: '-',
      categories: '-',
      status: 'ongoing',
      publishStatus: '已发布',
      recommendationWeight: '0',
      latestChapterTitle: 'Chapter 2: The Trial',
      updatedAt: '2026-06-08T00:00:00Z',
    })).toEqual([
      'Dragon Gate',
      'Happy Novel Team',
      '-',
      '-',
      'ongoing',
      '已发布',
      '0',
      '查看',
    ])
  })

  it('maps crawling site rows to Chinese table cells', () => {
    expect(siteRowCells({
      id: 'site-1',
      name: '示例站点',
      baseDomain: 'https://novels.example.com',
      enabledStatus: '启用',
      rateLimitLabel: '30 次/分钟',
      maxConcurrency: '2',
      lastFailureReason: '-',
    })).toEqual([
      '示例站点',
      'https://novels.example.com',
      '启用',
      '30 次/分钟',
      '2',
      '-',
      '编辑',
    ])
  })

  it('maps crawling task rows to Chinese table cells', () => {
    expect(taskRowCells({
      id: 'task-1',
      type: 'CRAWL_BOOK',
      status: 'SUCCEEDED',
      targetId: 'source-1',
      retryCount: '0',
      failureReason: '-',
      duration: '-',
    })).toEqual([
      'task-1',
      'CRAWL_BOOK',
      'SUCCEEDED',
      'source-1',
      '0',
      '-',
      '-',
      '查看',
    ])
  })

  it('maps copyright complaint rows to Chinese table cells', () => {
    expect(complaintRowCells({
      id: 'complaint-1',
      source: 'email',
      bookTitle: 'Dragon Gate',
      chapterTitle: 'Chapter 1',
      status: 'OPEN',
      note: '等待处理',
    })).toEqual([
      'email',
      'Dragon Gate',
      'Chapter 1',
      'OPEN',
      '等待处理',
      '查看',
    ])
  })

  it('maps chapter rows to Chinese table cells', () => {
    expect(chapterRowCells({
      id: 'chapter-seed-1',
      order: '1',
      title: 'Chapter 1: Azure Cloud Sect',
      crawlStatus: '已抓取',
      cleanStatus: '已清洗',
      translationStatus: '已翻译',
      publishStatus: '已发布',
    })).toEqual([
      '1',
      'Chapter 1: Azure Cloud Sect',
      '已抓取',
      '已清洗',
      '已翻译',
      '已发布',
      '查看',
    ])
  })

  it('maps glossary rows to Chinese table cells', () => {
    expect(glossaryRowCells({
      id: 'term-1',
      sourceTerm: '青云宗',
      translatedTerm: 'Azure Cloud Sect',
      type: 'ORGANIZATION',
      enabledStatus: '启用',
      updatedAt: '-',
    })).toEqual([
      '青云宗',
      'Azure Cloud Sect',
      'ORGANIZATION',
      '启用',
      '-',
      '编辑',
    ])
  })
})
