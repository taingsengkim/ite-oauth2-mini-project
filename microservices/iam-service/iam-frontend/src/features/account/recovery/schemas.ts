import { z } from 'zod'
import { securitySchema } from '../security/schemas'
import type { PasswordRequirements } from '@/lib/password-policy'
import { recoveryMessages } from './i18n'
export function recoveryEmailSchema(locale: 'en' | 'km') {
  return z.object({ email: z.string().trim().max(255).pipe(z.email(recoveryMessages[locale].emailRule)) })
}
export function recoveryResetSchema(policy: PasswordRequirements, locale: 'en' | 'km') {
  const t = recoveryMessages[locale]
  return z.object({ newPassword: securitySchema(policy, locale).shape.newPassword, confirmPassword: z.string().min(1, t.required) })
    .refine((value) => value.newPassword === value.confirmPassword, { path: ['confirmPassword'], message: t.mismatch })
}
