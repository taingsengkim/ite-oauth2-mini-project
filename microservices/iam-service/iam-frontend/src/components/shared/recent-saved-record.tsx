import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { CheckCircle2, ArrowUpRight, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { requestJson } from '@/lib/api-client'
import { clearRecentSave, useRecentSave } from '@/lib/recent-save'
import type { ManagementResource } from '@/lib/recent-save'
import { useAccountLocale } from '@/lib/account-locale'

type RecordDetails = { name?: string | null; username?: string; email?: string | null; description?: string | null; clientId?: string; resource?: string; action?: string; enabled?: boolean; active?: boolean }
export function RecentSavedRecord({ resource, onView }: { resource: ManagementResource; onView?: (id: string) => void }) {
  const recent = useRecentSave(resource)
  const { locale } = useAccountLocale()
  const [result, setResult] = useState<{ revision: number; data: RecordDetails } | null>(null)
  useEffect(() => {
    if (!recent) return
    const controller = new AbortController()
    requestJson<RecordDetails>(`/dashboard-api/${resource}/${encodeURIComponent(recent.id)}`, { signal: controller.signal })
      .then(data => { if (!controller.signal.aborted) setResult({ revision: recent.revision, data }) })
      .catch(() => { /* The list retains its own loading and error handling. */ })
    return () => controller.abort()
  }, [recent, resource])
  if (!recent || result?.revision !== recent.revision) return null
  const record = result.data
  const title = record.name || record.username || record.clientId || recent.id
  const detail = record.email || record.description || record.clientId || (record.resource ? `${record.resource} / ${record.action}` : '')
  const view = locale === 'km' ? 'មើលព័ត៌មានលម្អិត' : 'View details'
  const dismiss = locale === 'km' ? 'លាក់' : 'Dismiss'
  return <section aria-label={locale === 'km' ? 'ទិន្នន័យដែលទើបរក្សាទុក' : 'Recently saved'} className="space-y-3 rounded-xl border border-emerald-600/20 bg-card p-4">
    <div className="flex items-start justify-between gap-3"><div className="flex items-center gap-2"><CheckCircle2 className="size-4 shrink-0 text-emerald-600 dark:text-emerald-400" aria-hidden="true" /><p role="status" className="text-sm">{recent.message}</p></div><Button variant="ghost" size="icon-sm" aria-label={dismiss} onClick={() => clearRecentSave(resource)}><X className="size-4" aria-hidden="true" /></Button></div>
    <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg bg-muted/40 p-3"><div className="min-w-0 space-y-1"><p className="break-words font-medium">{title}</p>{detail && <p className="break-words text-sm text-muted-foreground">{detail}</p>}<Badge variant="outline">{locale === 'km' ? 'ទើបរក្សាទុក' : 'Recently saved'}</Badge></div>{onView ? <Button variant="outline" size="sm" onClick={() => onView(recent.id)}>{view}<ArrowUpRight className="size-3.5" aria-hidden="true" /></Button> : <Button variant="outline" size="sm" render={<Link to={`/${resource}/${encodeURIComponent(recent.id)}`} />}>{view}<ArrowUpRight className="size-3.5" aria-hidden="true" /></Button>}</div>
  </section>
}
