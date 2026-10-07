import { API_INTEGRATION_ENABLED } from '../api/clients'
import { auth } from '../stores/auth'
import { clearMockLogin } from '../mocks/profiles'
import { safePreviousPage } from '../stores/restrictedNavigation'
// Local-only logout. Replace this boundary with the API flow when authorized.
export async function logoutCurrentUser(){if(API_INTEGRATION_ENABLED){try{await auth.logout()}catch{auth.clear()}}clearMockLogin();auth.clear();auth.state.notice='';safePreviousPage.value='/complaints'}

