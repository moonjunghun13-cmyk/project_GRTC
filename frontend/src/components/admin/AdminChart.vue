<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { init, use } from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent, TitleComponent, AriaComponent } from 'echarts/components'
import { SVGRenderer } from 'echarts/renderers'
use([PieChart,BarChart,TooltipComponent,LegendComponent,GridComponent,TitleComponent,AriaComponent,SVGRenderer])
const props=defineProps({rows:{type:Array,required:true},expanded:Boolean,kind:{type:String,default:'bar'},unit:{type:String,default:'건'},total:{default:null},label:{type:String,required:true}})
const element=ref(null)
const available=computed(()=>props.rows.length>0&&props.rows.every(row=>row.value!=null))
const description=computed(()=>props.label+': '+props.rows.map(r=>r.label+' '+(r.value==null?'조회 불가':r.value+props.unit)).join(', '))
let chart,observer,frame,motion
const palette=['#006BA6','#62B5D4','#83AF38','#387EA6','#8E9DAA']
const percentage=v=>props.total>0?(v/props.total*100).toFixed(1):'0.0'
function draw(){
 if(!chart)return
 const colors=props.rows.map((row,i)=>row.color||palette[i%palette.length])
 const base={animation:!motion.matches,animationDuration:550,color:colors,textStyle:{fontFamily:'SUIT, sans-serif'},aria:{enabled:true,label:{description:description.value}},tooltip:{trigger:props.kind==='donut'?'item':'axis',renderMode:'richText',confine:true},legend:{show:false}}
 if(!available.value){chart.clear();chart.setOption({...base,title:{text:'데이터를 조회할 수 없습니다',left:'center',top:'middle',textStyle:{fontSize:14,fontWeight:400,color:'#637588'}}});return}
 const outerRadius=Math.max(12,Math.min(chart.getWidth()*(props.expanded?.30:.27),chart.getHeight()*.38))
 const values=props.rows.map((r,i)=>({name:r.label,value:r.value,itemStyle:{color:colors[i]}}))
 if(props.kind==='donut')chart.setOption({...base,title:{text:String(props.total??'—')+props.unit,subtext:'전체 차량',left:props.expanded?'34%':'30%',top:chart.getHeight()*(.5)-(props.expanded?48:26),textAlign:'center',textStyle:{fontSize:props.expanded?56:Math.min(28,Math.max(16,Math.min(chart.getWidth(),chart.getHeight())*.1)),fontWeight:800,color:'#16374B'},subtextStyle:{fontSize:props.expanded?20:14,color:'#637588'},itemGap:props.expanded?12:8},legend:{show:true,orient:'vertical',left:props.expanded?'72%':'60%',top:'middle',width:props.expanded?'26%':'40%',itemWidth:props.expanded?16:10,itemHeight:props.expanded?16:10,itemGap:props.expanded?32:12,textStyle:{color:'#43596B',fontSize:props.expanded?22:12},formatter:name=>{const r=props.rows.find(row=>row.label===name);return name+'  '+r.value+props.unit+' · '+percentage(r.value)+'%'}},tooltip:{...base.tooltip,formatter:'{b}: {c}'+props.unit+' ({d}%)'},series:[{type:'pie',radius:[outerRadius*.67,outerRadius],center:props.expanded?['34%','50%']:['30%','50%'],avoidLabelOverlap:true,label:{show:false},emphasis:{label:{show:false},scaleSize:4},itemStyle:{borderColor:'#fff',borderWidth:3,borderRadius:3},data:values}]},true)
 else {
 const horizontal=props.kind==='horizontal'
 chart.setOption({...base,grid:{containLabel:true,left:8,right:horizontal?112:24,top:28,bottom:20},xAxis:horizontal?{type:'value',minInterval:1,axisLabel:{show:false},splitLine:{lineStyle:{color:'#ECF0F3'}}}:{type:'category',data:props.rows.map(r=>r.label),axisTick:{show:false},axisLine:{lineStyle:{color:'#D8E1E7'}},axisLabel:{color:'#43596B',fontSize:13}},yAxis:horizontal?{type:'category',inverse:true,data:props.rows.map(r=>r.label),axisLine:{show:false},axisTick:{show:false},axisLabel:{color:'#43596B',fontSize:13}}:{type:'value',minInterval:1,axisLabel:{color:'#637588',fontSize:12},splitLine:{lineStyle:{color:'#ECF0F3'}}},series:[{type:'bar',barMaxWidth:horizontal?15:48,data:values,itemStyle:{borderRadius:horizontal?[0,3,3,0]:[4,4,0,0]},label:{show:true,position:horizontal?'right':'top',distance:8,color:'#284859',fontSize:12,formatter:p=>p.value+props.unit+(props.total==null?'':' · '+percentage(p.value)+'%')}}]},true)
 }
}
onMounted(()=>{motion=window.matchMedia('(prefers-reduced-motion: reduce)');chart=init(element.value,null,{renderer:'svg'});draw();observer=new ResizeObserver(()=>{cancelAnimationFrame(frame);frame=requestAnimationFrame(()=>{if(chart){chart.resize();if(props.kind==='donut')draw();}})});observer.observe(element.value);motion.addEventListener('change',draw)})
watch(()=>[props.rows,props.total,props.kind,props.expanded],draw,{deep:true})
onBeforeUnmount(()=>{observer?.disconnect();cancelAnimationFrame(frame);motion?.removeEventListener('change',draw);chart?.dispose();chart=null})
</script>
<template><div ref="element" class="admin-chart" role="img" :aria-label="description"></div></template>
<style scoped>.admin-chart{width:100%;height:100%;min-height:0;min-width:0}</style>
