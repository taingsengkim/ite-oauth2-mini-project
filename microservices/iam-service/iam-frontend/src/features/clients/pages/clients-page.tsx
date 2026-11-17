import { useRecentSave } from '@/lib/recent-save'
import { useLocation, useNavigate } from 'react-router-dom'
import { SearchForm } from '@/components/shared/search-form'
import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { getDashboardSession } from '@/lib/api-client'
import { listClients } from '../api/clients-api'
import type { ClientAccess, ClientPage } from '../types'

export function ClientsPage() {
  const recent = useRecentSave('clients')
  const navigate = useNavigate()
  const location = useLocation()
  const [access, setAccess] = useState<ClientAccess>({ create: false, update: false, delete: false })
  const [sessionError, setSessionError] = useState<string | null>(null)

  const [query, setQuery] = useState({ search: '', page: 0 })
  const [revision, setRevision] = useState(0)
  const [result, setResult] = useState<{ key: string; data?: ClientPage; error?: string } | null>(null)
  const key = JSON.stringify([query, revision, recent?.id])

  useEffect(() => {
    const controller = new AbortController()
    getDashboardSession(controller.signal).then((session) => {
      if (!controller.signal.aborted) setAccess({ create: session.authorities.includes('client:create'), update: session.authorities.includes('client:update'), delete: session.authorities.includes('client:delete') })
    }).catch((failure: unknown) => { if (!controller.signal.aborted) setSessionError(failure instanceof Error ? failure.message : 'Could not load account permissions.') })
    return () => controller.abort()
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    listClients(query.search, query.page, controller.signal, recent?.id).then((data) => {
      if (controller.signal.aborted) return
      if (query.page > 0 && query.page >= data.totalPages) { setQuery((current) => ({ ...current, page: Math.max(0, data.totalPages - 1) })); return }
      setResult({ key, data })
    }).catch((failure: unknown) => { if (!controller.signal.aborted) setResult({ key, error: failure instanceof Error ? failure.message : 'Could not load clients.' }) })
    return () => controller.abort()
  }, [query, key, recent?.id])

  const loading = result?.key !== key
  const data = !loading ? result?.data : undefined

  return <section aria-label="OAuth client management" className="space-y-4">
    {location.state?.notice && <p role="status" className="rounded-lg border bg-muted/40 p-3 text-sm">{location.state.notice}</p>}
    {sessionError && <p role="alert" className="text-sm text-destructive">{sessionError}</p>}
    <div className="flex flex-wrap items-end justify-between gap-4">
      <SearchForm label="Search clients" onSearch={(value) => { setQuery({ search: value, page: 0 }); setRevision((current) => current + 1) }} />
      {access.create && <Button onClick={() => { void navigate('/clients/new') }}>Register client</Button>}
    </div>
    {loading ? <p role="status" className="py-8 text-sm text-muted-foreground">Loading clients…</p>
      : result?.error ? <div role="alert" className="space-y-3"><p className="text-sm text-destructive">{result.error}</p><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>Retry</Button></div>
        : data && <>
          <p className="text-sm text-muted-foreground">{data.totalElements} clients found</p>
          <div className="overflow-hidden rounded-lg border"><Table><caption className="sr-only">OAuth clients and management actions</caption>
            <TableHeader><TableRow>{['Application', 'Client ID', 'Authentication', 'Grant types', 'Actions'].map((label) => <TableHead key={label}>{label}</TableHead>)}</TableRow></TableHeader>
            <TableBody>{!data.content.length ? <TableRow><TableCell colSpan={5} className="h-28 text-center text-muted-foreground">No matching clients.</TableCell></TableRow>
              : data.content.map((client) => <TableRow key={client.id} className={`cursor-pointer ${recent?.id === client.id ? 'bg-emerald-500/5 ring-1 ring-inset ring-emerald-500/20' : ''}`} onClick={(event) => {
                const target = event.target
                if (target instanceof Element && event.currentTarget.contains(target) && !target.closest('button,[role="menuitem"],[data-client-actions]') && !window.getSelection()?.toString()) void navigate('/clients/' + encodeURIComponent(client.id))
              }}>
                <TableCell><button className="rounded-sm text-left font-medium hover:underline focus-visible:outline-2" onClick={() => void navigate('/clients/' + encodeURIComponent(client.id))} aria-label={`View ${client.name}`}>{client.name}</button>{client.protectedClient && <Badge variant="outline" className="ml-2">Built-in</Badge>}</TableCell>
                <TableCell className="max-w-52 break-words whitespace-normal">{client.clientId}</TableCell>
                <TableCell>{client.authenticationMethods.includes('none') ? 'Public / PKCE' : client.authenticationMethods.join(', ')}</TableCell>
                <TableCell><div className="flex flex-wrap gap-1">{client.grantTypes.map((grant) => <Badge key={grant} variant="secondary">{grant}</Badge>)}</div></TableCell>
                <TableCell data-client-actions className="cursor-default"><DropdownMenu>
                  <DropdownMenuTrigger render={<Button variant="outline" size="sm" aria-label={`Actions for ${client.name}`} />}>Actions</DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="min-w-44"><DropdownMenuItem onClick={() => void navigate('/clients/' + encodeURIComponent(client.id))}>View details</DropdownMenuItem>
                    {access.update && <><DropdownMenuItem disabled={!client.editable} onClick={() => { void navigate('/clients/' + encodeURIComponent(client.id) + '/edit') }}>Edit client</DropdownMenuItem>
                      <DropdownMenuItem disabled={client.authenticationMethods.includes('none') || !client.authenticationMethods.some((method) => ['client_secret_basic', 'client_secret_post'].includes(method))} onClick={() => { void navigate('/clients/' + encodeURIComponent(client.id) + '/rotate') }}>Rotate secret</DropdownMenuItem></>}
                    {access.delete && <><DropdownMenuSeparator /><DropdownMenuItem variant="destructive" disabled={client.protectedClient} onClick={() => { void navigate('/clients/' + encodeURIComponent(client.id) + '/delete') }}>Delete client</DropdownMenuItem></>}
                    {client.protectedClient && <p className="px-2 py-1 text-xs text-muted-foreground">Built-in clients cannot be deleted.</p>}
                  </DropdownMenuContent>
                </DropdownMenu></TableCell>
              </TableRow>)}</TableBody>
          </Table></div>
          <div className="flex items-center justify-between gap-3 text-sm"><p>{data.totalPages ? `Page ${data.page + 1} of ${data.totalPages}` : 'No pages'}</p><div className="flex gap-2"><Button variant="outline" disabled={!query.page} onClick={() => setQuery((current) => ({ ...current, page: current.page - 1 }))}>Previous</Button><Button variant="outline" disabled={query.page + 1 >= data.totalPages} onClick={() => setQuery((current) => ({ ...current, page: current.page + 1 }))}>Next</Button></div></div>
        </>}
  </section>
}
