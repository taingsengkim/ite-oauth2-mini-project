import { requestJson } from '@/lib/api-client'
import type { AuditFilters } from '../schemas'
import type { AuditActionOption, AuditPage } from '../types'

export function listAuditLogs(filters: AuditFilters, page: number, signal: AbortSignal, size = 20) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  for (const [name, value] of Object.entries(filters)) {
    if (value) params.set(name, name === 'from' || name === 'to' ? new Date(value).toISOString() : value)
  }
  return requestJson<AuditPage>(`/dashboard-api/audit-logs?${params}`, { signal })
}
export function getAuditOptions(signal: AbortSignal) {
  return requestJson<AuditActionOption[]>('/dashboard-api/audit-logs/options', { signal })
}
