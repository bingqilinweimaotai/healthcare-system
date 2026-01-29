import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const axiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

axiosInstance.interceptors.request.use((config) => {
  const token = useAuthStore().tokenValue()
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

axiosInstance.interceptors.response.use(
  (res) => res.data,
  (err) => {
    if (err.response?.status === 401) {
      useAuthStore().logout()
      router.replace('/login')
    }
    return Promise.reject(err.response?.data?.message ?? err.message)
  }
)

export async function get<T = any>(url: string, config?: any): Promise<T> {
  return axiosInstance.get(url, config) as Promise<T>
}

export async function post<T = any>(url: string, data?: any, config?: any): Promise<T> {
  return axiosInstance.post(url, data, config) as Promise<T>
}

export async function put<T = any>(url: string, data?: any, config?: any): Promise<T> {
  return axiosInstance.put(url, data, config) as Promise<T>
}

const request = { get, post, put }
export default request
