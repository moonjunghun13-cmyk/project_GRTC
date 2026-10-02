import { computed, reactive, ref } from 'vue'
import { mockComplaints } from '../mocks/complaints'
export function useComplaintList() {
 const draft=reactive({keyword:'',category:'',status:''}), applied=reactive({...draft})
 const page=ref(1), size=10, selected=ref([])
 const filtered=computed(()=>mockComplaints.filter(c=>(!applied.category||c.category===applied.category)&&(!applied.status||c.status===applied.status)&&(!applied.keyword||[c.title,c.content,c.authorName].some(v=>String(v||'').toLowerCase().includes(applied.keyword.toLowerCase())))))
 const totalPages=computed(()=>Math.max(1,Math.ceil(filtered.value.length/size)))
 const rows=computed(()=>filtered.value.slice((page.value-1)*size,page.value*size))
 const pages=computed(()=>{const start=Math.floor((page.value-1)/5)*5+1;return Array.from({length:Math.min(5,totalPages.value-start+1)},(_,i)=>start+i)})
 const allSelected=computed(()=>rows.value.length>0&&rows.value.every(c=>selected.value.includes(c.id)))
 function search(){Object.assign(applied,{...draft,keyword:draft.keyword.trim()});page.value=1;selected.value=[]}
 function move(next){page.value=Math.max(1,Math.min(totalPages.value,next));selected.value=[]}
 function selectAll(checked){selected.value=checked?rows.value.map(c=>c.id):[]}
 return {draft,page,rows,pages,totalPages,selected,allSelected,search,move,selectAll}
}
