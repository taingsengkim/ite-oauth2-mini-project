export type RoleSummary = {
  uuid: string
  name: string
  description: string | null
  system: boolean
  active: boolean
}

export type Role = RoleSummary & { createdAt: string; updatedAt: string }
export type RoleInput = { name: string; description: string }
export type Page<T> = {
  content: T[]
  page: { number: number; size: number; totalElements: number; totalPages: number }
}

export type Permission = {
  uuid: string
  name: string
  resource: string
  action: string
  description: string | null
  active: boolean
}

export type AssignedPermission = Omit<Permission, 'description'> & {
  assignedAt: string
  assignedBy: string
  expiresAt: string | null
}

export type RoleAccess = { create: boolean; update: boolean; delete: boolean; assign: boolean; browsePermissions: boolean }
