import { z } from 'zod'

const date = z.string().refine((value) => !value || Number.isFinite(new Date(value).getTime()), 'Choose a valid date and time.')
export const auditFiltersSchema = z.object({
  actor: z.string().trim().max(255), action: z.string().max(64), resource: z.string().max(30),
  targetId: z.string().trim().max(100), outcome: z.enum(['', 'SUCCESS', 'FAILURE']), from: date, to: date,
}).refine((value) => !value.from || !value.to || new Date(value.from) <= new Date(value.to), { path: ['to'], message: 'To must be after From.' })
export type AuditFilters = z.infer<typeof auditFiltersSchema>
export const emptyAuditFilters: AuditFilters = { actor: '', action: '', resource: '', targetId: '', outcome: '', from: '', to: '' }
