import { z } from 'zod'
import { accountMessages } from '../i18n'
import type { AccountLocale } from '../i18n'
import { profileDetails } from './details-i18n'

export function validPictureUrl(value: string) {
  if (!value) return true
  try { const uri = new URL(value); return /^https:\/\//i.test(value) && !/[\s#]/.test(value) && !/%(?![\da-f]{2})/i.test(value) && Boolean(uri.hostname) && !uri.username && !uri.password && !uri.hostname.includes('_') && !value.match(/^https:\/\/[^/]*@/i) } catch { return false }
}
function validWebUrl(value: string) {
  if (!value) return true
  try { const uri = new URL(value); return ['http:', 'https:'].includes(uri.protocol) && Boolean(uri.hostname) && !uri.username && !uri.password } catch { return false }
}
export function profileSchema(locale: AccountLocale, currentUsername: string) {
  const t = accountMessages[locale]
  const d = profileDetails[locale]
  const optional = (max: number) => z.string().trim().max(max, t.tooLong)
  const required = (max: number) => z.string().trim().min(1, t.required).max(max, t.tooLong)
  return z.object({
    username: required(50).refine((value) => value === currentUsername || /^[A-Za-z0-9._-]{3,50}$/.test(value), t.usernameRule),
    email: z.string().trim().max(255, t.tooLong).pipe(z.email(t.emailRule)), givenName: required(100), familyName: required(100),
    nickname: z.string().trim().max(100, t.tooLong), phoneNumber: z.string().trim().refine((value) => !value || /^\+[1-9][0-9]{1,14}$/.test(value), t.phoneRule),
    pictureUrl: z.string().trim().max(500, t.tooLong).refine(validPictureUrl, t.pictureRule), locale: z.enum(['en', 'km'], { error: t.languageRule }),
    middleName: optional(100), gender: z.enum(['', 'male', 'female', 'other']),
    birthdate: z.string().refine(value => !value || (/^\d{4}-\d{2}-\d{2}$/.test(value) && Number.isFinite(Date.parse(value)) && new Date(value).toISOString().slice(0,10) === value && value <= new Date().toISOString().slice(0,10)), d.invalidDate),
    zoneinfo: optional(50).refine(value => { if (!value) return true; try { new Intl.DateTimeFormat('en', { timeZone: value }); return true } catch { return false } }, d.invalidTimezone),
    profileUrl: optional(500).refine(validWebUrl, locale === 'km' ? 'សូមបញ្ចូលតំណ HTTP ឬ HTTPS ត្រឹមត្រូវ។' : 'Use an absolute HTTP or HTTPS URL without credentials.'), websiteUrl: optional(500).refine(validWebUrl, locale === 'km' ? 'សូមបញ្ចូលតំណ HTTP ឬ HTTPS ត្រឹមត្រូវ។' : 'Use an absolute HTTP or HTTPS URL without credentials.'),
    address: z.object({ streetAddress: optional(255), locality: optional(100), region: optional(100), postalCode: optional(20), country: optional(2).transform(value => value.toUpperCase()).refine(value => !value || /^[A-Z]{2}$/.test(value), d.invalidCountry) }),
  })
}
export type ProfileInput = z.infer<ReturnType<typeof profileSchema>>
export type AccountProfile = Omit<ProfileInput, 'nickname' | 'phoneNumber' | 'pictureUrl' | 'middleName' | 'gender' | 'birthdate' | 'zoneinfo' | 'profileUrl' | 'websiteUrl' | 'address'> & {
  middleName: string | null; gender: '' | 'male' | 'female' | 'other' | null; birthdate: string | null; zoneinfo: string | null; profileUrl: string | null; websiteUrl: string | null
  address: { streetAddress: string | null; locality: string | null; region: string | null; postalCode: string | null; country: string | null } | null
  lastLoginAt: string | null
  uuid: string; nickname: string | null; phoneNumber: string | null; pictureUrl: string | null
  emailVerified: boolean; phoneNumberVerified: boolean; createdAt: string; updatedAt: string
}
export type ProfileSaved = { profile: AccountProfile; signInRequired: boolean }
