import { mutateJson, requestJson } from '@/lib/api-client'
import type { Page } from '@/features/roles/types'
import type { Permission, PermissionInput, PermissionQuery } from '../types'
const base = '/dashboard-api/permissions'
const path = (uuid: string) => `${base}/${encodeURIComponent(uuid)}`
export function listPermissions(query: PermissionQuery, signal?: AbortSignal) {
  const params = new URLSearchParams({ page: String(query.page), size: '20', search: query.search, sort: 'updatedAt,desc' })
  if (query.priorityUuid) params.set('priorityUuid', query.priorityUuid)
  for (const key of ['resource', 'action', 'active'] as const) if (query[key]) params.set(key, query[key])
  return requestJson<Page<Permission>>(`${base}?${params}`, { signal })
}
export const getPermission = (uuid: string, signal?: AbortSignal) => requestJson<Permission>(path(uuid), { signal })
export const createPermission = (input: PermissionInput) => mutateJson<Permission>(base, 'POST', input)
export const updatePermission = (uuid: string, input: PermissionInput) => mutateJson<Permission>(path(uuid), 'PATCH', input)
export const deletePermission = (uuid: string) => mutateJson<void>(path(uuid), 'DELETE')
export const changePermissionStatus = (uuid: string, active: boolean) => mutateJson<void>(`${path(uuid)}/${active ? 'activate' : 'deactivate'}`, 'POST')
