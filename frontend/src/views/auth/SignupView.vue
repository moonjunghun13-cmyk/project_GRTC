<script setup>
import '../../styles/auth-entry.css'
import { reactive, ref } from 'vue'
import { errorMessage } from '../../api/clients'
import { signup } from '../../api/auth'
import { useRouter } from 'vue-router'
import happy from '../../assets/happy.png'
import logo from '../../assets/logo.png'
const router = useRouter()
const welcomeDialog = ref(null)
function goToLogin() { welcomeDialog.value?.close(); router.push({ name: 'login' }) }
const form = reactive({ name: '', username: '', email: '', password: '', passwordConfirmation: '', ageConfirmed: false })
const emailInput = ref(null)
const emailError = ref('')
const message = ref('')
const busy = ref(false)
function validateEmail() {
  const input = emailInput.value
  if (!input) return false
  emailError.value = input.validity.valueMissing
    ? '이메일을 입력해 주세요.'
    : input.validity.typeMismatch ? '올바른 이메일 주소를 입력해 주세요.' : ''
  return !emailError.value
}
async function submit() {
  if (busy.value || !validateEmail()) return
  if (form.password !== form.passwordConfirmation) { message.value = '비밀번호가 일치하지 않습니다.'; return }
  if (form.username.length < 4 || form.username.length > 20) { message.value = '아이디는 4자 이상 20자 이하로 입력해주세요.'; return }
  if (!/^[A-Za-z0-9_]+$/.test(form.username)) { message.value = '아이디는 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.'; return }
  if (form.password.length < 8 || !/[^A-Za-z0-9]/.test(form.password)) { message.value = '비밀번호는 8자 이상이며 특수문자를 포함해야 합니다.'; return }
  busy.value = true
  message.value = ''
  try {
    // 백엔드 SignUpRequestDto: name, loginId, email, password, passwordConfirm, over14
    await signup(form)
    welcomeDialog.value.showModal()
  } catch (error) { message.value = errorMessage(error) }
  finally { busy.value = false }
}
</script>

<template>
  <section class="authentication-screen" aria-labelledby="signup-title">
    <div class="authentication-heading auth-enter" style="--auth-enter-delay: 0ms">
      <h1 id="signup-title">회원가입</h1>
      <p class="authentication-description">광주교통공사와 함께<br />더 스마트한 도시철도를 만들어가요.</p>
    </div>
    <form class="authentication-form" @submit.prevent="submit">
      <div class="authentication-field auth-enter" style="--auth-enter-delay: 80ms"><label for="signup-name">이름</label><input id="signup-name" v-model="form.name" name="name" type="text" autocomplete="name" placeholder="이름을 입력하세요" required /></div>
      <div class="authentication-field auth-enter" style="--auth-enter-delay: 160ms"><label for="signup-username">아이디</label><input id="signup-username" v-model="form.username" name="username" type="text" autocomplete="username" placeholder="아이디를 입력하세요" required /></div>
      <div class="authentication-field auth-enter" style="--auth-enter-delay: 240ms">
        <label for="signup-email">이메일</label>
        <input id="signup-email" ref="emailInput" v-model="form.email" name="email" type="email" autocomplete="email" placeholder="example@email.com" required :aria-invalid="!!emailError" :aria-describedby="emailError ? 'signup-email-error' : undefined" @blur="validateEmail" @input="emailError && validateEmail()" @invalid.prevent="validateEmail" />
        <p v-if="emailError" id="signup-email-error" class="email-error" role="alert">{{ emailError }}</p>
      </div>
      <div class="authentication-field auth-enter" style="--auth-enter-delay: 320ms"><label for="signup-password">비밀번호</label><input id="signup-password" v-model="form.password" name="password" type="password" autocomplete="new-password" placeholder="비밀번호를 입력하세요" required /></div>
      <div class="authentication-field auth-enter" style="--auth-enter-delay: 400ms"><label for="signup-confirmation">비밀번호 확인</label><input id="signup-confirmation" v-model="form.passwordConfirmation" name="passwordConfirmation" type="password" autocomplete="new-password" placeholder="비밀번호를 다시 입력하세요" required /></div>
      <label class="authentication-age auth-enter" style="--auth-enter-delay: 480ms"><input v-model="form.ageConfirmed" name="ageConfirmed" type="checkbox" required />14세 이상입니다.</label>
      <button class="authentication-submit auth-enter" style="--auth-enter-delay: 560ms" type="submit" :disabled="busy" :aria-busy="busy">회원가입</button>
    </form>
    <p v-if="message" class="authentication-message" role="status">{{ message }}</p>
    <p class="authentication-footer auth-enter" style="--auth-enter-delay: 640ms">이미 계정이 있으신가요?<RouterLink to="/login">로그인</RouterLink></p>
    <Teleport to="body">
      <dialog ref="welcomeDialog" class="signup-welcome" aria-labelledby="welcome-title" aria-describedby="welcome-description">
        <img class="welcome-logo" :src="logo" alt="광주교통공사" />
        <button class="welcome-close" type="button" aria-label="환영 팝업 닫기" @click="welcomeDialog.close()">×</button>
        <img class="welcome-character" :src="happy" alt="환영하는 광주교통공사 캐릭터" />
        <h2 id="welcome-title">환영합니다! 🎉</h2>
        <p id="welcome-description">회원가입이 완료되었습니다.</p>
        <button type="button" autofocus @click="goToLogin">로그인하러 가기</button>
      </dialog>
    </Teleport>
  </section>
</template>

<style scoped>
.email-error { margin: 0; color: #c52f35; font-size: 14px; line-height: 1.5; }
.authentication-field input[aria-invalid="true"] { border-color: #c52f35; }
.signup-welcome { box-sizing:border-box; width:min(520px,calc(100vw - 40px)); aspect-ratio:1; max-height:calc(100dvh - 40px); overflow-y:auto; padding:30px 32px 32px; border:0; border-radius:24px; background:#fff; color:#124675; text-align:center; box-shadow:0 16px 60px rgba(15,47,78,.2); }
.signup-welcome::backdrop { background:rgba(12,32,52,.46); backdrop-filter:blur(7px); -webkit-backdrop-filter:blur(7px); }
.signup-welcome .welcome-character { display:block; width:376px; max-width:100%; height:260px; object-fit:contain; object-position:center; margin:0 auto 18px; }
.signup-welcome h2 { margin:0 0 12px; font-size:34px; font-weight:800; text-align:center; }
.signup-welcome p { margin:0 0 18px; color:#526A7D; font-size:17px; line-height:1.6; }
.signup-welcome button { width:100%; min-height:48px; padding:12px 20px; border:0; border-radius:12px; background:#075786; color:white; font:inherit; font-weight:700; cursor:pointer; }
.signup-welcome button:hover { background:#006EAF; }
.signup-welcome button:focus-visible { outline:3px solid #008EE0; outline-offset:4px; }
@media(max-width:560px),(max-height:600px){.signup-welcome{aspect-ratio:auto;padding:20px}.signup-welcome .welcome-character{height:min(240px,30dvh);margin-bottom:10px}.signup-welcome h2{font-size:28px}.signup-welcome p{margin-bottom:14px}}

.signup-welcome {
  position: fixed;
  isolation: isolate;
  background: #fff url('../../assets/glass.png') center / cover no-repeat;
}
.signup-welcome::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: -1;
  pointer-events: none;
  border-radius: inherit;
  background: radial-gradient(ellipse at 50% 48%, rgba(255,255,255,.98) 18%, rgba(255,255,255,.94) 42%, rgba(255,255,255,.65) 65%, rgba(255,255,255,.12) 100%);
}

.signup-welcome .welcome-character { transform: translateY(22px); }

.signup-welcome .welcome-logo { position:absolute; left:24px; top:20px; display:block; width:140px; max-width:40%; height:auto; object-fit:contain; margin:0; }
.signup-welcome .welcome-close { position:absolute; right:16px; top:12px; width:36px; min-height:36px; padding:0; border:0; border-radius:50%; background:rgba(255,255,255,.8); color:#124675; font-size:28px; line-height:1; box-shadow:none !important; }
.signup-welcome .welcome-close:hover { background:#E5F3FC; }
</style>
