import { lazy, Suspense, useState } from 'react'

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

export default function App() {
  const [page, setPage] = useState<DashboardPage>('Overview')

  return (
      <DashboardLayout
          activePage={page}
          onNavigate={setPage}
      >
        <div>
          <h1 className="text-3xl font-semibold tracking-tight">
            {page}
          </h1>

          <p className="mt-2 text-muted-foreground">
            Manage identities and access across your applications.
          </p>
        </div>

        {page === 'Overview' ? (
            <OverviewPage onNavigate={setPage} />
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
