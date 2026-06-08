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

  it('updates compliance config through backend API', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url, init) => {
        expect(url).toBe('http://localhost:8080/api/admin/compliance')
        expect(init?.method).toBe('PUT')
        expect(JSON.parse(String(init?.body))).toEqual({
          privacyPolicyTitle: 'Updated Privacy',
          privacyPolicyUrl: 'https://example.com/privacy',
          termsTitle: 'Updated Terms',
          termsUrl: 'https://example.com/terms',
          adDisclosureEnabled: false,
          adDisclosureText: 'Ads are disabled.',
        })
        return new Response(JSON.stringify({
          config: {
            privacyPolicyTitle: 'Updated Privacy',
            privacyPolicyUrl: 'https://example.com/privacy',
            termsTitle: 'Updated Terms',
            termsUrl: 'https://example.com/terms',
            adDisclosureEnabled: false,
            adDisclosureText: 'Ads are disabled.',
          },
          complaints: [],
          emptyText: '暂无版权投诉记录',
        }))
      },
    })

    const compliance = await api.updateCompliance({
      privacyPolicyTitle: 'Updated Privacy',
      privacyPolicyUrl: 'https://example.com/privacy',
      termsTitle: 'Updated Terms',
      termsUrl: 'https://example.com/terms',
      adDisclosureEnabled: false,
      adDisclosureText: 'Ads are disabled.',
    })

    expect(compliance.configCards).toEqual([
      '隐私政策：Updated Privacy',
      '服务条款：Updated Terms',
      '广告披露：未启用',
    ])
  })

  it('loads admin chapter rows from backend response', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url) => {
        expect(url).toBe('http://localhost:8080/api/admin/chapters?bookId=book-seed-1')
        return new Response(JSON.stringify({
          emptyText: '暂无章节，请先触发书籍抓取。',
          chapters: [
            {
              id: 'chapter-seed-1',
              order: 1,
              title: 'Chapter 1: Azure Cloud Sect',
              crawlStatus: '已抓取',
              cleanStatus: '已清洗',
              translationStatus: '已翻译',
              publishStatus: '已发布',
              updatedAt: '2026-06-08T00:00:00Z',
            },
          ],
        }))
      },
    })

    const response = await api.listChapters()

    expect(response.emptyText).toBe('暂无章节，请先触发书籍抓取。')
    expect(response.chapters).toEqual([
      {
        id: 'chapter-seed-1',
        order: '1',
        title: 'Chapter 1: Azure Cloud Sect',
        crawlStatus: '已抓取',
        cleanStatus: '已清洗',
        translationStatus: '已翻译',
        publishStatus: '已发布',
      },
    ])
  })

  it('loads admin glossary rows from backend response', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url) => {
        expect(url).toBe('http://localhost:8080/api/admin/glossary')
        return new Response(JSON.stringify({
          emptyText: '暂无术语，请为书籍添加术语表。',
          terms: [
            {
              id: 'term-1',
              bookId: 'book-seed-1',
              sourceTerm: '青云宗',
              translatedTerm: 'Azure Cloud Sect',
              type: 'ORGANIZATION',
              enabledStatus: '启用',
              description: '主角初入的宗门',
            },
          ],
        }))
      },
    })

    const response = await api.listGlossaryTerms()

    expect(response.emptyText).toBe('暂无术语，请为书籍添加术语表。')
    expect(response.terms).toEqual([
      {
        id: 'term-1',
        sourceTerm: '青云宗',
        translatedTerm: 'Azure Cloud Sect',
        type: 'ORGANIZATION',
        enabledStatus: '启用',
        updatedAt: '-',
      },
    ])
  })

  it('loads admin audit rows from backend response', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url) => {
        expect(url).toBe('http://localhost:8080/api/admin/audit')
        return new Response(JSON.stringify({
          emptyText: '暂无审计记录。',
          entries: [
            {
              id: 'audit-1',
              actor: 'admin',
              action: 'BOOK_TAKEDOWN',
              target: 'book:book-seed-1',
              summary: '下架测试书籍',
              createdAt: '2026-06-08T00:00:00Z',
            },
          ],
        }))
      },
    })

    const response = await api.listAuditLogs()

    expect(response.emptyText).toBe('暂无审计记录。')
    expect(response.entries).toEqual([
      {
        id: 'audit-1',
        actor: 'admin',
        action: 'BOOK_TAKEDOWN',
        target: 'book:book-seed-1',
        summary: '下架测试书籍',
        createdAt: '2026-06-08T00:00:00Z',
      },
    ])
  })

  it('loads admin recommendation rows from backend response', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url) => {
        expect(url).toBe('http://localhost:8080/api/admin/recommendations')
        return new Response(JSON.stringify({
          emptyText: '暂无推荐配置。',
          items: [
            {
              id: 'category-fantasy',
              name: 'Fantasy',
              type: '分类',
              boundBook: '-',
              sortWeight: '0',
              enabledStatus: '启用',
            },
          ],
        }))
      },
    })

    const response = await api.listRecommendations()

    expect(response.emptyText).toBe('暂无推荐配置。')
    expect(response.items).toEqual([
      {
        id: 'category-fantasy',
        name: 'Fantasy',
        type: '分类',
        boundBook: '-',
        sortWeight: '0',
        enabledStatus: '启用',
      },
    ])
  })
})
