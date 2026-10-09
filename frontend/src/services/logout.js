import { API_INTEGRATION_ENABLED } from '../api/clients'
import { auth } from '../stores/auth'
import { clearMockLogin } from '../mocks/profiles'
import { safePreviousPage } from '../stores/restrictedNavigation'
// 로그아웃: API 연동 중이면 서버 로그아웃(Refresh 토큰 폐기)까지 하고, 화면 미리보기 상태도 함께 정리한다.
export async function logoutCurrentUser(){
  clearMockLogin()
  if (API_INTEGRATION_ENABLED) { try { await auth.logout() } catch { auth.clear() } }
  else auth.clear()
  auth.state.notice=''
  safePreviousPage.value='/complaints'
}
