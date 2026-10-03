import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { mainApi, dashboardApi, authError, API_INTEGRATION_ENABLED } from '../src/api/clients.js'
import { login, signup } from '../src/api/auth.js'
import { auth } from '../src/stores/auth.js'
import { listParams, listPage } from '../src/api/pagination.js'

// Isolated axios adapters only: no mock authentication is installed in application code.
test('API contract, session restoration, concurrency and guarded routes', { skip: !API_INTEGRATION_ENABLED }, async () => {
  assert.equal(mainApi.defaults.baseURL, 'http://localhost:8081')
  assert.equal(dashboardApi.defaults.baseURL, 'http://localhost:8081')
  assert.equal(mainApi.defaults.withCredentials, true)
  assert.equal(dashboardApi.defaults.withCredentials, true)
  const requests = []
  let user = { id: 1, name: '검증 사용자', loginId: 'tester', role: 'USER' }
  // 백엔드 공통 응답 { success, data, error } 형식
  mainApi.defaults.adapter = async config => {
    requests.push(config)
    const data = config.url === '/api/v1/auth/me' ? user
      : config.url === '/api/v1/auth/login' ? { accessToken: 'test-token', tokenType: 'Bearer', expiresIn: 1800, member: { ...user, redirectPath: '/complaints' } }
      : { id: 2 }
    return { data: { success: true, data, error: null }, status: 200, statusText: 'OK', headers: {}, config }
  }
  await login('tester', 'test-password!')
  assert.deepEqual(requests.at(-1).data, { loginId: 'tester', password: 'test-password!' })
  await signup({ username:'tester', name:'검증 사용자', email:'example@email.com', password:'password!', passwordConfirmation:'password!', ageConfirmed:true })
  assert.deepEqual(requests.at(-1).data, { name:'검증 사용자', loginId:'tester', email:'example@email.com', password:'password!', passwordConfirm:'password!', over14:true })
  auth.clear()
  const count = requests.length
  await Promise.all([auth.restore(), auth.restore()])
  assert.equal(requests.length, count + 1)
  assert.deepEqual(auth.profile.value, { name:'검증 사용자', username:'tester', role:'USER' })
  assert.equal(requests.at(-1).headers.Authorization, 'Bearer test-token')
  await auth.restore()
  assert.equal(requests.length, count + 1)
  const source = (await readFile(new URL('../src/router/index.js', import.meta.url), 'utf8'))
    .replace("'../stores/auth'", JSON.stringify(new URL('../src/stores/auth.js', import.meta.url).href))
    .replace("'../api/clients'", JSON.stringify(new URL('../src/api/clients.js', import.meta.url).href))
    .replace("'vue-router'", JSON.stringify(import.meta.resolve('vue-router')))
    .replace('createWebHistory', 'createMemoryHistory').replace('createWebHistory', 'createMemoryHistory')
    .replace('import.meta.env.BASE_URL', "'/'")
    .replace(/component: \(\) => import\('[^']+'\)/g, "component: { render: () => null }")
  const { default: router } = await import('data:text/javascript;base64,' + Buffer.from(source).toString('base64'))
  assert.equal(router.resolve('/dashboard/complaints/12').name, 'admin-complaint-detail')
  assert.equal(router.resolve('/admin/complaints/12').name, 'admin-complaint-detail')
  assert.equal(router.resolve('/complaints/new').name, 'user-complaint-create')
  assert.equal(router.resolve('/user/complaints/12/edit').name, 'user-complaint-edit')
  await router.push('/dashboard')
  assert.equal(router.currentRoute.value.path, '/forbidden')
  await router.push('/complaints')
  assert.equal(router.currentRoute.value.path, '/complaints')
  user = { ...user, role:'ADMIN' }
  auth.clear(); await auth.restore()
  await router.push('/dashboard')
  assert.equal(router.currentRoute.value.path, '/dashboard')
  assert.equal(authError({ response:{status:401,data:{error:{code:'UNAUTHORIZED'}}} }), 'unauthorized')
  assert.equal(authError({ response:{status:403,data:{error:{code:'FORBIDDEN'}}} }), 'forbidden')
  mainApi.defaults.adapter = async config => { throw { config, response:{status:401,data:{success:false,data:null,error:{code:'UNAUTHORIZED',message:'로그인이 필요합니다.'}}} } }
  await assert.rejects(mainApi.get('/api/private'))
  await router.isReady()
  await new Promise(resolve => setImmediate(resolve))
  assert.equal(auth.state.user, null)
  assert.equal(router.currentRoute.value.path, '/login')
  user = { role:'USER' }
  mainApi.defaults.adapter = async config => ({data:{success:true,data:user,error:null},status:200,headers:{},config})
  auth.clear()
  await assert.rejects(auth.restore(), /응답 명세 확인/)
  assert.equal(auth.state.user, null)
  assert.equal(listParams({page:0}).page, 1)
  assert.deepEqual(listPage({content:[],page:1,size:10,totalElements:0,totalPages:0}).content, [])
})

test('preview mode sends no backend requests', async () => {
  assert.equal(API_INTEGRATION_ENABLED, false)
  let calls = 0
  for (const api of [mainApi, dashboardApi]) {
    api.defaults.adapter = async () => { calls++; throw new Error('must not send') }
    await assert.rejects(api.get('/api/auth/me'), /비활성화/)
  }
  auth.clear()
  assert.equal(await auth.restore(), null)
  assert.equal(calls, 0)
})
