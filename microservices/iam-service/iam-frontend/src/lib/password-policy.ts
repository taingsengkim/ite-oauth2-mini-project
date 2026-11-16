import { requestJson } from './api-client'
import { passwordSchema } from './form-schemas'

export type PasswordRequirements = { minLength: number; maxLength: number; requireUppercase: boolean; requireLowercase: boolean; requireDigit: boolean; requireSpecial: boolean }
export function getPasswordRequirements(signal: AbortSignal) { return requestJson<PasswordRequirements>('/dashboard-api/password-policy', { signal }) }
export function passwordWithRequirements(policy: PasswordRequirements) {
  return passwordSchema.superRefine((value, context) => {
    const length = Array.from(value).length
    const issue = (message: string) => context.addIssue({ code: 'custom', message })
    if (length < policy.minLength || length > policy.maxLength) issue(`Use ${policy.minLength}–${policy.maxLength} characters.`)
    if (policy.requireUppercase && !/\p{Uppercase}/u.test(value)) issue('Include an uppercase letter.')
    if (policy.requireLowercase && !/\p{Lowercase}/u.test(value)) issue('Include a lowercase letter.')
    if (policy.requireDigit && !/\p{Nd}/u.test(value)) issue('Include a digit.')
    if (policy.requireSpecial && !/[\p{P}\p{S}]/u.test(value)) issue('Include a special character.')
  })
}
export function passwordRequirementsText(policy: PasswordRequirements) {
  const required = [policy.requireUppercase && 'uppercase', policy.requireLowercase && 'lowercase', policy.requireDigit && 'digit', policy.requireSpecial && 'special character'].filter(Boolean)
  return `${policy.minLength}–${policy.maxLength} characters, at most 72 UTF-8 bytes.${required.length ? ` Include: ${required.join(', ')}.` : ''}`
}
