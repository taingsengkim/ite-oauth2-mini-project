import { ApiError, requestJson } from '@/lib/api-client'
import type { PasswordRequirements } from '@/lib/password-policy'
export type RecoveryContext = { enabled: boolean; policy: PasswordRequirements; csrfHeaderName: string; csrfToken: string }
export const recoveryContext = (signal?: AbortSignal) => requestJson<RecoveryContext>('/recovery-api/context', { signal })
export async function recoveryMutation<T>(path: 'request' | 'reset', body: unknown) {
  const context = await recoveryContext()
  if (!context.enabled) throw new ApiError('Email recovery is unavailable', 503)
  return requestJson<T>(`/recovery-api/${path}`, { method: 'POST', headers: { 'Content-Type': 'application/json', [context.csrfHeaderName]: context.csrfToken }, body: JSON.stringify(body) })
}
