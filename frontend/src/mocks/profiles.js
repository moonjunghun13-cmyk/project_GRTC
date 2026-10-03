import { ref } from 'vue'
// Screen-preview identity only; never writes to the real auth store.
export const previewProfiles = {
 admin: Object.freeze({id:'1',name:'관리자',loginId:'admin',role:'ADMIN'}),
 user: Object.freeze({id:'2',name:'미리보기 사용자',loginId:'preview-user',role:'USER'}),
}
let initial='USER'
try {
 const explicit=new URLSearchParams(window.location.search).get('previewRole')
 const stored=sessionStorage.getItem('grtc.preview.role')
 if (['ADMIN','USER'].includes(explicit)) initial=explicit
 else if (['ADMIN','USER'].includes(stored)) initial=stored
} catch {}
let loggedOut=false
try{loggedOut=localStorage.getItem('grtc.preview.loggedOut')==='true' || sessionStorage.getItem('grtc.preview.loggedOut')==='true'}catch{}
export const mockAuthenticated=ref(!loggedOut)
export const previewRole=ref(initial)
export function setPreviewRole(role){
 if(!mockAuthenticated.value || !['ADMIN','USER'].includes(role)) return
 previewRole.value=role
 try{sessionStorage.setItem('grtc.preview.role',role)}catch{}
}
export function previewUser(){return !mockAuthenticated.value ? null : previewRole.value==='ADMIN'?previewProfiles.admin:previewProfiles.user}

export function clearMockLogin(){
 mockAuthenticated.value=false;previewRole.value=null
 try{sessionStorage.removeItem('grtc.preview.role');sessionStorage.setItem('grtc.preview.loggedOut','true');localStorage.setItem('grtc.preview.loggedOut','true')}catch{}
}
export function loginMock(loginId,password){
 const role=loginId==='admin'&&password==='admin1234'?'ADMIN':loginId==='preview-user'&&password==='user1234'?'USER':null
 if(!role) throw new Error('아이디 또는 비밀번호가 일치하지 않습니다.')
 mockAuthenticated.value=true
 try{sessionStorage.removeItem('grtc.preview.loggedOut');localStorage.removeItem('grtc.preview.loggedOut')}catch{}
 setPreviewRole(role)
 return previewUser()
}

try{window.addEventListener('storage',event=>{if(event.key==='grtc.preview.loggedOut' && event.newValue==='true'){mockAuthenticated.value=false;previewRole.value=null}})}catch{}

