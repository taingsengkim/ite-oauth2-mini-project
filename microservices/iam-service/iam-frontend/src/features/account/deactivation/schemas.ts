import { z } from 'zod'
import { deactivationMessages } from './i18n'
export function deactivationSchema(username: string, hasPassword: boolean, locale: 'en' | 'km') {
  const t = deactivationMessages[locale]
  return z.object({ confirmation: z.string().refine((value) => value === username, t.match),
    currentPassword: z.string().refine((value) => !hasPassword || Boolean(value.trim()), t.required),
    acknowledged: z.boolean().refine((value) => value, t.acknowledgeError) })
}
