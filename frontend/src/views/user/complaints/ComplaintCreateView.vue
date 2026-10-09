<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import write from '../../../assets/write.png'
import ComplaintEditor from '../../../components/complaints/ComplaintEditor.vue'
import { complaintEditorConfig } from '../../../mocks/complaintForm'
import { currentUser } from '../../../stores/currentUser'
import { createComplaint, complaintOptions } from '../../../services/complaintApi'
import {errorMessage} from '../../../api/clients'

const complaintTypes=reactive([]),categories=reactive([])
const router = useRouter()
const route = useRoute()
const listPath = computed(()=>route.name==='admin-complaint-create'?'/dashboard/complaints':'/complaints')
function cancel(){router.push({name:route.name==='admin-complaint-create'?'admin-complaints':'user-complaints'})}
const user = currentUser
const draft = reactive({type:'',category:'',title:'',content:''})
const errors = ref({})
const files = ref([])
const fileError = ref('')
const fileInput = ref(null)
const busy = ref(false)
function chooseFiles(event) {
 const incoming = Array.from(event.target.files || [])
 const valid = incoming.filter(file => file.type.startsWith('image/') || file.type === 'application/pdf')
 fileError.value = valid.length !== incoming.length ? '이미지 또는 PDF 파일만 선택해 주세요.' : ''
 files.value.push(...valid)
 event.target.value=''
}
async function submit() {
 if (busy.value) return
 const next={}
 if (!complaintTypes.some(item=>item.value===draft.type)) next.type='민원유형을 선택해 주세요.'
 if (!categories.some(o=>o.value===draft.category)) next.category='분류를 선택해 주세요.'
 if (!draft.title.trim()) next.title='제목을 입력해 주세요.'
 if (!draft.content.trim()) next.content='민원 내용을 입력해 주세요.'
 else if (Array.from(draft.content).length > complaintEditorConfig.maxLength) next.content='내용은 5,000자까지 입력할 수 있습니다.'
 errors.value=next
 if (Object.keys(next).length) return
 busy.value=true
 try { await createComplaint(draft,files.value,route.name==='admin-complaint-create'); await router.push(listPath.value) }
 catch(error) { fileError.value=errorMessage(error); busy.value=false }
}
complaintOptions(route.name==='admin-complaint-create').then(o=>{complaintTypes.splice(0,complaintTypes.length,...o.types.map(s=>({value:s.code,label:s.label})));categories.splice(0,categories.length,...o.categories.map(s=>({value:s.code,label:s.label})))}).catch(e=>fileError.value=errorMessage(e))
</script>
<template>
 <section class="complaint-write-page" aria-labelledby="write-title">
  <header class="write-heading"><h1 id="write-title">민원 양식</h1><div class="write-decoration"><div class="write-bubble">소중한 의견을 기다려요</div><img :src="write" alt="의견을 기다리는 광주교통공사 캐릭터" /></div><svg class="write-wave" viewBox="0 0 1000 12" preserveAspectRatio="none" aria-hidden="true"><path d="M0 6 Q25 0 50 6 T100 6 T150 6 T200 6 T250 6 T300 6 T350 6 T400 6 T450 6 T500 6 T550 6 T600 6 T650 6 T700 6 T750 6 T800 6 T850 6 T900 6 T950 6 T1000 6" /></svg></header>
  <form class="write-form" novalidate @submit.prevent="submit">
   <div class="write-first-row"><div class="write-row"><label for="complaint-type">민원유형 <span>*</span></label><div class="write-field"><select id="complaint-type" v-model="draft.type" required :aria-invalid="!!errors.type" :aria-describedby="errors.type ? 'type-error' : undefined"><option value="">선택해 주세요</option><option v-for="item in complaintTypes" :key="item.value" :value="item.value">{{ item.label }}</option></select><p v-if="errors.type" id="type-error" class="write-error">{{ errors.type }}</p></div></div><div class="write-row"><label for="complaint-author">민원인 <span>*</span></label><input id="complaint-author" :value="user.name" readonly /></div></div>
   <div class="write-row"><label for="complaint-category">분류 <span>*</span></label><div class="write-field"><select id="complaint-category" v-model="draft.category" required :aria-invalid="!!errors.category"><option value="">선택해 주세요</option><option v-for="o in categories" :key="o.value" :value="o.value">{{o.label}}</option></select><p v-if="errors.category" class="write-error">{{errors.category}}</p></div></div>
   <div class="write-row"><label for="complaint-title">제목 <span>*</span></label><div class="write-field"><input id="complaint-title" v-model="draft.title" required placeholder="제목을 입력해 주세요." :aria-invalid="!!errors.title" :aria-describedby="errors.title ? 'title-error' : undefined" /><p v-if="errors.title" id="title-error" class="write-error">{{ errors.title }}</p></div></div>
   <div class="write-row write-content-row"><label id="content-label">내용 <span>*</span></label><div class="write-field write-content-field"><ComplaintEditor :max-length="complaintEditorConfig.maxLength" :error="errors.content" @update="draft.content=$event.text" /><p id="complaint-content-error" class="write-error" v-if="errors.content">{{ errors.content }}</p></div></div>
   <div class="write-row"><label for="complaint-files">첨부파일</label><div class="write-field"><div class="write-file-controls"><button type="button" class="write-file-button" @click="fileInput.click()">파일 선택</button><input id="complaint-files" ref="fileInput" class="write-file-input" type="file" accept="image/*,application/pdf" multiple @change="chooseFiles" /><span>이미지 또는 PDF 파일을 첨부할 수 있습니다.</span></div><ul v-if="files.length" class="write-files"><li v-for="(file,index) in files" :key="index"><span>{{ file.name }}</span><button type="button" :aria-label="file.name + ' 제거'" @click="files.splice(index,1)">×</button></li></ul><p v-if="fileError" class="write-error" role="alert">{{ fileError }}</p></div></div>
   <div class="write-actions"><button type="button" class="write-cancel" @click="cancel">취소</button><button type="submit" class="write-submit" :disabled="busy">등록하기</button></div>
  </form>
 </section>
</template>
<style scoped>
.complaint-write-page { min-height: 100%; display: flex; flex-direction: column; color: #064b76; }
.write-heading { position: relative; display: flex; align-items: flex-end; justify-content: space-between; flex-shrink: 0; min-height: 165px; padding-bottom: 18px; margin-bottom: 20px; gap: 20px; }
.write-heading h1 { font-size: 34px; font-weight: 800; margin: 0 0 8px; }
.write-decoration { width: 400px; max-width: 55%; display: flex; flex-direction: column; align-items: center; }
.write-decoration img { display: block; width: 100%; height: auto; }
.write-bubble { position: relative; padding: 8px 18px; margin-bottom: 10px; border: 1px solid #acd8ed; border-radius: 16px; background: white; font-size: 16px; font-weight: 700; white-space: nowrap; }
.write-bubble::after { content: ''; position: absolute; bottom: -6px; left: 50%; width: 10px; height: 10px; background: white; border-right: 1px solid #acd8ed; border-bottom: 1px solid #acd8ed; transform: rotate(45deg); }
.write-wave { position: absolute; bottom: 0; left: 0; width: 100%; height: 10px; fill: none; stroke: #85bedb; stroke-width: 1.5; }
.write-form { display: flex; flex-direction: column; flex: 1; gap: 16px; min-height: 0; }
.write-first-row { display: grid; grid-template-columns: minmax(0,1fr) minmax(0,1fr); gap: 32px; }
.write-row { display: grid; grid-template-columns: 110px minmax(0,1fr); gap: 18px; align-items: start; }
.write-row > label { font-size: 17px; font-weight: 700; padding-top: 12px; }
.write-row > label span { color: #c53740; }
.write-field { min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.write-row input:not([type=file]), .write-row select { width: 100%; min-width: 0; height: 46px; padding: 0 16px; border: 1px solid #c8dce9; border-radius: 9px; background: #fff; color: #28465c; font: inherit; font-size: 17px; }
.write-row input[readonly] { background: #f2f7fa; color: #6c8294; }
.write-row [aria-invalid=true] { border-color: #c53740; }
.write-content-row { flex: 1 1 0; min-height: 190px; grid-template-rows: minmax(0, 1fr); }
.write-content-field { height: 100%; min-height: 0; }
.write-content-field :deep(.complaint-editor) { min-height: 160px; }
.write-error { margin: 0; font-size: 14px; color: #c53740; line-height: 1.4; }
.write-file-controls { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; color: #7c8d9a; font-size: 15px; }
.write-file-button { min-height: 38px; padding: 0 18px; border: 1px solid #b7d1e1; border-radius: 7px; color: #064b76; background: #f8fbfd; font: inherit; cursor: pointer; }
.write-file-input { position: absolute; width: 1px; height: 1px; clip-path: inset(50%); }
.write-files { margin: 0; padding: 0; list-style: none; display: flex; flex-wrap: wrap; gap: 8px; }
.write-files li { display: flex; align-items: center; gap: 8px; max-width: 100%; padding: 4px 10px; background: #edf6fb; border-radius: 6px; font-size: 14px; }
.write-files li span { overflow-wrap: anywhere; }
.write-files button { border: 0; background: transparent; color: #446a83; font-size: 20px; cursor: pointer; }
.write-actions { display: flex; justify-content: flex-end; gap: 12px; flex-shrink: 0; }
.write-actions button { min-height: 46px; padding: 0 28px; border-radius: 9px; font: inherit; font-size: 17px; font-weight: 700; cursor: pointer; transition: background-color 200ms ease-out; }
.write-cancel { border: 1px solid #086b9f; color: #086b9f; background: white; }
.write-submit { border: 1px solid #064b76; color: white; background: #064b76; }
.write-cancel:hover { background: #edf6fb; } .write-submit:hover { background: #033955; }
.write-form :is(input,select,button):focus-visible { outline: 3px solid #3298db; outline-offset: 2px; }
@media (min-width:1001px) and (max-height:850px) { .write-heading { min-height: 130px; margin-bottom: 14px; padding-bottom: 12px; } .write-decoration { width: 285px; } .write-heading h1 { font-size: 32px; } .write-form { gap: 12px; } .write-bubble { padding: 6px 14px; font-size: 15px; margin-bottom: 6px; } }
@media (max-width:1000px) { .complaint-write-page { min-height: auto; } .write-first-row { grid-template-columns: 1fr; gap: 16px; } .write-content-row { min-height: 280px; } .write-heading { min-height: 150px; } .write-decoration { width: 300px; } .write-row { grid-template-columns: 90px minmax(0,1fr); gap: 12px; } }
@media (max-width:650px) { .write-row { grid-template-columns: 1fr; gap: 6px; } .write-row > label { padding-top: 0; } .write-heading h1 { font-size: 30px; } .write-decoration { max-width: 60%; } .write-bubble { font-size: 12px; padding: 6px 8px; } }
</style>



