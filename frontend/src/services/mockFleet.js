import { vehicles,dispatches,drivers,vehicleStatuses,dispatchStatuses } from '../mocks/fleet.js'
// Replace only this supplier when actual integration is authorized.
export function getFleetSource(kind){return kind==='vehicles'?{rows:vehicles,statuses:vehicleStatuses}:{rows:dispatches,statuses:dispatchStatuses,vehicles,drivers}}
export function fleetDestination(kind,action,id){return {message:action==='create'?'등록 화면 준비 중':'상세 화면 준비 중',id,kind}} // No corresponding routes exist yet.
