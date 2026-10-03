// All list requests start at page 1; keep the server pagination envelope intact.
export function listParams(params = {}) {
  return { ...params, page: Math.max(1, Number(params.page) || 1), size: Math.max(1, Number(params.size) || 10) }
}
export function listPage(data) {
  if (!data || !Array.isArray(data.content) || !Number.isInteger(data.page) || data.page < 1 || !Number.isInteger(data.size) || !Number.isInteger(data.totalElements) || !Number.isInteger(data.totalPages)) throw new Error('목록 API 응답 형식을 확인해 주세요.')
  return data
}
