const MOCK_DELAY = 160

const MOCK_USER = {
  user_id: 'u_mock_001',
  openid: 'openid_mock_001',
  nickname: '小爪',
  avatar_url: 'https://dummyimage.com/120x120/5B8FF9/ffffff&text=C',
  couple_id: 'cp_mock_001',
  is_new_user: false,
  together_days: 520,
  bound_at: '2024-10-01T10:00:00Z'
}

const MOCK_PARTNER = {
  user_id: 'u_mock_002',
  nickname: '小印',
  avatar_url: 'https://dummyimage.com/120x120/F6BD16/ffffff&text=M'
}

const MOCK_CITY_LIGHTS = [
  { city_code: 'CN-11-BJ', city_name: '北京', province: '北京市', photo_count: 5, cover_thumbnail: '', latest_date: '2025-06-02' },
  { city_code: 'CN-53-KM', city_name: '昆明', province: '云南省', photo_count: 8, cover_thumbnail: '', latest_date: '2026-02-14' },
  { city_code: 'CN-53-DL', city_name: '大理', province: '云南省', photo_count: 4, cover_thumbnail: '', latest_date: '2025-10-08' },
  { city_code: 'CN-51-CD', city_name: '成都', province: '四川省', photo_count: 6, cover_thumbnail: '', latest_date: '2025-11-03' },
  { city_code: 'CN-44-GZ', city_name: '广州', province: '广东省', photo_count: 7, cover_thumbnail: '', latest_date: '2026-01-21' },
  { city_code: 'CN-33-HZ', city_name: '杭州', province: '浙江省', photo_count: 3, cover_thumbnail: '', latest_date: '2025-12-30' }
]

let inviteCode = 'LOVE-A3F9'
let inviteExpiresAt = '2026-12-31T23:59:59Z'

let albumSeed = 100
let todoSeed = 200

let albums = [
  { album_id: 'alb_001', type: 'city', title: '云南旅行', city_code: 'CN-53-KM', cover_thumbnail: '', photo_count: 56, created_at: '2026-02-14T18:10:00Z' },
  { album_id: 'alb_002', type: 'city', title: '成都周末', city_code: 'CN-51-CD', cover_thumbnail: '', photo_count: 31, created_at: '2025-11-03T20:20:00Z' },
  { album_id: 'alb_003', type: 'custom', title: '纪念日合集', city_code: null, cover_thumbnail: '', photo_count: 24, created_at: '2026-01-01T09:00:00Z' }
]

let todos = [
  { todo_id: 'td_001', title: '订清明出行机票', description: '', type: 'mine', status: 'pending', deadline: '2026-03-30T23:59:59Z', is_urging: false, created_by: 'u_mock_001', assignee_id: 'u_mock_001', completed_by: null, completed_at: null, created_at: '2026-03-01T10:00:00Z' },
  { todo_id: 'td_002', title: '整理旅行照片', description: '', type: 'shared', status: 'done', deadline: null, is_urging: false, created_by: 'u_mock_001', assignee_id: null, completed_by: 'u_mock_002', completed_at: '2026-03-10T22:00:00Z', created_at: '2026-03-05T09:20:00Z' },
  { todo_id: 'td_003', title: '提醒TA买生日礼物', description: '', type: 'partner', status: 'pending', deadline: '2026-04-08T23:59:59Z', is_urging: true, created_by: 'u_mock_001', assignee_id: 'u_mock_002', completed_by: null, completed_at: null, created_at: '2026-03-12T18:30:00Z' }
]

function ok(data, message = 'success') {
  return { code: 0, message, data }
}

function fail(message, code = 1) {
  return { code, message, data: null }
}

function parseQuery(url) {
  const queryIndex = url.indexOf('?')
  if (queryIndex < 0) return {}
  const queryString = url.slice(queryIndex + 1)
  const pairs = queryString.split('&').filter(Boolean)
  const query = {}
  pairs.forEach((pair) => {
    const [rawKey, rawValue] = pair.split('=')
    const key = decodeURIComponent(rawKey || '')
    const value = decodeURIComponent(rawValue || '')
    query[key] = value
  })
  return query
}

function pathOf(url) {
  return url.split('?')[0]
}

function needAuth(path) {
  return path !== '/auth/login' && path !== '/auth/refresh'
}

function toPage(list, page = 1, pageSize = 20) {
  const safePage = Math.max(1, Number(page) || 1)
  const safePageSize = Math.max(1, Number(pageSize) || 20)
  const start = (safePage - 1) * safePageSize
  return {
    items: list.slice(start, start + safePageSize),
    total: list.length,
    page: safePage,
    page_size: safePageSize
  }
}

function nowText() {
  return new Date().toISOString()
}

function route({ url, method, data, token }) {
  const path = pathOf(url)
  const upperMethod = (method || 'GET').toUpperCase()

  if (needAuth(path) && !token) {
    return fail('未登录或 Token 已失效', 1002)
  }

  if (path === '/auth/login' && upperMethod === 'POST') {
    const code = data && data.code
    if (!code) return fail('参数缺失或格式错误', 1001)
    return ok({
      access_token: `mock-token-${Date.now()}`,
      expires_in: 604800,
      user: {
        user_id: MOCK_USER.user_id,
        openid: MOCK_USER.openid,
        nickname: (data && data.nickname) || MOCK_USER.nickname,
        avatar_url: (data && data.avatar_url) || MOCK_USER.avatar_url,
        couple_id: MOCK_USER.couple_id,
        is_new_user: false
      }
    })
  }

  if (path === '/auth/refresh' && upperMethod === 'POST') {
    return ok({ access_token: `mock-token-${Date.now()}`, expires_in: 604800 })
  }

  if (path === '/users/me' && upperMethod === 'GET') {
    return ok({
      user_id: MOCK_USER.user_id,
      nickname: MOCK_USER.nickname,
      avatar_url: MOCK_USER.avatar_url,
      couple_id: MOCK_USER.couple_id,
      partner: MOCK_PARTNER,
      together_days: MOCK_USER.together_days,
      bound_at: MOCK_USER.bound_at
    })
  }

  if (path === '/users/me' && upperMethod === 'PUT') {
    MOCK_USER.nickname = (data && data.nickname) || MOCK_USER.nickname
    MOCK_USER.avatar_url = (data && data.avatar_url) || MOCK_USER.avatar_url
    return ok({
      user_id: MOCK_USER.user_id,
      nickname: MOCK_USER.nickname,
      avatar_url: MOCK_USER.avatar_url
    })
  }

  if (path === '/couples/invite' && upperMethod === 'POST') {
    inviteCode = `LOVE-${String(Math.floor(Math.random() * 9000) + 1000)}`
    inviteExpiresAt = new Date(Date.now() + 24 * 3600 * 1000).toISOString()
    return ok({ invite_code: inviteCode, expires_at: inviteExpiresAt })
  }

  if (path === '/couples/bind' && upperMethod === 'POST') {
    if (!data || !data.invite_code) return fail('参数缺失或格式错误', 1001)
    return ok({
      couple_id: MOCK_USER.couple_id,
      partner: MOCK_PARTNER,
      bound_at: nowText(),
      together_days: 0
    })
  }

  if (path === '/couples/bind' && upperMethod === 'DELETE') {
    return ok(null, '解绑成功')
  }

  if (path === '/upload/token' && upperMethod === 'POST') {
    return ok({
      upload_url: 'https://oss.example.com/photos/',
      upload_key: `photos/${MOCK_USER.user_id}/${Date.now()}.jpg`,
      sts_token: {
        access_key_id: 'STS.mock',
        access_key_secret: 'mock-secret',
        security_token: 'mock-security-token',
        expiration: new Date(Date.now() + 3600 * 1000).toISOString()
      },
      cdn_url: `https://cdn.example.com/photos/${MOCK_USER.user_id}/${Date.now()}.jpg`
    })
  }

  if (path === '/notifications/subscribe' && upperMethod === 'POST') {
    return ok(null, '订阅记录已保存')
  }

  const cityMatch = path.match(/^\/couples\/([^/]+)\/map\/cities$/)
  if (cityMatch && upperMethod === 'GET') {
    return ok({ cities: MOCK_CITY_LIGHTS, total_cities: MOCK_CITY_LIGHTS.length })
  }

  const albumListMatch = path.match(/^\/couples\/([^/]+)\/albums$/)
  if (albumListMatch && upperMethod === 'GET') {
    const query = parseQuery(url)
    const type = query.type || 'all'
    const page = query.page || 1
    const pageSize = query.page_size || 20
    const filtered = type === 'all' ? albums.slice() : albums.filter((a) => a.type === type)
    const result = toPage(filtered, page, pageSize)
    return ok({ albums: result.items, total: result.total, page: result.page, page_size: result.page_size })
  }

  if (albumListMatch && upperMethod === 'POST') {
    const title = data && String(data.title || '').trim()
    if (!title) return fail('参数缺失或格式错误', 1001)
    albumSeed += 1
    const item = {
      album_id: `alb_${albumSeed}`,
      type: 'custom',
      title,
      description: (data && data.description) || '',
      photo_count: 0,
      created_at: nowText()
    }
    albums = [item].concat(albums)
    return ok(item)
  }

  const albumDeleteMatch = path.match(/^\/couples\/([^/]+)\/albums\/([^/]+)$/)
  if (albumDeleteMatch && upperMethod === 'DELETE') {
    const albumId = albumDeleteMatch[2]
    const target = albums.find((a) => a.album_id === albumId)
    if (!target) return fail('资源不存在', 1004)
    if (target.type === 'city') return fail('资源冲突', 1005)
    albums = albums.filter((a) => a.album_id !== albumId)
    return ok(null, '删除成功')
  }

  const photosListMatch = path.match(/^\/couples\/([^/]+)\/photos$/)
  if (photosListMatch && upperMethod === 'GET') {
    const query = parseQuery(url)
    const albumId = query.album_id || ''
    const cityCode = query.city_code || ''
    const year = query.year || ''
    const cursor = query.cursor || ''
    const limit = Math.min(50, Math.max(1, Number(query.limit) || 20))

    let list = photos.slice()

    if (albumId) {
      list = list.filter((p) => Array.isArray(p.album_ids) && p.album_ids.includes(albumId))
    }
    if (cityCode) {
      list = list.filter((p) => p.city_code === cityCode)
    }
    if (year) {
      list = list.filter((p) => String(p.shot_date || '').startsWith(String(year)))
    }

    list.sort((a, b) => {
      const d = String(b.shot_date || '').localeCompare(String(a.shot_date || ''))
      if (d !== 0) return d
      return String(b.photo_id || '').localeCompare(String(a.photo_id || ''))
    })

    let start = 0
    if (cursor) {
      const idx = list.findIndex((p) => p.photo_id === cursor)
      if (idx >= 0) start = idx + 1
    }

    const pageItems = list.slice(start, start + limit)
    const hasMore = start + limit < list.length
    const nextCursor = hasMore && pageItems.length > 0 ? pageItems[pageItems.length - 1].photo_id : ''

    return ok({
      photos: pageItems,
      next_cursor: nextCursor,
      has_more: hasMore
    })
  }

  const todoListMatch = path.match(/^\/couples\/([^/]+)\/todos$/)
  if (todoListMatch && upperMethod === 'GET') {
    const query = parseQuery(url)
    const type = query.type || 'all'
    const status = query.status || 'all'
    const page = query.page || 1
    const pageSize = query.page_size || 20
    let filtered = todos.slice()
    if (type !== 'all') filtered = filtered.filter((t) => t.type === type)
    if (status !== 'all') filtered = filtered.filter((t) => t.status === status)
    const result = toPage(filtered, page, pageSize)
    return ok({ todos: result.items, total: result.total, page: result.page, page_size: result.page_size })
  }

  if (todoListMatch && upperMethod === 'POST') {
    const title = data && String(data.title || '').trim()
    if (!title) return fail('参数缺失或格式错误', 1001)
    todoSeed += 1
    const item = {
      todo_id: `td_${todoSeed}`,
      title,
      description: (data && data.description) || '',
      type: (data && data.type) || 'mine',
      status: 'pending',
      deadline: (data && data.deadline) || null,
      is_urging: false,
      created_by: MOCK_USER.user_id,
      assignee_id: null,
      completed_by: null,
      completed_at: null,
      created_at: nowText()
    }
    todos = [item].concat(todos)
    return ok(item)
  }

  const todoItemMatch = path.match(/^\/couples\/([^/]+)\/todos\/([^/]+)$/)
  if (todoItemMatch && upperMethod === 'PUT') {
    const todoId = todoItemMatch[2]
    const index = todos.findIndex((t) => t.todo_id === todoId)
    if (index < 0) return fail('资源不存在', 1004)
    todos[index] = {
      ...todos[index],
      title: data && data.title ? String(data.title).trim() : todos[index].title,
      description: data && data.description !== undefined ? data.description : todos[index].description,
      deadline: data && data.deadline !== undefined ? data.deadline : todos[index].deadline
    }
    return ok(todos[index])
  }

  if (todoItemMatch && upperMethod === 'DELETE') {
    const todoId = todoItemMatch[2]
    todos = todos.filter((t) => t.todo_id !== todoId)
    return ok(null, '删除成功')
  }

  const todoCompleteMatch = path.match(/^\/couples\/([^/]+)\/todos\/([^/]+)\/complete$/)
  if (todoCompleteMatch && upperMethod === 'PATCH') {
    const todoId = todoCompleteMatch[2]
    const index = todos.findIndex((t) => t.todo_id === todoId)
    if (index < 0) return fail('资源不存在', 1004)
    todos[index] = {
      ...todos[index],
      status: 'done',
      completed_by: MOCK_USER.user_id,
      completed_at: nowText()
    }
    return ok(todos[index])
  }

  const todoUrgeMatch = path.match(/^\/couples\/([^/]+)\/todos\/([^/]+)\/urge$/)
  if (todoUrgeMatch && upperMethod === 'POST') {
    const todoId = todoUrgeMatch[2]
    const index = todos.findIndex((t) => t.todo_id === todoId)
    if (index < 0) return fail('资源不存在', 1004)
    todos[index] = { ...todos[index], is_urging: true }
    return ok({ todo_id: todoId, next_urge_available_at: nowText() }, '催办通知已发送')
  }

  return fail(`Mock接口未实现: ${upperMethod} ${path}`, 1004)
}

export function mockRequest({ url, method, data, token }) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(route({ url, method, data, token }))
    }, MOCK_DELAY)
  })
}
