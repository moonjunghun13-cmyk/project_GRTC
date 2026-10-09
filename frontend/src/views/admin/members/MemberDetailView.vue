<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loadMember, updateMember, validateMember, memberOptions } from '../../../services/memberApi'
import { errorMessage } from '../../../api/clients'
import {isAdminAccount} from '../../../utils/memberAccount'
import {maskedPhone} from '../../../services/myMemberApi'
const departments=computed(()=>memberOptions.departments),ranks=computed(()=>memberOptions.positions)
import { profileImage } from '../../../utils/profileImage'
import './members.css'
const route = useRoute()
const router = useRouter()
function cancel() {
  router.push('/dashboard/members')
}
const member = ref(null), loading=ref(false)
const staff=computed(()=>isAdminAccount(member.value)),phoneDisplay=computed(()=>staff.value?member.value?.phone:maskedPhone(member.value?.phone))
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
let sequence=0
watch(() => route.params.id, async id=>{const request=++sequence;member.value=null;loading.value=true;try{const result=await loadMember(id);if(request!==sequence)return;member.value=result;reset()}catch(e){if(request===sequence)message.value=errorMessage(e)}finally{if(request===sequence)loading.value=false}}, { immediate: true })
async function save() {
  errors.value = validateMember(form,staff.value)
  message.value = ''
  if (Object.keys(errors.value).length) return
  try { member.value=await updateMember(route.params.id, form,staff.value); message.value = '회원정보가 저장되었습니다.' }
  catch (error) { message.value = errorMessage(error) }
}
</script>
<template>
  <section class="members-page">
    <header class="members-heading"><h1>회원정보</h1><p>등록된 회원 정보를 조회하고 관리할 수 있습니다.</p></header>
    <p v-if="loading" role="status">불러오는 중입니다.</p><template v-else-if="member">
      <section class="member-summary" aria-label="저장된 회원정보">
        <div class="member-photo-wrap"><img class="member-photo" :src="memberImage" :alt="member.name + ' 프로필'" @error="imageFailed=true" /></div>
        <dl class="member-facts"><div><dt>이름</dt><dd>{{ member.name }}</dd></div><div><dt>아이디</dt><dd>{{ member.username }}</dd></div><div v-if="staff"><dt>소속 부서</dt><dd>{{ member.department }}</dd></div><div v-if="staff"><dt>직급</dt><dd>{{ member.rank }}</dd></div><div><dt>이메일</dt><dd>{{ member.email }}</dd></div><div><dt>연락처</dt><dd>{{ phoneDisplay || '미등록' }}</dd></div></dl>

      </section>
      <section class="member-edit-card" aria-labelledby="personal-tab">
        <div class="member-tab-line"><h2 id="personal-tab">개인 정보</h2></div>
        <form novalidate @submit.prevent="save">
          <div class="member-form-columns" :class="{'citizen-fields':!staff}">
            <div class="member-form-column">
              <div class="member-field"><label for="member-name">이름 <span>*</span></label><input id="member-name" v-model="form.name" autocomplete="name" required :aria-invalid="!!errors.name" :aria-describedby="errors.name ? 'name-error' : undefined" /><p v-if="errors.name" id="name-error" class="member-error">{{ errors.name }}</p></div>
              <div class="member-field"><label for="member-email">이메일 <span>*</span></label><div class="member-input-icon"><svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="14" rx="2" /><path d="m3 6 9 7 9-7" /></svg><input id="member-email" v-model="form.email" type="email" autocomplete="email" required :aria-invalid="!!errors.email" :aria-describedby="errors.email ? 'email-error' : undefined" /></div><p v-if="errors.email" id="email-error" class="member-error">{{ errors.email }}</p></div>
              <div class="member-field"><label for="member-phone">연락처</label><div class="member-input-icon"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 3H3c-1 10 8 19 18 18v-4l-5-2-2 2c-3-1-6-4-7-7l2-2Z" /></svg><input v-if="staff" id="member-phone" v-model="form.phone" type="tel" inputmode="tel" autocomplete="tel" /><input v-else id="member-phone" :value="phoneDisplay" readonly aria-readonly="true" /></div></div>
            </div>
            <div v-if="staff" class="member-form-column">
              <div class="member-field"><label for="member-department">소속 부서 <span>*</span></label><select id="member-department" v-model="form.department" required :aria-invalid="!!errors.department" :aria-describedby="errors.department ? 'department-error' : undefined"><option value="">선택해 주세요</option><option v-for="item in departments" :key="item">{{ item }}</option></select><p v-if="errors.department" id="department-error" class="member-error">{{ errors.department }}</p></div>
              <div class="member-field"><label for="member-rank">직급 <span>*</span></label><select id="member-rank" v-model="form.rank" required :aria-invalid="!!errors.rank" :aria-describedby="errors.rank ? 'rank-error' : undefined"><option value="">선택해 주세요</option><option v-for="item in ranks" :key="item">{{ item }}</option></select><p v-if="errors.rank" id="rank-error" class="member-error">{{ errors.rank }}</p></div>
            </div>
          </div>
          <div class="member-actions"><button class="member-cancel" type="button" @click="cancel">취소</button><button class="member-save" type="submit">저장하기</button></div>
          <p v-if="message" class="member-message" role="status">{{ message }}</p>
        </form>
      </section>

    </template>
    <section v-else class="member-edit-card member-not-found"><p>{{message || '해당 회원 정보를 찾을 수 없습니다.'}}</p><RouterLink class="member-save" to="/dashboard/members">회원목록으로 돌아가기</RouterLink></section>
  </section>
</template>



<style scoped>.citizen-fields { grid-template-columns:minmax(0,1fr); }</style>

