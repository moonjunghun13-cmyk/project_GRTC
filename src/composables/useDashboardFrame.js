import { computed, reactive, ref } from 'vue'
import { getComplaintOptions } from '../services/complaintOptions'
import { getFleetSource } from '../services/mockFleet'
export const periods = [{value:'TODAY',label:'오늘'},{value:'THIS_WEEK',label:'이번 주'},{value:'THIS_MONTH',label:'이번 달'},{value:'THIS_YEAR',label:'올해'},{value:'ALL',label:'전체'}]
export function displayNumber(value) { return value == null ? '—' : String(value) }
export function useDashboardFrame() {
  // UI view model only. No backend response schema or demo totals are assumed.
  const data = reactive({ stations:null, trips:null, operating:null, waiting:null, maintenance:null, fleet:null, complaintTotal:null, answerTotal:null,
    complaints:[{label:'단순',value:null},{label:'건의',value:null},{label:'제보',value:null},{label:'불만',value:null}],
    answers:getComplaintOptions().statuses.map(o=>({label:o.label,value:null})) })
  const source=getFleetSource('dispatches')
  const dispatchSummary=computed(()=>({total:source.rows?.length ?? null,states:source.statuses.map(o=>({...o,count:source.rows ? source.rows.filter(d=>d.status===o.value).length : null}))}))
  return { data, dispatchSummary, complaintPeriod:ref('THIS_MONTH'), answerPeriod:ref('THIS_MONTH') }
}

// Preserve server byStatus labels and array order; no sorting or fabricated counts.
export function dashboardStatusRows(byStatus){return byStatus.map(item=>({...item}))}
