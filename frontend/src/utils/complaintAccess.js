// Confirmed backend enum and AdminComplainDto.withdrawnAt.
export const withdrawnComplaintStatuses = new Set(['WITHDRAWN'])
export function isDeletedComplaint(complaint) {
  return !!complaint && (withdrawnComplaintStatuses.has(complaint.status) || (complaint.withdrawnAt != null && complaint.withdrawnAt !== ''))
}
export function complaintDetailMode({ complaint, user, httpStatus }) {
  if (isDeletedComplaint(complaint)) return 'deleted'
  if (httpStatus === 404) return 'not-found'
  if (httpStatus === 403) return 'ownership'
  if (!complaint) return 'error'
  if (user?.loginId === 'admin') return 'detail'
  return user?.id != null && complaint.writerId != null && String(user.id) === String(complaint.writerId)
    ? 'detail' : 'ownership'
}
export const isWithdrawnComplaint = isDeletedComplaint
