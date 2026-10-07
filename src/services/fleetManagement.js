import {general} from '../api/clients'
export const fleetPayload=(kind,f)=>kind==='vehicles'?{vehicleNo:f.vehicleNo.trim(),status:f.status,...(f.lastInspectionDate?{lastInspectionDate:f.lastInspectionDate}:{})}:{dispatchDate:f.dispatchDate,vehicleId:Number(f.vehicleId),driverName:f.driverName.trim(),departureTime:f.departureTime,arrivalTime:f.arrivalTime,remark:f.remark.trim(),...(f.status?{status:f.status}:{})}
export const loadFleetRecord=(kind,id)=>general('get','/api/v1/admin/'+kind+'/'+encodeURIComponent(id))
export const updateFleetRecord=(kind,id,form)=>general('patch','/api/v1/admin/'+kind+'/'+encodeURIComponent(id),fleetPayload(kind,form))
export const deleteVehicle=id=>general('delete','/api/v1/admin/vehicles/'+encodeURIComponent(id))
export const cancelDispatch=id=>general('patch','/api/v1/admin/dispatches/'+encodeURIComponent(id)+'/cancel')

export const deleteDispatch=id=>general('delete','/api/v1/admin/dispatches/'+encodeURIComponent(id))
