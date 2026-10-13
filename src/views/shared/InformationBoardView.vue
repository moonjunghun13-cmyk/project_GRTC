<script setup>
import { computed, ref, watch, onBeforeUnmount } from 'vue'
import { currentUser } from '../../stores/currentUser'
import { canAccessAdminPath } from '../../utils/adminAccess'
import ListSummaryCards from '../../components/admin/ListSummaryCards.vue'
import { boardExamples } from '../../data/boardExamples'
import boardBanner from '../../assets/min.png'
import { Megaphone, FolderOpen, Search, FileText, ArrowLeft, Upload, X } from 'lucide-vue-next'
const props=defineProps({kind:{type:String,required:true},admin:Boolean})
const showRouteMap=computed(()=>props.admin&&props.kind==='resources'&&canAccessAdminPath(currentUser.value,'/dashboard/operations'))
const isNotice=computed(()=>props.kind==='notices')
const title=computed(()=>isNotice.value?'공지사항':'자료실')
const categories=computed(()=>isNotice.value?['일반 안내','서비스 안내','이용 안내']:['이용 안내','서식','기타'])
const keyword=ref(''),searchTerm=ref(''),category=ref(''),writing=ref(false)
const draft=ref({title:'',category:'',content:''})
const fileInput=ref(null),attachments=ref([]),dragging=ref(false)
let fileSequence=0
function addFiles(files){
 for(const file of Array.from(files||[])){
  if(!attachments.value.some(item=>item.file.name===file.name&&item.file.size===file.size&&item.file.lastModified===file.lastModified))attachments.value.push({id:++fileSequence,file})
 }
 dragging.value=false
}
function selectFiles(event){addFiles(event.target.files);event.target.value=''}
function dropFiles(event){addFiles(event.dataTransfer.files)}
function fileSize(bytes){return bytes<1024?bytes+' B':bytes<1024*1024?(bytes/1024).toFixed(1)+' KB':(bytes/1024/1024).toFixed(1)+' MB'}
watch(writing,()=>{attachments.value=[];dragging.value=false})

// Preview data is separate from the page so a real provider can replace it later.
const previewBoards=ref(Object.fromEntries(Object.entries(boardExamples).map(([key,items])=>[key,items.map(item=>({...item,attachments:[...item.attachments]}))])))
const entries=computed(()=>previewBoards.value[props.kind]??[])
const editingId=ref(null),previewMessage=ref('')
const summaryItems=computed(()=>[{label:'전체 '+title.value,value:'',count:entries.value.length},...categories.value.map(category=>({label:category,value:category,count:entries.value.filter(item=>item.category===category).length})),{label:'첨부파일 포함',value:'__attachments',count:entries.value.filter(item=>item.attachments?.length).length}])
const filtered=computed(()=>entries.value.filter(item=>(!category.value||(category.value==='__attachments'?item.attachments?.length:item.category===category.value))&&(!searchTerm.value||[item.title,item.content].some(value=>String(value??'').includes(searchTerm.value)))))
const selectedEntry=ref(null)
watch(()=>props.kind,()=>{selectedEntry.value=null})
const page=ref(1),pageSize=ref(5),tableViewport=ref(null)
let listObserver,measureFrame
function fitRows(){
 const viewport=tableViewport.value
 if(!viewport)return
 const header=viewport.querySelector('thead'),row=viewport.querySelector('tbody tr')
 const rowHeight=Math.max(48,row?.getBoundingClientRect().height||48)
 const available=viewport.clientHeight-(header?.getBoundingClientRect().height||44)
 const capacity=Math.max(1,Math.floor(available/rowHeight))
 if(pageSize.value!==capacity){const firstItem=(page.value-1)*pageSize.value;pageSize.value=capacity;page.value=Math.floor(firstItem/capacity)+1}
}
watch(tableViewport,viewport=>{
 listObserver?.disconnect();cancelAnimationFrame(measureFrame)
 if(!viewport)return
 listObserver=new ResizeObserver(()=>{cancelAnimationFrame(measureFrame);measureFrame=requestAnimationFrame(fitRows)})
 listObserver.observe(viewport)
},{flush:'post'})
onBeforeUnmount(()=>{listObserver?.disconnect();cancelAnimationFrame(measureFrame)})
const totalPages=computed(()=>Math.max(1,Math.ceil(filtered.value.length/pageSize.value)))
const visibleEntries=computed(()=>filtered.value.slice((page.value-1)*pageSize.value,page.value*pageSize.value))
const pageButtons=computed(()=>{const start=Math.floor((page.value-1)/5)*5+1;return Array.from({length:Math.min(5,totalPages.value-start+1)},(_,i)=>start+i)})
watch([()=>props.kind,category,searchTerm],()=>{page.value=1})
watch(()=>props.kind,()=>{keyword.value='';searchTerm.value='';category.value='';writing.value=false;draft.value={title:'',category:'',content:''}})
function startWriting(){editingId.value=null;previewMessage.value='';draft.value={title:'',category:categories.value[0],content:''};writing.value=true}
function editEntry(entry){
 if(!props.admin)return
 editingId.value=entry.id;draft.value={title:entry.title,category:entry.category,content:entry.content};selectedEntry.value=null;writing.value=true;previewMessage.value=''
}
function savePreview(){
 if(!props.admin||!editingId.value)return
 if(!draft.value.title.trim()||!draft.value.content.trim()){previewMessage.value='제목과 내용을 입력해 주세요.';return}
 const entry=entries.value.find(item=>item.id===editingId.value)
 if(!entry)return
 Object.assign(entry,{title:draft.value.title.trim(),category:draft.value.category,content:draft.value.content.trim(),attachments:[...entry.attachments,...attachments.value.map(item=>({name:item.file.name,file:item.file,previewOnly:true}))]})
 writing.value=false;selectedEntry.value=entry;editingId.value=null;previewMessage.value='예시 글을 수정했습니다. 서버에는 저장되지 않으며 새로고침하면 원래 데이터로 돌아갑니다.'
}
function deleteEntry(entry){
 if(!props.admin||!window.confirm('이 예시 글을 삭제할까요? 서버 데이터는 변경되지 않으며 새로고침하면 복원됩니다.'))return
 previewBoards.value[props.kind]=entries.value.filter(item=>item.id!==entry.id);selectedEntry.value=null;writing.value=false;page.value=Math.min(page.value,totalPages.value);previewMessage.value='예시 글을 삭제했습니다. 새로고침하면 복원됩니다.'
}
watch(()=>props.kind,()=>{editingId.value=null;previewMessage.value=''})
</script>
<template>
 <section class="information-board" :class="{'citizen-board':!admin,'admin-board':admin,'board-browsing':admin&&!writing&&!selectedEntry}" :aria-label="title">
  <header class="board-heading" :style="{ '--board-banner': `url(${boardBanner})` }"><div class="board-heading-icon"><component :is="isNotice?Megaphone:FolderOpen" :size="28" /></div><div><p>{{admin?'관리자 게시판':'광주교통공사 안내'}}</p><h1>{{title}}</h1><span>{{isNotice?'광주교통공사의 새로운 소식과 이용 안내를 확인하세요.':'서비스 이용에 필요한 안내 자료와 서식을 확인하세요.'}}</span></div></header>
  
  
  <p v-if="previewMessage" role="status" class="draft-notice">{{previewMessage}}</p><section v-if="selectedEntry" class="board-card board-detail" aria-label="게시글 상세보기"><header class="detail-heading"><div><span>{{selectedEntry.category}} · {{selectedEntry.createdAt}}</span><h2>{{selectedEntry.title}}</h2></div><button type="button" class="secondary" @click="selectedEntry=null"><ArrowLeft :size="16" /> 목록으로</button></header><div class="detail-body"><p>{{selectedEntry.content}}</p><section v-if="selectedEntry.attachments.length" class="detail-files"><h3>첨부파일</h3><p v-for="file in selectedEntry.attachments" :key="file.name"><FileText :size="18" /> {{file.name}} <span>(예시 파일 · 다운로드 미지원)</span></p></section></div><footer v-if="admin" class="detail-actions"><button type="button" class="secondary" @click="editEntry(selectedEntry)">수정</button><button type="button" class="secondary delete-preview-button" @click="deleteEntry(selectedEntry)">삭제</button></footer></section><section v-else-if="admin&&writing" class="board-card board-editor" aria-label="게시글 작성">
   <div class="editor-heading"><h2>{{title}} {{editingId?'수정':'작성'}}</h2><button type="button" class="secondary" @click="writing=false"><ArrowLeft :size="16" /> 목록으로</button></div>
   <form @submit.prevent="savePreview">
    <label>분류<select v-model="draft.category"><option v-for="option in categories" :key="option">{{option}}</option></select></label>
    <label>제목<input v-model="draft.title" placeholder="제목을 입력해 주세요" maxlength="200" /></label>
    <label>내용<textarea v-model="draft.content" rows="10" placeholder="안내 내용을 입력해 주세요" /></label>
    <section class="board-attachments" aria-label="첨부파일">
     <div class="attachment-heading"><strong>첨부파일</strong><span>{{attachments.length}}개 선택</span></div>
     <input ref="fileInput" class="board-file-input" type="file" multiple aria-label="첨부파일 선택" @change="selectFiles" />
     <div class="file-drop-zone" :class="{dragging}" @dragover.prevent="dragging=true" @dragleave.prevent="dragging=false" @drop.prevent="dropFiles"><Upload :size="25" aria-hidden="true"/><div><strong>파일을 끌어 놓거나 선택해 주세요</strong><p>여러 파일을 함께 선택할 수 있습니다.</p></div><button type="button" class="secondary" @click="fileInput?.click()">파일 선택</button></div>
     <ul v-if="attachments.length" class="selected-files"><li v-for="item in attachments" :key="item.id"><FileText :size="18" aria-hidden="true"/><span class="selected-file-name" :title="item.file.name">{{item.file.name}}</span><span class="selected-file-size">{{fileSize(item.file.size)}}</span><button type="button" class="remove-file" :aria-label="item.file.name+' 첨부 제거'" @click="attachments=attachments.filter(file=>file.id!==item.id)"><X :size="17" /></button></li></ul>
     <p class="file-storage-notice">현재는 파일 선택만 가능합니다. 실제 업로드·저장은 서버 연결 후 지원됩니다.</p>
    </section>
    <p class="draft-notice">입력한 내용은 서버에 저장되지 않습니다. 목록으로 돌아가거나 페이지를 떠나면 작성 내용이 유지되지 않습니다.</p>
    <div class="editor-actions"><button type="button" class="secondary" @click="writing=false">취소</button><button v-if="editingId" type="submit" class="primary">수정 확인 (미리보기)</button><button v-else type="button" class="primary" disabled>등록 (서버 연결 예정)</button></div>
   </form>
  </section>
  <template v-else>
   <ListSummaryCards v-if="admin" :items="summaryItems" :active-key="category" @select="category=$event.value" :kind="isNotice?'file':'resource'"/><form class="board-search board-card" @submit.prevent="searchTerm=keyword.trim()"><select v-model="category" aria-label="분류 필터"><option value="">전체 분류</option><option v-for="option in categories" :key="option">{{option}}</option><option v-if="admin" value="__attachments">첨부파일 포함</option></select><div class="search-input"><Search :size="18" aria-hidden="true"/><input v-model="keyword" :placeholder="title+' 제목 또는 내용 검색'" aria-label="검색어" /></div><button class="primary" type="submit">검색</button><button v-if="admin" class="primary" type="button" @click="startWriting">+ {{title}} 작성</button><RouterLink v-if="showRouteMap" to="/dashboard/operations" class="resource-map-link">노선도 <span aria-hidden="true">↗</span></RouterLink></form>
   <div class="board-card board-list"><div class="list-heading"><h2>{{title}} 목록</h2><div class="list-heading-actions"><span>총 {{filtered.length}}건</span></div></div><div ref="tableViewport" class="board-table-scroll"><table><thead><tr><th class="number">번호</th><th class="category">분류</th><th>제목</th><th class="date">등록일</th><th v-if="!isNotice" class="file">첨부파일</th></tr></thead><tbody><tr v-if="!filtered.length"><td :colspan="isNotice?4:5"><div class="board-empty"><component :is="isNotice?Megaphone:FolderOpen" :size="44" aria-hidden="true"/><strong>검색 결과가 없습니다</strong><p>검색어 또는 분류를 변경해 주세요.</p></div></td></tr><tr v-for="entry in visibleEntries" :key="entry.id"><td>{{entry.number}}</td><td>{{entry.category}}</td><td><button type="button" class="board-title-button text-button" @click="selectedEntry=entry">{{entry.title}}</button></td><td>{{entry.createdAt}}</td><td v-if="!isNotice"><span v-if="entry.attachments?.length" :title="entry.attachments.map(file=>file.name).join(', ')">{{entry.attachments.length}}개 (예시)</span><span v-else>—</span></td></tr></tbody></table></div></div><nav class="board-pagination" aria-label="게시판 페이지 이동"><button type="button" :disabled="page===1" @click="page--">이전</button><button v-for="number in pageButtons" :key="number" type="button" :class="{active:page===number}" :aria-current="page===number?'page':undefined" @click="page=number">{{number}}</button><button type="button" :disabled="page===totalPages" @click="page++">다음</button></nav>
  </template>
 </section>
</template>
<style scoped>
.information-board{max-width:1600px;width:100%;margin:0 auto;color:#163D58;box-sizing:border-box}.board-heading{display:flex;align-items:center;gap:18px;margin-bottom:24px}.board-heading-icon{display:grid;place-items:center;width:60px;height:60px;border-radius:16px;background:#E8F4FB;color:#0078AE;flex-shrink:0}.board-heading p{margin:0 0 5px;font-size:13px;color:#637C8D}.board-heading h1{font-size:28px;margin:0 0 8px;font-weight:800}.board-heading span{font-size:15px;color:#62788A}.connection-notice{padding:14px 18px;margin:0 0 20px;border-radius:10px;background:#EEF5FB;color:#476578;font-size:14px;line-height:1.6}.board-card{background:#FFF;border:1px solid #D5E4EF;border-radius:14px}.board-search{display:flex;align-items:center;gap:12px;padding:18px;margin-bottom:20px}.information-board input,.information-board select,.information-board textarea{border:1px solid #ADC8DC;border-radius:7px;background:white;color:#163D58;font:inherit;padding:11px 12px;box-sizing:border-box;min-width:0}.search-input{display:flex;align-items:center;gap:8px;flex:1;border:1px solid #ADC8DC;border-radius:7px;padding-left:12px}.search-input input{width:100%;border:0;background:transparent}.information-board button{display:inline-flex;align-items:center;justify-content:center;gap:6px;padding:11px 18px;font:inherit;font-weight:700;border-radius:7px;cursor:pointer;white-space:nowrap}.primary{background:#0078AE;color:white;border:1px solid #0078AE}.primary:hover:not(:disabled){background:#005F8B}.secondary{background:white;color:#245577;border:1px solid #ADC8DC}.information-board button:disabled{opacity:.55;cursor:not-allowed}.information-board :is(button,input,select,textarea):focus-visible{outline:2px solid #0078AE;outline-offset:3px}.list-heading,.editor-heading{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:20px 24px}.list-heading h2,.editor-heading h2{margin:0;font-size:19px}.list-heading span{font-size:14px;color:#62788A}.board-table-scroll{overflow-x:auto;border-radius:0 0 14px 14px}.board-list table{width:100%;min-width:640px;border-collapse:collapse;table-layout:fixed}.board-list th{background:#006BA6;color:#FFF;font-size:14px;padding:15px}.board-list td{padding:16px;border-bottom:1px solid #E0EBF2;text-align:center;overflow-wrap:anywhere}.number{width:60px}.category{width:120px}.date{width:130px}.file{width:85px}.board-empty{min-height:290px;display:flex;align-items:center;justify-content:center;flex-direction:column;gap:16px;color:#70899B}.board-empty strong{color:#325B76;font-size:19px}.board-empty p{font-size:14px;margin:0}.board-editor form{padding:0 24px 24px;display:grid;gap:18px}.board-editor label{display:grid;gap:8px;font-size:14px;font-weight:700}.board-editor textarea{resize:vertical;line-height:1.7}.attachment-placeholder{display:flex;align-items:center;gap:12px;background:#F4F8FB;padding:14px;border-radius:8px;font-size:14px}.attachment-placeholder span{flex:1}.draft-notice{font-size:13px;color:#677F90;margin:0;line-height:1.6}.editor-actions{display:flex;justify-content:flex-end;gap:10px}.citizen-board{max-width:1400px}.citizen-board .board-heading-icon{border-radius:50%}@media(max-width:750px){.board-search{flex-wrap:wrap;padding:14px}.search-input{flex-basis:100%;order:-1}.board-search select{flex:1}.board-heading h1{font-size:24px}.board-heading{align-items:flex-start}.attachment-placeholder{flex-wrap:wrap}.editor-heading,.list-heading{padding:16px}.board-editor form{padding:0 16px 16px}.board-heading span{line-height:1.6;display:block}}
</style>
<style scoped>
/* Fit the board into the available layout height; scroll only long lists. */
@media(min-width:1001px) and (min-height:700px){
.information-board{height:100%;min-height:0;display:flex;flex-direction:column}
.board-heading{margin-bottom:16px;flex-shrink:0}
.connection-notice{margin-bottom:16px;flex-shrink:0}
.board-search{margin-bottom:16px;flex-shrink:0}
.board-list{display:flex;flex-direction:column;flex:1;min-height:0}
.list-heading{flex-shrink:0}
.board-table-scroll{flex:1;min-height:0;overflow:auto}
.board-list th{position:sticky;top:0;z-index:1}
.board-editor{min-height:0;overflow:auto}
}
</style><style scoped>
.board-heading{background-image:var(--board-banner);background-size:cover;background-position:center;min-height:200px;padding:24px 36px;box-sizing:border-box;border-radius:14px;flex-shrink:0}
.board-heading-icon{display:none}
.board-heading>div:last-child{max-width:60%}
.board-heading h1{font-size:36px;color:#006BA6;margin-bottom:12px}
.board-heading p{color:#476A82}
.board-heading span{display:block;color:#385C74;line-height:1.65}
@media(max-width:750px){.board-heading{min-height:165px;padding:22px 20px;background-position:60% center}.board-heading>div:last-child{max-width:68%}.board-heading h1{font-size:28px}.board-heading span{font-size:13px}}
</style>
<style scoped>
.resource-map-navigation{flex-shrink:0;margin-bottom:16px}
.resource-map-link{display:inline-flex;align-items:center;justify-content:center;gap:14px;min-height:42px;padding:0 18px;background:#0078AE;color:white;border:1px solid #0078AE;border-radius:7px;text-decoration:none;font-weight:700;font-size:14px}
.resource-map-link:hover{background:#005F8B}.resource-map-link:focus-visible{outline:3px solid #62B5D4;outline-offset:3px}
</style>

<style scoped>.list-heading-actions{display:flex;align-items:center;gap:14px}.list-heading-actions .resource-map-link{min-height:34px;padding:0 12px;font-size:13px}.list-heading-actions .resource-map-link span{color:inherit}</style>
<style scoped>
.list-heading-actions .resource-map-link{background:#E5F2FA;color:#245B7C;border-color:#BFDCEA}
.list-heading-actions .resource-map-link:hover{background:#D3EAF7;color:#164B6E}
</style>
<style scoped>
.board-attachments{display:flex;flex-direction:column;gap:12px;min-width:0}.attachment-heading{display:flex;justify-content:space-between;align-items:center;gap:12px;font-size:14px}.attachment-heading span{font-size:12px;color:#6B8394}.information-board .board-file-input{display:none}.file-drop-zone{display:flex;align-items:center;gap:14px;padding:22px;border:1px dashed #9ABFD5;border-radius:10px;background:#F5FAFD;color:#356683}.file-drop-zone>div{flex:1;min-width:0}.file-drop-zone strong{font-size:14px}.file-drop-zone p{margin:6px 0 0;font-size:12px;color:#70899B}.file-drop-zone.dragging{background:#E2F2FB;border-color:#0078AE}.selected-files{list-style:none;margin:0;padding:0;display:flex;flex-direction:column;gap:8px}.selected-files li{display:flex;align-items:center;gap:10px;padding:10px 12px;background:white;border:1px solid #D5E4EF;border-radius:8px;min-width:0}.selected-file-name{flex:1;min-width:0;overflow-wrap:anywhere;font-size:13px}.selected-file-size{font-size:12px;color:#70899B;white-space:nowrap}.information-board .remove-file{padding:5px;border:0;background:transparent;color:#668297;flex-shrink:0}.information-board .remove-file:hover{background:#EFF6FB;color:#BA3550}.file-storage-notice{margin:0;font-size:12px;color:#6B8394;line-height:1.6}@media(max-width:600px){.file-drop-zone{flex-wrap:wrap;padding:16px}.file-drop-zone .secondary{width:100%}}
</style>

<style scoped>
.board-heading{min-height:150px;height:150px;padding:16px 28px}
.board-heading h1{font-size:32px;margin-bottom:8px}.board-heading span{font-size:14px;line-height:1.5}
.board-search{padding:12px 16px}.board-search :is(input,select,button){height:38px;padding-top:0;padding-bottom:0}.search-input{height:38px;box-sizing:border-box}
.list-heading{padding:12px 20px}.board-list th{padding:12px}.board-list td{height:44px;box-sizing:border-box;padding:10px 16px}
.board-pagination{display:flex;justify-content:center;gap:7px;margin-top:16px;flex-shrink:0}
.information-board .board-pagination button{height:34px;min-width:34px;padding:0 10px;border:1px solid #ADC8DC;background:white;color:#163D58;border-radius:6px;font-size:13px;font-weight:400}
.information-board .board-pagination button.active{background:#0078AE;color:white;border-color:#0078AE}
@media(max-width:750px){.board-heading{height:auto;min-height:130px;padding:16px 20px}.board-heading h1{font-size:28px}.board-heading span{font-size:13px}}
</style>
<style scoped>
/* Keep notice and resource list headings and rows at identical heights. */
.list-heading{height:60px;min-height:60px;box-sizing:border-box}
.board-list th{height:44px;padding:10px 12px;box-sizing:border-box}
.board-list td{height:48px;padding:8px 12px;box-sizing:border-box}
.board-list th,.board-list td{vertical-align:middle}
.board-list .file{width:100px}
.board-list td:last-child{white-space:nowrap}
@media(max-width:600px){.list-heading{padding-inline:16px}}
</style><style scoped>
.information-board .board-title-button{border:0;background:transparent;padding:0;color:#006BA6;font-size:14px;font-weight:600;white-space:normal;text-align:center}
.board-title-button:hover{text-decoration:underline}.board-detail{display:flex;flex-direction:column;flex:1;min-height:0}.detail-heading{display:flex;justify-content:space-between;align-items:flex-start;gap:16px;padding:24px;border-bottom:1px solid #D5E4EF}.detail-heading span{font-size:13px;color:#6B8394}.detail-heading h2{font-size:23px;margin:12px 0 0;overflow-wrap:anywhere}.detail-body{padding:24px;overflow:auto;min-height:0;line-height:1.8;overflow-wrap:anywhere}.detail-body>p{white-space:pre-wrap;margin:0}.detail-files{border-top:1px solid #D5E4EF;margin-top:28px;padding-top:18px}.detail-files h3{font-size:16px;margin:0 0 12px}.detail-files p{display:flex;align-items:center;gap:8px;flex-wrap:wrap;font-size:13px}.detail-files span{color:#6B8394}@media(max-width:600px){.detail-heading{flex-wrap:wrap;padding:18px}.detail-body{padding:18px}}
</style>
<style scoped>.detail-actions{display:flex;gap:8px;flex-wrap:wrap}.information-board .delete-preview-button{color:#BA3550;border-color:#E4BCC6}</style>

<style scoped>.board-detail .detail-body{flex:1}.board-detail .detail-actions{justify-content:flex-end;flex-shrink:0;padding:16px 24px;border-top:1px solid #D5E4EF}@media(max-width:600px){.board-detail .detail-actions{padding:14px 18px}}</style>
