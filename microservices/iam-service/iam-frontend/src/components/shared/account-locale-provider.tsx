import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import type { AccountLocale } from '@/features/account/i18n'
import { AccountLocaleContext } from '@/lib/account-locale'

export function AccountLocaleProvider({ children }: { children: ReactNode }) {
  const [locale, setLocale] = useState<AccountLocale>(() => {
    const requested = new URLSearchParams(window.location.search).get('lang')
    if (requested === 'en' || requested === 'km') return requested
    try { return localStorage.getItem('iam.account.locale') === 'km' ? 'km' : 'en' } catch { return 'en' }
  })
  useEffect(() => {
    document.documentElement.lang = locale
    try { localStorage.setItem('iam.account.locale', locale) } catch { /* Storage may be disabled. */ }
  }, [locale])
  return <AccountLocaleContext.Provider value={{ locale, setLocale }}>{children}</AccountLocaleContext.Provider>
}
