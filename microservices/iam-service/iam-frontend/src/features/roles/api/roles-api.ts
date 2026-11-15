import { mutateJson, requestJson } from '@/lib/api-client'
import type { Role, RoleInput, RoleSummary, Permission, AssignedPermission, Page } from '../types'

const base = '/dashboard-api/roles'
const path = (uuid: string) => `${base}/${encodeURIComponent(uuid)}`

export function listRoles(query: { page: number; search: string; active: string }, signal?: AbortSignal) {
  const params = new URLSearchParams({ page: String(query.page), size: '20', search: query.search })
  if (query.active !== '') params.set('active', query.active)
  return requestJson<Page<RoleSummary>>(`${base}?${params}`, { signal })
}
export const getRole = (uuid: string, signal?: AbortSignal) => requestJson<Role>(path(uuid), { signal })
export const createRole = (input: RoleInput) => mutateJson<Role>(base, 'POST', input)
export const updateRole = (uuid: string, input: RoleInput) => mutateJson<Role>(path(uuid), 'PATCH', input)
export const deleteRole = (uuid: string) => mutateJson<void>(path(uuid), 'DELETE')
export const changeRoleStatus = (uuid: string, active: boolean) => mutateJson<void>(`${path(uuid)}/${active ? 'activate' : 'deactivate'}`, 'POST')
export const getRolePermissions = (uuid: string, signal?: AbortSignal) => requestJson<AssignedPermission[]>(`${path(uuid)}/permissions`, { signal })
export const assignPermissions = (uuid: string, permissionUuids: string[]) => mutateJson<unknown>(`${path(uuid)}/permissions`, 'POST', {
  permissionUuids, reason: 'Assigned from role management',
})
export const revokePermission = (uuid: string, permissionUuid: string) => mutateJson<void>(`${path(uuid)}/permissions/${encodeURIComponent(permissionUuid)}`, 'DELETE')
export function listPermissions(page: number, search: string, signal?: AbortSignal) {
  const params = new URLSearchParams({ page: String(page), size: '20', search, active: 'true' })
  return requestJson<Page<Permission>>(`/dashboard-api/permissions?${params}`, { signal })
}
