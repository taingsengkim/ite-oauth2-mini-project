import { useAccountLocale } from '@/lib/account-locale'
import { pageText, shellMessages } from './navigation'
import type { DashboardPage } from './navigation'

export function AppHeader({ title }: { title: DashboardPage }) {
  const { locale, setLocale } = useAccountLocale()
  const t = shellMessages[locale]
  return <header className="flex min-h-16 flex-wrap items-center justify-between gap-3 border-b px-4 py-3 sm:px-6">
    <p className="min-w-0 text-sm leading-relaxed"><span className="hidden text-muted-foreground sm:inline">{t.workspace} / </span><span className="font-medium">{pageText(title, locale).title}</span></p>
    <label className="shrink-0 text-sm"><span className="sr-only">{t.language}</span><select aria-label={t.language} value={locale} onChange={(event) => setLocale(event.target.value === 'km' ? 'km' : 'en')} className="h-10 rounded-md border bg-background px-3"><option value="en">English</option><option value="km">ខ្មែរ</option></select></label>
  </header>
}
