import { mutateJson, requestJson } from '@/lib/api-client'
import type { DirectUserPermission, PermissionEffect } from '../types/types'
const path = (uuid: string) => `/dashboard-api/users/${encodeURIComponent(uuid)}/permissions`
export const getUserPermissions = (uuid: string, signal?: AbortSignal) => requestJson<DirectUserPermission[]>(path(uuid), { signal })
export const getUserAuthorities = (uuid: string, signal?: AbortSignal) => requestJson<{ effectivePermissions: string[] }>(`/dashboard-api/users/${encodeURIComponent(uuid)}/authorities`, { signal })
export const grantUserPermissions = (uuid: string, permissionUuids: string[], effect: PermissionEffect, expiresAt: string | null, reason: string) =>
  mutateJson<unknown>(path(uuid), 'POST', { permissionUuids, effect, expiresAt, reason })
export const revokeUserPermission = (uuid: string, permissionUuid: string) => mutateJson<void>(`${path(uuid)}/${encodeURIComponent(permissionUuid)}`, 'DELETE')
