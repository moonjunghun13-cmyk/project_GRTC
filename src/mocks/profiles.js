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
export const previewRole=ref(initial)
export function setPreviewRole(role){
 if(!['ADMIN','USER'].includes(role)) return
 previewRole.value=role
 try{sessionStorage.setItem('grtc.preview.role',role)}catch{}
}
export function previewUser(){return previewRole.value==='ADMIN'?previewProfiles.admin:previewProfiles.user}
