import test from 'node:test'
import assert from 'node:assert/strict'
import { findComplaint, canReadComplaint } from '../src/mocks/complaints.js'
test('ownership is checked before rendering, administrators are exempt', () => {
  const user = {id:'2',role:'USER'}
  assert.equal(canReadComplaint(user,findComplaint('1')),true)
  assert.equal(canReadComplaint(user,findComplaint('2')),false)
  assert.equal(canReadComplaint({id:'1',role:'ADMIN'},findComplaint('2')),true)
  assert.equal(canReadComplaint(user,findComplaint('missing')),false)
  assert.equal(canReadComplaint(null,findComplaint('1')),false)
})
