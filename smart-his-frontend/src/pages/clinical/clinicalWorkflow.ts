import type { ClinicalOrder, Diagnosis, MedicalRecord } from '@/types/clinical'

export function canCloseEncounter(status: string | undefined, records: MedicalRecord[], diagnoses: Diagnosis[]) {
  return status === 'IN_PROGRESS'
    && records.some((record) => record.recordStatus === 'SIGNED')
    && diagnoses.length > 0
}

export function ordersForEncounter(orders: ClinicalOrder[], encounterId: number | undefined) {
  return orders.filter((order) => !encounterId || !order.encounterId || order.encounterId === encounterId)
}
