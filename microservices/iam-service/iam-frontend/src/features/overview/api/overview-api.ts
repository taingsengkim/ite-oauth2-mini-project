import { requestJson } from '@/lib/api-client'

export type Overview = {
  statistics: { totalUsers: number; activeUsers: number; disabledUsers: number; lockedUsers: number; roles: number; permissions: number; clients: number }
  generatedAt: string
}
export function getOverview(signal: AbortSignal) {
  return requestJson<Overview>('/dashboard-api/overview', { signal })
}
