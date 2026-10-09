import {serviceDate} from '../mocks/depotTimetable.js'
// 목록 검색 조건 기본값. 배차관리는 처음 열 때 오늘 운행일의 입·출고 배차를 보여 준다. (날짜 해제 시 전체)
export const initialFleetFilters=kind=>({keyword:'',date:kind==='dispatches'?serviceDate():'',vehicleId:'',driver:'',status:'',moveType:''})
