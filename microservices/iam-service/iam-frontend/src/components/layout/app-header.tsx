import { SidebarTrigger } from '@/components/ui/sidebar'
import { ThemeToggle } from '@/components/shared/theme-toggle'
import { useAccountLocale } from '@/lib/account-locale'
import { pageText, shellMessages } from './navigation'
import type { DashboardPage } from './navigation'
import { LanguageSwitcher } from '@/components/shared/language-switcher'

export function AppHeader({ title }: { title: DashboardPage }) {
  const { locale, setLocale } = useAccountLocale()
  const t = shellMessages[locale]
  return <header className="flex min-h-16 flex-wrap items-center justify-between gap-3 border-b border-border bg-card px-4 py-3 sm:px-6">
    <div className="flex min-w-0 items-center gap-3"><SidebarTrigger /><p className="min-w-0 text-sm leading-relaxed"><span className="hidden text-muted-foreground sm:inline">{t.workspace} / </span><span className="font-semibold text-primary">{pageText(title, locale).title}</span></p></div>
    <div className="flex items-center gap-2"><ThemeToggle /><LanguageSwitcher value={locale} onChange={setLocale} label={t.language} /></div>
  </header>
}
