import { useAccountLocale } from '@/lib/account-locale'
import { accountMessages } from '@/features/account/i18n'
import { securityMessages } from '@/features/account/security/i18n'
import { accountsMessages } from '@/features/account/accounts/i18n'
import { sessionsMessages } from '@/features/account/sessions/i18n'
import { historyMessages } from '@/features/account/login-history/i18n'
import { activityMessages } from '@/features/account/activity/i18n'
import { deactivationMessages } from '@/features/account/deactivation/i18n'
import { lazy, Suspense, useEffect, useState } from 'react'
import { allowedPages, dashboardNavigation, initialPage, pageText, shellMessages } from '@/components/layout/navigation'
import { getDashboardSession } from '@/lib/api-client'
import type { DashboardSession } from '@/lib/api-client'

import { DashboardLayout } from '@/components/layout/dashboard-layout'
import type { DashboardPage } from '@/components/layout/app-sidebar'
import { OverviewPage } from '@/features/overview/pages/overview-page'

import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {UsersPage} from "@/features/users/components/users-page.tsx";
import { RolesPage } from '@/features/roles/pages/roles-page'
import { PermissionsPage } from '@/features/permissions/pages/permissions-page'

const ClientsPage = lazy(() => import('@/features/clients/pages/clients-page').then((module) => ({ default: module.ClientsPage })))

const AuditLogsPage = lazy(() => import('@/features/audit/pages/audit-logs-page').then((module) => ({ default: module.AuditLogsPage })))


const ConfigurationPage = lazy(() => import('@/features/configuration/pages/configuration-page').then((module) => ({ default: module.ConfigurationPage })))

const ProfilePage = lazy(() => import('@/features/account/profile/pages/profile-page').then((module) => ({ default: module.ProfilePage })))
const SecurityPage = lazy(() => import('@/features/account/security/pages/security-page').then((module) => ({ default: module.SecurityPage })))
const AccountsPage = lazy(() => import('@/features/account/accounts/pages/accounts-page').then((module) => ({ default: module.AccountsPage })))

const SessionsPage = lazy(() => import('@/features/account/sessions/pages/sessions-page').then((module) => ({ default: module.SessionsPage })))
const AccountLoginHistoryPage = lazy(() => import('@/features/account/login-history/pages/login-history-page').then((module) => ({ default: module.AccountLoginHistoryPage })))

const ActivityPage = lazy(() => import('@/features/account/activity/pages/activity-page').then((module) => ({ default: module.ActivityPage })))

const DeactivationPage = lazy(() => import('@/features/account/deactivation/pages/deactivation-page').then((module) => ({ default: module.DeactivationPage })))

export default function App() {
  const { locale } = useAccountLocale()
  const t = shellMessages[locale]
  const [result, setResult] = useState<{ session?: DashboardSession; error?: boolean } | null>(null)
  useEffect(() => {
    const controller = new AbortController()
    getDashboardSession(controller.signal).then((session) => { if (!controller.signal.aborted) setResult({ session }) })
      .catch(() => { if (!controller.signal.aborted) setResult({ error: true }) })
    return () => controller.abort()
  }, [])
  if (!result) return <main className="p-6" role="status">{t.loading}</main>
  if (!result.session) return <main className="space-y-4 p-6"><p role="alert">{t.failed}</p><a className="underline" href="/dashboard/index.html">{t.signIn}</a></main>
  return <DashboardApp session={result.session} />
}

function DashboardApp({ session }: { session: DashboardSession }) {
  const [page, setPage] = useState<DashboardPage>(() => initialPage(session.authorities, window.location.search))
  useEffect(() => {
    const onBack = () => setPage(initialPage(session.authorities, window.location.search))
    window.addEventListener('popstate', onBack)
    return () => window.removeEventListener('popstate', onBack)
  }, [session.authorities])
  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    params.set('view', dashboardNavigation.find((item) => item.page === page)!.view)
    window.history.replaceState(null, '', `${window.location.pathname}?${params}`)
  }, [page])
  function navigate(next: DashboardPage) {
    const target = allowedPages(session.authorities).find((item) => item.page === next)
    if (!target || next === page) return
    const params = new URLSearchParams(window.location.search); params.set('view', target.view)
    window.history.pushState(null, '', `${window.location.pathname}?${params}`)
    setPage(next)
  }
  const { locale } = useAccountLocale()
  const profileText = accountMessages[locale]

  return (
      <DashboardLayout
          activePage={page}
          onNavigate={navigate}
          authorities={session.authorities}
      >
        <div>
          <h1 className="text-3xl font-semibold tracking-tight">
            {pageText(page, locale).title}
          </h1>

          <p className="mt-2 text-muted-foreground">
            {pageText(page, locale).description}
          </p>
        </div>

        {page === 'Account Deactivation' ? (
            <Suspense fallback={<p role="status">{deactivationMessages[locale].loading}</p>}><DeactivationPage /></Suspense>
        ) : page === 'Account Activity' ? (
            <Suspense fallback={<p role="status">{activityMessages[locale].loading}</p>}><ActivityPage /></Suspense>
        ) : page === 'Login History' ? (
            <Suspense fallback={<p role="status">{historyMessages[locale].loading}</p>}><AccountLoginHistoryPage /></Suspense>
        ) : page === 'Active Sessions' ? (
            <Suspense fallback={<p role="status">{sessionsMessages[locale].loading}</p>}><SessionsPage /></Suspense>
        ) : page === 'Linked Accounts' ? (
            <Suspense fallback={<p role="status">{accountsMessages[locale].loading}</p>}><AccountsPage /></Suspense>
        ) : page === 'Security & Password' ? (
            <Suspense fallback={<p role="status">{securityMessages[locale].loading}</p>}><SecurityPage /></Suspense>
        ) : page === 'My Profile' ? (
            <Suspense fallback={<p role="status">{profileText.loading}</p>}><ProfilePage /></Suspense>
        ) : page === 'Overview' ? (
            <OverviewPage onNavigate={navigate} />
        )  : page === 'Users' ? (
                    <UsersPage />
        ) : page === 'Roles' ? (
                    <RolesPage />
        ) : page === 'System Configuration' ? (
                    <Suspense fallback={<p role="status">Loading system configuration...</p>}><ConfigurationPage /></Suspense>
        ) : page === 'Audit Logs' ? (
                    <Suspense fallback={<p role="status">Loading audit logs...</p>}><AuditLogsPage /></Suspense>
        ) : page === 'Clients' ? (
                    <Suspense fallback={<p role="status">Loading clients...</p>}><ClientsPage /></Suspense>
        ) : page === 'Permissions' ? (
                    <PermissionsPage />
        ) : (
            <Card>
              <CardHeader>
                <CardTitle>{page} management</CardTitle>
                <CardDescription>
                  This page is ready for its management interface.
                </CardDescription>
              </CardHeader>
            </Card>
        )}
      </DashboardLayout>
  )
}
