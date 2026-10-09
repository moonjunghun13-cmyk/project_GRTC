import {canAccessAdminPath,adminMenuForPath} from '../utils/adminAccess'
import {isAdminAccount} from '../utils/memberAccount'
import { setPreviewRole } from '../mocks/profiles'
import { findComplaint, canReadComplaint } from '../mocks/complaints'
import { safePreviousPage } from '../stores/restrictedNavigation'
import { auth } from '../stores/auth'
import { currentUser } from '../stores/currentUser'
import { authError, installApiErrorHandlers, API_INTEGRATION_ENABLED } from '../api/clients'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [{ path: '/', name: 'intro', component: () => import('../views/public/IntroView.vue') },
{ path: '/', component: () => import('../layouts/AuthLayout.vue'), children: [
{ path: 'login', name: 'login', component: () => import('../views/auth/LoginView.vue') },
{ path: 'signup', name: 'signup', component: () => import('../views/auth/SignupView.vue') },
] },
{ path: '/dashboard', alias: '/admin', meta: { requiresAuth: true, role: 'ADMIN' }, component: () => import('../layouts/AdminLayout.vue'),  children: [{ path: '', name: 'admin-dashboard', component: () => import('../views/admin/DashboardView.vue') },
{ path: 'access-denied', name: 'admin-menu-access-denied', component: () => import('../views/errors/MenuAccessDenied.vue') },
{ path: 'vehicles/new', name: 'admin-vehicle-create', component: () => import('../components/fleet/FleetRegistration.vue'), props: {kind:'vehicles'} },
{ path: 'dispatches/new', name: 'admin-dispatch-create', component: () => import('../components/fleet/FleetRegistration.vue'), props: {kind:'dispatches'} },
{ path: 'vehicles/:id/edit', name: 'admin-vehicle-edit', component: () => import('../components/fleet/FleetRegistration.vue'), props: {kind:'vehicles'} },
{ path: 'dispatches/:id/edit', name: 'admin-dispatch-edit', component: () => import('../components/fleet/FleetRegistration.vue'), props: {kind:'dispatches'} },
{ path: 'vehicles', name: 'admin-vehicles', component: () => import('../views/admin/vehicles/VehicleListView.vue') },
{ path: 'dispatches', name: 'admin-dispatches', component: () => import('../views/admin/dispatches/DispatchListView.vue') },
{ path: 'dispatches/:id', name: 'admin-dispatch-detail', component: () => import('../components/fleet/FleetDetailPreview.vue'), props: {kind:'dispatches'} },
{ path: 'vehicles/:id', name: 'admin-vehicle-detail', component: () => import('../components/fleet/FleetDetailPreview.vue'), props: {kind:'vehicles'} },
{ path: 'operations', name: 'admin-operations', component: () => import('../views/admin/operations/OperationView.vue') },
{ path: 'complaints', name: 'admin-complaints', component: () => import('../views/admin/complaints/AdminComplaintListView.vue') },
{ path: 'complaints/new', name: 'admin-complaint-create', component: () => import('../views/user/complaints/ComplaintCreateView.vue') },
{ path: 'complaints/:id', name: 'admin-complaint-detail', component: () => import('../views/admin/complaints/AdminComplaintDetailView.vue') },
{ path: 'members', name: 'admin-members', component: () => import('../views/admin/members/MemberListView.vue') },
{ path: 'members/:id', name: 'admin-member-detail', component: () => import('../views/admin/members/MemberDetailView.vue') }] },
{ path: '/complaints', alias: '/user/complaints', meta: { requiresAuth: true }, component: () => import('../layouts/UserLayout.vue'), children: [{ path: '', name: 'user-complaints', component: () => import('../views/user/complaints/UserComplaintListView.vue') },
{ path: '/members/me', name: 'user-member-info', meta: {citizenInfo:true}, component: () => import('../views/user/members/MyMemberView.vue') },
{ path: 'new', name: 'user-complaint-create', component: () => import('../views/user/complaints/ComplaintCreateView.vue') },
{ path: ':id', name: 'user-complaint-detail', component: () => import('../views/user/complaints/UserComplaintDetailView.vue') },
{ path: '/access-denied', name: 'menu-access-denied', component: () => import('../views/errors/MenuAccessDenied.vue') },
{ path: '/forbidden', name: 'forbidden', component: () => import('../views/errors/ForbiddenView.vue') },
{ path: ':id/edit', name: 'user-complaint-edit', component: () => import('../views/user/complaints/ComplaintEditView.vue') }] }],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to, from) => {
  const guestPage = ['intro','login','signup'].includes(to.name)
  const home = user => ({name:isAdminAccount(user)?'admin-dashboard':'user-complaints',replace:true})
  if (!API_INTEGRATION_ENABLED) {
    setPreviewRole(to.query.previewRole)
    const user = currentUser.value
    if (to.meta.requiresAuth && !user) return {name:'login',replace:true}
    if (!user) return true
    if (guestPage) return home(user)
    const safe = route => route.matched.length && route.name !== 'forbidden' && (!route.meta.role || user.role === route.meta.role) && (!['user-complaint-detail','user-complaint-edit'].includes(route.name) || canReadComplaint(user,findComplaint(route.params.id)))
    if (from.fullPath !== to.fullPath && safe(from)) safePreviousPage.value = from.fullPath
    if (to.meta.role === 'ADMIN' && user.role !== 'ADMIN') return {name:'forbidden',query:{reason:'admin'},replace:true}
    if (['user-complaint-detail','user-complaint-edit'].includes(to.name)) {
      const complaint = findComplaint(to.params.id)
      if (complaint && !canReadComplaint(user,complaint)) return {name:'forbidden',query:{reason:'ownership'},replace:true}
    }
    return true
  }
  if (!to.meta.requiresAuth && !guestPage) return true
  let user
  try { user = await auth.restore() } catch (error) {
    auth.state.notice = auth.errorMessage(error)
    if (to.meta.requiresAuth) return authError(error) === 'forbidden' ? '/forbidden' : '/login'
    return true
  }
  if (guestPage && user) return home(user)
  if (to.meta.requiresAuth && !user) return '/login'
  if (to.meta.citizenInfo && isAdminAccount(user)) return '/dashboard'
  if (to.meta.role && user?.role !== to.meta.role) return {name:'menu-access-denied',query:{menu:to.path.replace(/^\/admin/, '/dashboard').split('/')[2]||'complaints'},replace:true}
  if(to.meta.role==='ADMIN' && to.name!=='admin-menu-access-denied' && !canAccessAdminPath(user,to.path))return {name:'admin-menu-access-denied',query:{menu:adminMenuForPath(to.path)||'dashboard'},replace:true}
  return true
})
installApiErrorHandlers((kind, error) => {
  if (kind === 'unauthorized') {
    auth.clear()
    auth.state.notice = auth.errorMessage(error)
    if (router.currentRoute.value.path !== '/login') void router.replace('/login')
   } else if (kind === 'forbidden') {
    const current=router.currentRoute.value
    if (['admin-menu-access-denied','menu-access-denied','forbidden'].includes(current.name))return
    const menu=current.path.replace(/^\/admin(?=\/|$)/,'/dashboard').split('/')[2]
    if(current.meta.role==='ADMIN'&&['vehicles','dispatches','operations','complaints','members'].includes(menu))void router.replace({name:'admin-menu-access-denied',query:{menu}})
    else void router.replace('/forbidden')
  }
})
export default router



