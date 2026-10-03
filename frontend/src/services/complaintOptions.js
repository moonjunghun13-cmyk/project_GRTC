import { complaintTypes, complaintCategories, complaintStatuses } from '../mocks/complaintForm'
// Local supplier only. Replace with options.categories/statuses without relabeling or sorting at integration.
export function getComplaintOptions(){return {types:complaintTypes,categories:complaintCategories,statuses:complaintStatuses}}
export const answerStatuses = complaintStatuses.filter(o=>['RECEIVED','ANSWERED','TRANSFERRED'].includes(o.value))
export function statusText(complaint){return complaint.statusLabel ?? complaintStatuses.find(o=>o.value===complaint.status)?.label ?? '—'}
