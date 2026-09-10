import { describe, expect, it } from 'vitest'
import { maskIdNumber, maskPhone } from './maskSensitive'

describe('sensitive data masking', () => {
  it('masks a Chinese identity number', () => {
    expect(maskIdNumber('110101199001011234')).toBe('110********1234')
  })

  it('masks a mobile number', () => {
    expect(maskPhone('13800138000')).toBe('138****8000')
  })

  it('does not expose short identifiers', () => {
    expect(maskIdNumber('A12345')).toBe('A***5')
    expect(maskPhone('123456')).toBe('12***')
  })

  it('uses a placeholder for missing values', () => {
    expect(maskIdNumber()).toBe('—')
    expect(maskPhone()).toBe('—')
  })
})
