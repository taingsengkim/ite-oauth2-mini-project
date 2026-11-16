export const userActionRoutes = [
  { action: 'roles', label: 'Manage roles', authority: 'user:assign' },
  { action: 'permissions', label: 'Manage permissions', authority: 'user:assign' },
  { action: 'sessions', label: 'Active sessions', authority: 'user:read' },
  { action: 'login-history', label: 'Login history', authority: 'user:read' },
  { action: 'reset-password', label: 'Reset password', authority: 'user:update' },
  { action: 'enable', label: 'Activate account', authority: 'user:update' },
  { action: 'disable', label: 'Deactivate account', authority: 'user:update' },
  { action: 'lock', label: 'Lock account', authority: 'user:update' },
  { action: 'unlock', label: 'Unlock account', authority: 'user:update' },
  { action: 'delete', label: 'Delete user', authority: 'user:delete' },
] as const
export type UserAction = typeof userActionRoutes[number]['action'] | 'new' | 'all-history'
