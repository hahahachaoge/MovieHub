import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../api'

interface UserInfo {
  id: number
  username: string
  email: string
  phone: string
  avatar: string
  role: 'VIP' | 'NORMAL' | 'ADMIN'
  vipExpireTime: string | null
  balance: number
  createTime: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(
    localStorage.getItem('userInfo')
      ? JSON.parse(localStorage.getItem('userInfo')!)
      : null
  )

  const isLoggedIn = computed(() => !!token.value)
  const isVip = computed(() => userInfo.value?.role === 'VIP' || userInfo.value?.role === 'ADMIN')
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  // 登录
  async function login(credentials: { username: string; password: string }) {
    const res = await request.post('/user/login', credentials)
    if (res.data.code === 200) {
      token.value = res.data.data
      localStorage.setItem('token', res.data.data)
      await fetchUserInfo()
    } else {
      throw new Error(res.data.message)
    }
  }

  // 注册
  async function register(data: {
    username: string
    password: string
    email: string
    phone?: string
  }) {
    const res = await request.post('/user/register', data)
    if (res.data.code === 200) {
      token.value = res.data.data
      localStorage.setItem('token', res.data.data)
      await fetchUserInfo()
    } else {
      throw new Error(res.data.message)
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    try {
      const res = await request.get('/user/info')
      if (res.data.code === 200) {
        userInfo.value = res.data.data
        localStorage.setItem('userInfo', JSON.stringify(res.data.data))
      }
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  }

  // 退出登录
  async function logout() {
    try {
      await request.post('/user/logout')
    } catch (e) {
      console.error('退出请求失败', e)
    } finally {
      token.value = null
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }

  // 更新用户信息
  async function updateUserInfo(data: Partial<UserInfo>) {
    const res = await request.put('/user/update', data)
    if (res.data.code === 200) {
      await fetchUserInfo()
    } else {
      throw new Error(res.data.message)
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isVip,
    isAdmin,
    login,
    register,
    logout,
    fetchUserInfo,
    updateUserInfo
  }
})
