import request from './index'

export interface RankingVO {
  rank: number
  movieId: number
  title: string
  coverUrl: string
  rating: number
  playCount: number
  region: string
  categoryNames: string
}

export function getWeeklyRanking() {
  return request.get('/ranking/weekly')
}

export function getMonthlyRanking() {
  return request.get('/ranking/monthly')
}

export function getAllTimeRanking() {
  return request.get('/ranking/all')
}

export function getTopRated() {
  return request.get('/ranking/top-rated')
}
