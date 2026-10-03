<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ComplaintContent from '../../../components/complaints/ComplaintContent.vue'
import AccessNotice from '../../../components/common/AccessNotice.vue'
import { currentUser } from '../../../stores/currentUser'
import { findComplaint, canReadComplaint } from '../../../mocks/complaints'
const route = useRoute()
const complaint = computed(() => findComplaint(route.params.id))
const allowed = computed(() => canReadComplaint(currentUser.value, complaint.value))
</script>
<template><section v-if="!complaint"><p>민원 정보를 찾을 수 없습니다.</p><RouterLink to="/complaints">민원 목록으로 이동</RouterLink></section><AccessNotice v-else-if="!allowed" reason="ownership" /><section v-else><ComplaintContent :complaint="complaint" /><RouterLink to="/complaints">목록으로 돌아가기</RouterLink></section></template>
