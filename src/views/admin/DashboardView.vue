<script setup>
import {computed} from 'vue'
import {useRouter} from 'vue-router'
import {canAccessAdminPath} from '../../utils/adminAccess'
import {currentUser} from '../../stores/currentUser'
import {dashboardWidgetsFor} from '../../config/adminDashboardWidgets'
import DashboardIcon from '../../components/common/DashboardIcon.vue'
import AdminChart from '../../components/admin/AdminChart.vue'
import {useDashboardFrame,periods,displayNumber as n} from '../../composables/useDashboardFrame'
const {data,dispatchSummary,complaintPeriod,answerPeriod,error}=useDashboardFrame()
const router=useRouter()
const enabledWidgets=computed(()=>new Set(dashboardWidgetsFor(currentUser.value)))
const canReadDispatch=computed(()=>canAccessAdminPath(currentUser.value,'/dashboard/dispatches'))
const vehicleRows=computed(()=>[{label:'운행중',value:data.operating,color:'#006BA6'},{label:'대기',value:data.waiting,color:'#62B5D4'},{label:'정비',value:data.maintenance,color:'#83AF38'},{label:'운행정지',value:data.stopped,color:'#8E9DAA'}])
const dispatchRows=computed(()=>dispatchSummary.value.states.map(r=>({label:r.label,value:r.count})))
function navigate(event,path){if(event.target.closest('a,button,select,option,input,.chart-region'))return;router.push(path)}
</script>
<template>
<p v-if="error" class="dashboard-notice" role="alert">{{error}}</p>
<p v-if="!enabledWidgets.size" class="dashboard-notice" role="status">현재 관리자 유형에 표시할 대시보드 위젯이 없습니다.</p>
<section v-else class="dashboard-frame" :class="{'widgets-one':enabledWidgets.size===1,'widgets-two':enabledWidgets.size===2,'widgets-three':enabledWidgets.size===3,'widgets-four':enabledWidgets.size===4}" aria-label="관리자 대시보드">

 <article v-if="enabledWidgets.has('vehicles')" class="dashboard-card vehicle-card" @click="navigate($event,'/dashboard/vehicles')"><header class="dashboard-card-heading"><div><DashboardIcon type="vehicle"/><h2><RouterLink to="/dashboard/vehicles">차량운행현황 <span aria-hidden="true">↗</span></RouterLink></h2></div><span class="route-status">운행중 {{n(data.operating)}}대</span></header><div class="chart-region"><AdminChart :rows="vehicleRows" :expanded="enabledWidgets.size===1" kind="donut" unit="대" :total="data.fleet" label="차량운행현황"/></div></article>
 <article v-if="enabledWidgets.has('dispatches')" class="dashboard-card dispatch-card" @click="navigate($event,'/dashboard/dispatches')"><header class="dashboard-card-heading"><div><DashboardIcon type="vehicle"/><h2><RouterLink to="/dashboard/dispatches">배차현황 <span aria-hidden="true">↗</span></RouterLink></h2></div></header><template v-if="canReadDispatch"><p class="metric-total"><span>전체 배차</span><strong>{{n(dispatchSummary.total)}}<small>건</small></strong></p><div class="chart-region"><AdminChart :rows="dispatchRows" kind="horizontal" :total="dispatchSummary.total" label="배차현황"/></div></template><div v-else class="dispatch-permission-notice" role="status"><DashboardIcon type="vehicle"/><h3>배차관리 권한 연결이 필요합니다</h3><p>현재 서버에서 이 계정의 배차 조회를 허용하지 않습니다.<br/>권한이 추가되면 실제 배차현황이 표시됩니다.</p></div></article>
 <article v-if="enabledWidgets.has('complaints')" class="dashboard-card complaint-card" @click="navigate($event,'/dashboard/complaints')"><header class="dashboard-card-heading"><div><DashboardIcon type="complaint"/><h2><RouterLink to="/dashboard/complaints">민원건수 <span aria-hidden="true">↗</span></RouterLink></h2></div><select v-model="complaintPeriod" aria-label="민원건수 기간" @click.stop @keydown.stop><option v-for="period in periods" :key="period.value" :value="period.value">{{period.label}}</option></select></header><p class="metric-total"><span>전체 민원</span><strong>{{n(data.complaintTotal)}}<small>건</small></strong></p><div class="chart-region"><AdminChart :rows="data.complaints" :total="data.complaintTotal" label="민원 유형별 건수"/></div></article>
 <article v-if="enabledWidgets.has('answers')" class="dashboard-card answer-card" @click="navigate($event,'/dashboard/complaints')"><header class="dashboard-card-heading"><div><DashboardIcon type="answer"/><h2><RouterLink to="/dashboard/complaints">답변건수 <span aria-hidden="true">↗</span></RouterLink></h2></div><select v-model="answerPeriod" aria-label="답변건수 기간" @click.stop @keydown.stop><option v-for="period in periods" :key="period.value" :value="period.value">{{period.label}}</option></select></header><p class="metric-total"><span>전체 답변</span><strong>{{n(data.answerTotal)}}<small>건</small></strong></p><div class="chart-region"><AdminChart :rows="data.answers" kind="horizontal" :total="data.answerTotal" label="민원 상태별 건수"/></div></article>
</section>
</template>
<style scoped>
.dashboard-frame{flex:1;min-height:700px;display:grid;grid-template-columns:repeat(6,minmax(0,1fr));grid-template-rows:minmax(310px,.9fr) minmax(330px,1.1fr);gap:20px}
.dashboard-card{grid-column:span 2;display:flex;flex-direction:column;min-width:0;min-height:0;padding:24px;background:white;border:1px solid #DAE3E9;border-radius:12px;cursor:pointer}
.complaint-card,.answer-card{grid-column:span 3}
.dashboard-card-heading{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-shrink:0}
.dashboard-card-heading>div{display:flex;align-items:center;gap:10px;min-width:0}.dashboard-card-heading h2{margin:0;font-size:18px;white-space:nowrap}.dashboard-card-heading a{color:inherit;text-decoration:none}.dashboard-card-heading a span{font-size:14px;color:#668798;margin-left:4px}.dashboard-card-heading a:focus-visible{outline:2px solid #006BA6;outline-offset:4px}
.route-status{font-size:12px;color:#436175;background:#F0F5F8;padding:6px 9px;border-radius:6px;white-space:nowrap}
.chart-region{flex:1 1 0;min-height:0;min-width:0;margin-top:14px;position:relative}
.metric-total{display:flex;align-items:baseline;gap:12px;margin:18px 0 0}.metric-total>span{color:#637588;font-size:13px}.metric-total strong{font-size:30px;font-weight:800;line-height:1.2}.metric-total small{font-size:14px;margin-left:5px}
.route-overview{flex:1;display:flex;flex-direction:column;justify-content:center;gap:16px}.eyebrow{font-size:12px;color:#637588}.route-overview h3{font-size:24px;margin:0}.route-line{display:flex;align-items:center;gap:12px;font-size:14px}.route-line i{height:3px;flex:1;background:#006BA6;border-radius:2px}.route-metrics{display:grid;grid-template-columns:1fr 1fr;border-top:1px solid #E4EAEE;padding-top:20px;gap:20px}.route-metrics div{display:flex;flex-direction:column;gap:8px}.route-metrics span{font-size:13px;color:#637588}.route-metrics strong{font-size:32px}.route-metrics small{font-size:14px;margin-left:5px}
.dashboard-card-heading select{height:36px;border:1px solid #CAD6DE;border-radius:6px;background:#fff;color:#43596B;padding:0 10px;font:inherit;font-size:13px}.dashboard-notice{margin:0 0 12px;color:#80520F;font-size:14px}
@media(max-width:1400px){.dashboard-card{padding:18px}.dashboard-card-heading h2{font-size:16px}.dashboard-card-heading .dashboard-icon{width:32px;height:32px}.route-status{font-size:11px}.dashboard-frame{gap:16px;min-height:700px}.chart-region{min-height:0}}
@media(max-width:1100px){.dashboard-frame{flex:none;height:auto;grid-template-columns:repeat(2,minmax(0,1fr));grid-template-rows:none;grid-auto-rows:380px}.dashboard-card{grid-column:span 1}.route-card{grid-column:1/-1}.chart-region{min-height:0}}
@media(max-width:700px){.dashboard-frame{grid-template-columns:minmax(0,1fr)}.dashboard-card{grid-column:1}}
</style>

<style scoped>.dashboard-card-heading{flex-wrap:wrap;row-gap:8px}.dashboard-card-heading>div{flex:1;min-width:180px}.dashboard-card-heading h2{white-space:normal;overflow-wrap:anywhere}.chart-region{position:relative;min-height:0;overflow:visible}.route-overview h3{overflow-wrap:anywhere}.route-metrics strong,.metric-total strong{overflow-wrap:anywhere}.dashboard-card{overflow:visible}</style>

<style scoped>:global(.admin-ui .dashboard-content-shell){display:flex;flex-direction:column;gap:12px}.chart-region :deep(.admin-chart){position:absolute;inset:0;height:100%;min-height:0}.dashboard-notice{flex-shrink:0;margin:0}@media(max-height:850px) and (min-width:1101px){.dashboard-frame{flex:none;height:720px}}@media(max-width:700px){.dashboard-frame{grid-auto-rows:380px}}
</style>

<style scoped>
.dashboard-frame.widgets-two{grid-template-columns:repeat(2,minmax(0,1fr));grid-template-rows:minmax(0,1fr)}
.dashboard-frame.widgets-three{grid-template-columns:repeat(3,minmax(0,1fr));grid-template-rows:minmax(0,1fr)}
.dashboard-frame.widgets-two .dashboard-card,.dashboard-frame.widgets-three .dashboard-card{grid-column:span 1}
@media(max-width:1100px){.dashboard-frame.widgets-two,.dashboard-frame.widgets-three{grid-template-columns:repeat(2,minmax(0,1fr));grid-template-rows:none;grid-auto-rows:380px}.dashboard-frame.widgets-three .route-card{grid-column:1/-1}}
@media(max-width:700px){.dashboard-frame.widgets-two,.dashboard-frame.widgets-three{grid-template-columns:minmax(0,1fr)}.dashboard-frame.widgets-three .route-card{grid-column:1}}
</style>

<style scoped>
@media(min-width:1101px) and (min-height:700px){
.dashboard-frame,.dashboard-frame.widgets-two,.dashboard-frame.widgets-three{flex:1;height:auto;min-height:0}
.dashboard-frame{grid-template-rows:minmax(0,.9fr) minmax(0,1.1fr)}
.dashboard-frame.widgets-two,.dashboard-frame.widgets-three{grid-template-rows:minmax(0,1fr)}
.dashboard-card{min-height:0;padding:20px}
.chart-region{min-height:0}
.metric-total{margin-top:12px}
.route-overview{min-height:0;gap:12px}
.route-metrics{padding-top:14px}
}
</style>

<style scoped>.dashboard-frame.widgets-one{grid-template-columns:minmax(0,1fr);grid-template-rows:minmax(0,1fr)}.dashboard-frame.widgets-one .dashboard-card{grid-column:1}</style>

<style scoped>
.dashboard-frame.widgets-four{grid-template-columns:repeat(2,minmax(0,1fr));grid-template-rows:repeat(2,minmax(0,1fr))}
.dashboard-frame.widgets-four .dashboard-card{grid-column:span 1}
@media(max-width:1100px){.dashboard-frame.widgets-four{grid-template-rows:none;grid-auto-rows:380px}}
@media(max-width:700px){.dashboard-frame.widgets-four{grid-template-columns:minmax(0,1fr)}}
</style>

<style scoped>
.dispatch-permission-notice{flex:1;display:flex;align-items:center;justify-content:center;flex-direction:column;gap:16px;text-align:center;padding:24px;box-sizing:border-box;min-height:0}
.dispatch-permission-notice h3{font-size:18px;margin:0;color:#325B76}.dispatch-permission-notice p{font-size:14px;line-height:1.8;margin:0;color:#637588}
</style>
