import { z } from 'zod'
const flag = z.enum(['', 'true', 'false'])
export const userFilterSchema = z.object({ search: z.string().trim().max(255), enabled: flag, locked: flag, emailVerified: flag, sort: z.enum(['username,asc', 'username,desc', 'createdAt,desc', 'createdAt,asc']), size: z.enum(['10', '20', '50', '100']) })
export type UserFilters = z.infer<typeof userFilterSchema>
export const defaultUserFilters: UserFilters = { search: '', enabled: '', locked: '', emailVerified: '', sort: 'username,asc', size: '20' }
