<script setup>
import {computed} from 'vue'
import normal from '../../assets/normal.png'
import loud from '../../assets/loud.png'
import star from '../../assets/star2.png'
import mad from '../../assets/mad.png'
import DashboardIcon from '../../components/common/DashboardIcon.vue'
import { useDashboardFrame, periods, displayNumber as n } from '../../composables/useDashboardFrame'
const { data, depot, complaintPeriod, answerPeriod, error } = useDashboardFrame()
// 입·출고현황 시간대별 막대 (05시대 ~ 0시대)
const hourMax = computed(() => Math.max(1, ...(depot.value?.hourly ?? []).map(h => Math.max(h.departs, h.returns))))
const currentHour = computed(() => { depot.value; const h = new Date().getHours(); return Math.min(24, h < 3 ? h + 24 : h) }) // depot 이 1분마다 갱신되면 함께 다시 계산
const hourLabel = h => String(h % 24).padStart(2, '0')
const shortDate = v => v ? v.slice(5).replace('-', '.') : ''
const moveTime = m => m ? m.time.slice(0, 5) : '—'
const vehicleLegend = [{label:'운행',key:'operating',color:'#0085ca'},{label:'대기',key:'waiting',color:'#a4d9f0'},{label:'정비',key:'maintenance',color:'#c6cdd5'},{label:'운행정지',key:'stopped',color:'#d98888'}]
const characterTypes=[{code:'SIMPLE',label:'단순',image:normal,footOffset:(1254-1238)/1254*100},{code:'SUGGESTION',label:'건의',image:loud,footOffset:(1145-1119)/1145*100},{code:'REPORT',label:'제보',image:star,footOffset:(1448-1391)/1448*100},{code:'COMPLAINT',label:'불만',image:mad,footOffset:(1536-1518)/1536*100}]
const complaintCharacters=computed(()=>characterTypes.map(type=>({...type,value:data.complaints.find(item=>item.code===type.code||item.label===type.label)?.value??null})))
const complaintBarHeight=value=>{
 const maximum=Math.max(0,...complaintCharacters.value.map(item=>item.value??0))
 return maximum>0&&value!=null?Math.max(0,Math.min(100,value/maximum*100))+'%':'0%'
}
const characterSize=value=>{
 const maximum=Math.max(0,...complaintCharacters.value.map(item=>item.value??0))
 // Responsive fixed size bounds avoid every high-count image hitting the same cap.
 const ratio=value==null||maximum===0?0.5:Math.max(0,Math.min(1,value/maximum))
 return (55+60*ratio)+'px'
}
const vehicleTotal = computed(() => vehicleLegend.reduce((sum,item) => sum+(data[item.key]??0),0))
const vehicleGradient = computed(() => {
 if (!vehicleTotal.value) return '#edf1f5'
 let offset=0
 return 'conic-gradient('+vehicleLegend.map(item=>{
  const start=offset
  offset+=(data[item.key]??0)/vehicleTotal.value*100
  return `${item.color} ${start}% ${offset}%`
 }).join(',')+')'
})
const answerMax = () => Math.max(1, ...data.answers.map(item => item.value ?? 0))
const answerRanks = computed(() => [...new Set(data.answers.map(item => item.value).filter(value => value != null && value > 0))].sort((a,b) => b-a))
const answerColor = value => {
 if (value > 0 && value === answerRanks.value[0]) return '#d96060'
 const rank = answerRanks.value.indexOf(value)
 const blues = ['#168bc5', '#61a8cf', '#94c3dc', '#c3deeb']
 return blues[Math.min(blues.length-1, Math.max(0, rank < 0 ? answerRanks.value.length-1 : rank-1))]
}
const percentage = (value,total) => value == null || total == null ? '—%' : (total > 0 ? value/total*100 : 0).toFixed(1)+'%'
</script>
<template><p v-if="error" role="alert">{{error}}</p>
  <section class="dashboard-frame" aria-label="관리자 대시보드">
      <article class="dashboard-card route-card"><RouterLink class="card-navigation" to="/dashboard/operations" aria-label="노선운영현황: 운행관리로 이동"/><header class="dashboard-card-heading"><div><DashboardIcon type="train" /><h2>노선운영현황 <span class="card-arrow" aria-hidden="true">↗</span></h2></div><span class="waiting-badge">{{data.routeStatus || '정보 대기'}}</span></header><div class="route-card-body"><h3>{{data.routeName || '광주 도시철도 1호선'}}</h3><div class="decorative-route" aria-label="녹동과 평동"><span>녹동</span><div aria-hidden="true"><i></i><i></i><i></i><i></i><i></i></div><span>평동</span></div></div><footer class="route-stats"><div><span>운행 역수</span><strong>{{ n(data.stations) }}<small>개</small></strong></div><div><span>운행 횟수</span><strong>{{ n(data.trips) }}<small>회</small></strong></div></footer></article>
      <article class="dashboard-card vehicle-card"><RouterLink class="card-navigation" to="/dashboard/vehicles" aria-label="차량운행현황: 차량관리로 이동"/><header class="dashboard-card-heading"><div><DashboardIcon type="vehicle" /><h2>차량운행현황 <span class="card-arrow" aria-hidden="true">↗</span></h2></div><p class="dashboard-total vehicle-operating-total">운행 중 <strong>{{ n(data.operating) }}</strong><small>대</small></p></header><div class="vehicle-card-body"><div class="vehicle-pie" role="img" :aria-label="vehicleLegend.map(item=>item.label+' '+n(data[item.key])+'대, '+percentage(data[item.key],data.fleet)).join('; ')" :style="{background:vehicleGradient}"><div class="vehicle-pie-center"><span>전체 차량</span><strong>{{n(data.fleet)}}<small>대</small></strong></div></div></div><footer class="vehicle-legend"><div v-for="item in vehicleLegend" :key="item.key"><span><i :style="{background:item.color}"></i>{{ item.label }}</span><strong>{{ n(data[item.key]) }}대</strong><small class="stat-percentage">{{percentage(data[item.key],data.fleet)}}</small></div></footer></article>
      <article class="dashboard-card dispatch-card depot-card"><RouterLink class="card-navigation" to="/dashboard/dispatches" aria-label="입·출고현황: 배차관리로 이동"/><header class="dashboard-card-heading"><div><DashboardIcon type="vehicle"/><h2>입·출고현황 <span class="card-arrow" aria-hidden="true">↗</span></h2></div><span class="waiting-badge">{{depot ? shortDate(depot.date)+' · '+depot.dayTypeLabel : '정보 대기'}}</span></header>
        <template v-if="depot"><div class="depot-stats"><div data-tone="blue"><span><i aria-hidden="true"></i>출고</span><strong>{{depot.departDone}}<small>/ {{depot.departTotal}}회</small></strong></div><div data-tone="green"><span><i aria-hidden="true"></i>입고</span><strong>{{depot.returnDone}}<small>/ {{depot.returnTotal}}회</small></strong></div><div data-tone="navy"><span><i aria-hidden="true"></i>운행 중</span><strong>{{depot.outNow}}<small>편성</small></strong></div></div>
        <p class="depot-next"><span>다음 출고 <b>{{depot.nextDepart ? depot.nextDepart.trainNo+' · '+moveTime(depot.nextDepart) : '없음'}}</b></span><span>다음 입고 <b>{{depot.nextReturn ? depot.nextReturn.trainNo+' · '+moveTime(depot.nextReturn) : '없음'}}</b></span></p>
        <div class="depot-hours" role="img" :aria-label="'시간대별 출고·입고: '+depot.hourly.filter(h=>h.departs||h.returns).map(h=>hourLabel(h.hour)+'시 출고 '+h.departs+'회 입고 '+h.returns+'회').join(', ')"><div v-for="h in depot.hourly" :key="h.hour" class="depot-hour" :class="{now:h.hour===currentHour}"><div class="bars"><i class="depart" :style="{height:h.departs/hourMax*100+'%'}"></i><i class="return" :style="{height:h.returns/hourMax*100+'%'}"></i></div><small>{{(h.hour-5)%3===0 ? hourLabel(h.hour) : ''}}</small></div></div></template>
        <p v-else class="depot-empty">입·출고 시간표 정보를 불러오는 중입니다.</p></article>
      <article class="dashboard-card complaint-card"><RouterLink class="card-navigation" to="/dashboard/complaints" aria-label="민원건수: 민원관리로 이동"/><header class="dashboard-card-heading"><div><DashboardIcon type="complaint"/><h2>민원건수 <span class="card-arrow" aria-hidden="true">↗</span></h2></div><div class="complaint-heading-controls"><p class="complaint-total">총 <strong>{{n(data.complaintTotal)}}</strong><small>건</small></p><select v-model="complaintPeriod" @click.stop @keydown.stop aria-label="민원건수 기간"><option v-for="period in periods" :key="period.value" :value="period.value">{{period.label}}</option></select></div></header><div class="complaint-characters"><div v-for="item in complaintCharacters" :key="item.code" class="complaint-character" :data-type="item.code"><h3 class="character-category">{{item.label}}</h3><div class="character-stage"><span v-if="item.value>0" class="character-bar" aria-hidden="true" :style="{height:complaintBarHeight(item.value)}"></span><img :src="item.image" :alt="item.label+' 민원 캐릭터'" :style="{height:characterSize(item.value),'--foot-offset':item.footOffset+'%'}"/></div><p class="character-count"><strong>{{n(item.value)}}건</strong><small class="stat-percentage">{{percentage(item.value,data.complaintTotal)}}</small></p></div></div></article>
      <article class="dashboard-card answer-card"><RouterLink class="card-navigation" to="/dashboard/complaints" aria-label="답변건수: 민원관리로 이동"/><header class="dashboard-card-heading"><div><DashboardIcon type="answer" /><h2>답변건수 <span class="card-arrow" aria-hidden="true">↗</span></h2></div><select v-model="answerPeriod" @click.stop @keydown.stop aria-label="답변건수 기간"><option v-for="period in periods" :key="period.value" :value="period.value">{{ period.label }}</option></select></header><p class="dashboard-total">총 <strong>{{ n(data.answerTotal) }}</strong><small>건</small></p><div class="answer-chart"><div v-for="item in data.answers" :key="item.label" class="answer-row"><span>{{ item.label }}</span><div class="answer-track"><i v-if="item.value != null" :style="{width:item.value / answerMax() * 100 + '%',backgroundColor:answerColor(item.value)}"></i></div><strong class="answer-count">{{ n(item.value) }}건<small class="stat-percentage">{{percentage(item.value,data.answerTotal)}}</small></strong></div></div></article>
  </section>
</template>
<style scoped>
.character-category{margin:0;box-sizing:border-box;width:calc(100% - 14px);text-align:center;font-size:16px;font-weight:800;color:#31536c;padding:8px 4px;border:0;border-radius:7px;background:var(--bar-bottom);box-shadow:inset 0 1px 0 rgb(255 255 255 / 65%),0 2px 0 rgb(49 83 108 / 10%),0 3px 6px rgb(49 83 108 / 5%)}.complaint-character{position:relative}.complaint-character+.complaint-character::before{content:"";position:absolute;left:-7px;top:0;bottom:0;border-left:2px dashed #cbdce7;pointer-events:none}@media(max-width:1450px),(max-height:850px){.complaint-character+.complaint-character::before{left:-5px}}@media(max-width:550px){.complaint-character:nth-child(3)::before{display:none}}.character-count{padding-top:2px}
.character-bar{position:absolute;bottom:0;left:calc(50% - 20px);width:40px;z-index:0;border-radius:8px 8px 2px 2px;background:linear-gradient(180deg,var(--bar-top,#71b5c0),var(--bar-bottom,#d9eef1));border:0;box-sizing:border-box;pointer-events:none;box-shadow:none;transform-origin:bottom;animation:complaint-bar-rise 650ms cubic-bezier(.22,.61,.36,1) both;transition:height 500ms ease-out}
.complaint-character[data-type=SIMPLE]{--bar-top:#9ecdf2;--bar-bottom:#e0effb}
.complaint-character[data-type=SUGGESTION]{--bar-top:#a6d9ba;--bar-bottom:#e3f3e9}
.complaint-character[data-type=REPORT]{--bar-top:#7fa8cc;--bar-bottom:#d8e6f3}
.complaint-character[data-type=COMPLAINT]{--bar-top:#e4afc4;--bar-bottom:#f7e5ed}
.complaint-character:nth-child(2) .character-bar{animation-delay:70ms}.complaint-character:nth-child(3) .character-bar{animation-delay:140ms}.complaint-character:nth-child(4) .character-bar{animation-delay:210ms}
@keyframes complaint-bar-rise{from{transform:scaleY(.12);opacity:.3}to{transform:scaleY(1);opacity:1}}
@media(prefers-reduced-motion:reduce){.character-bar{animation:none;transition:none}.character-stage img{transition:none}}
.complaint-heading-controls{display:flex;align-items:center;gap:12px;flex-shrink:0}
.complaint-total{margin:0;white-space:nowrap;font-size:16px;font-weight:700;color:#173e5c}
.complaint-total strong{font-size:26px;font-weight:800;margin-left:4px}
.complaint-total small{font-size:15px;margin-left:3px}
.complaint-characters{--character-min:48px;--character-max:105px;flex:1;min-height:0;display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px;margin-top:20px;padding-bottom:6px;align-items:stretch}
.complaint-character{min-width:0;min-height:0;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;gap:14px}
.character-stage{border-bottom:1px solid #cddce6;box-sizing:border-box;position:relative;isolation:isolate;width:100%;height:clamp(100px,21dvh,225px);max-height:calc(100% - 78px);display:flex;align-items:flex-end;justify-content:center;min-height:60px}
.character-stage img{position:absolute;bottom:0;left:50%;transform:translate(-50%,var(--foot-offset,0%));z-index:1;display:block;width:auto;object-fit:contain;object-position:center bottom;max-width:none;max-height:none;pointer-events:none;transition:height 900ms cubic-bezier(.22,.61,.36,1)}
.complaint-character p{margin:0;font-size:16px;color:#607d90;text-align:center;white-space:nowrap}
.complaint-character strong{color:#173e5c;font-weight:800}
@media(max-width:1450px),(max-height:850px){.complaint-heading-controls{gap:8px}.complaint-total strong{font-size:23px}.complaint-characters{--character-min:40px;--character-max:82px;gap:8px;margin-top:16px}.complaint-character{gap:10px}.complaint-character p{font-size:15px}}
@media(max-width:550px){.complaint-characters{grid-template-columns:repeat(2,minmax(0,1fr))}.character-stage{height:110px}.complaint-heading-controls{flex-wrap:wrap}.complaint-card{min-height:370px}}
.dashboard-frame { height: 100%; min-height: 0; box-sizing: border-box; display: grid; grid-template-columns: repeat(6,minmax(0,1fr)); grid-template-rows: repeat(2,minmax(0,1fr)); gap: 16px; color: #173e5c; }
.dashboard-card { grid-column: span 2; min-height: 0; box-sizing: border-box; margin: 0; }
.complaint-card, .answer-card { grid-column: span 3; }
.dashboard-card { position: relative; border: 1px solid transparent; transition: transform 180ms ease-out, border-color 180ms ease-out, box-shadow 180ms ease-out; min-width: 0; display: flex; flex-direction: column; padding: 24px; background: #fff; border-radius: 18px; }
.dashboard-card-heading { min-height: 40px; flex-shrink: 0; display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.dashboard-card-heading > div { display: flex; align-items: center; gap: 10px; min-width: 0; }
.dashboard-card-heading h2 { margin: 0; font-size: 20px; font-weight: 800; white-space: nowrap; }
.waiting-badge { padding: 6px 12px; border-radius: 20px; background: #eef4f8; color: #718394; font-size: 14px; white-space: nowrap; }
.dashboard-card-heading select { width: 110px; height: 36px; padding: 0 10px; border: 1px solid #d9e4ec; border-radius: 8px; color: #365870; background: white; font: inherit; font-size: 15px; }
.dashboard-card-heading select:focus-visible { outline: 3px solid #3298db; outline-offset: 2px; }
.route-card-body, .vehicle-card-body { flex: 1; min-height: 0; display: flex; flex-direction: column; justify-content: center; gap: 24px; padding: 18px 0; }
.route-card h3 { margin: 0; font-size: 23px; font-weight: 800; }
.decorative-route { display: flex; align-items: center; gap: 12px; font-size: 16px; font-weight: 700; }
.decorative-route > div { flex: 1; height: 5px; background: #0683c1; display: flex; align-items: center; justify-content: space-between; }
.decorative-route i { width: 12px; height: 12px; border: 3px solid #0683c1; border-radius: 50%; background: white; }
.route-stats { display: grid; grid-template-columns: 1fr 1fr; padding-top: 18px; border-top: 1px solid #eaf0f4; gap: 16px; }
.route-stats > div { display: flex; flex-direction: column; gap: 6px; }
.route-stats span { color: #7c8d9a; font-size: 16px; }
.route-stats strong { font-size: 32px; font-weight: 800; }
.route-stats small, .dashboard-total small { margin-left: 4px; font-size: 16px; font-weight: 600; }
.dashboard-total { margin: 22px 0; font-size: 21px; font-weight: 700; }
.dashboard-total strong { font-size: 36px; font-weight: 800; margin-left: 5px; }
.vehicle-card-body .dashboard-total { margin: 0; }
.vehicle-track { height: 18px; display: flex; background: #edf1f5; border-radius: 9px; }
.vehicle-track span { height: 100%; }
.vehicle-legend { display: grid; grid-template-columns: repeat(4,minmax(0,1fr)); gap: 10px; padding-top: 18px; border-top: 1px solid #eaf0f4; }
.vehicle-legend > div { display: flex; flex-direction: column; gap: 9px; }
.vehicle-legend span { display: flex; align-items: center; gap: 6px; font-size: 15px; color: #6c8293; }
.vehicle-legend i { width: 9px; height: 9px; border-radius: 50%; }
.vehicle-legend strong { font-size: 19px; }
.answer-chart { flex: 1; min-height: 0; display: grid; grid-template-rows: repeat(4,minmax(30px,1fr)); gap: 14px; }
.answer-row { display: grid; grid-template-columns: 70px minmax(0,1fr) 94px; gap: 16px; align-items: center; font-size: 16px; }
.answer-row > span { color: #6c8293; }
.answer-row strong { text-align: right; font-weight: 800; font-size: 20px; white-space: nowrap; }
.answer-track { background: #edf1f5; height: 14px; border-radius: 7px; }
.answer-track i { display: block; height: 100%; background: #168bc5; border-radius: 7px; transform-origin: left center; animation: answer-bar-grow 650ms cubic-bezier(.22,.61,.36,1) both; transition: width 900ms cubic-bezier(.22,.61,.36,1), background-color 500ms ease; }
.answer-row:nth-child(2) .answer-track i { animation-delay: 70ms; }
.answer-row:nth-child(3) .answer-track i { animation-delay: 140ms; }
.answer-row:nth-child(4) .answer-track i { animation-delay: 210ms; }
.answer-row:nth-child(5) .answer-track i { animation-delay: 280ms; }
@keyframes answer-bar-grow { from { transform: scaleX(.12); opacity: .3; } to { transform: scaleX(1); opacity: 1; } }
@media (prefers-reduced-motion: reduce) { .answer-track i { animation: none; transition: none; } }
@media (max-width: 1450px), (max-height: 850px) {
 .dashboard-card { padding: 18px; } .dashboard-card-heading h2 { font-size: 18px; } .dashboard-card-heading { gap: 8px; } .dashboard-card-heading > div { gap: 8px; } .waiting-badge { padding: 5px 8px; }
 .route-card-body, .vehicle-card-body { gap: 18px; padding: 12px 0; } .route-card h3 { font-size: 21px; }
 .dashboard-total { margin: 16px 0; } .route-stats, .vehicle-legend { padding-top: 12px; }
 .answer-chart { gap: 10px; }
}
@media (max-width: 1150px), (max-height: 650px) { .dashboard-frame { height: auto; grid-template-columns: repeat(2,minmax(0,1fr)); grid-template-rows: none; grid-auto-rows: minmax(300px,auto); } .dashboard-card { grid-column: span 1; } .answer-card { grid-column: 1 / -1; } }
@media (max-width: 800px) { .dashboard-frame {grid-template-columns: minmax(0,1fr);} .dashboard-card {grid-column: 1;} .dashboard-card-heading { flex-wrap: wrap; } }
.card-navigation {position:absolute;inset:0;z-index:1;border-radius:inherit;cursor:pointer;}
.card-navigation:focus-visible {outline:3px solid #3298db;outline-offset:-3px;}
.dashboard-card:hover,.dashboard-card:focus-within {transform:translateY(-3px);border-color:#b5dcec;box-shadow:0 5px 16px #086b9f12;}
.dashboard-card-heading select {position:relative;z-index:2;cursor:pointer;}
.card-arrow {font-size:16px;color:#65a4c5;margin-left:4px;}
.dispatch-total {margin:18px 0;}
.dispatch-states {flex:1;min-height:0;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px;align-content:stretch;}
.dispatch-states > div {display:flex;flex-direction:column;justify-content:center;gap:8px;background:#f7fbfd;border-radius:10px;padding:12px;}
.dispatch-states span {display:flex;align-items:center;gap:6px;font-size:15px;color:#607d90;white-space:nowrap;}
.dispatch-states i {width:6px;height:6px;border-radius:50%;background:#5aa0cf;}
.dispatch-states [data-tone=green] i {background:#51a276;}.dispatch-states [data-tone=orange] i {background:#da9a4b;}.dispatch-states [data-tone=red] i {background:#d07777;}
.dispatch-states strong {font-size:26px;}.dispatch-states small {font-size:15px;font-weight:500;margin-left:4px;}
@media (max-width:1450px),(max-height:850px){.dashboard-card-heading > div {gap:6px;}.dashboard-card-heading h2 {font-size:17px;}.card-arrow {margin-left:0;font-size:14px;}.waiting-badge{font-size:12px;padding:5px 6px;}.dispatch-states{gap:8px;}.dispatch-states > div{padding:8px;gap:5px;}.dispatch-total{margin:12px 0;}}
@media(prefers-reduced-motion:reduce){.dashboard-card{transition:none;}.dashboard-card:hover,.dashboard-card:focus-within{transform:none;}}
.stat-percentage { font-size: 13px; font-weight: 500; color: #6c8293; }
.character-count .stat-percentage { margin-left: 7px; }
.answer-count .stat-percentage { display: block; margin-top: 2px; line-height: 1.2; }
@media (max-width: 550px) { .answer-row { grid-template-columns: 70px minmax(0,1fr) 76px; gap: 10px; } }
.vehicle-card-body { align-items: center; gap: 14px; }
.vehicle-pie { width: clamp(120px,15dvh,170px); height: clamp(120px,15dvh,170px); flex-shrink: 0; border-radius: 50%; display: grid; place-items: center; }
.vehicle-pie-center { width: 68%; height: 68%; border-radius: 50%; background: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 5px; }
.vehicle-pie-center > span { color: #6c8293; font-size: 13px; }
.vehicle-pie-center strong { font-size: 25px; font-weight: 800; }
.vehicle-pie-center small { font-size: 14px; margin-left: 3px; }
.vehicle-legend > div { gap: 6px; }
.vehicle-legend span { white-space: nowrap; font-size: 14px; }
@media (max-width:1450px), (max-height:850px) { .vehicle-card-body { gap: 8px; padding: 6px 0; } .vehicle-pie { width: 112px; height: 112px; } .vehicle-legend { gap: 7px; } }
/* Vehicle summary: large chart left, operating total and status rows right. */
.vehicle-card { display: grid; grid-template-columns: minmax(0,1fr) minmax(125px,.9fr); grid-template-rows: 40px minmax(0,1fr); column-gap: 18px; row-gap: 20px; }
.vehicle-card > .dashboard-card-heading { grid-column: 1 / -1; }
.vehicle-card-body { grid-column: 1 / -1; grid-row: 2; display: grid; grid-template-columns: minmax(0,1fr) minmax(125px,.9fr); grid-template-rows: auto minmax(0,1fr); column-gap: 18px; gap: 18px; padding: 0; align-items: start; }
.vehicle-card-body .dashboard-total { grid-column: 2; grid-row: 1; font-size: 18px; white-space: nowrap; }
.vehicle-card-body .dashboard-total strong { font-size: 34px; }
.vehicle-pie { grid-column: 1; grid-row: 1 / -1; align-self: center; justify-self: center; width: min(100%,220px); height: auto; aspect-ratio: 1; }
.vehicle-legend { grid-column: 2; grid-row: 2; align-self: end; display: flex; flex-direction: column; gap: 13px; padding: 0; border: 0; }
.vehicle-legend > div { display: grid; grid-template-columns: minmax(0,1fr) auto; gap: 3px 8px; align-items: center; }
.vehicle-legend > div > span { grid-column: 1; grid-row: 1 / 3; }
.vehicle-legend strong, .vehicle-legend .stat-percentage { grid-column: 2; text-align: right; }
@media (max-width:1450px), (max-height:850px) {
 .vehicle-card, .vehicle-card-body { column-gap: 12px; }
 .vehicle-card { row-gap: 14px; }
 .vehicle-card-body .dashboard-total { font-size: 16px; }
 .vehicle-card-body .dashboard-total strong { font-size: 28px; }
 .vehicle-legend { gap: 8px; }
 .vehicle-legend strong { font-size: 17px; }
}
.vehicle-operating-total { margin: 0; font-size: 18px; white-space: nowrap; flex-shrink: 0; }
.vehicle-operating-total strong { font-size: 34px; }
.vehicle-card-body { grid-template-rows: minmax(0,1fr); }
.vehicle-pie { grid-row: 1; }
.vehicle-legend { align-self: center; }
@media (max-width:1450px), (max-height:850px) {
 .vehicle-operating-total { font-size: 16px; }
 .vehicle-operating-total strong { font-size: 28px; }
 .vehicle-card > .dashboard-card-heading { gap: 6px; }
}
.vehicle-pie { animation: vehicle-pie-appear 900ms cubic-bezier(.22,.61,.36,1) both; }
@keyframes vehicle-pie-appear {
 from { opacity: 0; transform: scale(.84); }
 to { opacity: 1; transform: scale(1); }
}
@media (prefers-reduced-motion: reduce) { .vehicle-pie { animation: none; } }
/* 입·출고현황 카드 */
.depot-card{gap:10px;}
.depot-stats{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:8px;margin-top:6px;}
.depot-stats>div{display:flex;flex-direction:column;gap:4px;background:#f7fbfd;border-radius:10px;padding:8px 10px;min-width:0;}
.depot-stats span{display:flex;align-items:center;gap:6px;font-size:14px;color:#607d90;white-space:nowrap;}
.depot-stats i{width:7px;height:7px;border-radius:50%;background:#3d8fd1;}
.depot-stats [data-tone=green] i{background:#51a276;}
.depot-stats [data-tone=navy] i{background:#064b76;}
.depot-stats strong{font-size:24px;font-weight:800;white-space:nowrap;}
.depot-stats small{font-size:13px;font-weight:500;margin-left:3px;color:#607d90;}
.depot-next{display:flex;flex-wrap:wrap;gap:4px 14px;margin:0;font-size:14px;color:#607d90;}
.depot-next b{color:#173e5c;font-weight:700;}
.depot-hours{flex:1;min-height:56px;display:grid;grid-template-columns:repeat(20,minmax(0,1fr));gap:2px;align-items:end;}
.depot-hour{height:100%;display:flex;flex-direction:column;justify-content:flex-end;align-items:center;gap:2px;border-radius:4px;}
.depot-hour.now{background:#eef7fc;}
.depot-hour .bars{flex:1;width:100%;display:flex;justify-content:center;align-items:flex-end;gap:1px;min-height:0;}
.depot-hour .bars i{width:40%;max-width:6px;border-radius:2px 2px 0 0;}
.depot-hour .depart{background:#3d8fd1;}
.depot-hour .return{background:#7cc49a;}
.depot-hour small{font-size:11px;color:#8798a5;line-height:1;height:11px;}
.depot-empty{margin:auto 0;color:#7c8d9a;font-size:15px;}
</style>

