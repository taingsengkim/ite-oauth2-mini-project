import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { FormField, FormError } from '@/components/shared/form-field'
import { useSchemaForm } from '@/lib/forms'
import { auditFiltersSchema, emptyAuditFilters } from '../schemas'
import type { AuditFilters } from '../schemas'
import { listAuditLogs, getAuditOptions } from '../api/audit-api'
import type { AuditActionOption, AuditPage, AuditEvent } from '../types'
import { AuditDetailsDialog } from '../components/audit-details-dialog'

export function AuditLogsPage() {
  const form = useSchemaForm(auditFiltersSchema, emptyAuditFilters)
  const [filters, setFilters] = useState<AuditFilters>(emptyAuditFilters)
  const [page, setPage] = useState(0)
  const [revision, setRevision] = useState(0)
  const [result, setResult] = useState<{ key: string; data?: AuditPage; error?: string } | null>(null)
  const [options, setOptions] = useState<AuditActionOption[]>([])
  const [optionsError, setOptionsError] = useState<string | null>(null)
  const [view, setView] = useState<AuditEvent | null>(null)
  const key = JSON.stringify([filters, page, revision])
  useEffect(() => {
    const controller = new AbortController()
    getAuditOptions(controller.signal).then((data) => { if (!controller.signal.aborted) { setOptions(data); setOptionsError(null) } })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setOptionsError(failure instanceof Error ? failure.message : 'Could not load audit filters.') })
    return () => controller.abort()
  }, [revision])
  useEffect(() => {
    const controller = new AbortController()
    listAuditLogs(filters, page, controller.signal).then((data) => { if (!controller.signal.aborted) setResult({ key, data }) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setResult({ key, error: failure instanceof Error ? failure.message : 'Could not load audit logs.' }) })
    return () => controller.abort()
  }, [filters, page, key])
  const loading = result?.key !== key
  const data = loading ? undefined : result?.data
  const labels = new Map(options.map((option) => [option.code, option.label]))
  const resources = [...new Set(options.map((option) => option.resource))].sort()

  return <section aria-label="Audit logs" className="space-y-5">
    <form noValidate className="space-y-4 rounded-lg border p-4" onSubmit={form.submit((values) => { setFilters(values); setPage(0); setRevision((value) => value + 1) })}>
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <FormField form={form} name="actor" label="Actor (exact username, email, or user ID)" />
        <FormField form={form} name="resource" label="Resource" as="select" options={[{ value: '', label: 'All resources' }, ...resources.map((resource) => ({ value: resource, label: resource.charAt(0).toUpperCase() + resource.slice(1) }))]} />
        <FormField form={form} name="action" label="Action" as="select" options={[{ value: '', label: 'All actions' }, ...options.map((option) => ({ value: option.code, label: option.label }))]} />
        <FormField form={form} name="targetId" label="Target ID (exact)" />
        <FormField form={form} name="outcome" label="Result" as="select" options={[{ value: '', label: 'All results' }, { value: 'SUCCESS', label: 'Successful' }, { value: 'FAILURE', label: 'Failed' }]} />
        <FormField form={form} name="from" label="From (local time)" type="datetime-local" /><FormField form={form} name="to" label="To (local time)" type="datetime-local" />
      </div>
      <FormError message={optionsError ?? undefined} />
      <div className="flex flex-wrap gap-2"><Button type="submit">Apply filters</Button><Button type="button" variant="outline" onClick={() => { form.reset(emptyAuditFilters); setFilters(emptyAuditFilters); setPage(0); setRevision((value) => value + 1) }}>Reset</Button><Button type="button" variant="outline" disabled={loading} onClick={() => setRevision((value) => value + 1)}>Refresh</Button></div>
    </form>
    <p className="text-sm text-muted-foreground">Read-only history, newest first. Administrator events start from this update; previously recorded login history is included.</p>
    {loading ? <p role="status" className="py-8 text-sm text-muted-foreground">Loading audit logs...</p>
      : result?.error ? <div role="alert" className="space-y-3"><p className="text-sm text-destructive">{result.error}</p><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>Retry</Button></div>
        : data && <>
          <p className="text-sm text-muted-foreground">{data.totalElements} matching events. Click a row for details.</p>
          <div className="overflow-hidden rounded-lg border"><Table><caption className="sr-only">Administrator actions and sign-in events</caption>
            <TableHeader><TableRow>{['When', 'Actor', 'Action', 'Resource / target', 'Result', 'IP address'].map((name) => <TableHead key={name}>{name}</TableHead>)}</TableRow></TableHeader>
            <TableBody>{data.content.length === 0 ? <TableRow><TableCell colSpan={6} className="h-28 text-center text-muted-foreground">No audit events match these filters.</TableCell></TableRow>
              : data.content.map((event) => <TableRow key={event.uuid} className="cursor-pointer" onClick={(click) => {
                const target = click.target
                if (target instanceof Element && click.currentTarget.contains(target) && !target.closest('button') && !window.getSelection()?.toString()) setView(event)
              }}>
                <TableCell className="whitespace-nowrap"><time dateTime={event.occurredAt}>{new Date(event.occurredAt).toLocaleString()}</time></TableCell>
                <TableCell className="max-w-48 break-words whitespace-normal">{event.actor}</TableCell>
                <TableCell><button type="button" className="rounded-sm text-left font-medium hover:underline focus-visible:outline-2" onClick={() => setView(event)}>{labels.get(event.action) ?? event.action}</button></TableCell>
                <TableCell className="max-w-52 whitespace-normal"><Badge variant="outline">{event.resource}</Badge><p className="mt-1 break-all text-xs text-muted-foreground">{event.targetId || 'Unidentified account'}</p></TableCell>
                <TableCell><Badge variant={event.outcome === 'SUCCESS' ? 'secondary' : 'destructive'}>{event.outcome === 'SUCCESS' ? 'Successful' : 'Failed'}</Badge></TableCell>
                <TableCell>{event.ipAddress || 'Unavailable'}</TableCell>
              </TableRow>)}</TableBody>
          </Table></div>
          <div className="flex items-center justify-between gap-3 text-sm"><p>Page {data.page + 1} of {Math.max(data.totalPages, 1)}</p><div className="flex gap-2"><Button variant="outline" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>Previous</Button><Button variant="outline" disabled={page + 1 >= data.totalPages} onClick={() => setPage((value) => value + 1)}>Next</Button></div></div>
        </>}
    {view && <AuditDetailsDialog event={view} label={labels.get(view.action) ?? view.action} onClose={() => setView(null)} />}
  </section>
}
