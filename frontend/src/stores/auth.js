import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getCurrentUser } from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isUser = computed(() => user.value?.role === 'USER')
  const isMerchant = computed(() => user.value?.role === 'MERCHANT')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(credentials) {
    const res = await loginApi(credentials)
    token.value = res.data.token
    user.value = {
      userId: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname,
      role: res.data.role,
      avatar: res.data.avatar,
      storeName: res.data.storeName
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
    return res
  }

  async function register(data) {
    return await registerApi(data)
  }

  async function fetchUser() {
    try {
      const res = await getCurrentUser()
      user.value = {
        userId: res.data.userId,
        username: res.data.username,
        nickname: res.data.nickname,
        role: res.data.role,
        avatar: res.data.avatar,
        storeName: res.data.storeName
      }
      localStorage.setItem('user', JSON.stringify(user.value))
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isLoggedIn, isUser, isMerchant, isAdmin, login, register, fetchUser, logout }
})
