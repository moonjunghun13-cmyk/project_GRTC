import axios from 'axios'
export const API_INTEGRATION_ENABLED = import.meta.env?.VITE_ENABLE_API === 'true'
export const mainApi = axios.create({ baseURL: import.meta.env?.VITE_MAIN_API_URL || 'http://localhost:8081', withCredentials: true, timeout: 10000 })
// 백엔드가 서버 하나(8081)로 합쳐져서 관리자 API 도 같은 주소를 쓴다. (기존 코드 호환을 위해 이름만 유지)
export const dashboardApi = axios.create({ baseURL: import.meta.env?.VITE_DASHBOARD_API_URL || import.meta.env?.VITE_MAIN_API_URL || 'http://localhost:8081', withCredentials: true, timeout: 10000 })

// ---- Access 토큰 보관 (명세서 3-1: Access 30분은 Authorization 헤더, Refresh 14일은 HttpOnly 쿠키) ----
//   새로고침해도 로그인이 풀리지 않도록 sessionStorage 에도 둔다. (탭을 닫으면 사라짐, Refresh 쿠키로 다시 발급 가능)
const TOKEN_KEY = 'grtc.accessToken'
let accessToken = null
try { accessToken = sessionStorage.getItem(TOKEN_KEY) } catch {}
export function getAccessToken() { return accessToken }
export function setAccessToken(token) {
  accessToken = token || null
  try { token ? sessionStorage.setItem(TOKEN_KEY, token) : sessionStorage.removeItem(TOKEN_KEY) } catch {}
}
export function clearAccessToken() { setAccessToken(null) }

// 성공 응답 { success, data, error } 에서 data 만 꺼낸다. (화면 코드에서 필요할 때만 사용)
export function unwrap(response) { return response?.data?.data }

for (const api of [mainApi, dashboardApi]) api.interceptors.request.use(config => {
  if (!API_INTEGRATION_ENABLED) throw new Error('현재는 화면 제작 단계입니다. API 연동은 비활성화되어 있습니다.')
  if (accessToken && !config.skipAuth && !config.headers?.Authorization) config.headers.Authorization = `Bearer ${accessToken}`
  return config
})

// ---- Access 토큰 자동 재발급 ----
//   401 TOKEN_EXPIRED 를 받으면 POST /api/v1/auth/reissue (Refresh 쿠키) 로 새 토큰을 받아 원래 요청을 한 번 다시 보낸다.
//   여러 요청이 동시에 만료돼도 재발급은 한 번만 한다.
let reissuing = null
export function reissueAccessToken() {
  if (!reissuing) {
    reissuing = mainApi.post('/api/v1/auth/reissue', null, { localAuthError: true, skipReissue: true, skipAuth: true })
      .then(response => { const token = unwrap(response)?.accessToken; if (!token) throw new Error('토큰 재발급 응답을 확인해 주세요.'); setAccessToken(token); return token })
      .catch(error => { clearAccessToken(); throw error })
      .finally(() => { reissuing = null })
  }
  return reissuing
}
// 프론트 브랜치(feat/frontend) 코드 호환용 별칭
export const refreshAccessToken = reissueAccessToken
function errorCode(error) { return error?.response?.data?.error?.code ?? error?.response?.data?.code }
for (const api of [mainApi, dashboardApi]) api.interceptors.response.use(response => response, async error => {
  const config = error?.config
  if (error?.response?.status === 401 && errorCode(error) === 'TOKEN_EXPIRED' && config && !config.skipReissue && !config._retried) {
    config._retried = true
    const token = await reissueAccessToken()
    config.headers.Authorization = `Bearer ${token}`
    return api.request(config)
  }
  return Promise.reject(error)
})

export function errorMessage(error) {
  const body = error?.response?.data
  // 입력값 검증 실패(INVALID_INPUT)는 어떤 항목이 왜 틀렸는지(details) 첫 번째 것을 보여 준다.
  if (body?.error?.details?.length) return body.error.details[0].message
  if (body?.error?.message) return body.error.message
  if (body?.message) return body.message
  if (error?.code === 'ERR_NETWORK') return '서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.'
  if (error?.code === 'ECONNABORTED') return '서버 응답이 지연되고 있습니다. 다시 시도해 주세요.'
  return error?.message || '요청을 처리할 수 없습니다.'
}
export function authError(error) {
  const code = errorCode(error)
  if (error?.response?.status === 401 || code === 'UNAUTHORIZED' || code === 'A002') return 'unauthorized'
  if (error?.response?.status === 403 || code === 'FORBIDDEN' || code === 'A001') return 'forbidden'
  return null
}
export function installApiErrorHandlers(handler) {
  for (const api of [mainApi, dashboardApi]) api.interceptors.response.use(response => response, error => {
    if (!error.config?.localAuthError) handler(authError(error), error)
    return Promise.reject(error)
  })
}
export async function general(method,url,data,params){return (await mainApi.request({method,url,data,params})).data.data}
