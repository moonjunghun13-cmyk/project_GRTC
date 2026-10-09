import {API_INTEGRATION_ENABLED} from '../api/clients'
import {loadFleetRecord} from './fleetManagement'
import {dispatches,dispatchStatuses,vehicles,vehicleStatuses} from '../mocks/fleet'
const key=(kind,id)=>'grtc.'+kind+'-detail-preview.'+String(id)
export function rememberFleetPreview(kind,row){
 try{sessionStorage.setItem(key(kind,row.id),JSON.stringify({...row,id:String(row.id)}))}catch{}
}
function preview(kind,id){
 let selected=null
 try{selected=JSON.parse(sessionStorage.getItem(key(kind,id))||'null')}catch{}
 const rows=kind==='vehicles'?vehicles:dispatches
 const statuses=kind==='vehicles'?vehicleStatuses:dispatchStatuses
 const row=selected?.id===String(id)?selected:rows.find(row=>row.id===String(id)||(kind==='vehicles'?row.vehicleNo:row.dispatchNo)===String(id))
 return row?{...row,statusLabel:row.statusLabel||statuses.find(s=>s.value===row.status)?.label||'—'}:null
}
// Inject a real API loader returning the UI view model at integration. Real data wins;
// failures propagate rather than being concealed by dummy data.
export async function getFleetDetail(kind,id,{loadActual}={}){
 if(API_INTEGRATION_ENABLED && !loadActual)loadActual=id=>loadFleetRecord(kind,id)
 if(loadActual){const record=await loadActual(id);return {record,preview:false}}
 return {record:preview(kind,id),preview:true}
}