// Widget presentation only. Page/API permissions remain in adminAccess and the router.
const existingWidgets=Object.freeze(['vehicles','dispatches','complaints','answers'])
// Keep the department-head dashboard independent of future changes to other presets.
const departmentHeadWidgets=Object.freeze([...existingWidgets])
export const adminDashboardWidgetPresets=Object.freeze({
 SYSTEM_ADMIN:existingWidgets,
 DEPARTMENT_HEAD:departmentHeadWidgets,
 COMPLAINT_MANAGER:Object.freeze(['complaints','answers']),
 VEHICLE_MANAGER:Object.freeze(['vehicles','dispatches']),
})
export function dashboardWidgetsFor(user){
 // Use the server-provided adminType; never derive a type from the login ID.
 return user?.role==='ADMIN' ? (adminDashboardWidgetPresets[user.adminType]||[]) : []
}
