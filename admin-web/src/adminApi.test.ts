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

  it('unpublishes book through backend API', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url, init) => {
        expect(url).toBe('http://localhost:8080/api/admin/books/book-seed-1/unpublish')
        expect(init?.method).toBe('POST')
        return new Response(JSON.stringify({
          bookId: 'book-seed-1',
          publishStatus: '已下架',
        }))
      },
    })

    const response = await api.unpublishBook('book-seed-1')

    expect(response).toEqual({
      bookId: 'book-seed-1',
      publishStatus: '已下架',
    })
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

  it('creates crawling site through backend API', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url, init) => {
        expect(url).toBe('http://localhost:8080/api/admin/crawling/sites')
        expect(init?.method).toBe('POST')
        expect(JSON.parse(String(init?.body))).toEqual({
          name: '示例站点',
          baseDomain: 'https://novels.example.com',
          rateLimitPerMinute: 30,
          maxConcurrency: 2,
          chapterListSelector: '.chapter-list a',
          chapterBodySelector: '.chapter-content',
          adBlocklist: ['请收藏本站', '最新网址'],
        })
        return new Response(JSON.stringify({
          id: 'site-1',
          name: '示例站点',
          baseDomain: 'https://novels.example.com',
          enabled: true,
          rateLimitPerMinute: 30,
          maxConcurrency: 2,
        }))
      },
    })

    const site = await api.createSite({
      name: '示例站点',
      baseDomain: 'https://novels.example.com',
      rateLimitPerMinute: 30,
      maxConcurrency: 2,
      chapterListSelector: '.chapter-list a',
      chapterBodySelector: '.chapter-content',
      adBlocklist: ['请收藏本站', '最新网址'],
    })

    expect(site).toEqual({
      id: 'site-1',
      name: '示例站点',
      baseDomain: 'https://novels.example.com',
      enabledStatus: '启用',
      rateLimitLabel: '30 次/分钟',
      maxConcurrency: '2',
      lastFailureReason: '-',
    })
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

  it('retries crawling task through backend API', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url, init) => {
        expect(url).toBe('http://localhost:8080/api/admin/crawling/tasks/task-1/retry')
        expect(init?.method).toBe('POST')
        expect(JSON.parse(String(init?.body))).toEqual({ html: '<div class="chapter-list"></div>' })
        return new Response(JSON.stringify({
          id: 'task-1-retry',
          type: 'CRAWL_BOOK',
          status: 'SUCCEEDED',
          targetId: 'source-1',
          retryCount: 1,
          failureReason: null,
        }))
      },
    })

    const task = await api.retryTask('task-1', '<div class="chapter-list"></div>')

    expect(task).toEqual({
      id: 'task-1-retry',
      type: 'CRAWL_BOOK',
      status: 'SUCCEEDED',
      targetId: 'source-1',
      retryCount: '1',
      failureReason: '-',
      duration: '-',
    })
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
          adConfig: {
            enabled: true,
            readerBannerEnabled: true,
            interstitialEveryChapters: 5,
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
      '广告开关：已启用',
      '阅读页横幅：已启用',
      '插屏频率：每 5 章',
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
          adConfig: {
            enabled: true,
            readerBannerEnabled: true,
            interstitialEveryChapters: 5,
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
      '广告开关：已启用',
      '阅读页横幅：已启用',
      '插屏频率：每 5 章',
    ])
  })

  it('updates ad config through backend API', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url, init) => {
        expect(url).toBe('http://localhost:8080/api/admin/compliance/ad-config')
        expect(init?.method).toBe('PUT')
        expect(JSON.parse(String(init?.body))).toEqual({
          enabled: false,
          readerBannerEnabled: false,
          interstitialEveryChapters: 8,
        })
        return new Response(JSON.stringify({
          config: {
            privacyPolicyTitle: 'HappyNovel Privacy Policy',
            privacyPolicyUrl: 'https://example.com/privacy',
            termsTitle: 'HappyNovel Terms of Service',
            termsUrl: 'https://example.com/terms',
            adDisclosureEnabled: true,
            adDisclosureText: 'This app may show ads.',
          },
          adConfig: {
            enabled: false,
            readerBannerEnabled: false,
            interstitialEveryChapters: 8,
          },
          complaints: [],
          emptyText: '暂无版权投诉记录',
        }))
      },
    })

    const compliance = await api.updateAdConfig({
      enabled: false,
      readerBannerEnabled: false,
      interstitialEveryChapters: 8,
    })

    expect(compliance.configCards).toContain('广告开关：未启用')
    expect(compliance.configCards).toContain('阅读页横幅：未启用')
    expect(compliance.configCards).toContain('插屏频率：每 8 章')
  })

  it('creates glossary term through backend API', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url, init) => {
        expect(url).toBe('http://localhost:8080/api/admin/glossary')
        expect(init?.method).toBe('POST')
        expect(JSON.parse(String(init?.body))).toEqual({
          bookId: 'book-seed-1',
          sourceTerm: '林辰',
          translatedTerm: 'Lin Chen',
          type: 'PERSON',
          description: '主角姓名',
        })
        return new Response(JSON.stringify({
          emptyText: '暂无术语，请为书籍添加术语表。',
          terms: [
            {
              id: 'term-1',
              sourceTerm: '林辰',
              translatedTerm: 'Lin Chen',
              type: 'PERSON',
              enabledStatus: '启用',
            },
          ],
        }))
      },
    })

    const response = await api.createGlossaryTerm({
      bookId: 'book-seed-1',
      sourceTerm: '林辰',
      translatedTerm: 'Lin Chen',
      type: 'PERSON',
      description: '主角姓名',
    })

    expect(response.terms).toEqual([
      {
        id: 'term-1',
        sourceTerm: '林辰',
        translatedTerm: 'Lin Chen',
        type: 'PERSON',
        enabledStatus: '启用',
        updatedAt: '-',
      },
    ])
  })

  it('creates copyright complaint through backend API', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url, init) => {
        expect(url).toBe('http://localhost:8080/api/admin/compliance/complaints')
        expect(init?.method).toBe('POST')
        expect(JSON.parse(String(init?.body))).toEqual({
          source: 'email',
          bookTitle: '测试书籍',
          chapterTitle: '第一章',
          note: '权利人要求下架章节',
        })
        return new Response(JSON.stringify({
          config: {
            privacyPolicyTitle: 'HappyNovel Privacy Policy',
            privacyPolicyUrl: 'https://example.com/privacy',
            termsTitle: 'HappyNovel Terms of Service',
            termsUrl: 'https://example.com/terms',
            adDisclosureEnabled: true,
            adDisclosureText: 'This app may show ads.',
          },
          adConfig: {
            enabled: true,
            readerBannerEnabled: true,
            interstitialEveryChapters: 5,
          },
          complaints: [
            {
              id: 'complaint-1',
              source: 'email',
              bookTitle: '测试书籍',
              chapterTitle: '第一章',
              status: '待处理',
              note: '权利人要求下架章节',
            },
          ],
          emptyText: '暂无版权投诉记录',
        }))
      },
    })

    const compliance = await api.createComplaint({
      source: 'email',
      bookTitle: '测试书籍',
      chapterTitle: '第一章',
      note: '权利人要求下架章节',
    })

    expect(compliance.complaints).toEqual([
      {
        id: 'complaint-1',
        source: 'email',
        bookTitle: '测试书籍',
        chapterTitle: '第一章',
        status: '待处理',
        note: '权利人要求下架章节',
      },
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

  it('hides chapter through backend API', async () => {
    const api = createAdminApi({
      baseUrl: 'http://localhost:8080',
      fetcher: async (url, init) => {
        expect(url).toBe('http://localhost:8080/api/admin/chapters/chapter-seed-1/hide')
        expect(init?.method).toBe('POST')
        return new Response(JSON.stringify({
          chapterId: 'chapter-seed-1',
          publishStatus: '已隐藏',
        }))
      },
    })

    const response = await api.hideChapter('chapter-seed-1')

    expect(response).toEqual({
      chapterId: 'chapter-seed-1',
      publishStatus: '已隐藏',
    })
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
