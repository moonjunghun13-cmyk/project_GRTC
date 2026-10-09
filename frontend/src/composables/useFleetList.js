import {API_INTEGRATION_ENABLED} from '../api/clients'
import {useLiveFleet} from './useLiveFleet'
import { ref,reactive,computed } from 'vue'
import { getFleetSource } from '../services/mockFleet.js'
export function useFleetList(kind){if(API_INTEGRATION_ENABLED)return useLiveFleet(kind)
 const source=getFleetSource(kind),draft=reactive({keyword:'',date:'',vehicleId:'',driver:'',status:''}),applied=reactive({...draft})
 const page=ref(1),size=10,selected=ref([])
 const base=computed(()=>source.rows.filter(c=>{
  const keyword=applied.keyword.toLowerCase()
  return (!keyword||(kind==='vehicles'?[c.vehicleNo]:[c.dispatchNo,c.vehicleNo,c.driverName]).some(v=>v.toLowerCase().includes(keyword)))&&(!applied.date||c.dispatchDate===applied.date)&&(!applied.vehicleId||c.vehicleId===applied.vehicleId)&&(!applied.driver||c.driverName===applied.driver)
 }))
 const filtered=computed(()=>base.value.filter(c=>!applied.status||c.status===applied.status))
 const summary=computed(()=>{const items=kind==='vehicles'?source.rows:base.value;return [{label:kind==='vehicles'?'전체 차량':'전체 배차',value:items.length,tone:'all',status:''},...source.statuses.map(o=>({...o,status:o.value,value:items.filter(c=>c.status===o.value).length}))]})
 const totalPages=computed(()=>Math.max(1,Math.ceil(filtered.value.length/size))),rows=computed(()=>filtered.value.slice((page.value-1)*size,page.value*size))
 const requestPage=computed(()=>page.value-1) // General API page=0; UI page=1.
 const pages=computed(()=>{const start=Math.floor((page.value-1)/5)*5+1;return Array.from({length:Math.min(5,totalPages.value-start+1)},(_,i)=>start+i)})
 const allSelected=computed(()=>rows.value.length>0&&rows.value.every(c=>selected.value.includes(c.id)))
 function search(){Object.assign(applied,{...draft,keyword:draft.keyword.trim()});page.value=1;selected.value=[]}
 function move(p){page.value=Math.max(1,Math.min(totalPages.value,p));selected.value=[]}
 function selectAll(checked){selected.value=checked?rows.value.map(c=>c.id):[]}
 return {appliedStatus:computed(()=>applied.status),source,draft,page,size,requestPage,rows,summary,totalPages,pages,selected,allSelected,search,move,selectAll}
}

