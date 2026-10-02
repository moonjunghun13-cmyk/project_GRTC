<script setup>
import { computed } from 'vue'
import { profileImage } from '../../utils/profileImage'
import { currentUser } from '../../stores/currentUser'
const props = defineProps({ label: { type: String, default: '사용자' }, admin: Boolean, role: { type: String, default: 'user' }, user: { type: Object, default: null } })
const identity = computed(() => props.user || currentUser.value || {})
</script>
<template>
  <div v-if="admin" class="admin-profile">
    <img class="profile-avatar" :src="profileImage(identity.role)" alt="로그인 사용자 프로필" />
    <div class="profile-identity"><strong :title="identity.name">{{ identity.name || label }}</strong><span :title="identity.loginId">{{ identity.loginId || '로그인 정보 없음' }}</span></div>
    <svg class="profile-settings" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="m9 3-1 3-3 1-2 5 2 5 3 1 1 3h6l1-3 3-1 2-5-2-5-3-1-1-3Z" stroke="currentColor" stroke-width="1.5" /><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.5" /></svg>
  </div>
  <div v-else class="sidebar-profile">{{ label }}</div>
</template>
<style scoped>
.admin-profile { display: flex; align-items: center; gap: 10px; margin: 0 12px 10px; padding: 14px 12px; min-height: 76px; background: white; border-radius: 18px; }
.profile-avatar { width: 40px; height: 40px; flex-shrink: 0; object-fit: contain; border-radius: 50%; }
.profile-identity { display: flex; flex-direction: column; min-width: 0; flex: 1; gap: 3px; }
.profile-identity strong { color: #064b76; font-size: 17px; font-weight: 800; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.profile-identity span { color: #7a858b; font-size: 12px; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.profile-settings { width: 22px; height: 22px; margin-left: auto; flex-shrink: 0; color: #73858e; }
</style>
