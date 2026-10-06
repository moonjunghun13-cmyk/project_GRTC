<script setup>
import DashboardIcon from '../../components/common/DashboardIcon.vue'
import { useDashboardFrame, periods, displayNumber as n } from '../../composables/useDashboardFrame'
const { data, dispatchSummary, complaintPeriod, answerPeriod } = useDashboardFrame()
const vehicleLegend = [{label:'운행',key:'operating',color:'#0085ca'},{label:'대기',key:'waiting',color:'#a4d9f0'},{label:'정비',key:'maintenance',color:'#c6cdd5'}]
const complaintMax = () => Math.max(1, ...data.complaints.map(item => item.value ?? 0))
const answerMax = () => Math.max(1, ...data.answers.map(item => item.value ?? 0))
</script>
<template>
  <section class="dashboard-frame" aria-label="관리자 대시보드">
      <article class="dashboard-card route-card"><RouterLink class="card-navigation" to="/dashboard/operations" aria-label="노선운영현황: 운행관리로 이동"/><header class="dashboard-card-heading"><div><DashboardIcon type="train" /><h2>노선운영현황 <span class="card-arrow" aria-hidden="true">↗</span></h2></div><span class="waiting-badge">정보 대기</span></header><div class="route-card-body"><h3>광주 도시철도 1호선</h3><div class="decorative-route" aria-label="녹동과 평동"><span>녹동</span><div aria-hidden="true"><i></i><i></i><i></i><i></i><i></i></div><span>평동</span></div></div><footer class="route-stats"><div><span>운행 역수</span><strong>{{ n(data.stations) }}<small>개</small></strong></div><div><span>운행 횟수</span><strong>{{ n(data.trips) }}<small>회</small></strong></div></footer></article>
      <article class="dashboard-card vehicle-card"><RouterLink class="card-navigation" to="/dashboard/vehicles" aria-label="차량운행현황: 차량관리로 이동"/><header class="dashboard-card-heading"><div><DashboardIcon type="vehicle" /><h2>차량운행현황 <span class="card-arrow" aria-hidden="true">↗</span></h2></div></header><div class="vehicle-card-body"><p class="dashboard-total">운행 중 <strong>{{ n(data.operating) }}</strong><small>대</small></p><div class="vehicle-track" aria-label="차량 운행 비율 데이터 대기"><span v-for="item in vehicleLegend" :key="item.key" :style="{background:item.color,width:data.fleet > 0 && data[item.key] != null ? data[item.key] / data.fleet * 100 + '%' : '0%'}"></span></div></div><footer class="vehicle-legend"><div v-for="item in vehicleLegend" :key="item.key"><span><i :style="{background:item.color}"></i>{{ item.label }}</span><strong>{{ n(data[item.key]) }}대</strong></div></footer></article>
      <article class="dashboard-card dispatch-card"><RouterLink class="card-navigation" to="/dashboard/dispatches" aria-label="배차현황: 배차관리로 이동"/><header class="dashboard-card-heading"><div><DashboardIcon type="vehicle"/><h2>배차현황 <span class="card-arrow" aria-hidden="true">↗</span></h2></div></header><p class="dashboard-total dispatch-total">전체 <strong>{{ n(dispatchSummary.total) }}</strong><small>건</small></p><div class="dispatch-states"><div v-for="item in dispatchSummary.states" :key="item.value" :data-tone="item.tone"><span><i aria-hidden="true"></i>{{item.label}}</span><strong>{{n(item.count)}}<small>건</small></strong></div></div></article>
      <article class="dashboard-card complaint-card"><RouterLink class="card-navigation" to="/dashboard/complaints" aria-label="민원건수: 민원관리로 이동"/><header class="dashboard-card-heading"><div><DashboardIcon type="complaint" /><h2>민원건수 <span class="card-arrow" aria-hidden="true">↗</span></h2></div><select v-model="complaintPeriod" @click.stop @keydown.stop aria-label="민원건수 기간"><option v-for="period in periods" :key="period.value" :value="period.value">{{ period.label }}</option></select></header><p class="dashboard-total">총 <strong>{{ n(data.complaintTotal) }}</strong><small>건</small></p><div class="complaint-chart"><p v-if="data.complaints.every(item => item.value == null)" class="chart-placeholder">데이터 연결 예정</p><div class="complaint-bars"><div v-for="item in data.complaints" :key="item.label"><div class="bar-space"><span v-if="item.value != null" :style="{height:item.value / complaintMax() * 100 + '%'}"></span></div><span>{{ item.label }}</span></div></div></div></article>
      <article class="dashboard-card answer-card"><RouterLink class="card-navigation" to="/dashboard/complaints" aria-label="답변건수: 민원관리로 이동"/><header class="dashboard-card-heading"><div><DashboardIcon type="answer" /><h2>답변건수 <span class="card-arrow" aria-hidden="true">↗</span></h2></div><select v-model="answerPeriod" @click.stop @keydown.stop aria-label="답변건수 기간"><option v-for="period in periods" :key="period.value" :value="period.value">{{ period.label }}</option></select></header><p class="dashboard-total">총 <strong>{{ n(data.answerTotal) }}</strong><small>건</small></p><div class="answer-chart"><div v-for="item in data.answers" :key="item.label" class="answer-row"><span>{{ item.label }}</span><div class="answer-track"><i v-if="item.value != null" :style="{width:item.value / answerMax() * 100 + '%'}"></i></div><strong>{{ n(item.value) }}건</strong></div></div></article>
  </section>
</template>
<style scoped>
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
.route-stats strong { font-size: 32px; font-weight: 900; }
.route-stats small, .dashboard-total small { margin-left: 4px; font-size: 16px; font-weight: 600; }
.dashboard-total { margin: 22px 0; font-size: 21px; font-weight: 700; }
.dashboard-total strong { font-size: 36px; font-weight: 900; margin-left: 5px; }
.vehicle-card-body .dashboard-total { margin: 0; }
.vehicle-track { height: 18px; display: flex; background: #edf1f5; border-radius: 9px; }
.vehicle-track span { height: 100%; }
.vehicle-legend { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 10px; padding-top: 18px; border-top: 1px solid #eaf0f4; }
.vehicle-legend > div { display: flex; flex-direction: column; gap: 9px; }
.vehicle-legend span { display: flex; align-items: center; gap: 6px; font-size: 15px; color: #6c8293; }
.vehicle-legend i { width: 9px; height: 9px; border-radius: 50%; }
.vehicle-legend strong { font-size: 19px; }
.complaint-chart { flex: 1; min-height: 0; position: relative; display: flex; }
.chart-placeholder { position: absolute; top: 35%; left: 0; right: 0; margin: 0; text-align: center; color: #8597a5; font-size: 16px; }
.complaint-bars { display: grid; grid-template-columns: repeat(4,minmax(0,1fr)); gap: 16px; width: 100%; }
.complaint-bars > div { display: flex; flex-direction: column; align-items: center; gap: 12px; font-size: 16px; color: #6c8293; }
.bar-space { flex: 1; width: 100%; border-bottom: 1px solid #e4ebf1; display: flex; justify-content: center; align-items: flex-end; }
.bar-space span { width: 35%; background: #168bc5; border-radius: 5px 5px 0 0; }
.answer-chart { flex: 1; min-height: 0; display: grid; grid-template-rows: repeat(4,minmax(30px,1fr)); gap: 14px; }
.answer-row { display: grid; grid-template-columns: 70px minmax(0,1fr) 52px; gap: 16px; align-items: center; font-size: 16px; }
.answer-row > span { color: #6c8293; }
.answer-row strong { text-align: right; font-weight: 700; }
.answer-track { background: #edf1f5; height: 14px; border-radius: 7px; }
.answer-track i { display: block; height: 100%; background: #168bc5; border-radius: 7px; }
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
</style>
