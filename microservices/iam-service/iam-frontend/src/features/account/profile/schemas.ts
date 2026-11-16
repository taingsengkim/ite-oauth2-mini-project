import { z } from 'zod'
import { accountMessages } from '../i18n'
import type { AccountLocale } from '../i18n'

export function validPictureUrl(value: string) {
  if (!value) return true
  try { const uri = new URL(value); return /^https:\/\//i.test(value) && !/[\s#]/.test(value) && !/%(?![\da-f]{2})/i.test(value) && Boolean(uri.hostname) && !uri.username && !uri.password && !uri.hostname.includes('_') && !value.match(/^https:\/\/[^/]*@/i) } catch { return false }
}
export function profileSchema(locale: AccountLocale, currentUsername: string) {
  const t = accountMessages[locale]
  const required = (max: number) => z.string().trim().min(1, t.required).max(max, t.tooLong)
  return z.object({
    username: required(50).refine((value) => value === currentUsername || /^[A-Za-z0-9._-]{3,50}$/.test(value), t.usernameRule),
    email: z.string().trim().max(255, t.tooLong).pipe(z.email(t.emailRule)), givenName: required(100), familyName: required(100),
    nickname: z.string().trim().max(100, t.tooLong), phoneNumber: z.string().trim().refine((value) => !value || /^\+[1-9][0-9]{1,14}$/.test(value), t.phoneRule),
    pictureUrl: z.string().trim().max(500, t.tooLong).refine(validPictureUrl, t.pictureRule), locale: z.enum(['en', 'km'], { error: t.languageRule }),
  })
}
export type ProfileInput = z.infer<ReturnType<typeof profileSchema>>
export type AccountProfile = Omit<ProfileInput, 'nickname' | 'phoneNumber' | 'pictureUrl'> & {
  uuid: string; nickname: string | null; phoneNumber: string | null; pictureUrl: string | null
  emailVerified: boolean; phoneNumberVerified: boolean; createdAt: string; updatedAt: string
}
export type ProfileSaved = { profile: AccountProfile; signInRequired: boolean }
