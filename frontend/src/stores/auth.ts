import { defineStore } from 'pinia'
import { ref } from 'vue'
import { get } from '@/api/request'

export type Role = 'PATIENT' | 'DOCTOR' | 'ADMIN'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') ?? '')
  const userId = ref<number | null>(null)
  const username = ref<string>('')
  const role = ref<Role | null>(null)
  const nickname = ref<string>('')
  const avatar = ref<string>('')

  async function fetchInfo() {
    try {
      const data = await get<{ token: string; userId: number; username: string; role: string; nickname?: string; avatar?: string }>('/auth/info')
      if (data.token) token.value = data.token
      if (data.userId != null) userId.value = data.userId
      if (data.username) username.value = data.username
      if (data.role) role.value = data.role as Role
      if (data.nickname) nickname.value = data.nickname
      else if (data.username) nickname.value = data.username
      if (data.avatar) avatar.value = data.avatar
      if (data.token) localStorage.setItem('token', data.token)
    } catch {
      /* 401 等已在 request 中处理，此处仅避免未捕获异常 */
    }
  }

  function setLogin(p: { token: string; userId: number; username: string; role: Role; nickname?: string; avatar?: string }) {
    token.value = p.token
    userId.value = p.userId
    username.value = p.username
    role.value = p.role
    nickname.value = p.nickname ?? p.username
    avatar.value = p.avatar ?? ''
    localStorage.setItem('token', p.token)
  }

  function logout() {
    token.value = ''
    userId.value = null
    username.value = ''
    role.value = null
    nickname.value = ''
    avatar.value = ''
    localStorage.removeItem('token')
  }

  const tokenValue = () => token.value ?? ''

  return { token, userId, username, role, nickname, avatar, tokenValue, fetchInfo, setLogin, logout }
})
