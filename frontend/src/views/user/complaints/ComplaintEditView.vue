<script setup>
import { computed,ref } from 'vue'
import { useRoute } from 'vue-router'
import ComplaintForm from '../../../components/complaints/ComplaintForm.vue'
import { findComplaint,updateMockComplaint } from '../../../mocks/complaints'
import { currentUser } from '../../../stores/currentUser'
const route=useRoute(),complaint=computed(()=>findComplaint(route.params.id)),message=ref('')
function save(draft){try{updateMockComplaint(route.params.id,draft,currentUser.value);message.value='민원이 수정되었습니다.'}catch(e){message.value=e.message}}
</script>
<template><section><ComplaintForm v-if="complaint && complaint.status==='WAITING'" :key="complaint.id" :initial-value="complaint" submit-label="수정" @submit="save"/><p v-else>접수대기 민원만 수정할 수 있습니다.</p><p role="status">{{message}}</p><RouterLink to="/complaints">목록으로 돌아가기</RouterLink></section></template>
