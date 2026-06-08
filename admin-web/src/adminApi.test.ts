import { describe, expect, it } from 'vitest'
import { createAdminApi } from './adminApi'

describe('admin api client', () => {
  it('loads admin book rows from backend response', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url) => {
        expect(url).toBe('http://localhost:8080/api/admin/books?limit=50')
        return new Response(JSON.stringify({
          emptyText: '暂无书籍，请添加小说来源 URL。',
          books: [
            {
              id: 'book-admin-1',
              title: 'Dragon Gate',
              author: 'Happy Novel Team',
              status: 'ongoing',
              latestChapterTitle: 'Chapter 2: The Trial',
              updatedAt: '2026-06-08T00:00:00Z',
            },
          ],
        }))
      },
    })

    const response = await api.listBooks()

    expect(response.emptyText).toBe('暂无书籍，请添加小说来源 URL。')
    expect(response.books).toEqual([
      {
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
      },
    ])
  })

  it('loads crawling sites from backend response', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url) => {
        expect(url).toBe('http://localhost:8080/api/admin/crawling/sites')
        return new Response(JSON.stringify([
          {
            id: 'site-1',
            name: '示例站点',
            baseDomain: 'https://novels.example.com',
            enabled: true,
            rateLimitPerMinute: 30,
            maxConcurrency: 2,
          },
        ]))
      },
    })

    const sites = await api.listSites()

    expect(sites).toEqual([
      {
        id: 'site-1',
        name: '示例站点',
        baseDomain: 'https://novels.example.com',
        enabledStatus: '启用',
        rateLimitLabel: '30 次/分钟',
        maxConcurrency: '2',
        lastFailureReason: '-',
      },
    ])
  })

  it('loads crawling tasks from backend response', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url) => {
        expect(url).toBe('http://localhost:8080/api/admin/crawling/tasks')
        return new Response(JSON.stringify([
          {
            id: 'task-1',
            type: 'CRAWL_BOOK',
            status: 'SUCCEEDED',
            targetId: 'source-1',
            retryCount: 0,
            failureReason: null,
            chaptersFound: 2,
          },
        ]))
      },
    })

    const tasks = await api.listTasks()

    expect(tasks).toEqual([
      {
        id: 'task-1',
        type: 'CRAWL_BOOK',
        status: 'SUCCEEDED',
        targetId: 'source-1',
        retryCount: '0',
        failureReason: '-',
        duration: '-',
      },
    ])
  })

  it('loads compliance config and complaint rows from backend response', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url) => {
        expect(url).toBe('http://localhost:8080/api/admin/compliance')
        return new Response(JSON.stringify({
          config: {
            privacyPolicyTitle: 'HappyNovel Privacy Policy',
            privacyPolicyUrl: 'https://example.com/privacy',
            termsTitle: 'HappyNovel Terms of Service',
            termsUrl: 'https://example.com/terms',
            adDisclosureEnabled: true,
            adDisclosureText: 'This app may show ads.',
          },
          complaints: [],
          emptyText: '暂无版权投诉记录',
        }))
      },
    })

    const compliance = await api.loadCompliance()

    expect(compliance.configCards).toEqual([
      '隐私政策：HappyNovel Privacy Policy',
      '服务条款：HappyNovel Terms of Service',
      '广告披露：已启用',
    ])
    expect(compliance.complaints).toEqual([])
    expect(compliance.emptyText).toBe('暂无版权投诉记录')
  })
})
