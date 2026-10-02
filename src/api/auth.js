import { mainApi } from './clients.js'
export function login(loginId, password) {
  return mainApi.post('/api/auth/login', { loginId, password }, { localAuthError: true })
}
export function signup(form) {
  return mainApi.post('/api/auth/signup', { nickname: form.username, password: form.password, name: form.name, email: form.email, over14: form.ageConfirmed }, { localAuthError: true })
}
