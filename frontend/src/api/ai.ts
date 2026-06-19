import request from './index'

/**
 * AI智能推荐（基于观看历史）
 */
export function getAiRecommendations(userId?: number) {
  const params: any = {}
  if (userId) params.userId = userId
  return request.get('/ai/recommend', { params })
}

/**
 * AI智能搜索
 */
export function aiSearch(query: string) {
  return request.get('/ai/search', { params: { q: query } })
}