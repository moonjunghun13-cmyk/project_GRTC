import {general,mainApi} from '../api/clients'
export function getMyMember(){return general('get','/api/v1/members/me')}
export function updateMyMember(name,email,savedEmail){const payload={name:name.trim()};if(email.trim()!==(savedEmail||''))payload.email=email.trim();return general('patch','/api/v1/members/me',payload)}
export function maskedPhone(phone){if(!phone)return '';const digits=phone.replace(/\D/g,'');return digits.length>=3?digits.slice(0,3)+'-****-****':''}
export function memberProfileUrl(path){return path?new URL(path,mainApi.defaults.baseURL).href:null}
