import {mainApi,setAccessToken} from './clients'
export async function login(loginId,password){const r=await mainApi.post('/api/v1/auth/login',{loginId,password},{localAuthError:true});setAccessToken(r.data.data.accessToken);return {data:r.data.data.member}}
export function signup(form){return mainApi.post('/api/v1/auth/signup',{name:form.name,loginId:form.username,password:form.password,passwordConfirm:form.passwordConfirmation,over14:form.ageConfirmed},{localAuthError:true})}

