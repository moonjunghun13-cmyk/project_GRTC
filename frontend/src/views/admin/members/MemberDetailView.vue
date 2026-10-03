<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMember, updateMember, validateMember } from '../../../services/mockMembers'
import { departments, ranks } from '../../../mocks/members'
import { profileImage } from '../../../utils/profileImage'
import './members.css'
const route = useRoute()
const member = computed(() => getMember(route.params.id))
const form = reactive({ name:'', email:'', phone:'', department:'', rank:'' })
const errors = ref({})
const message = ref('')
const imageFailed=ref(false)
const memberImage=computed(()=>!imageFailed.value && (member.value?.profileImageUrl || member.value?.photo) || profileImage(member.value?.role))
function reset() {
  imageFailed.value=false
  if (member.value) for (const key of Object.keys(form)) form[key] = member.value[key] || ''
  errors.value = {}; message.value = ''
}
watch(() => route.params.id, reset, { immediate: true })
function save() {
  errors.value = validateMember(form)
  message.value = ''
  if (Object.keys(errors.value).length) return
  try { updateMember(route.params.id, form); message.value = '회원정보가 저장되었습니다.' }
  catch (error) { message.value = error.message }
}
</script>
<template>
  <section class="members-page">
    <header class="members-heading"><h1>회원정보</h1><p>등록된 회원 정보를 조회하고 관리할 수 있습니다.</p></header>
    <template v-if="member">
      <section class="member-summary" aria-label="저장된 회원정보">
        <div class="member-photo-wrap"><img class="member-photo" :src="memberImage" :alt="member.name + ' 프로필'" @error="imageFailed=true" /></div>
        <dl class="member-facts"><div><dt>이름</dt><dd>{{ member.name }}</dd></div><div><dt>아이디</dt><dd>{{ member.username }}</dd></div><div><dt>소속 부서</dt><dd>{{ member.department }}</dd></div><div><dt>직급</dt><dd>{{ member.rank }}</dd></div><div><dt>이메일</dt><dd>{{ member.email }}</dd></div><div><dt>연락처</dt><dd>{{ member.phone || '-' }}</dd></div></dl>

      </section>
      <section class="member-edit-card" aria-labelledby="personal-tab">
        <div class="member-tab-line"><h2 id="personal-tab">개인 정보</h2></div>
        <form novalidate @submit.prevent="save">
          <div class="member-form-columns">
            <div class="member-form-column">
              <div class="member-field"><label for="member-name">이름 <span>*</span></label><input id="member-name" v-model="form.name" autocomplete="name" required :aria-invalid="!!errors.name" :aria-describedby="errors.name ? 'name-error' : undefined" /><p v-if="errors.name" id="name-error" class="member-error">{{ errors.name }}</p></div>
              <div class="member-field"><label for="member-email">이메일 <span>*</span></label><div class="member-input-icon"><svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="14" rx="2" /><path d="m3 6 9 7 9-7" /></svg><input id="member-email" v-model="form.email" type="email" autocomplete="email" required :aria-invalid="!!errors.email" :aria-describedby="errors.email ? 'email-error' : undefined" /></div><p v-if="errors.email" id="email-error" class="member-error">{{ errors.email }}</p></div>
              <div class="member-field"><label for="member-phone">연락처</label><div class="member-input-icon"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 3H3c-1 10 8 19 18 18v-4l-5-2-2 2c-3-1-6-4-7-7l2-2Z" /></svg><input id="member-phone" v-model="form.phone" type="tel" inputmode="tel" autocomplete="tel" /></div></div>
            </div>
            <div class="member-form-column">
              <div class="member-field"><label for="member-department">소속 부서 <span>*</span></label><select id="member-department" v-model="form.department" required :aria-invalid="!!errors.department" :aria-describedby="errors.department ? 'department-error' : undefined"><option value="">선택해 주세요</option><option v-for="item in departments" :key="item">{{ item }}</option></select><p v-if="errors.department" id="department-error" class="member-error">{{ errors.department }}</p></div>
              <div class="member-field"><label for="member-rank">직급 <span>*</span></label><select id="member-rank" v-model="form.rank" required :aria-invalid="!!errors.rank" :aria-describedby="errors.rank ? 'rank-error' : undefined"><option value="">선택해 주세요</option><option v-for="item in ranks" :key="item">{{ item }}</option></select><p v-if="errors.rank" id="rank-error" class="member-error">{{ errors.rank }}</p></div>
            </div>
          </div>
          <div class="member-actions"><button class="member-cancel" type="button" @click="reset">취소</button><button class="member-save" type="submit">저장하기</button></div>
          <p v-if="message" class="member-message" role="status">{{ message }}</p>
        </form>
      </section>
      <RouterLink class="member-back" to="/dashboard/members">회원목록으로 돌아가기</RouterLink>
    </template>
    <section v-else class="member-edit-card member-not-found"><p>해당 회원 정보를 찾을 수 없습니다.</p><RouterLink class="member-save" to="/dashboard/members">회원목록으로 돌아가기</RouterLink></section>
  </section>
</template>
