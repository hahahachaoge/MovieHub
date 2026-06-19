import request from './index'
import type { MovieVO } from './movie'

export interface CrewVO {
  id: number
  name: string
  avatar: string
  bio: string
  birthDate: string
  roleType: 'ACTOR' | 'DIRECTOR'
  createTime: string
}

export function searchCrew(keyword: string, page = 1, size = 10) {
  return request.get('/crew/search', { params: { keyword, page, size } })
}

export function getCrewDetail(id: number) {
  return request.get(`/crew/${id}/detail`)
}

export function getMoviesByCrew(id: number, page = 1, size = 12) {
  return request.get(`/crew/${id}/movies`, { params: { page, size } })
}

export function getMoviesByRole(id: number) {
  return request.get(`/crew/${id}/movies-by-role`)
}
