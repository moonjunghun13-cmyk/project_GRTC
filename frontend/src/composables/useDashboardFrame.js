import {computed,reactive,ref,watch,onBeforeUnmount,getCurrentInstance} from 'vue'
import {general,mainApi,API_INTEGRATION_ENABLED,errorMessage} from '../api/clients'
import {currentUser} from '../stores/currentUser'
import {canAccessAdminPath} from '../utils/adminAccess'
import {excludeWithdrawn} from '../services/dashboardComplaints'
import {getComplaintOptions} from '../services/complaintOptions'
import {getFleetSource} from '../services/mockFleet'
import {depotMoves,depotSummary,serviceDate} from '../mocks/depotTimetable.js'
export const periods=[{value:'TODAY',label:'오늘'},{value:'THIS_WEEK',label:'이번 주'},{value:'THIS_MONTH',label:'이번 달'},{value:'THIS_YEAR',label:'올해'},{value:'ALL',label:'전체'}]
export function displayNumber(value){return value==null?'—':String(value)}
export function useDashboardFrame(){
 const data=reactive({stations:null,trips:null,operating:null,waiting:null,maintenance:null,stopped:null,fleet:null,complaintTotal:null,answerTotal:null,complaints:[{label:'단순',value:null},{label:'건의',value:null},{label:'제보',value:null},{label:'불만',value:null}],answers:getComplaintOptions().statuses.map(o=>({label:o.label,value:null}))})
 const source=getFleetSource('dispatches'),remoteSummary=ref(null),error=ref(''),complaintPeriod=ref('THIS_MONTH'),answerPeriod=ref('THIS_MONTH');let sequence=0
 // 입·출고현황: 1분마다 다시 계산(다음 출고/입고, 지금까지 출고·입고 수가 시간에 따라 바뀜)
 const tick=ref(0),remoteDepot=ref(null),timer=setInterval(()=>tick.value++,60000)
 if(getCurrentInstance())onBeforeUnmount(()=>clearInterval(timer))
 const depot=computed(()=>{tick.value;if(API_INTEGRATION_ENABLED)return remoteDepot.value;const date=serviceDate();return depotSummary(depotMoves(date),date)})
 if(API_INTEGRATION_ENABLED)watch([complaintPeriod,answerPeriod,tick],async()=>{
 const request=++sequence,period=complaintPeriod.value;error.value=''
 try{
 const canDispatch=canAccessAdminPath(currentUser.value,'/dashboard/dispatches'),canComplaints=canAccessAdminPath(currentUser.value,'/dashboard/complaints')
 const [d,s]=await Promise.all([general('get','/api/v1/admin/dashboard',undefined,{complainPeriod:period,answerPeriod:answerPeriod.value}),canDispatch?general('get','/api/v1/admin/dispatches/summary'):Promise.resolve(null)])
 const complaints=canComplaints?await excludeWithdrawn(d.complaints,period):{total:null,byType:d.complaints.byType.map(o=>({...o,count:null}))}
 if(request!==sequence)return
 Object.assign(data,{routeName:d.route?.name,routeStatus:d.route?.statusLabel,stations:d.route?.stationCount,trips:d.route?.dailyRunCount,operating:d.vehicles.running,waiting:d.vehicles.standby,maintenance:d.vehicles.maintenance,stopped:d.vehicles.stopped,fleet:d.vehicles.total,complaintTotal:complaints.total,answerTotal:d.answers.total,complaints:complaints.byType.map(o=>({code:o.code,label:o.label,value:o.count})),answers:d.answers.byStatus.map(o=>({label:o.label,value:o.count}))});remoteSummary.value=s;remoteDepot.value=d.depot??null
 if(!canComplaints)error.value='현재 계정의 대시보드 API는 철회를 포함한 통계를 반환합니다. 철회 제외 유형별 통계를 조회할 권한이 없어 민원건수는 표시하지 않습니다.'
 }catch(e){if(request===sequence)error.value=errorMessage(e)}},{immediate:true})
 const dispatchSummary=computed(()=>API_INTEGRATION_ENABLED?{total:remoteSummary.value?.total??null,states:source.statuses.map(o=>({...o,count:remoteSummary.value?.[{COMPLETED:'completed',WAITING:'waiting',CHANGED:'changed',CANCELLED:'cancelled'}[o.value]]??null}))}:{total:source.rows?.length??null,states:source.statuses.map(o=>({...o,count:source.rows?source.rows.filter(d=>d.status===o.value).length:null}))})
 return {data,dispatchSummary,depot,complaintPeriod,answerPeriod,error}
}
export function dashboardStatusRows(byStatus){return byStatus.map(item=>({...item}))}
