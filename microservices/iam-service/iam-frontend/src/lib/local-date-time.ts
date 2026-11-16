export function parseLocalDateTime(value: string): Date | undefined {
  const match = /^(\d{4})-(\d{2})-(\d{2})(?:T(\d{2}):(\d{2})(?::\d{2})?)?$/.exec(value)
  if (!match) return undefined
  const [, year, month, day, hour = '00', minute = '00'] = match
  const date = new Date(Number(year), Number(month) - 1, Number(day), Number(hour), Number(minute))
  return date.getFullYear() === Number(year) && date.getMonth() === Number(month) - 1 && date.getDate() === Number(day)
    && date.getHours() === Number(hour) && date.getMinutes() === Number(minute) ? date : undefined
}
export function localDateValue(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
export function selectedDateValue(date: Date, previous: string, includeTime: boolean) {
  const time = /^\d{4}-\d{2}-\d{2}T(\d{2}:\d{2})/.exec(previous)?.[1] || '00:00'
  return localDateValue(date) + (includeTime ? `T${time}` : '')
}
