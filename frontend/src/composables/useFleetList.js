import {API_INTEGRATION_ENABLED} from '../api/clients'
import {useLiveFleet} from './useLiveFleet'
import { ref,reactive,computed } from 'vue'
import { getFleetSource } from '../services/mockFleet.js'
import {buildDepotDispatches} from '../mocks/fleet.js'
import {dayTypeOf,dayTypeLabel} from '../mocks/depotTimetable.js'
import {initialFleetFilters} from './fleetFilters.js'
export {initialFleetFilters}
export function useFleetList(kind){if(API_INTEGRATION_ENABLED)return useLiveFleet(kind)
 const source=getFleetSource(kind),draft=reactive(initialFleetFilters(kind)),applied=reactive({...draft})
 const page=ref(1),size=10,selected=ref([]),error=ref(''),version=ref(0)
 const base=computed(()=>{version.value;return source.rows.filter(c=>{
  const keyword=applied.keyword.toLowerCase()
  return (!keyword||(kind==='vehicles'?[c.vehicleNo]:[c.dispatchNo,c.vehicleNo,c.driverName,c.trainNo||'']).some(v=>v.toLowerCase().includes(keyword)))&&(!applied.date||c.dispatchDate===applied.date)&&(!applied.vehicleId||c.vehicleId===applied.vehicleId)&&(!applied.driver||c.driverName===applied.driver)&&(!applied.moveType||c.moveType===applied.moveType)
 })})
 const filtered=computed(()=>base.value.filter(c=>!applied.status||c.status===applied.status))
 const summary=computed(()=>{const items=kind==='vehicles'?source.rows:base.value;return [{label:kind==='vehicles'?'전체 차량':'전체 배차',value:items.length,tone:'all',status:''},...source.statuses.map(o=>({...o,status:o.value,value:items.filter(c=>c.status===o.value).length}))]})
 const depotCounts=computed(()=>kind==='vehicles'?null:{departs:base.value.filter(c=>c.moveType==='DEPART'&&c.status!=='CANCELLED').length,returns:base.value.filter(c=>c.moveType==='RETURN'&&c.status!=='CANCELLED').length})
 const dayInfo=computed(()=>applied.date?{date:applied.date,dayType:dayTypeOf(applied.date),dayTypeLabel:dayTypeLabel(dayTypeOf(applied.date))}:null)
 const totalPages=computed(()=>Math.max(1,Math.ceil(filtered.value.length/size))),rows=computed(()=>filtered.value.slice((page.value-1)*size,page.value*size))
 const requestPage=computed(()=>page.value-1) // General API page=0; UI page=1.
 const pages=computed(()=>{const start=Math.floor((page.value-1)/5)*5+1;return Array.from({length:Math.min(5,totalPages.value-start+1)},(_,i)=>start+i)})
 const allSelected=computed(()=>rows.value.length>0&&rows.value.every(c=>selected.value.includes(c.id)))
 function search(){Object.assign(applied,{...draft,keyword:draft.keyword.trim()});page.value=1;selected.value=[]}
 function move(p){page.value=Math.max(1,Math.min(totalPages.value,p));selected.value=[]}
 function selectAll(checked){selected.value=checked?rows.value.map(c=>c.id):[]}
 // 선택한 날짜의 입·출고 시간표로 배차 만들기 (화면 확인용: 메모리에만 추가)
 async function generate(date){
  if(source.rows.some(r=>r.dispatchDate===date&&r.moveType))throw new Error('선택한 날짜에는 이미 시간표로 만든 입·출고 배차가 있습니다.')
  const startId=Math.max(0,...source.rows.map(r=>Number(r.id)))+1,created=buildDepotDispatches(date,{startId})
  source.rows.push(...created);version.value++;return {date,dayTypeLabel:dayTypeLabel(dayTypeOf(date)),created:created.length}
 }
 return {appliedStatus:computed(()=>applied.status),source,draft,page,size,requestPage,rows,summary,depotCounts,dayInfo,totalPages,pages,selected,allSelected,search,move,selectAll,generate,error,reload:()=>version.value++}
}
