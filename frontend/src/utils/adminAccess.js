export const adminMenuForPath=path=>{
 const normalized=path.replace(/^\/admin(?=\/|$)/,'/dashboard')
 if(normalized==='/dashboard'||normalized==='/dashboard/')return 'dashboard'
 return normalized.startsWith('/dashboard/')?normalized.split('/')[2]:null
}
export function canAccessAdminPath(user,path){
 if(user?.role!=='ADMIN')return false
 const normalized=path.replace(/^\/admin(?=\/|$)/,'/dashboard')
 return Array.isArray(user.pages)&&user.pages.some(page=>page.path==='/dashboard'?normalized==='/dashboard'||normalized==='/dashboard/':normalized===page.path||normalized.startsWith(page.path+'/'))
}
