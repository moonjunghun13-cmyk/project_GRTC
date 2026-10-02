import { reactive } from 'vue'
import { initialMembers, departments, ranks } from '../mocks/members.js'
const key = 'grtc.mock.members.v1'
function restore() {
  try {
    const saved = JSON.parse(sessionStorage.getItem(key))
    if (Array.isArray(saved) && saved.length === initialMembers.length && saved.every(m => initialMembers.some(x => x.id === m.id && x.role === m.role) && typeof m.name === 'string' && typeof m.username === 'string')) return saved
  } catch { /* Start from fixtures if storage is unavailable. */ }
  return initialMembers.map(member => ({ ...member }))
}
const members = reactive(restore())
export function listMembers() { return members }
export function getMember(id) { return members.find(member => member.id === String(id)) }
export function validateMember(form) {
  const errors = {}
  if (!form.name.trim()) errors.name = '이름을 입력해 주세요.'
  if (!form.email.trim()) errors.email = '이메일을 입력해 주세요.'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) errors.email = '올바른 이메일 주소를 입력해 주세요.'
  if (!departments.includes(form.department)) errors.department = '소속 부서를 선택해 주세요.'
  if (!ranks.includes(form.rank)) errors.rank = '직급을 선택해 주세요.'
  return errors
}
export function updateMember(id, form, photo) {
  const member = getMember(id)
  if (!member) throw new Error('회원 정보를 찾을 수 없습니다.')
  const changes = { name:form.name.trim(), email:form.email.trim(), department:form.department, rank:form.rank, phone:form.phone.trim(), ...(photo === undefined ? {} : {photo}) }
  const next = members.map(item => item.id === member.id ? { ...item, ...changes } : { ...item })
  // Mock records only, not credentials or authentication tokens.
  try { sessionStorage.setItem(key, JSON.stringify(next)) }
  catch { throw new Error('미리보기 저장 공간이 부족합니다. 저장 공간을 확인해 주세요.') }
  Object.assign(member, changes)
  return member
}
