import request from './index'

export interface MovieVO {
  id: number
  title: string
  originalTitle: string
  coverUrl: string
  description: string
  releaseDate: string
  duration: number
  region: string
  language: string
  rating: number
  ratingCount: number
  playUrl: string
  isVip: boolean
  playCount: number
  isHot: boolean
  categoryNames: string[]
  directors: CrewVO[]
  actors: CrewVO[]
}

export interface CrewVO {
  id: number
  name: string
  avatar: string
  roleType: string
  characterName: string
  position: string
}

export interface Category {
  id: number
  name: string
  description: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// 获取热播电影
export function getHotMovies(limit = 10) {
  return request.get('/movie/public/hot', { params: { limit } })
}

// 获取轮播图电影（指定5部）
export function getCarouselMovies() {
  return request.get('/movie/public/carousel')
}

// 按分类获取电影
export function getMoviesByCategory(categoryId: number, page = 1, size = 12) {
  return request.get(`/movie/public/category/${categoryId}`, { params: { page, size } })
}

// 按地区获取电影
export function getMoviesByRegion(region: string, page = 1, size = 12) {
  return request.get(`/movie/public/region/${region}`, { params: { page, size } })
}

// 获取电影详情
export function getMovieDetail(id: number) {
  return request.get(`/movie/public/detail/${id}`)
}

// 获取所有分类
export function getAllCategories() {
  return request.get('/movie/public/categories')
}

// 获取所有地区
export function getAllRegions() {
  return request.get('/movie/public/regions')
}

// 搜索电影
export function searchMovies(keyword: string, page = 1, size = 12) {
  return request.get('/movie/public/search', { params: { keyword, page, size } })
}

// 获取最新电影
export function getLatestMovies(limit = 6) {
  return request.get('/movie/public/latest', { params: { limit } })
}

// 按分类+地区组合查询
export function filterMovies(categoryId?: number | null, region?: string, page = 1, size = 12, keyword?: string) {
  const params: any = { page, size }
  if (categoryId) params.categoryId = categoryId
  if (region) params.region = region
  if (keyword) params.keyword = keyword
  return request.get('/movie/public/filter', { params })
}

export function getRelatedMovies(id: number) {
  return request.get(`/movie/public/related/${id}`)
}
