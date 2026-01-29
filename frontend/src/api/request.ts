import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

export type ApiResult<T> = {
  code: number
  message: string
  data: T
  errors?: any
}

export type ApiError = {
  code?: number
  message: string
  errors?: any
}

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
  (res) => {
    const body = res.data as ApiResult<any>
    // 统一解包：后端成功时返回 Result<T>
    if (body && typeof body.code === 'number' && 'message' in body && 'data' in body) {
      if (body.code === 200) return body.data
      const e: ApiError = { code: body.code, message: body.message ?? '请求失败', errors: body.errors }
      // 兼容：即使后端用 200 返回业务 401/403，也做同样处理
      if (body.code === 401) {
        useAuthStore().logout()
        router.replace('/login')
      }
      return Promise.reject(e)
    }
    // 兜底：若某些接口仍返回裸数据
    return res.data
  },
  (err) => {
    const status = err.response?.status
    const data = err.response?.data as ApiResult<any> | undefined
    if (status === 401 || data?.code === 401) {
      useAuthStore().logout()
      router.replace('/login')
    }
    const apiErr: ApiError = {
      code: data?.code ?? status,
      message: data?.message ?? err.message,
      errors: data?.errors,
    }
    return Promise.reject(apiErr)
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
