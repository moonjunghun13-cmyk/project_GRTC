<script setup>
import '../../styles/auth-entry.css'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { errorMessage, authError } from '../../api/clients'
import { login } from '../../api/auth'
import { auth } from '../../stores/auth'
import { API_INTEGRATION_ENABLED } from '../../api/clients'
import { loginMock } from '../../mocks/profiles'
const message = ref(auth.state.notice)
const loginId = ref('')
const password = ref('')
const busy = ref(false)
const router = useRouter()
async function submit() {
  if (busy.value) return
  busy.value = true
  message.value = ''
  try {
    if (!API_INTEGRATION_ENABLED) {
      const user=loginMock(loginId.value,password.value)
      await router.replace(user.role==='ADMIN'?'/dashboard':'/complaints')
      return
    }
    const { data } = await login(loginId.value, password.value)
    if (!['/dashboard', '/complaints'].includes(data?.redirectPath)) throw new Error('로그인 응답의 redirectPath를 확인해 주세요.')
    auth.clear()
    const user = await auth.restore(true)
    if (!user) throw new Error('로그인 세션을 확인할 수 없습니다.')
    if (data.redirectPath === '/dashboard' && user.role !== 'ADMIN') throw new Error('로그인 이동 경로와 사용자 권한이 일치하지 않습니다.')
    auth.state.notice = ''
    await router.push(data.redirectPath)
  } catch (error) {
    auth.clear()
    message.value = errorMessage(error)
    if (authError(error) === 'forbidden') await router.push('/forbidden')
  } finally { busy.value = false }
}
</script>

<template>
  <section class="authentication-screen" aria-labelledby="login-title">
    <div class="authentication-heading auth-enter" style="--auth-enter-delay: 0ms">
      <h1 id="login-title">로그인</h1>
      <p class="authentication-description">아이디와 비밀번호를 입력하여 로그인하세요.</p>
    </div>
    <form class="authentication-form" @submit.prevent="submit">
      <div class="authentication-field auth-enter" style="--auth-enter-delay: 100ms"><label for="login-username">아이디</label><input id="login-username" v-model="loginId" name="username" type="text" autocomplete="username" placeholder="아이디를 입력하세요" required /></div>
      <div class="authentication-field auth-enter" style="--auth-enter-delay: 200ms"><label for="login-password">비밀번호</label><input id="login-password" v-model="password" name="password" type="password" autocomplete="current-password" placeholder="비밀번호를 입력하세요" required /></div>
      <RouterLink class="authentication-signup-link auth-enter" style="--auth-enter-delay: 300ms" to="/signup">회원가입</RouterLink>
      <button class="authentication-submit auth-enter" style="--auth-enter-delay: 400ms" type="submit" :disabled="busy" :aria-busy="busy">로그인</button>
    </form>
    <p v-if="message" class="authentication-message" role="status">{{ message }}</p>
  </section>
</template>

