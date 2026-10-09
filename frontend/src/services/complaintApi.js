import {mainApi} from '../api/clients'
export const complaintBase=admin=>admin?'/api/admin/complaints':'/api/complaints'
export const mapComplaint=c=>({...c,id:String(c.id),authorId:c.writerId==null?null:String(c.writerId),authorName:c.writerName,receivedAt:c.createdAt?.slice(0,10),attachments:(c.attachments||[]).map(a=>({...a,name:a.originalFileName,size:a.fileSize}))})
export async function getComplaint(id,admin=false){return mapComplaint((await mainApi.get(complaintBase(admin)+'/'+id,{localAuthError:true})).data)}
export async function saveAnswer(id,status,content){return mapComplaint((await mainApi.put('/api/admin/complaints/'+id+'/answer',{status,content})).data)}
export async function complaintOptions(admin=false){return (await mainApi.get(complaintBase(admin)+'/options')).data}
export async function createComplaint(form,files,admin=false){const body=new FormData();for(const key of ['type','category','title','content'])body.append(key,form[key]);for(const file of files)body.append('files',file);return mapComplaint((await mainApi.post(complaintBase(admin),body)).data)}
export async function updateComplaint(id,form,files=[],deleteFileIds=[]){const body=new FormData();for(const key of ['type','category','title','content'])body.append(key,form[key]);for(const file of files)body.append('files',file);for(const id of deleteFileIds)body.append('deleteFileIds',String(id));return mapComplaint((await mainApi.put('/api/complaints/'+id,body)).data)}

// The previous server uses the same DELETE URL for permanent deletion.
// Verify the deployed contract before issuing a destructive request.
export async function withdrawComplaint(id){
 if(import.meta.env.VITE_COMPLAINT_WITHDRAWAL_ENABLED!=='true')throw new Error('철회 서버 연결이 아직 활성화되지 않았습니다. 수정된 백엔드를 실행한 뒤 연결 설정을 확인해 주세요.')
 await mainApi.delete('/api/complaints/'+encodeURIComponent(id))
}