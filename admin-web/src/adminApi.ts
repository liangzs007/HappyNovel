export interface AdminBookRow {
  id: string
  title: string
  author: string
  sourceSite: string
  categories: string
  status: string
  publishStatus: string
  recommendationWeight: string
  latestChapterTitle: string
  updatedAt: string
}

export interface AdminBooksResult {
  books: AdminBookRow[]
  emptyText: string
}

interface BackendBookSummary {
  id: string
  title: string
  author?: string
  status?: string
  latestChapterTitle?: string
  updatedAt?: string
}

interface BackendAdminBooksResponse {
  books: BackendBookSummary[]
  emptyText: string
}

interface AdminApiOptions {
  baseUrl?: string
  fetcher?: typeof fetch
}

export function createAdminApi(options: AdminApiOptions = {}) {
  const baseUrl = options.baseUrl ?? ''
  const fetcher = options.fetcher ?? fetch

  return {
    async listBooks(limit = 50): Promise<AdminBooksResult> {
      const response = await fetcher(`${baseUrl}/api/admin/books?limit=${limit}`)
      if (!response.ok) {
        throw new Error(`后台书籍列表加载失败：${response.status}`)
      }
      const payload = await response.json() as BackendAdminBooksResponse
      return {
        emptyText: payload.emptyText,
        books: payload.books.map(toBookRow),
      }
    },
  }
}

function toBookRow(book: BackendBookSummary): AdminBookRow {
  return {
    id: book.id,
    title: book.title,
    author: book.author ?? '-',
    sourceSite: '-',
    categories: '-',
    status: book.status ?? '-',
    publishStatus: '已发布',
    recommendationWeight: '0',
    latestChapterTitle: book.latestChapterTitle ?? '-',
    updatedAt: book.updatedAt ?? '-',
  }
}
