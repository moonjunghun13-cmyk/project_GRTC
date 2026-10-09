export const complaintTypes = [{value:'SIMPLE',label:'단순'},{value:'SUGGESTION',label:'건의'},{value:'REPORT',label:'제보'},{value:'COMPLAINT',label:'불만'}]
export const complaintCategories = [{value:'OPERATION',label:'운행관련'},{value:'FACILITY',label:'시설물'},{value:'ROUTE_TIME',label:'노선·시간'},{value:'FARE_PAYMENT',label:'요금·결제'},{value:'ETC',label:'기타'}]
export const complaintStatuses = [{value:'WAITING',label:'접수대기'},{value:'RECEIVED',label:'답변중'},{value:'ANSWERED',label:'답변완료'},{value:'TRANSFERRED',label:'이관안내'}]
export const complaintEditorConfig = { maxLength: 5000 }
export const complaintLabel = (options, value) => options.find(item=>item.value===value)?.label || '—'
