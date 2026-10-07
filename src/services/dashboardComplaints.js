import {mainApi} from '../api/clients'
// DashboardPeriod uses the Korea calendar, Monday weeks, and creation time.
export function periodStart(period,now=new Date()) {
 const today=new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Seoul',year:'numeric',month:'2-digit',day:'2-digit'}).format(now)
 let date=new Date(today+'T00:00:00+09:00')
 if(period==='ALL')return new Date('1970-01-01T00:00:00+09:00')
 if(period==='THIS_YEAR')return new Date(today.slice(0,4)+'-01-01T00:00:00+09:00')
 if(period==='THIS_MONTH')return new Date(today.slice(0,7)+'-01T00:00:00+09:00')
 if(period==='THIS_WEEK'){const weekday=new Date(today+'T00:00:00Z').getUTCDay();date=new Date(date.getTime()-((weekday+6)%7)*86400000)}
 return date
}
export async function excludeWithdrawn(card,period){
 const start=periodStart(period);const counts=new Map();let page=1,totalPages=1,removed=0
 do{const r=(await mainApi.get('/api/admin/complaints',{params:{status:'WITHDRAWN',page,size:100}})).data
 for(const row of r.content){if(row.status==='WITHDRAWN'&&new Date(row.createdAt+'+09:00')>=start){removed++;counts.set(row.type,(counts.get(row.type)||0)+1)}}
 totalPages=r.totalPages;page++
 }while(page<=totalPages)
 return {...card,total:Math.max(0,card.total-removed),byType:card.byType.map(item=>({...item,count:Math.max(0,item.count-(counts.get(item.code)||0))}))}
}
