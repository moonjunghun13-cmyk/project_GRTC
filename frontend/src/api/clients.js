import axios from 'axios'
export const API_INTEGRATION_ENABLED = import.meta.env?.VITE_ENABLE_API === 'true'
export const mainApi = axios.create({ baseURL: import.meta.env?.VITE_MAIN_API_URL || 'http://localhost:8081', withCredentials: true, timeout: 10000 })
// 백엔드가 서버 하나(8081)로 합쳐져서 관리자 API 도 같은 주소를 쓴다. (기존 코드 호환을 위해 이름만 유지)
export const dashboardApi = axios.create({ baseURL: import.meta.env?.VITE_DASHBOARD_API_URL || import.meta.env?.VITE_MAIN_API_URL || 'http://localhost:8081', withCredentials: true, timeout: 10000 })
for (const api of [mainApi, dashboardApi]) api.interceptors.request.use(config => {
  if (!API_INTEGRATION_ENABLED) throw new Error('현재는 화면 제작 단계입니다. API 연동은 비활성화되어 있습니다.')
  return config
})
export function errorMessage(error) {
  if (error.response?.data?.message) return error.response.data.message
  if (error.code === 'ERR_NETWORK') return '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.'
  if (error.code === 'ECONNABORTED') return '서버 응답이 지연되고 있습니다. 다시 시도해 주세요.'
  return error.message || '요청을 처리할 수 없습니다.'
}
export function authError(error) {
  if (error.response?.status === 401 || error.response?.data?.code === 'A002') return 'unauthorized'
  if (error.response?.status === 403 || error.response?.data?.code === 'A001') return 'forbidden'
  return null
}
export function installApiErrorHandlers(handler) {
  for (const api of [mainApi, dashboardApi]) api.interceptors.response.use(response => response, error => {
    if (!error.config?.localAuthError) handler(authError(error), error)
    return Promise.reject(error)
  })
}
