import { z } from 'zod'

export const requiredText = (label: string, max: number) => z.string().trim().min(1, `${label} is required.`).max(max, `${label} must not exceed ${max} characters.`)
export const passwordSchema = z.string().refine((value) => Boolean(value.trim()), 'Password is required.').refine((value) => new TextEncoder().encode(value).length <= 72, 'Password must not exceed 72 UTF-8 bytes.')
const profile = {
  givenName: requiredText('Given name', 100), familyName: requiredText('Family name', 100),
  locale: z.enum(['en', 'km'], { error: 'Choose English or Khmer.' }),
  phoneNumber: z.string().trim().refine((value) => !value || /^\+[1-9][0-9]{1,14}$/.test(value), 'Use international format, such as +85512345678.'),
}
export const createUserSchema = z.object({ ...profile, username: requiredText('Username', 50), email: z.string().trim().max(255).pipe(z.email('Enter a valid email address.')), password: passwordSchema, confirmation: z.string().min(1, 'Confirm your password.') })
  .refine((value) => value.password === value.confirmation, { message: 'Passwords do not match.', path: ['confirmation'] })
export const editUserSchema = z.object({ ...profile, nickname: z.string().trim().max(100) })
export const resetPasswordSchema = z.object({ newPassword: passwordSchema, confirmPassword: z.string().min(1, 'Confirm your password.') })
  .refine((value) => value.newPassword === value.confirmPassword, { message: 'Passwords do not match.', path: ['confirmPassword'] })
export const roleSchema = z.object({ name: requiredText('Role name', 50), description: z.string().trim().max(255) })
export const permissionSchema = z.object({ resource: requiredText('Resource', 50).refine((value) => !value.includes(':'), 'Resource must not contain a colon.'), action: requiredText('Action', 50), description: z.string().trim().max(255) })
  .refine((value) => `${value.resource}:${value.action}`.length <= 100, { message: 'Combined permission name must not exceed 100 characters.', path: ['action'] })
export const confirmationSchema = (expected: string) => z.object({ confirmation: z.string().refine((value) => value === expected, `Type ${expected} exactly to confirm.`) })
export const searchSchema = z.object({ search: z.string().trim().max(255, 'Search must not exceed 255 characters.') })
export const permissionSearchSchema = searchSchema.extend({ resource: z.string().trim().max(50), action: z.string().trim().max(50) })
export const futureExpirySchema = z.string().refine((value) => !value || (Number.isFinite(new Date(value).getTime()) && new Date(value).getTime() > Date.now()), 'Choose a future expiration time, or leave it empty for permanent access.')
export const assignmentSchema = z.object({ selected: z.array(z.string().uuid()).min(1, 'Select at least one item.').max(100), expiresAt: futureExpirySchema, reason: z.string().trim().max(255) })
export const permissionAssignmentSchema = assignmentSchema.extend({ effect: z.enum(['GRANT', 'DENY']) })
export const expirySchema = z.object({ expiresAt: futureExpirySchema })
export const selectionSchema = z.object({ selected: z.array(z.string().uuid()).min(1, 'Select at least one permission.').max(100) })
const optionalDate = z.string().refine((value) => !value || Number.isFinite(new Date(value).getTime()), 'Choose a valid date and time.')
export const loginHistorySchema = z.object({ login: z.string().trim().max(255), outcome: z.enum(['', 'SUCCESS', 'FAILURE']), from: optionalDate, to: optionalDate, suspiciousOnly: z.boolean() })
  .refine((value) => !value.from || !value.to || new Date(value.from) <= new Date(value.to), { message: 'To must be after From.', path: ['to'] })
export const toExpiry = (value: string) => value ? new Date(value).toISOString() : null
