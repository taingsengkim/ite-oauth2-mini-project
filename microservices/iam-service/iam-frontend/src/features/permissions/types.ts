export type Permission = {
  uuid: string; name: string; resource: string; action: string; description: string | null
  active: boolean; system: boolean; createdAt: string; updatedAt: string
}
export type PermissionInput = { resource: string; action: string; description: string }
export type PermissionAccess = { create: boolean; update: boolean; delete: boolean }
export type PermissionAction = 'delete' | 'activate' | 'deactivate'
export type PermissionQuery = { priorityUuid?: string; page: number; search: string; resource: string; action: string; active: string }
