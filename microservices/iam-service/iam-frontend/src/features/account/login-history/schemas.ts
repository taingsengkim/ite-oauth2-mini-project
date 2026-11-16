import { z } from 'zod'
import { historyMessages } from './i18n'
export const emptyHistoryFilters = { outcome: '' as '' | 'SUCCESS' | 'FAILURE', from: '', to: '', suspiciousOnly: false }
export function historyFilterSchema(locale: 'en' | 'km') {
  const t = historyMessages[locale]
  const date = z.string().refine((value) => !value || Number.isFinite(new Date(value).getTime()), t.invalidDate)
  return z.object({ outcome: z.enum(['', 'SUCCESS', 'FAILURE']), from: date, to: date, suspiciousOnly: z.boolean() })
    .refine((value) => !value.from || !value.to || new Date(value.from) <= new Date(value.to), { path: ['to'], message: t.invalidRange })
}
export type HistoryFilters = z.infer<ReturnType<typeof historyFilterSchema>>
export function historyParams(filters: HistoryFilters, page: number) {
  const params = new URLSearchParams({ page: String(page), size: '20' })
  if (filters.outcome) params.set('outcome', filters.outcome)
  if (filters.from) params.set('from', new Date(filters.from).toISOString())
  if (filters.to) params.set('to', new Date(filters.to).toISOString())
  if (filters.suspiciousOnly) params.set('suspiciousOnly', 'true')
  return params
}
