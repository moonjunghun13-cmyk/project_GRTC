import test from 'node:test'
import assert from 'node:assert/strict'
import {vehicles,dispatches,buildDepotDispatches} from '../src/mocks/fleet.js'
import {useFleetList} from '../src/composables/useFleetList.js'
import {depotTimetable,dayTypeOf,depotMoves,depotSummary,serviceMinute,serviceDate,isoDate} from '../src/mocks/depotTimetable.js'
test('입·출고 시간표: 평일 출고 50·입고 50, 토요일·휴일 각 41회', () => {
 assert.equal(depotTimetable.WEEKDAY.departs.length, 50); assert.equal(depotTimetable.WEEKDAY.returns.length, 50)
 for (const d of ['SATURDAY', 'HOLIDAY']) { assert.equal(depotTimetable[d].departs.length, 41); assert.equal(depotTimetable[d].returns.length, 41) }
})
test('날짜별 시간표 구분: 토요일, 일요일·공휴일(휴일), 평일', () => {
 assert.equal(dayTypeOf('2026-10-10'), 'SATURDAY')
 assert.equal(dayTypeOf('2026-10-11'), 'HOLIDAY')
 assert.equal(dayTypeOf('2026-10-09'), 'HOLIDAY') // 한글날
 assert.equal(dayTypeOf('2026-10-03'), 'HOLIDAY') // 개천절(토)
 assert.equal(dayTypeOf('2026-10-12'), 'WEEKDAY')
 assert.ok(serviceMinute('00:05') > serviceMinute('23:50'))
})
test('시간표 배차: 출고·입고가 각각 한 건, 차량 정보가 일치하고 시각은 하나', () => {
 const rows = buildDepotDispatches('2026-10-12', {now: new Date('2026-10-12T09:00:00')})
 assert.equal(rows.length, 100)
 assert.equal(rows.filter(r => r.moveType === 'DEPART').length, 50)
 for (const d of rows) { assert.equal(vehicles.find(v => v.id === d.vehicleId).vehicleNo, d.vehicleNo); assert.equal(d.arrivalTime, d.departureTime); assert.ok(d.trainNo) }
 assert.equal(rows[0].trainNo, '1901'); assert.equal(rows[0].status, 'COMPLETED')
 assert.equal(rows.at(-1).status, 'WAITING')
 // 같은 차량이 나가 있는 동안 다시 출고되지 않는다
 const out = new Set()
 for (const d of rows) { if (d.moveType === 'DEPART') { assert.ok(!out.has(d.vehicleId)); out.add(d.vehicleId) } else out.delete(d.vehicleId) }
})
test('대시보드 입·출고현황: 09시 기준 다음 출고·입고와 운행 중 편성', () => {
 const date = '2026-10-12', card = depotSummary(depotMoves(date), date, new Date('2026-10-12T09:00:00'))
 assert.equal(card.departTotal, 50); assert.equal(card.returnTotal, 50)
 assert.equal(card.outNow, card.departDone - card.returnDone)
 assert.deepEqual(card.nextDepart, {trainNo: '1063', time: '09:14'})
 assert.deepEqual(card.nextReturn, {trainNo: '1048', time: '09:10'})
 assert.equal(card.hourly.reduce((a, h) => a + h.departs, 0), 50)
})
test('배차 목록: 오늘 운행일로 시작, 구분·열번 검색, 요약은 상태 필터 제외', () => {
 const s = useFleetList('dispatches')
 assert.equal(s.summary.value[0].value, dispatches.length)
 s.draft.moveType = 'DEPART'; s.search(); assert.ok(s.rows.value.every(d => d.moveType === 'DEPART'))
 assert.equal(s.depotCounts.value.returns, 0)
 s.draft.moveType = ''; s.draft.keyword = '1901'; s.search(); assert.ok(s.rows.value.length >= 1 && s.rows.value.every(d => d.trainNo.includes('1901') || d.dispatchNo.includes('1901')))
 s.draft.keyword = ''; s.draft.status = 'COMPLETED'; s.search(); assert.equal(s.summary.value[0].value, dispatches.length)
 s.move(2); assert.equal(s.requestPage.value, s.totalPages.value > 1 ? 1 : 0)
 const v = useFleetList('vehicles'); v.draft.keyword = '101편성'; v.search(); assert.equal(v.rows.value.length, 1); assert.equal(v.summary.value[0].value, 23)
})
test('차량: 실제 1호선 23편성(101~123편성), 평일 최대 운행 편성보다 배차 가능 차량이 많다', () => {
 assert.equal(vehicles.length, 23)
 assert.equal(vehicles[0].vehicleNo, '101편성'); assert.equal(vehicles.at(-1).vehicleNo, '123편성')
 const available = vehicles.filter(v => v.status === 'RUNNING' || v.status === 'STANDBY').length
 const rows = buildDepotDispatches('2026-10-12'); let out = 0, peak = 0
 for (const d of rows) { out += d.moveType === 'DEPART' ? 1 : -1; peak = Math.max(peak, out) }
 assert.ok(peak <= available, `peak ${peak} > available ${available}`)
})
test('시간표로 배차 만들기: 같은 날짜는 두 번 만들 수 없다', async () => {
 // 오늘 운행일은 이미 목업 배차가 있으므로, 그 날짜와 겹치지 않는 평일을 고른다
 const d = new Date(serviceDate() + 'T12:00:00'); d.setDate(d.getDate() + 30)
 while (dayTypeOf(isoDate(d)) !== 'WEEKDAY') d.setDate(d.getDate() + 1)
 const date = isoDate(d), s = useFleetList('dispatches'), r = await s.generate(date)
 assert.equal(r.created, 100)
 await assert.rejects(() => s.generate(date))
 await assert.rejects(() => s.generate(serviceDate()))
})
