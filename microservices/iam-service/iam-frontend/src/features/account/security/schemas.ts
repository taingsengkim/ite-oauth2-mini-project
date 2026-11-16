import { z } from 'zod'
import type { PasswordRequirements } from '@/lib/password-policy'
import { securityMessages } from './i18n'

export function securitySchema(policy: PasswordRequirements, locale: 'en' | 'km') {
  const t = securityMessages[locale]
  return z.object({
    currentPassword: z.string().min(1, t.required),
    newPassword: z.string().refine((value) => Boolean(value.trim()), t.required).superRefine((value, context) => {
      const issue = (message: string) => context.addIssue({ code: 'custom', message })
      if (new TextEncoder().encode(value).length > 72) issue(t.bytes)
      const length = Array.from(value).length
      if (length < policy.minLength || length > policy.maxLength) issue(t.length.replace('{min}', String(policy.minLength)).replace('{max}', String(policy.maxLength)))
      if (policy.requireUppercase && !/\p{Uppercase}/u.test(value)) issue(t.uppercase)
      if (policy.requireLowercase && !/\p{Lowercase}/u.test(value)) issue(t.lowercase)
      if (policy.requireDigit && !/\p{Nd}/u.test(value)) issue(t.digit)
      if (policy.requireSpecial && !/[\p{P}\p{S}]/u.test(value)) issue(t.special)
    }),
    confirmPassword: z.string().min(1, t.required),
  }).refine((value) => value.newPassword === value.confirmPassword, { path: ['confirmPassword'], message: t.mismatch })
    .refine((value) => value.newPassword !== value.currentPassword, { path: ['newPassword'], message: t.different })
}
export function securityRequirements(policy: PasswordRequirements, locale: 'en' | 'km') {
  const t = securityMessages[locale]
  return [t.length.replace('{min}', String(policy.minLength)).replace('{max}', String(policy.maxLength)), t.bytes,
    policy.requireUppercase && t.uppercase, policy.requireLowercase && t.lowercase, policy.requireDigit && t.digit, policy.requireSpecial && t.special].filter(Boolean).join(' ')
}
