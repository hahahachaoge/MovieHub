import request from './index'

export function getHotGraph(limit = 20) {
  return request.get('/graph/hot', { params: { limit } })
}

export function searchGraph(keyword: string, limit = 30) {
  return request.get('/graph/search', { params: { keyword, limit } })
}
