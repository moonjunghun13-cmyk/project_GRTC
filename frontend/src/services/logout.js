import { auth } from '../stores/auth'
import { clearMockLogin } from '../mocks/profiles'
import { safePreviousPage } from '../stores/restrictedNavigation'
// Local-only logout. Replace this boundary with the API flow when authorized.
export function logoutCurrentUser(){clearMockLogin();auth.clear();auth.state.notice='';safePreviousPage.value='/complaints'}
