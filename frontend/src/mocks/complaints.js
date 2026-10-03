import { reactive, ref } from 'vue'
import { complaintTypes, complaintCategories, complaintStatuses } from './complaintForm.js'
// Separate mock fixtures, not an API response schema.
export const mockComplaints = reactive([
 {id:'1',authorId:'2',authorName:'미리보기 사용자',type:'SIMPLE',category:'OPERATION',status:'WAITING',receivedAt:'2026-10-01',title:'본인 민원 확인용',content:'본인이 작성한 민원 미리보기입니다.',attachments:[]},
 {id:'2',authorId:'3',authorName:'이시민',type:'REPORT',category:'FACILITY',status:'RECEIVED',receivedAt:'2026-10-01',title:'다른 회원의 민원',content:'다른 회원의 비공개 민원 내용입니다.',attachments:[]},
])
// Frontend fixtures only, not an API response contract.
for (let i=3;i<=57;i++) mockComplaints.push({id:String(i),authorId:i%3===0?'2':'3',authorName:i%3===0?'미리보기 사용자':'이시민',title:['열차 안내 개선을 건의합니다','역사 시설 점검 요청','노선과 시간 안내에 대한 의견','요금 결제 이용 문의'][i%4] + ' '+i,content:'화면 확인용 mock 민원 내용입니다.',type:complaintTypes[i%4].value,category:complaintCategories[i%5].value,status:complaintStatuses[i%4].value,receivedAt:'2026-09-'+String(1+i%28).padStart(2,'0'),attachments:[]})
for (const c of mockComplaints) c.statusLabel=complaintStatuses.find(o=>o.value===c.status)?.label
export function findComplaint(id) { return mockComplaints.find(item => item.id === String(id)) }
export function canReadComplaint(user, complaint) { return !!user && !!complaint && (user.role === 'ADMIN' || String(user.id) === String(complaint.authorId)) }

export const complaintNotice = ref('')
export function addMockComplaint(draft, user, files) {
 if (!user?.id) throw new Error('미리보기 사용자 정보가 없습니다.')
 const id = String(Math.max(0, ...mockComplaints.map(item => Number(item.id) || 0)) + 1)
 const member = { id, authorId:String(user.id), authorName:user.name, receivedAt:new Date().toLocaleDateString('sv-SE'), status:'WAITING', statusLabel:'접수대기', category:draft.category, title:draft.title.trim(), content:draft.content, type:draft.type, attachments:files.map((file,index) => ({id:index+1,name:file.name})), localFiles:[...files] }
 mockComplaints.push(member)
 complaintNotice.value='민원이 등록되었습니다.'
 return member
}

export function updateMockComplaint(id,draft,user){
 const c=findComplaint(id)
 if(!canReadComplaint(user,c)) throw new Error('본인 민원만 수정할 수 있습니다.')
 if(c.status!=='WAITING') throw new Error('접수대기 민원만 수정할 수 있습니다.')
 Object.assign(c,{type:draft.type,category:draft.category,title:draft.title.trim(),content:draft.content})
 return c
}
