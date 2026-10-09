import { computed, reactive, ref, watch } from 'vue'
import {useRoute} from 'vue-router'
import {mainApi,errorMessage} from '../api/clients'
import {complaintBase,mapComplaint} from '../services/complaintApi'

export function useComplaintList() {
 const draft=reactive({keyword:'',status:''}), applied=reactive({...draft})
 const page=ref(1), size=10, selected=ref([])
 const route=useRoute(),rows=ref([]),totalPages=ref(1),error=ref('');let sequence=0
 async function load(){const request=++sequence;try{const r=(await mainApi.get(complaintBase(String(route.name).startsWith('admin-')),{params:{...applied,page:page.value,size}})).data;if(request!==sequence)return;rows.value=r.content.map(mapComplaint);totalPages.value=Math.max(1,r.totalPages);error.value=''}catch(e){if(request===sequence)error.value=errorMessage(e)}}
 watch(()=>[page.value,JSON.stringify(applied)],load,{immediate:true})
 const pages=computed(()=>{const start=Math.floor((page.value-1)/5)*5+1;return Array.from({length:Math.min(5,totalPages.value-start+1)},(_,i)=>start+i)})
 const allSelected=computed(()=>rows.value.length>0&&rows.value.every(c=>selected.value.includes(c.id)))
 function search(){Object.assign(applied,{...draft,keyword:draft.keyword.trim()});if(page.value===1)load();else page.value=1;selected.value=[]}
 function move(next){page.value=Math.max(1,Math.min(totalPages.value,next));selected.value=[]}
 function selectAll(checked){selected.value=checked?rows.value.map(c=>c.id):[]}
 return {error,draft,page,rows,pages,totalPages,selected,allSelected,search,move,selectAll}
}

