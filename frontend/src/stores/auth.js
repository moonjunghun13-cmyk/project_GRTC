import { computed, reactive } from 'vue'
import { authError, errorMessage, API_INTEGRATION_ENABLED, getAccessToken, reissueAccessToken, clearAccessToken } from '../api/clients.js'
import { fetchMe, logout as logoutApi } from '../api/auth.js'
const state = reactive({ user: null, checked: false, notice: '' })
let pending
let generation = 0
// /api/v1/auth/me 응답(LoginResponseDto: id, loginId, name, role, roleLabel, redirectPath)을 화면용 사용자로 맞춘다.
function toUser(data) {
  if (!data || !['USER', 'ADMIN'].includes(data.role) || typeof data.name !== 'string' || typeof data.loginId !== 'string') {
    throw new Error('/api/v1/auth/me 응답 명세 확인이 필요합니다. role, name, loginId 가 있어야 합니다.')
  }
  // 예전 화면 코드가 nickname / username 으로 아이디를 읽는 곳이 있어 같은 값을 함께 둔다.
  return { ...data, username: data.loginId, nickname: data.loginId }
}
export const auth = {
  state,
  profile: computed(() => state.user ? { name: state.user.name, username: state.user.loginId, role: state.user.role } : null),
  clear() { generation++; state.user = null; state.checked = false; pending = null },
  async restore(force = false) {
    if (!API_INTEGRATION_ENABLED) return null
    if (!force && state.checked) return state.user
    if (pending) return pending
    const version = generation
    pending = (async () => {
      // 보관한 Access 토큰이 없으면(새 탭 등) Refresh 쿠키로 먼저 발급을 시도한다. 실패하면 로그인 안 한 상태.
      if (!getAccessToken()) {
        try { await reissueAccessToken() } catch { return null }
      }
      return toUser(await fetchMe())
    })().then(user => {
      if (version === generation) { state.user = user; state.checked = true }
      return version === generation ? user : null
    }).catch(error => {
      if (version === generation) { state.user = null; state.checked = authError(error) === 'unauthorized' }
      if (authError(error) === 'unauthorized') { clearAccessToken(); return null }
      throw error
    }).finally(() => { if (version === generation) pending = null })
    return pending
  },
  async logout() {
    try { await logoutApi() } finally { auth.clear(); state.checked = true }
  },
  errorMessage,
}
