import {reactive} from 'vue'
import {general,mainApi} from '../api/clients'
export const members=reactive([]),memberOptions=reactive({departments:[],positions:[]})
const map=m=>({...m,id:String(m.id),username:m.loginId,rank:m.position,profileImageUrl:m.profileImageUrl?new URL(m.profileImageUrl,mainApi.defaults.baseURL).href:null})
export async function loadMembers(){const records=[];let page=0,totalPages=1;while(page<totalPages){const r=await general('get','/api/v1/admin/members',undefined,{page,size:20});records.push(...r.content);totalPages=r.totalPages;page++}members.splice(0,members.length,...records.map(map))}
export async function loadMember(id){const [m,o]=await Promise.all([general('get','/api/v1/admin/members/'+id),general('get','/api/v1/admin/members/options')]);Object.assign(memberOptions,o);return map(m)}
export async function updateMember(id,f,staff=true){return map(await general('patch','/api/v1/admin/members/'+id,{name:f.name.trim(),email:f.email.trim(),...(staff?{phone:f.phone.trim(),department:f.department,position:f.rank}:{})}))}
export function validateMember(f,staff=true){const e={};if(!f.name.trim())e.name='이름을 입력해 주세요.';if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email.trim()))e.email='올바른 이메일 주소를 입력해 주세요.';if(staff&&!f.department)e.department='소속 부서를 선택해 주세요.';if(staff&&!f.rank)e.rank='직급을 선택해 주세요.';return e}


