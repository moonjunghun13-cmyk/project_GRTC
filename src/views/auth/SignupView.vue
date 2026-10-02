<script setup>
import '../../styles/auth-entry.css'
import { reactive, ref } from 'vue'
import { errorMessage } from '../../api/clients'
import { signup } from '../../api/auth'
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
  if (form.username.length > 30) { message.value = '아이디는 최대 30자까지 입력 가능합니다.'; return }
  if (form.password.length < 8 || !/[^A-Za-z0-9]/.test(form.password)) { message.value = '비밀번호는 8자 이상이며 특수문자를 포함해야 합니다.'; return }
  busy.value = true
  message.value = ''
  try {
    // SignUpRequestDto uses nickname for the existing ID field and over14 for consent.
    await signup(form)
    message.value = '회원가입이 완료되었습니다. 로그인해 주세요.'
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
  </section>
</template>

<style scoped>
.email-error { margin: 0; color: #c52f35; font-size: 14px; line-height: 1.5; }
.authentication-field input[aria-invalid="true"] { border-color: #c52f35; }
</style>
