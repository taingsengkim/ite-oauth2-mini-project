import { z } from 'zod'

export const securityPolicySchema = z.object({
  maxFailedLoginAttempts: z.number().int().min(3).max(20), lockoutDurationSeconds: z.number().int().min(60).max(86400),
  passwordMinLength: z.number().int().min(8).max(64), passwordMaxLength: z.number().int().min(8).max(72),
  passwordRequireUppercase: z.boolean(), passwordRequireLowercase: z.boolean(), passwordRequireDigit: z.boolean(), passwordRequireSpecial: z.boolean(),
  sessionIdleTimeoutSeconds: z.number().int().min(60).max(86400), defaultAccessTokenTtlSeconds: z.number().int().min(60).max(86400),
  defaultRefreshTokenTtlSeconds: z.number().int().min(300).max(2592000),
}).superRefine((value, context) => {
  if (value.passwordMaxLength < value.passwordMinLength) context.addIssue({ code: 'custom', path: ['passwordMaxLength'], message: 'Maximum length must be at least the minimum.' })
  if (value.defaultRefreshTokenTtlSeconds < value.defaultAccessTokenTtlSeconds) context.addIssue({ code: 'custom', path: ['defaultRefreshTokenTtlSeconds'], message: 'Refresh tokens must last at least as long as access tokens.' })
})
export type SecurityPolicy = z.infer<typeof securityPolicySchema>
export type Configuration = { id: string; version: number; policy: SecurityPolicy; updatedAt: string; updatedBy: string }
export type ClientDefaults = { accessTokenTtlSeconds: number; refreshTokenTtlSeconds: number }
