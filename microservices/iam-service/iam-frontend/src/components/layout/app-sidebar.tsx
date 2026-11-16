import { useState } from 'react'
import { UserRound, Settings, ClipboardList, AppWindow, KeyRound, LayoutDashboard, Shield, Users, Menu } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useAccountLocale } from '@/lib/account-locale'
import { allowedPages, pageText, shellMessages } from './navigation'
import type { DashboardPage } from './navigation'
export type { DashboardPage } from './navigation'

const icons = { 'My Profile': UserRound, 'Security & Password': KeyRound, 'Linked Accounts': UserRound, 'Active Sessions': Shield, 'Login History': ClipboardList, 'Account Activity': ClipboardList, 'Account Deactivation': Shield, Overview: LayoutDashboard, Users, Roles: Shield, Permissions: KeyRound, Clients: AppWindow, 'Audit Logs': ClipboardList, 'System Configuration': Settings }
export function AppSidebar({ activePage, onNavigate, authorities }: { activePage: DashboardPage; onNavigate: (page: DashboardPage) => void; authorities: readonly string[] }) {
  const { locale } = useAccountLocale()
  const t = shellMessages[locale]
  const [open, setOpen] = useState(false)
  const pages = allowedPages(authorities)
  return <aside className="border-b bg-muted/30 p-4 md:sticky md:top-0 md:h-dvh md:w-64 md:shrink-0 md:overflow-y-auto md:border-r md:border-b-0 md:p-5">
    <div className="flex items-center justify-between gap-3 md:mb-6"><div className="flex items-center gap-3"><div className="rounded-lg bg-primary p-2 text-primary-foreground"><Shield aria-hidden="true" className="size-5" /></div><div><p className="font-semibold">ISTAD IAM</p><p className="text-xs text-muted-foreground">{t.identity}</p></div></div><Button type="button" variant="outline" className="md:hidden" aria-expanded={open} aria-controls="dashboard-navigation" onClick={() => setOpen((value) => !value)}><Menu aria-hidden="true" className="size-4" /><span className="sr-only">{t.navigation}</span></Button></div>
    <nav id="dashboard-navigation" aria-label={t.navigation} className={`${open ? 'block' : 'hidden'} mt-4 space-y-5 md:mt-0 md:block`}>
      {(['account', 'management'] as const).map((section) => {
        const items = pages.filter((item) => section === 'account' ? item.authority === null : item.authority !== null)
        return items.length > 0 && <div key={section} className="space-y-1"><p className="px-3 py-2 text-xs font-medium text-muted-foreground">{t[section]}</p>{items.map(({ page }) => {
          const Icon = icons[page]
          return <Button key={page} variant={activePage === page ? 'secondary' : 'ghost'} className="h-auto min-h-10 w-full justify-start gap-3 whitespace-normal text-left leading-relaxed" aria-current={activePage === page ? 'page' : undefined} onClick={() => { onNavigate(page); setOpen(false) }}><Icon aria-hidden="true" className="size-4 shrink-0" />{pageText(page, locale).title}</Button>
        })}</div>
      })}
    </nav>
  </aside>
}
