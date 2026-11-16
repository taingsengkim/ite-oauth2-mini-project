import { z } from 'zod'
import { activityActions, activityMessages } from './i18n'
export const emptyActivityFilters = { action: '', from: '', to: '' }
export function activitySchema(locale: 'en' | 'km') {
  const t = activityMessages[locale]
  const date = z.string().refine((value) => !value || Number.isFinite(new Date(value).getTime()), t.invalidDate)
  return z.object({ action: z.string().refine((value) => !value || Object.hasOwn(activityActions, value), t.action), from: date, to: date })
    .refine((value) => !value.from || !value.to || new Date(value.from) <= new Date(value.to), { path: ['to'], message: t.invalidRange })
}
export type ActivityFilters = z.infer<ReturnType<typeof activitySchema>>
export function activityParams(filters: ActivityFilters, page: number) {
  const params = new URLSearchParams({ page: String(page), size: '20' })
  if (filters.action) params.set('action', filters.action)
  if (filters.from) params.set('from', new Date(filters.from).toISOString())
  if (filters.to) params.set('to', new Date(filters.to).toISOString())
  return params
}
