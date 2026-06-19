import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { MovieVO } from '../api/movie'

export const useMovieStore = defineStore('movie', () => {
  const hotMovies = ref<MovieVO[]>([])
  const latestMovies = ref<MovieVO[]>([])
  const currentMovies = ref<MovieVO[]>([])
  const loading = ref(false)

  return {
    hotMovies,
    latestMovies,
    currentMovies,
    loading
  }
})
