import { computed, reactive } from 'vue'
import { mainApi, authError, errorMessage, API_INTEGRATION_ENABLED } from '../api/clients.js'
const state = reactive({ user: null, checked: false, notice: '' })
let pending
let generation = 0
export const auth = {
  state,
  profile: computed(() => state.user ? { name: state.user.name, username: state.user.nickname, role: state.user.role } : null),
  clear() { generation++; state.user = null; state.checked = false; pending = null },
  async restore(force = false) {
    if (!API_INTEGRATION_ENABLED) return null
    if (!force && state.checked) return state.user
    if (pending) return pending
    const version = generation
    pending = mainApi.get('/api/auth/me', { localAuthError: true }).then(({ data }) => {
      // Confirmed local SignUpResponseDto fields; reject unknown /me shapes rather than guessing aliases.
      if (!data || !['USER', 'ADMIN'].includes(data.role) || typeof data.name !== 'string' || typeof data.nickname !== 'string') {
        throw new Error('/api/auth/me 응답 명세 확인이 필요합니다. role, name, nickname 사용자 DTO와 일치하지 않습니다.')
      }
      if (version === generation) { state.user = data; state.checked = true }
      return version === generation ? data : null
    }).catch(error => {
      if (version === generation) { state.user = null; state.checked = authError(error) === 'unauthorized' }
      if (authError(error) === 'unauthorized') return null
      throw error
    }).finally(() => { if (version === generation) pending = null })
    return pending
  },
  async logout() {
    await mainApi.post('/api/auth/logout')
    auth.clear()
  },
  errorMessage,
}
