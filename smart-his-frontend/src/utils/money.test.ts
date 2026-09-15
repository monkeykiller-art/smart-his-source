import { describe, expect, it } from 'vitest'
import { formatMoney, moneyDue, moneySum, validPayment } from './money'

describe('exact billing amounts', () => {
  it('retains four decimal places and sums without floating point drift', () => {
    expect(moneySum(['0.10', '0.20'])).toBe('0.30')
    expect(moneyDue('99999999999999.9999', '99999999999999.9998')).toBe('0.0001')
    expect(formatMoney('12.3456')).toBe('¥12.3456')
    expect(formatMoney('12')).toBe('¥12.00')
  })
  it('rejects excessive precision, zero, negative and overpayment', () => {
    expect(validPayment('0.0001', '0.0001')).toBe(true)
    for (const value of ['0', '-1', 'NaN', '1e2', '0.00001', '0.0002']) {
      expect(validPayment(value, '0.0001')).toBe(false)
    }
    expect(moneyDue('1', '2')).toBe('0.00')
  })
})
