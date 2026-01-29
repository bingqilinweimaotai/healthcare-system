/// <reference types="vite/client" />

// 路径别名 @/ 的类型声明，供 IDE（含 Vetur）解析
declare module '@/api/request' {
  export type ApiResult<T> = { code: number; message: string; data: T; errors?: any }
  export type ApiError = { code?: number; message: string; errors?: any }
  export function get<T = any>(url: string, config?: any): Promise<T>
  export function post<T = any>(url: string, data?: any, config?: any): Promise<T>
  export function put<T = any>(url: string, data?: any, config?: any): Promise<T>
  export default { get: typeof get, post: typeof post, put: typeof put }
}
