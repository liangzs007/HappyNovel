export type AdminPageKey =
  | 'dashboard'
  | 'sites'
  | 'books'
  | 'chapters'
  | 'glossary'
  | 'tasks'
  | 'recommendations'
  | 'compliance'
  | 'audit'

export interface AdminNavItem {
  key: AdminPageKey
  label: string
}

export interface AdminMetric {
  label: string
  value: string
  tone: 'neutral' | 'success' | 'warning' | 'danger'
}

export interface AdminPageDefinition {
  key: AdminPageKey
  title: string
  description: string
  primaryAction?: string
  tableColumns: string[]
  emptyText: string
}

export interface AdminBookTableRow {
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

export interface AdminSiteTableRow {
  id: string
  name: string
  baseDomain: string
  enabledStatus: string
  rateLimitLabel: string
  maxConcurrency: string
  lastFailureReason: string
}

export interface AdminTaskTableRow {
  id: string
  type: string
  status: string
  targetId: string
  retryCount: string
  failureReason: string
  duration: string
}

export interface AdminComplaintTableRow {
  id: string
  source: string
  bookTitle: string
  chapterTitle: string
  status: string
  note: string
}

export const adminNavigation: AdminNavItem[] = [
  { key: 'dashboard', label: '仪表盘' },
  { key: 'sites', label: '站点管理' },
  { key: 'books', label: '书籍管理' },
  { key: 'chapters', label: '章节管理' },
  { key: 'glossary', label: '术语表' },
  { key: 'tasks', label: '任务管理' },
  { key: 'recommendations', label: '分类推荐' },
  { key: 'compliance', label: '合规发布' },
  { key: 'audit', label: '审计日志' },
]

export const dashboardMetrics: AdminMetric[] = [
  { label: '书籍总数', value: '0', tone: 'neutral' },
  { label: '章节总数', value: '0', tone: 'neutral' },
  { label: '今日抓取任务', value: '0', tone: 'success' },
  { label: '翻译任务', value: '0', tone: 'success' },
  { label: '失败任务', value: '0', tone: 'danger' },
  { label: '异常章节', value: '0', tone: 'warning' },
  { label: '待确认术语', value: '0', tone: 'warning' },
]

export const adminPages: Record<AdminPageKey, AdminPageDefinition> = {
  dashboard: {
    key: 'dashboard',
    title: '仪表盘',
    description: '查看书籍、章节、抓取、翻译和待处理事项的整体状态。',
    tableColumns: ['模块', '状态', '数量', '最近更新时间'],
    emptyText: '暂无运营数据',
  },
  sites: {
    key: 'sites',
    title: '站点管理',
    description: '配置公开小说站点、解析规则、请求限速和并发策略。',
    primaryAction: '新增站点',
    tableColumns: ['站点名称', '基础域名', '启用状态', '请求限速', '最大并发', '最近失败原因', '操作'],
    emptyText: '暂无站点，请先添加站点配置。',
  },
  books: {
    key: 'books',
    title: '书籍管理',
    description: '维护指定小说来源、分类标签、发布状态、推荐权重和广告开关。',
    primaryAction: '添加书籍',
    tableColumns: ['书名', '作者', '来源站点', '分类标签', '连载状态', '发布状态', '推荐权重', '操作'],
    emptyText: '暂无书籍，请添加小说来源 URL。',
  },
  chapters: {
    key: 'chapters',
    title: '章节管理',
    description: '查看章节抓取、清洗、翻译和发布状态，处理异常章节。',
    tableColumns: ['章节序号', '章节标题', '抓取状态', '清洗状态', '翻译状态', '发布状态', '操作'],
    emptyText: '暂无章节，请先触发书籍抓取。',
  },
  glossary: {
    key: 'glossary',
    title: '术语表',
    description: '维护人物、地点、组织、技能、物品和称谓的固定译名。',
    primaryAction: '新增术语',
    tableColumns: ['中文原词', '英文译名', '类型', '启用状态', '更新时间', '操作'],
    emptyText: '暂无术语，请为书籍添加术语表。',
  },
  tasks: {
    key: 'tasks',
    title: '任务管理',
    description: '观察抓取、清洗、翻译任务状态，查看失败原因并重试。',
    tableColumns: ['任务 ID', '类型', '状态', '目标对象', '重试次数', '失败原因', '耗时', '操作'],
    emptyText: '暂无任务。你可以从书籍详情页触发抓取或翻译。',
  },
  recommendations: {
    key: 'recommendations',
    title: '分类推荐',
    description: '维护分类、标签、推荐位和排序权重。',
    primaryAction: '新增推荐位',
    tableColumns: ['名称', '类型', '绑定书籍', '排序权重', '启用状态', '操作'],
    emptyText: '暂无推荐配置。',
  },
  compliance: {
    key: 'compliance',
    title: '合规发布',
    description: '配置隐私政策、服务条款、广告披露，处理版权投诉和下架记录。',
    primaryAction: '新增投诉记录',
    tableColumns: ['投诉来源', '关联书籍', '关联章节', '处理状态', '处理备注', '操作'],
    emptyText: '暂无版权投诉记录',
  },
  audit: {
    key: 'audit',
    title: '审计日志',
    description: '查看后台关键操作记录，包括下架、重翻译、修改术语和站点配置。',
    tableColumns: ['操作人', '操作类型', '目标对象', '摘要', '操作时间'],
    emptyText: '暂无审计记录。',
  },
}

export function bookRowCells(book: AdminBookTableRow): string[] {
  return [
    book.title,
    book.author,
    book.sourceSite,
    book.categories,
    book.status,
    book.publishStatus,
    book.recommendationWeight,
    '查看',
  ]
}

export function siteRowCells(site: AdminSiteTableRow): string[] {
  return [
    site.name,
    site.baseDomain,
    site.enabledStatus,
    site.rateLimitLabel,
    site.maxConcurrency,
    site.lastFailureReason,
    '编辑',
  ]
}

export function taskRowCells(task: AdminTaskTableRow): string[] {
  return [
    task.id,
    task.type,
    task.status,
    task.targetId,
    task.retryCount,
    task.failureReason,
    task.duration,
    '查看',
  ]
}

export function complaintRowCells(complaint: AdminComplaintTableRow): string[] {
  return [
    complaint.source,
    complaint.bookTitle,
    complaint.chapterTitle,
    complaint.status,
    complaint.note,
    '查看',
  ]
}
