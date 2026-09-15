export type MoneyValue = string | number | undefined

// Billing APIs send decimal strings; use 1/10000 yuan units throughout.
export function moneyUnits(value: MoneyValue): bigint {
  const text = String(value ?? '0')
  if (!/^\d+(\.\d{1,4})?$/.test(text)) throw new Error('Invalid money amount')
  const [whole, fraction = ''] = text.split('.')
  return BigInt(whole) * 10000n + BigInt(fraction.padEnd(4, '0'))
}

function fromUnits(units: bigint): string {
  const fraction = (units % 10000n).toString().padStart(4, '0')
  return `${units / 10000n}.${fraction.slice(0, 2)}${fraction.slice(2).replace(/0+$/, '')}`
}

export function moneyAmount(value: MoneyValue): string {
  return fromUnits(moneyUnits(value))
}

export function moneyDue(payable: MoneyValue, paid: MoneyValue): string {
  const due = moneyUnits(payable) - moneyUnits(paid)
  return fromUnits(due > 0n ? due : 0n)
}

export function moneySum(values: MoneyValue[]): string {
  return fromUnits(values.reduce<bigint>((sum, value) => sum + moneyUnits(value), 0n))
}

export function validPayment(value: string, maximum: MoneyValue): boolean {
  if (!/^\d{1,14}(\.\d{1,4})?$/.test(value)) return false
  const units = moneyUnits(value)
  return units > 0n && units <= moneyUnits(maximum)
}

export const formatMoney = (value: MoneyValue) => `¥${moneyAmount(value)}`
