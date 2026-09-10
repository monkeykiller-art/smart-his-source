export function maskIdNumber(value?: string) {
  if (!value) return '—'
  if (value.length <= 6) return `${value.slice(0, 1)}***${value.slice(-1)}`
  return `${value.slice(0, 3)}********${value.slice(-4)}`
}

export function maskPhone(value?: string) {
  if (!value) return '—'
  if (value.length < 7) return `${value.slice(0, 2)}***`
  return `${value.slice(0, 3)}****${value.slice(-4)}`
}
