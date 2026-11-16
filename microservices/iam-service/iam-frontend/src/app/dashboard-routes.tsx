import { userActionRoutes } from '@/features/users/user-action-routes'
import { lazy, Suspense, useEffect } from 'react'
import type { ComponentType, ReactNode } from 'react'
import { Link, Navigate, Outlet, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { DashboardLayout } from '@/components/layout/dashboard-layout'
import { allowedPages, dashboardNavigation, initialPage, pageForPath, pagePath, pageText, shellMessages } from '@/components/layout/navigation'
import type { DashboardPage } from '@/components/layout/navigation'
import type { DashboardSession } from '@/lib/api-client'
import { useAccountLocale } from '@/lib/account-locale'

const UserActionPage = lazy(() => import('./user-action-page').then(module => ({ default: module.UserActionPage })))
const ManagementResourcePage = lazy(() => import('./management-resource-page').then(module => ({ default: module.ManagementResourcePage })))

const ProfilePage = lazy(() => import('@/features/account/profile/pages/profile-page').then((module) => ({ default: module.ProfilePage })))
const SecurityPage = lazy(() => import('@/features/account/security/pages/security-page').then((module) => ({ default: module.SecurityPage })))
const AccountsPage = lazy(() => import('@/features/account/accounts/pages/accounts-page').then((module) => ({ default: module.AccountsPage })))
const SessionsPage = lazy(() => import('@/features/account/sessions/pages/sessions-page').then((module) => ({ default: module.SessionsPage })))
const LoginHistoryPage = lazy(() => import('@/features/account/login-history/pages/login-history-page').then((module) => ({ default: module.AccountLoginHistoryPage })))
const ActivityPage = lazy(() => import('@/features/account/activity/pages/activity-page').then((module) => ({ default: module.ActivityPage })))
const DeactivationPage = lazy(() => import('@/features/account/deactivation/pages/deactivation-page').then((module) => ({ default: module.DeactivationPage })))
const OverviewPage = lazy(() => import('@/features/overview/pages/overview-page').then((module) => ({ default: module.OverviewPage })))
const UsersPage = lazy(() => import('@/features/users/components/users-page').then((module) => ({ default: module.UsersPage })))
const RolesPage = lazy(() => import('@/features/roles/pages/roles-page').then((module) => ({ default: module.RolesPage })))
const PermissionsPage = lazy(() => import('@/features/permissions/pages/permissions-page').then((module) => ({ default: module.PermissionsPage })))
const ClientsPage = lazy(() => import('@/features/clients/pages/clients-page').then((module) => ({ default: module.ClientsPage })))
const AuditLogsPage = lazy(() => import('@/features/audit/pages/audit-logs-page').then((module) => ({ default: module.AuditLogsPage })))
const ConfigurationPage = lazy(() => import('@/features/configuration/pages/configuration-page').then((module) => ({ default: module.ConfigurationPage })))
const AboutPage = lazy(() => import('@/features/about/pages/about-page').then((module) => ({ default: module.AboutPage })))

function OverviewRoute() {
  const navigate = useNavigate()
  return <OverviewPage onNavigate={(page) => { void navigate(pagePath(page)) }} />
}
const pages: Record<DashboardPage, ComponentType> = {
  'About the Developer': AboutPage,
  'My Profile': ProfilePage, 'Security & Password': SecurityPage, 'Linked Accounts': AccountsPage,
  'Active Sessions': SessionsPage, 'Login History': LoginHistoryPage, 'Account Activity': ActivityPage,
  'Account Deactivation': DeactivationPage, Overview: OverviewRoute, Users: UsersPage, Roles: RolesPage,
  Permissions: PermissionsPage, Clients: ClientsPage, 'Audit Logs': AuditLogsPage, 'System Configuration': ConfigurationPage,
}
function LegacyEntry({ session }: { session: DashboardSession }) {
  const location = useLocation()
  const page = initialPage(session.authorities, location.search)
  const params = new URLSearchParams(location.search); params.delete('view'); params.delete('continue')
  return <Navigate replace to={{ pathname: pagePath(page), search: params.size ? `?${params}` : '' }} />
}
function RequirePage({ page, session, children }: { page: DashboardPage; session: DashboardSession; children: ReactNode }) {
  return allowedPages(session.authorities).some((item) => item.page === page) ? children : <Navigate replace to={pagePath('My Profile')} />
}
function DashboardShell({ session }: { session: DashboardSession }) {
  const { locale } = useAccountLocale()
  const location = useLocation()
  const page = pageForPath(location.pathname) || 'My Profile'
  const text = pageText(page, locale)
  useEffect(() => {
    document.title = `${text.title} · ISTAD IAM`
    document.getElementById('main-content')?.scrollIntoView({ block: 'start' })
  }, [location.pathname, text.title])
  return <DashboardLayout activePage={page} authorities={session.authorities}>
    <div><h1 className="text-3xl font-semibold tracking-tight">{text.title}</h1><p className="mt-2 text-muted-foreground">{text.description}</p></div>
    <Suspense fallback={<p role="status">{shellMessages[locale].loading}</p>}><Outlet /></Suspense>
  </DashboardLayout>
}
function NotFound({ session }: { session: DashboardSession }) {
  const { locale } = useAccountLocale()
  const t = shellMessages[locale]
  return <main className="space-y-4 p-6"><h1 className="text-2xl font-semibold">{t.notFound}</h1><Link className="underline" to={pagePath(initialPage(session.authorities, ''))}>{t.returnHome}</Link></main>
}
export function DashboardRoutes({ session }: { session: DashboardSession }) {
  return <Routes>
    <Route index element={<LegacyEntry session={session} />} />
    <Route path="index.html" element={<LegacyEntry session={session} />} />
    <Route element={<DashboardShell session={session} />}>
      <Route path="users/new" element={<RequirePage page="Users" session={session}><UserActionPage action="new" session={session} /></RequirePage>} />
      <Route path="users/login-history" element={<RequirePage page="Users" session={session}><UserActionPage action="all-history" session={session} /></RequirePage>} />
      {userActionRoutes.map(({ action }) => <Route key={action} path={'users/:id/' + action} element={<RequirePage page="Users" session={session}><UserActionPage action={action} session={session} /></RequirePage>} />)}
      {(['users', 'roles', 'clients'] as const).flatMap(resource => {
        const page = resource === 'users' ? 'Users' : resource === 'roles' ? 'Roles' : 'Clients'
        return [false, true].map(edit => <Route key={resource + String(edit)} path={resource + '/:id' + (edit ? '/edit' : '')} element={<RequirePage page={page} session={session}><ManagementResourcePage key={resource + String(edit)} resource={resource} edit={edit} session={session} /></RequirePage>} />)
      })}
      {dashboardNavigation.map(({ page, path }) => {
        const Page = pages[page]
        return <Route key={path} path={path} element={<RequirePage page={page} session={session}><Page /></RequirePage>} />
      })}
    </Route>
    <Route path="*" element={<NotFound session={session} />} />
  </Routes>
}
