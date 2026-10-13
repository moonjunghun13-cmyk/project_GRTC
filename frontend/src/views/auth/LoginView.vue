<script setup>
import '../../styles/auth-entry.css'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { errorMessage, authError } from '../../api/clients'
import { login } from '../../api/auth'
import { auth } from '../../stores/auth'
import { API_INTEGRATION_ENABLED } from '../../api/clients'
import { loginMock } from '../../mocks/profiles'
import sad from '../../assets/sad.png'
import logo from '../../assets/logo.png'
const failureDialog = ref(null)
const failureReason = ref('')
function closeFailure() { failureDialog.value?.close() }
function showLoginFailure(error) {
  if (!error.response || !/\/auth\/login(?:\?|$)/.test(error.config?.url || '')) return
  const code = error.response.data?.error?.code
  failureReason.value = code === 'INTERNAL_ERROR' || error.response.status >= 500
    ? '일시적인 오류가 발생했습니다.'
    : '아이디 또는 비밀번호가 올바르지 않습니다.'
  failureDialog.value?.showModal()
}
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
      await router.replace(user.role==='ADMIN'?'/dashboard':'/home')
      return
    }
    const { data } = await login(loginId.value, password.value)
    if (!['/dashboard', '/complaints'].includes(data?.redirectPath)) throw new Error('로그인 응답의 redirectPath를 확인해 주세요.')
    const user = await auth.restore(true)
    if (!user) throw new Error('로그인 세션을 확인할 수 없습니다.')
    if (data.redirectPath === '/dashboard' && user.role !== 'ADMIN') throw new Error('로그인 이동 경로와 사용자 권한이 일치하지 않습니다.')
    auth.state.notice = ''
    await router.push(data.redirectPath === '/complaints' ? {name:'user-home'} : data.redirectPath)
  } catch (error) {
    auth.clear()
    message.value = errorMessage(error)
    showLoginFailure(error)
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
    <Teleport to="body">
      <dialog ref="failureDialog" class="login-failure" aria-labelledby="login-failure-title" aria-describedby="login-failure-description login-failure-reason">
        <img class="failure-logo" :src="logo" alt="광주교통공사" />
        <button class="failure-close" type="button" aria-label="로그인 실패 팝업 닫기" @click="closeFailure">×</button>
        <img class="failure-character" :src="sad" alt="로그인 실패 안내 캐릭터" />
        <h2 id="login-failure-title">로그인에 실패하였습니다.</h2>
        <p id="login-failure-description">로그인 정보를 확인하고 다시 시도해 주세요.</p>
        <div id="login-failure-reason" class="failure-reason">{{ failureReason }}</div>
        <button type="button" autofocus @click="closeFailure">다시 로그인하기</button>
      </dialog>
    </Teleport>
  </section>
</template>



<style scoped>
.login-failure { box-sizing:border-box; width:min(440px,calc(100vw - 40px)); max-height:calc(100dvh - 40px); overflow-y:auto; padding:30px 32px 32px; border:0; border-radius:24px; background:#fff; color:#124675; text-align:center; box-shadow:0 16px 60px rgba(15,47,78,.2); }
.login-failure::backdrop { background:rgba(12,32,52,.46); backdrop-filter:blur(7px); -webkit-backdrop-filter:blur(7px); }
.login-failure .failure-character { display:block; width:220px; max-width:100%; height:180px; object-fit:contain; margin:0 auto 14px; }
.login-failure h2 { margin:0 0 12px; font-size:25px; font-weight:800; }
.login-failure p { margin:0 0 18px; color:#526A7D; font-size:17px; line-height:1.6; }
.failure-reason { padding:14px; margin-bottom:24px; border-radius:12px; background:#F0F6FC; color:#405A70; font-size:15px; line-height:1.6; overflow-wrap:anywhere; }
.login-failure button { width:100%; min-height:48px; padding:12px 20px; border:0; border-radius:12px; background:#075786; color:#fff; font:inherit; font-weight:700; cursor:pointer; }
.login-failure button:hover { background:#006EAF; }
.login-failure button:focus-visible { outline:3px solid #008EE0; outline-offset:4px; }

.login-failure .failure-logo { position:absolute; left:24px; top:20px; display:block; width:140px; max-width:40%; height:auto; object-fit:contain; margin:0; }

.login-failure .failure-close { position:absolute; right:16px; top:12px; width:36px; min-height:36px; padding:0; border:0; border-radius:50%; background:rgba(255,255,255,.8); color:#124675; font-size:28px; line-height:1; box-shadow:none !important; }
.login-failure .failure-close:hover { background:#E5F3FC; }
</style>
