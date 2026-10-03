import { mainApi, unwrap, setAccessToken, clearAccessToken } from './clients.js'
// 인증 API (명세서 3-3 ① 인증, Base URL /api/v1)

// 로그인: Access 토큰은 보관하고, 화면에는 회원 정보(member: id, loginId, name, role, redirectPath)를 돌려준다.
export async function login(loginId, password) {
  const response = await mainApi.post('/api/v1/auth/login', { loginId, password }, { localAuthError: true, skipReissue: true, skipAuth: true })
  const result = unwrap(response)
  if (!result?.accessToken) throw new Error('로그인 응답을 확인해 주세요.')
  setAccessToken(result.accessToken)
  return { data: result.member }
}

// 회원가입: SignUpRequestDto (name, loginId, email, password, passwordConfirm, over14)
export async function signup(form) {
  const response = await mainApi.post('/api/v1/auth/signup', {
    name: form.name,
    loginId: form.username,
    email: form.email,
    password: form.password,
    passwordConfirm: form.passwordConfirmation,
    over14: form.ageConfirmed,
  }, { localAuthError: true, skipAuth: true })
  return { data: unwrap(response) }
}

// 아이디 사용 가능 여부
export async function checkLoginId(loginId) {
  const response = await mainApi.get('/api/v1/auth/check-id', { params: { loginId }, localAuthError: true })
  return unwrap(response)?.available === true
}

// 내 정보 (새로고침 후 로그인 확인용)
export async function fetchMe() {
  return unwrap(await mainApi.get('/api/v1/auth/me', { localAuthError: true }))
}

// 로그아웃: 서버의 Refresh 토큰을 지우고, 보관한 Access 토큰도 버린다.
export async function logout() {
  try { await mainApi.post('/api/v1/auth/logout', null, { localAuthError: true, skipReissue: true }) }
  finally { clearAccessToken() }
}
