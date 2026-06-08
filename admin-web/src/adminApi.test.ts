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
})
