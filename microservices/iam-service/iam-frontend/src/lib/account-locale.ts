import { createContext, useContext } from 'react'
import type { AccountLocale } from '@/features/account/i18n'

export const AccountLocaleContext = createContext<{ locale: AccountLocale; setLocale: (locale: AccountLocale) => void } | null>(null)
export function useAccountLocale() {
  const context = useContext(AccountLocaleContext)
  if (!context) throw new Error('AccountLocaleProvider is required')
  return context
}
