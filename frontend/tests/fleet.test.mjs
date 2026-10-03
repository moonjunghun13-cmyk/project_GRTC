import test from 'node:test'
import assert from 'node:assert/strict'
import {vehicles,dispatches} from '../src/mocks/fleet.js'
import {useFleetList} from '../src/composables/useFleetList.js'
test('mock dispatches share vehicle identities and valid times',()=>{
 assert.ok(vehicles.length>=30 && dispatches.length>=30)
 for(const d of dispatches){assert.equal(vehicles.find(v=>v.id===d.vehicleId).vehicleNo,d.vehicleNo);assert.ok(d.arrivalTime>d.departureTime)}
 assert.ok(dispatches.slice(0,10).some(d=>d.status==='CANCELLED'&&d.remark==='결함 발견'))
})
test('summary ignores dispatch status but respects other applied filters; pagination is zero-based internally',()=>{
 const s=useFleetList('dispatches');s.draft.status='CANCELLED';s.search();assert.equal(s.summary.value[0].value,64);assert.ok(s.rows.value.every(d=>d.status==='CANCELLED'))
 s.move(2);assert.equal(s.requestPage.value,1);s.draft.keyword='D1004';s.search();assert.equal(s.page.value,1);assert.equal(s.summary.value[0].value,1)
 s.selectAll(true);assert.deepEqual(s.selected.value,['4']);s.move(1);assert.deepEqual(s.selected.value,[])
 const v=useFleetList('vehicles');v.draft.keyword='G101';v.search();assert.equal(v.rows.value.length,1);assert.equal(v.summary.value[0].value,64)
})
