<script setup>
import { reactive, ref } from 'vue'
import BaseButton from '../common/BaseButton.vue'
import { getComplaintOptions } from '../../services/complaintOptions'
const props=defineProps({initialValue:{type:Object,default:()=>({})},submitLabel:{type:String,default:'등록'}})
const emit=defineEmits(['submit'])
const {types,categories}=getComplaintOptions()
const form=reactive({type:props.initialValue.type||'',category:props.initialValue.category||'',title:props.initialValue.title||'',content:props.initialValue.content||''})
const error=ref('')
function submit(){if(!form.content.trim()){error.value='내용을 입력해 주세요.';return}error.value='';emit('submit',{...form})}
</script>
<template><form class="complaint-form" @submit.prevent="submit"><label>민원유형<select v-model="form.type" required><option value="">선택해 주세요</option><option v-for="o in types" :value="o.value" :key="o.value">{{o.label}}</option></select></label><label>분류<select v-model="form.category" required><option value="">선택해 주세요</option><option v-for="o in categories" :value="o.value" :key="o.value">{{o.label}}</option></select></label><label>제목<input v-model="form.title" required/></label><label>내용<textarea v-model="form.content" rows="8" required/></label><p v-if="error" role="alert">{{error}}</p><BaseButton type="submit">{{submitLabel}}</BaseButton></form></template>
<style scoped>.complaint-form{display:grid;gap:12px}.complaint-form label{display:grid;gap:6px}.complaint-form :is(input,select,textarea){padding:10px;border:1px solid #c8dce9;border-radius:8px;font:inherit}</style>
