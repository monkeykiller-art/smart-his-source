import { describe, expect, it } from 'vitest'
import { canCloseEncounter, ordersForEncounter } from './clinicalWorkflow'
import type { ClinicalOrder, Diagnosis, MedicalRecord } from '@/types/clinical'

const signedRecord = { id: 1, recordNo: 'BL001', patientId: 2, deptId: 3, doctorId: 4, recordType: 'OUTPATIENT', recordStatus: 'SIGNED' } as MedicalRecord
const diagnosis = { id: 1, patientId: 2, doctorId: 4, diagnosisName: '上呼吸道感染' } as Diagnosis

describe('clinical encounter workflow', () => {
  it('requires an active encounter, signed record, and diagnosis before closing', () => {
    expect(canCloseEncounter('PLANNED', [signedRecord], [diagnosis])).toBe(false)
    expect(canCloseEncounter('IN_PROGRESS', [], [diagnosis])).toBe(false)
    expect(canCloseEncounter('IN_PROGRESS', [signedRecord], [])).toBe(false)
    expect(canCloseEncounter('IN_PROGRESS', [signedRecord], [diagnosis])).toBe(true)
  })

  it('keeps orders from the selected encounter while accepting legacy orders without an encounter id', () => {
    const orders = [
      { id: 1, encounterId: 10, orderNo: 'YZ1', patientId: 2, deptId: 3, doctorId: 4, orderType: 'LAB', orderStatus: 'PENDING', items: [] },
      { id: 2, encounterId: 11, orderNo: 'YZ2', patientId: 2, deptId: 3, doctorId: 4, orderType: 'EXAM', orderStatus: 'PENDING', items: [] },
      { id: 3, orderNo: 'YZ3', patientId: 2, deptId: 3, doctorId: 4, orderType: 'MEDICINE', orderStatus: 'PENDING', items: [] },
    ] as ClinicalOrder[]
    expect(ordersForEncounter(orders, 10).map((order) => order.id)).toEqual([1, 3])
  })
})
