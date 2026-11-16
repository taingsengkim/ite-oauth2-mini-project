import { UserRound, Settings, ClipboardList, AppWindow, KeyRound, LayoutDashboard, Shield, Users, GraduationCap } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { useAccountLocale } from '@/lib/account-locale'
import { allowedPages, pageText, shellMessages } from './navigation'
import { SignOutButton } from '@/components/shared/sign-out-button'
import { Sidebar, SidebarHeader, SidebarContent, SidebarFooter, SidebarGroup, SidebarGroupLabel, SidebarGroupContent, SidebarMenu, SidebarMenuItem, SidebarMenuButton, useSidebar } from '@/components/ui/sidebar'
export type { DashboardPage } from './navigation'
const icons = { 'About the Developer': GraduationCap, 'My Profile': UserRound, 'Security & Password': KeyRound, 'Linked Accounts': UserRound, 'Active Sessions': Shield, 'Login History': ClipboardList, 'Account Activity': ClipboardList, 'Account Deactivation': Shield, Overview: LayoutDashboard, Users, Roles: Shield, Permissions: KeyRound, Clients: AppWindow, 'Audit Logs': ClipboardList, 'System Configuration': Settings }

export function AppSidebar({ authorities }: { authorities: readonly string[] }) {
  const { locale } = useAccountLocale()
  const t = shellMessages[locale]
  const { setOpenMobile } = useSidebar()
  const { pathname } = useLocation()
  const pages = allowedPages(authorities)
  return <Sidebar collapsible="offcanvas" className="border-sidebar-border">
    <SidebarHeader className="p-5"><Link to="/account/profile" className="flex items-center gap-3" onClick={() => setOpenMobile(false)}><img src="/assets/branding/istad-seal.png" alt="ISTAD seal" className="size-12 shrink-0 rounded-full border border-border bg-white object-contain p-0.5" width="48" height="48" /><div><p className="font-semibold text-sidebar-foreground">ISTAD IAM</p><p className="text-xs text-muted-foreground">{t.identity}</p></div></Link></SidebarHeader>
    <SidebarContent className="px-3"><nav aria-label={t.navigation}>
      {(['account', 'management'] as const).map(section => {
        const items = pages.filter(item => item.page !== 'About the Developer' && (section === 'account' ? item.authority === null : item.authority !== null))
        return items.length > 0 && <SidebarGroup key={section}><SidebarGroupLabel className="text-muted-foreground">{t[section]}</SidebarGroupLabel><SidebarGroupContent><SidebarMenu>{items.map(({ page, path }) => {
          const Icon = icons[page]
          return <SidebarMenuItem key={page}><SidebarMenuButton isActive={pathname === '/' + path || pathname.startsWith('/' + path + '/') } className="min-h-10 py-2 text-xs" render={<Link to={'/' + path} onClick={() => setOpenMobile(false)} />}><Icon aria-hidden="true" /><span>{pageText(page, locale).title}</span></SidebarMenuButton></SidebarMenuItem>
        })}</SidebarMenu></SidebarGroupContent></SidebarGroup>
      })}
    </nav></SidebarContent>
    <SidebarFooter className="gap-3 border-t border-sidebar-border p-4"><SidebarMenu><SidebarMenuItem><SidebarMenuButton isActive={pathname === '/about'} render={<Link to="/about" onClick={() => setOpenMobile(false)} />}><GraduationCap aria-hidden="true" /><span>{pageText('About the Developer', locale).title}</span></SidebarMenuButton></SidebarMenuItem></SidebarMenu><SignOutButton /></SidebarFooter>
  </Sidebar>
}
