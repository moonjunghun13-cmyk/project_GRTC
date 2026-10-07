<script setup>
import {ref} from 'vue'
import {downloadAttachment} from '../../services/attachmentApi'
import {errorMessage} from '../../api/clients'
defineProps({attachments:{type:Array,default:()=>[]}})
const busy=ref(null),error=ref('')
async function download(file){if(busy.value!==null)return;error.value='';busy.value=file.id;try{await downloadAttachment(file)}catch(e){if(e.response?.data instanceof Blob){try{const body=JSON.parse(await e.response.data.text());error.value=body.error?.message||body.message||errorMessage(e)}catch{error.value=errorMessage(e)}}else error.value=errorMessage(e)}finally{busy.value=null}}
</script>
<template><section class="complaint-attachments"><h3>첨부파일</h3><ul v-if="attachments.length"><li v-for="file in attachments" :key="file.id"><a :href="file.downloadUrl" @click.prevent="download(file)" :aria-busy="busy===file.id"><span>📎 {{file.originalFileName || file.name}}</span><span>{{busy===file.id?'다운로드 중…':'다운로드'}}</span></a></li></ul><p v-else>첨부파일이 없습니다.</p><p v-if="error" role="alert">{{error}}</p></section></template>

<style scoped>.complaint-attachments ul{list-style:none;padding:0;margin:0;display:grid;gap:10px}.complaint-attachments a{display:flex;justify-content:space-between;gap:18px;padding:12px 14px;border:1px solid #e0eaf1;border-radius:9px;color:#065782;text-decoration:none;font-size:15px}.complaint-attachments a span:first-child{min-width:0;overflow-wrap:anywhere}.complaint-attachments a span:last-child{white-space:nowrap}.complaint-attachments p{color:#788996}.complaint-attachments a:hover{background:#f4faff}</style>

