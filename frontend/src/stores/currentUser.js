import { computed } from 'vue'
import { API_INTEGRATION_ENABLED } from '../api/clients'
import { auth } from './auth'
import { previewUser } from '../mocks/profiles'
// Single identity boundary. Preview switches cannot modify actual authentication.
export const currentUser=computed(()=>API_INTEGRATION_ENABLED?auth.state.user:previewUser())
