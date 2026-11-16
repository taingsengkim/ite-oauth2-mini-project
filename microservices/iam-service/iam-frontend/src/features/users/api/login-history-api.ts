import { requestJson } from '@/lib/api-client'

export type LoginOutcome = 'SUCCESS' | 'FAILURE'
export type LoginHistoryEvent = {
  uuid: string; userUuid: string | null; attemptedLogin: string | null; provider: string
  outcome: LoginOutcome; reason: string; occurredAt: string
  ipAddress: string | null; userAgent: string | null; suspicious: boolean
}
export type LoginHistoryPage = {
  content: LoginHistoryEvent[]; page: number; size: number; totalElements: number; totalPages: number
}

export function listLoginHistory(url: string, signal: AbortSignal) {
  return requestJson<LoginHistoryPage>(url, { signal })
}
