import { useEffect, useState } from 'react'
import { Users, UserCheck, Shield, KeyRound, AppWindow, RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import type { DashboardPage } from '@/components/layout/app-sidebar'
import { getDashboardSession } from '@/lib/api-client'
import { listAuditLogs, getAuditOptions } from '@/features/audit/api/audit-api'
import { emptyAuditFilters } from '@/features/audit/schemas'
import { AuditDetailsDialog } from '@/features/audit/components/audit-details-dialog'
import type { AuditEvent, AuditActionOption } from '@/features/audit/types'
import { getOverview } from '../api/overview-api'
import type { Overview } from '../api/overview-api'

type Activity = { events: AuditEvent[]; options: AuditActionOption[] }
type Result<T> = { key: number; data?: T; error?: string; denied?: boolean }

export function OverviewPage({ onNavigate }: { onNavigate: (page: DashboardPage) => void }) {
  const [revision, setRevision] = useState(0)
  const [session, setSession] = useState<Result<string[]> | null>(null)
  const [counts, setCounts] = useState<Result<Overview> | null>(null)
  const [activity, setActivity] = useState<Result<Activity> | null>(null)
  const [view, setView] = useState<AuditEvent | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    getDashboardSession(controller.signal).then((session) => {
      if (controller.signal.aborted) return
      const authorities = session.authorities
      setSession({ key: revision, data: authorities })
      if (authorities.includes('overview:read')) {
        getOverview(controller.signal).then((data) => { if (!controller.signal.aborted) setCounts({ key: revision, data }) })
          .catch((failure: unknown) => { if (!controller.signal.aborted) setCounts({ key: revision, error: failure instanceof Error ? failure.message : 'Could not load overview statistics.' }) })
      } else setCounts({ key: revision, denied: true })
      if (authorities.includes('audit:read')) {
        Promise.all([listAuditLogs(emptyAuditFilters, 0, controller.signal, 5), getAuditOptions(controller.signal)])
          .then(([page, options]) => { if (!controller.signal.aborted) setActivity({ key: revision, data: { events: page.content, options } }) })
          .catch((failure: unknown) => { if (!controller.signal.aborted) setActivity({ key: revision, error: failure instanceof Error ? failure.message : 'Could not load recent activity.' }) })
      } else setActivity({ key: revision, denied: true })
    }).catch((failure: unknown) => {
      if (!controller.signal.aborted) setSession({ key: revision, error: failure instanceof Error ? failure.message : 'Could not load dashboard access.' })
    })
    return () => controller.abort()
  }, [revision])

  const sessionReady = session?.key === revision
  const authorities = sessionReady ? session?.data ?? [] : []
  const statistics = counts?.key === revision ? counts.data?.statistics : undefined
  const recent = activity?.key === revision ? activity.data : undefined
  const labels = new Map(recent?.options.map((option) => [option.code, option.label]))
  const loading = !sessionReady || (!session?.error && (counts?.key !== revision || activity?.key !== revision))
  const metrics = statistics ? [
    { title: 'Total users', value: statistics.totalUsers, icon: Users, description: `${statistics.disabledUsers.toLocaleString()} disabled · ${statistics.lockedUsers.toLocaleString()} locked (counts may overlap)`, page: 'Users' as const, permission: 'user:read' },
    { title: 'Active users', value: statistics.activeUsers, icon: UserCheck, description: 'Enabled, unlocked, unexpired accounts. Not a count of online sessions.', page: 'Users' as const, permission: 'user:read' },
    { title: 'Roles', value: statistics.roles, icon: Shield, description: 'All non-deleted roles, including inactive roles.', page: 'Roles' as const, permission: 'role:read' },
    { title: 'Permissions', value: statistics.permissions, icon: KeyRound, description: 'All non-deleted permission definitions.', page: 'Permissions' as const, permission: 'permission:read' },
    { title: 'OAuth clients', value: statistics.clients, icon: AppWindow, description: 'Registered applications, including built-in clients.', page: 'Clients' as const, permission: 'client:read' },
  ] : []

  return <section aria-label="Administrator overview" className="space-y-6">
    <div className="flex flex-wrap items-center justify-between gap-3"><p className="text-sm text-muted-foreground">{counts?.key === revision && counts.data ? `Statistics updated ${new Date(counts.data.generatedAt).toLocaleString()}` : 'Live account and access statistics'}</p><Button variant="outline" disabled={loading} onClick={() => setRevision((value) => value + 1)}><RefreshCw aria-hidden="true" className="size-4" />Refresh</Button></div>
    {!sessionReady ? <p role="status" className="py-6 text-muted-foreground">Loading dashboard...</p> : session?.error ? <div role="alert" className="space-y-3"><p className="text-sm text-destructive">{session.error}</p><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>Retry</Button></div> : <>
      {counts?.key !== revision ? <p role="status" className="py-6 text-muted-foreground">Loading statistics...</p> : counts.denied ? <p className="rounded-lg border p-4 text-sm text-muted-foreground">Viewing overview statistics requires overview:read access.</p> : counts.error ? <div role="alert" className="space-y-3 rounded-lg border p-4"><p className="text-sm text-destructive">{counts.error}</p><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>Retry statistics</Button></div> : <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {metrics.map(({ title, value, icon: Icon, description, page, permission }) => <Card key={title}>
          <CardHeader><div className="flex items-center justify-between gap-3"><CardTitle>{title}</CardTitle><Icon aria-hidden="true" className="size-5 text-muted-foreground" /></div></CardHeader>
          <CardContent className="space-y-3"><p className="text-3xl font-semibold tracking-tight">{value.toLocaleString()}</p><CardDescription>{description}</CardDescription>{authorities.includes(permission) && <Button variant="outline" onClick={() => onNavigate(page)}>Manage {page.toLowerCase()}</Button>}</CardContent>
        </Card>)}
      </div>}
      <Card><CardHeader><div className="flex flex-wrap items-center justify-between gap-3"><div className="space-y-1"><CardTitle>Recent activity</CardTitle><CardDescription>Latest administrator actions and sign-in events.</CardDescription></div>{authorities.includes('audit:read') && <Button variant="outline" onClick={() => onNavigate('Audit Logs')}>View all activity</Button>}</div></CardHeader>
        <CardContent>{activity?.key !== revision ? <p role="status" className="py-4 text-sm text-muted-foreground">Loading recent activity...</p> : activity.denied ? <p className="text-sm text-muted-foreground">Viewing recent activity requires audit:read access.</p> : activity.error ? <div role="alert" className="space-y-3"><p className="text-sm text-destructive">{activity.error}</p><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>Retry activity</Button></div> : recent && (recent.events.length === 0 ? <p className="py-4 text-sm text-muted-foreground">No recorded activity yet.</p> : <div className="overflow-hidden rounded-lg border"><Table><caption className="sr-only">Five latest audit events</caption><TableHeader><TableRow><TableHead>When</TableHead><TableHead>Actor</TableHead><TableHead>Activity</TableHead><TableHead>Result</TableHead></TableRow></TableHeader>
          <TableBody>{recent.events.map((event) => <TableRow key={event.uuid} className="cursor-pointer" onClick={(click) => {
            const target = click.target
            if (target instanceof Element && click.currentTarget.contains(target) && !target.closest('button') && !window.getSelection()?.toString()) setView(event)
          }}><TableCell className="whitespace-nowrap"><time dateTime={event.occurredAt}>{new Date(event.occurredAt).toLocaleString()}</time></TableCell><TableCell className="max-w-40 break-words whitespace-normal">{event.actor}</TableCell><TableCell><button type="button" className="rounded-sm text-left font-medium hover:underline focus-visible:outline-2" onClick={() => setView(event)}>{labels.get(event.action) ?? event.action}</button></TableCell><TableCell><Badge variant={event.outcome === 'SUCCESS' ? 'secondary' : 'destructive'}>{event.outcome === 'SUCCESS' ? 'Successful' : 'Failed'}</Badge></TableCell></TableRow>)}</TableBody>
        </Table></div>)}</CardContent>
      </Card>
      <p className="text-xs text-muted-foreground">Deleted accounts, roles, and permissions are excluded. Disabled and locked account counts can overlap.</p>
    </>}
    {view && <AuditDetailsDialog event={view} label={labels.get(view.action) ?? view.action} onClose={() => setView(null)} />}
  </section>
}
