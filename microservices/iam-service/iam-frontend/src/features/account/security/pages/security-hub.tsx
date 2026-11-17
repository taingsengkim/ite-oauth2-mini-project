import { lazy, Suspense } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useAccountLocale } from '@/lib/account-locale'
import { buttonVariants } from '@/components/ui/button'
const Password = lazy(() => import('./security-page').then(module => ({ default: module.SecurityPage })))
const Sessions = lazy(() => import('../../sessions/pages/sessions-page').then(module => ({ default: module.SessionsPage })))
const Accounts = lazy(() => import('../../accounts/pages/accounts-page').then(module => ({ default: module.AccountsPage })))
const History = lazy(() => import('../../login-history/pages/login-history-page').then(module => ({ default: module.AccountLoginHistoryPage })))
const Activity = lazy(() => import('../../activity/pages/activity-page').then(module => ({ default: module.ActivityPage })))
const tabs = { password: Password, sessions: Sessions, accounts: Accounts, history: History, activity: Activity }
const labels = {
  en: { title: 'Security', password: 'Password', sessions: 'Devices & sessions', accounts: 'Linked accounts', history: 'Login history', activity: 'Account activity', loading: 'Loading…' },
  km: { title: 'សុវត្ថិភាព', password: 'ពាក្យសម្ងាត់', sessions: 'ឧបករណ៍ និងវគ្គសកម្ម', accounts: 'គណនីដែលបានភ្ជាប់', history: 'ប្រវត្តិចូលគណនី', activity: 'សកម្មភាពគណនី', loading: 'កំពុងផ្ទុក…' },
}
export function SecurityHub() {
  const { locale } = useAccountLocale()
  const t = labels[locale]
  const [params] = useSearchParams()
  const value = params.get('tab') || 'password'
  const selected = Object.hasOwn(tabs, value) ? value as keyof typeof tabs : 'password'
  const Page = tabs[selected]
  return <section className="space-y-6" aria-label={t.title}>
    <nav aria-label={t.title} className="flex flex-wrap gap-2 rounded-xl border bg-muted/30 p-2">{(Object.keys(tabs) as (keyof typeof tabs)[]).map(tab => <Link key={tab} to={'/account/security?tab=' + tab} aria-current={tab === selected ? 'page' : undefined} className={buttonVariants({ variant: tab === selected ? 'default' : 'ghost', size: 'lg' })}>{t[tab]}</Link>)}</nav>
    <div><h2 className="mb-4 text-xl font-semibold">{t[selected]}</h2><Suspense fallback={<p role="status">{t.loading}</p>}><Page key={selected} /></Suspense></div>
  </section>
}
