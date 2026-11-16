import { mutateJson, requestJson } from '@/lib/api-client'
import type { AccountProfile, ProfileInput, ProfileSaved } from '../schemas'

const path = '/dashboard-api/account/profile'
export function getAccountProfile(signal: AbortSignal) { return requestJson<AccountProfile>(path, { signal }) }
export function updateAccountProfile(input: ProfileInput, expectedUpdatedAt: string) {
  return mutateJson<ProfileSaved>(path, 'PUT', { ...input, expectedUpdatedAt })
}
