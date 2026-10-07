import axios from 'axios'
export const API_INTEGRATION_ENABLED=import.meta.env.VITE_ENABLE_API==='true'
export const mainApi=axios.create({baseURL:import.meta.env.VITE_MAIN_API_URL||'http://localhost:8081',withCredentials:true,timeout:15000})
export const dashboardApi=axios.create({baseURL:import.meta.env.VITE_DASHBOARD_API_URL||'http://localhost:8081',withCredentials:true,timeout:15000})
let token=null,pending=null
export function setAccessToken(value){token=value}
export function clearAccessToken(){token=null}
export function refreshAccessToken(){if(!pending)pending=axios.post(mainApi.defaults.baseURL+'/api/v1/auth/reissue',{}, {withCredentials:true,timeout:15000}).then(r=>{token=r.data.data.accessToken;return token}).finally(()=>pending=null);return pending}
export function errorMessage(e){return e.response?.data?.error?.message||e.response?.data?.message||e.message||'요청에 실패했습니다.'}
export function authError(e){return e.response?.status===401?'unauthorized':e.response?.status===403?'forbidden':null}
let failure=()=>{}
export function installApiErrorHandlers(handler){failure=handler}
for(const api of [mainApi,dashboardApi]){
 api.interceptors.request.use(c=>{if(!API_INTEGRATION_ENABLED)throw new Error('API 연결 비활성화');if(token)c.headers.Authorization='Bearer '+token;return c})
 api.interceptors.response.use(r=>r,async e=>{const c=e.config;const publicAuth=/\/auth\/(login|signup|check-id)/.test(c?.url||'');if(e.response?.status===401&&c&&!c._retried&&!publicAuth){c._retried=true;try{await refreshAccessToken();return api(c)}catch{token=null}}if(!c?.localAuthError)failure(authError(e),e);throw e})
}
export async function general(method,url,data,params){return (await mainApi.request({method,url,data,params})).data.data}
