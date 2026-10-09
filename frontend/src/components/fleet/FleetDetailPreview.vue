<script setup>
import train from '../../assets/train.png'
import {ref,watch,computed} from 'vue'
import {useRoute,useRouter} from 'vue-router'
import {deleteVehicle,deleteDispatch} from '../../services/fleetManagement'
import {errorMessage} from '../../api/clients'
import {getFleetDetail} from '../../services/fleetDetailPreview'
const props=defineProps({kind:{type:String,required:true}})
const router=useRouter(),busy=ref(false)
const route=useRoute(),record=ref(null),loading=ref(false),error=ref(''),preview=ref(true)
const dispatch=computed(()=>props.kind==='dispatches')
const label=computed(()=>dispatch.value?'배차':'차량')
const listRoute=computed(()=>({name:dispatch.value?'admin-dispatches':'admin-vehicles'}))
let sequence=0
watch(()=>[props.kind,route.params.id],async()=>{
 const request=++sequence;loading.value=true;record.value=null;error.value=''
 try{const result=await getFleetDetail(props.kind,route.params.id);if(request===sequence){record.value=result.record;preview.value=result.preview}}
 catch(e){if(request===sequence)error.value=e.message}
 finally{if(request===sequence)loading.value=false}
},{immediate:true})
async function remove(){if(busy.value||preview.value)return;if(!window.confirm(dispatch.value?'이 배차를 삭제하시겠습니까? 삭제한 배차는 복구할 수 없습니다.':'이 차량을 삭제하시겠습니까? 배차·운행 이력이 있으면 삭제할 수 없습니다.'))return;busy.value=true;try{if(dispatch.value)await deleteDispatch(record.value.id);else await deleteVehicle(record.value.id);await router.push(listRoute.value)}catch(e){error.value=errorMessage(e)}finally{busy.value=false}}
const date=value=>value?.replaceAll('-','.')||'—'
const time=value=>value?.slice(0,5)||'—'
</script>
<template>
 <section class="fleet-detail">
  <header><div><h1>{{label}} 상세정보</h1><p>선택한 {{label}}의 일정과 정보를 확인할 수 있습니다.</p></div><RouterLink :to="listRoute" class="back-button">{{label}} 목록으로</RouterLink></header>
  <p v-if="loading" role="status">불러오는 중입니다.</p>
  <template v-else-if="record">
   <p v-if="preview" class="preview-notice">화면 확인용 임시 상세 데이터입니다. 실제 운영 정보가 아닙니다.</p>
   <article class="detail-card">
    <div class="card-heading"><div><span>{{label}}번호</span><h2>{{dispatch?record.dispatchNo:record.vehicleNo}}</h2></div><span class="detail-status">{{record.statusLabel||'—'}}</span></div>
    <dl v-if="dispatch"><div><dt>배차일자</dt><dd>{{date(record.dispatchDate)}}<small v-if="record.dayTypeLabel" class="day-type"> · {{record.dayTypeLabel}} 시간표</small></dd></div><div><dt>구분</dt><dd>{{record.moveTypeLabel||'일반 운행'}}</dd></div><div><dt>열번</dt><dd>{{record.trainNo||'—'}}</dd></div><div><dt>차량번호</dt><dd>{{record.vehicleNo||'—'}}</dd></div><div><dt>운전자명</dt><dd>{{record.driverName||'—'}}</dd></div><template v-if="record.moveType"><div><dt>{{record.moveTypeLabel}} 시각</dt><dd>{{time(record.departureTime)}}</dd></div></template><template v-else><div><dt>출발시간</dt><dd>{{time(record.departureTime)}}</dd></div><div><dt>도착시간</dt><dd>{{time(record.arrivalTime)}}</dd></div></template><div><dt>배차상태</dt><dd>{{record.statusLabel||'—'}}</dd></div></dl>
    <template v-else><img class="vehicle-preview-image" :src="train" alt="차량 기본 이미지"/><dl><div><dt>차량번호</dt><dd>{{record.vehicleNo||'—'}}</dd></div><div><dt>차량상태</dt><dd>{{record.statusLabel||'—'}}</dd></div><div><dt>최근 점검일</dt><dd>{{date(record.lastInspectionDate)}}</dd></div></dl></template>
    <div v-if="dispatch" class="detail-remark"><h3>비고</h3><p>{{record.remark||'등록된 비고가 없습니다.'}}</p></div>
    <p v-if="dispatch&&!preview" class="preview-notice">배차 취소는 수정 화면의 상태에서 선택할 수 있습니다. 삭제하면 배차 기록이 영구적으로 제거됩니다.</p>
    <p v-if="error" role="alert">{{error}}</p><div v-if="!preview" class="detail-actions"><RouterLink class="back-button" :to="{name:dispatch?'admin-dispatch-edit':'admin-vehicle-edit',params:{id:record.id}}">수정</RouterLink><button class="back-button" :disabled="busy" @click="remove">삭제</button></div>
   </article>
  </template>
  <article v-else class="detail-card empty"><h2>{{label}} 정보를 찾을 수 없습니다.</h2><p>{{error||'해당 ID의 상세 데이터가 없습니다.'}}</p><RouterLink :to="listRoute" class="back-button">목록으로 돌아가기</RouterLink></article>
 </section>
</template>
<style scoped>
.vehicle-preview-image{display:block;width:240px;max-width:100%;height:120px;object-fit:contain;margin:24px auto}
.fleet-detail{display:grid;gap:20px;color:#354755;min-width:0}.fleet-detail header{display:flex;justify-content:space-between;align-items:center;gap:20px}.fleet-detail h1{margin:0;color:#064b76;font-size:38px;font-weight:800}.fleet-detail header p{margin:10px 0 0;color:#788996;font-size:16px}.back-button{display:inline-flex;align-items:center;justify-content:center;min-height:44px;padding:0 20px;border:1px solid #bdd4e2;border-radius:9px;background:#fff;color:#064b76;text-decoration:none;font-weight:700;white-space:nowrap}.back-button:hover{background:#edf7fc}.back-button:focus-visible{outline:3px solid #64b3e0;outline-offset:3px}.preview-notice{margin:0;padding:13px 18px;background:#f1f8fc;border-radius:9px;font-size:15px;color:#607d90}.detail-card{padding:28px;border:1px solid #dce7ee;border-radius:16px;background:#fff;min-width:0}.card-heading{display:flex;justify-content:space-between;align-items:center;gap:16px;padding-bottom:24px;border-bottom:1px solid #e3edf3}.card-heading span{font-size:15px;color:#607d90}.card-heading h2{margin:8px 0 0;font-size:27px;color:#064b76;overflow-wrap:anywhere}.card-heading .detail-status{background:#eaf5fc;padding:8px 14px;border-radius:20px;color:#065782;font-weight:700;white-space:nowrap}dl{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:28px 24px;margin:26px 0}dt{color:#788996;font-size:15px;margin-bottom:10px}dd{margin:0;font-size:19px;font-weight:700;overflow-wrap:anywhere}.detail-remark{background:#f7fafc;padding:20px;border-radius:10px}.detail-remark h3{margin:0 0 10px;font-size:17px;color:#064b76}.detail-remark p{margin:0;white-space:pre-wrap;overflow-wrap:anywhere;font-size:16px;line-height:1.7}.empty{text-align:center}.empty .back-button{margin-top:16px}@media(max-width:900px){dl{grid-template-columns:repeat(2,minmax(0,1fr))}.fleet-detail h1{font-size:32px}}@media(max-width:550px){.fleet-detail header{align-items:flex-start;flex-direction:column}.detail-card{padding:20px}dl{grid-template-columns:1fr}.card-heading{flex-wrap:wrap}}
.day-type{font-size:15px;font-weight:500;color:#607d90}
.detail-actions{display:flex;justify-content:flex-end;gap:12px;margin-top:24px}.detail-actions button:disabled{opacity:.5;cursor:not-allowed}
</style>