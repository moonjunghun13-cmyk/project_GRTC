// Frontend mock view models, not an API DTO.
import {depotMoves,dayTypeOf,dayTypeLabel,serviceDate,moveTypes} from './depotTimetable.js'
export const vehicleStatuses=[{value:'RUNNING',label:'운행중',tone:'green'},{value:'STANDBY',label:'대기',tone:'blue'},{value:'MAINTENANCE',label:'정비',tone:'orange'},{value:'STOPPED',label:'운행정지',tone:'red'}]
export const dispatchStatuses=[{value:'COMPLETED',label:'배차 완료',tone:'green'},{value:'WAITING',label:'배차 대기',tone:'blue'},{value:'CHANGED',label:'배차 변경',tone:'orange'},{value:'CANCELLED',label:'배차 취소',tone:'red'}]
export const vehicles=Array.from({length:64},(_,i)=>({id:String(i+1),vehicleNo:'G'+String(101+i),status:vehicleStatuses[i%4].value,lastInspectionDate:'2026-09-'+String(1+i%28).padStart(2,'0')}))
const driverNames=['김민수','이지훈','박서연','장하늘','최유진','한지민','송재민','전수빈','정민호','윤지우','강도현','조예린','임태윤','오세라','신동욱','문가은','배준서','황보영','서지안','노현우']
export const drivers=driverNames.map(name=>({value:name,label:name}))
// 입·출고 시간표로 하루치 배차를 만든다. (백엔드 DispatchTimetableGenerator 와 같은 규칙)
//  - 출고 1회 = 배차 1건, 입고 1회 = 배차 1건. 시각은 하나라서 출발시간 = 도착시간.
//  - 출고: 가장 오래 대기한 차량(운행중/대기), 입고: 가장 먼저 나간 차량이 들어온다고 보고 예시 배정.
export function buildDepotDispatches(date,{now=new Date(),startId=1}={}){
 const available=vehicles.filter(v=>v.status==='RUNNING'||v.status==='STANDBY'),depot=[...available],out=[],dayType=dayTypeOf(date),label=dayTypeLabel(dayType),prefix='DISP'+date.slice(2).replaceAll('-','')+'-'
 let driver=0
 return depotMoves(date).map((m,i)=>{
  let vehicle,driverName,remark
  if(m.moveType==='DEPART'){vehicle=depot.shift()||available[i%available.length];driverName=driverNames[driver++%driverNames.length];out.push({vehicle,driverName,trainNo:m.trainNo,time:m.time});remark=label+' 시간표 출고 (차량·운전자는 예시 배정)'}
  else{const trip=out.shift();vehicle=trip?.vehicle||depot.shift()||available[0];driverName=trip?.driverName||driverNames[driver++%driverNames.length];depot.push(vehicle);remark=label+' 시간표 입고 ('+(trip?trip.trainNo+'열차 '+trip.time+' 출고분, ':'')+'예시 배정)'}
  const at=new Date(date+'T'+m.time+':00');if(m.minute>=24*60)at.setDate(at.getDate()+1)
  return {id:String(startId+i),dispatchNo:prefix+String(i+1).padStart(3,'0'),dispatchDate:date,vehicleId:vehicle.id,vehicleNo:vehicle.vehicleNo,driverName,departureTime:m.time+':00',arrivalTime:m.time+':00',moveType:m.moveType,moveTypeLabel:moveTypes.find(o=>o.value===m.moveType).label,trainNo:m.trainNo,dayType,dayTypeLabel:label,status:at>now?'WAITING':'COMPLETED',remark}
 })
}
export const dispatches=buildDepotDispatches(serviceDate())
