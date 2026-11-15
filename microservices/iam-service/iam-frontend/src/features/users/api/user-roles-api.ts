import { mutateJson, requestJson } from '@/lib/api-client'
import type { AssignedUserRole } from '../types/types'

const path = (uuid: string) => `/dashboard-api/users/${encodeURIComponent(uuid)}/roles`
export const getUserRoles = (uuid: string, signal?: AbortSignal) => requestJson<AssignedUserRole[]>(path(uuid), { signal })
export const assignUserRoles = (uuid: string, roleUuids: string[], expiresAt: string | null, reason: string) =>
  mutateJson<unknown>(path(uuid), 'POST', { roleUuids, expiresAt, reason })
export const changeUserRoleExpiry = (uuid: string, roleUuid: string, expiresAt: string | null) =>
  mutateJson<unknown>(`${path(uuid)}/${encodeURIComponent(roleUuid)}`, 'PUT', { expiresAt, reason: 'Assignment expiry changed from dashboard' })
export const revokeUserRole = (uuid: string, roleUuid: string) => mutateJson<void>(`${path(uuid)}/${encodeURIComponent(roleUuid)}`, 'DELETE')
