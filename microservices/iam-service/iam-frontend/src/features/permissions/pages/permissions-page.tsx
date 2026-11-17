import { useRecentSave, rememberSave, clearRecentSave } from '@/lib/recent-save'
import { PermissionSearchForm } from '@/components/shared/search-form'
import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { getDashboardSession } from '@/lib/api-client'
import type { Page } from '@/features/roles/types'
import { listPermissions } from '../api/permissions-api'
import { PermissionFormDialog, PermissionActionDialog, PermissionDetailsDialog } from '../components/permission-dialogs'
import type { Permission, PermissionQuery, PermissionAction, PermissionAccess } from '../types'

export function PermissionsPage() {
  const recent = useRecentSave('permissions')
  const [access, setAccess] = useState<PermissionAccess>({ create: false, update: false, delete: false })
  const [sessionError, setSessionError] = useState<string | null>(null)

  const [query, setQuery] = useState<PermissionQuery>({ page: 0, search: '', resource: '', action: '', active: '' })
  const [revision, setRevision] = useState(0)
  const [result, setResult] = useState<{ key: string; data?: Page<Permission>; error?: string } | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [editor, setEditor] = useState<{ permission?: Permission } | null>(null)
  const [view, setView] = useState<string | null>(null)
  const [action, setAction] = useState<{ permission: Permission; type: PermissionAction } | null>(null)
  const key = JSON.stringify([query, revision, recent?.id])
  useEffect(() => {
    const controller = new AbortController()
    getDashboardSession(controller.signal).then((session) => {
      if (!controller.signal.aborted) setAccess({ create: session.authorities.includes('permission:create'), update: session.authorities.includes('permission:update'), delete: session.authorities.includes('permission:delete') })
    }).catch((failure: unknown) => { if (!controller.signal.aborted) setSessionError(failure instanceof Error ? failure.message : 'Could not load account permissions.') })
    return () => controller.abort()
  }, [])
  useEffect(() => {
    const controller = new AbortController()
    listPermissions({ ...query, priorityUuid: recent?.id }, controller.signal).then((data) => {
      if (controller.signal.aborted) return
      if (query.page > 0 && query.page >= data.page.totalPages) { setQuery((current) => ({ ...current, page: Math.max(0, data.page.totalPages - 1) })); return }
      setResult({ key, data })
    }).catch((failure: unknown) => { if (!controller.signal.aborted) setResult({ key, error: failure instanceof Error ? failure.message : 'Could not load permissions.' }) })
    return () => controller.abort()
  }, [query, key, recent?.id])
  const loading = result?.key !== key
  const data = !loading ? result?.data : undefined
  return <section aria-label="Permission management" className="space-y-4">
    {success && <p role="status" className="rounded-lg border bg-muted/40 p-3 text-sm">{success}</p>}
    {sessionError && <p role="alert" className="text-sm text-destructive">{sessionError}</p>}
    <div className="flex flex-wrap items-end justify-between gap-4">
      <PermissionSearchForm onSearch={(values) => setQuery((current) => ({ ...current, ...values, page: 0 }))}><div className="space-y-2"><label htmlFor="permission-status" className="block text-sm font-medium">Status</label>
          <select id="permission-status" value={query.active} className="h-8 rounded-md border bg-background px-2 text-sm" onChange={(event) => setQuery((current) => ({ ...current, page: 0, active: event.target.value }))}>
            <option value="">All</option><option value="true">Active</option><option value="false">Inactive</option></select></div></PermissionSearchForm>
      {access.create && <Button onClick={() => { setSuccess(null); setEditor({}) }}>Create permission</Button>}
    </div>
    {loading ? <p role="status" className="py-8 text-sm text-muted-foreground">Loading permissions...</p>
      : result?.error ? <div role="alert" className="space-y-3"><p className="text-sm text-destructive">{result.error}</p><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>Retry</Button></div>
        : data && <>
          <p className="text-sm text-muted-foreground">{data.page.totalElements} permissions found</p>
          <div className="overflow-hidden rounded-lg border"><Table><caption className="sr-only">Permission definitions and management actions</caption>
            <TableHeader><TableRow>{['Permission', 'Resource', 'Action', 'Type', 'Status', 'Actions'].map((heading) => <TableHead key={heading}>{heading}</TableHead>)}</TableRow></TableHeader>
            <TableBody>{!data.content.length ? <TableRow><TableCell colSpan={6} className="h-28 text-center text-muted-foreground">No matching permissions.</TableCell></TableRow>
              : data.content.map((permission) => <TableRow key={permission.uuid} className={`cursor-pointer ${recent?.id === permission.uuid ? 'bg-emerald-500/5 ring-1 ring-inset ring-emerald-500/20' : ''}`} onClick={(event) => {
                const target = event.target
                if (target instanceof Element && event.currentTarget.contains(target) && !target.closest('button,[role="menuitem"],[data-permission-actions]') && !window.getSelection()?.toString()) setView(permission.uuid)
              }}>
                <TableCell><button type="button" className="rounded-sm text-left font-medium hover:underline focus-visible:outline-2" onClick={() => setView(permission.uuid)} aria-label={`View ${permission.name}`}>{permission.name}</button></TableCell>
                <TableCell>{permission.resource}</TableCell><TableCell>{permission.action}</TableCell>
                <TableCell><Badge variant="outline">{permission.system ? 'System' : 'Custom'}</Badge></TableCell>
                <TableCell><Badge variant={permission.active ? 'secondary' : 'outline'}>{permission.active ? 'Active' : 'Inactive'}</Badge></TableCell>
                <TableCell data-permission-actions className="cursor-default"><DropdownMenu>
                  <DropdownMenuTrigger render={<Button variant="outline" size="sm" aria-label={`Actions for ${permission.name}`} />}>Actions</DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="min-w-44"><DropdownMenuItem onClick={() => setView(permission.uuid)}>View details</DropdownMenuItem>
                    {access.update && <><DropdownMenuItem disabled={permission.system} onClick={() => { setSuccess(null); setEditor({ permission }) }}>Edit permission</DropdownMenuItem>
                      <DropdownMenuItem disabled={permission.system} onClick={() => { setSuccess(null); setAction({ permission, type: permission.active ? 'deactivate' : 'activate' }) }}>{permission.active ? 'Deactivate' : 'Activate'}</DropdownMenuItem></>}
                    {access.delete && <><DropdownMenuSeparator /><DropdownMenuItem variant="destructive" disabled={permission.system} onClick={() => { setSuccess(null); setAction({ permission, type: 'delete' }) }}>Delete permission</DropdownMenuItem></>}
                    {permission.system && <p className="px-2 py-1 text-xs text-muted-foreground">System permission: changes are protected.</p>}
                  </DropdownMenuContent>
                </DropdownMenu></TableCell>
              </TableRow>)}</TableBody>
          </Table></div>
          <div className="flex items-center justify-between gap-3 text-sm"><p>{data.page.totalPages ? `Page ${data.page.number + 1} of ${data.page.totalPages}` : 'No pages'}</p><div className="flex gap-2">
            <Button variant="outline" disabled={!query.page} onClick={() => setQuery((current) => ({ ...current, page: current.page - 1 }))}>Previous</Button>
            <Button variant="outline" disabled={query.page + 1 >= data.page.totalPages} onClick={() => setQuery((current) => ({ ...current, page: current.page + 1 }))}>Next</Button></div></div>
        </>}
    {editor && <PermissionFormDialog key={editor.permission?.uuid || 'create'} permission={editor.permission} onClose={() => setEditor(null)} onSaved={(permission) => {
      setSuccess(`Permission ${permission.name} saved successfully.`); setEditor(null); setRevision((value) => value + 1); rememberSave('permissions', permission.uuid, `Permission ${permission.name} saved successfully.`)
    }} />}
    {view && <PermissionDetailsDialog key={view} uuid={view} onClose={() => setView(null)} />}
    {action && <PermissionActionDialog permission={action.permission} action={action.type} onClose={() => setAction(null)} onDone={(message) => { if (action.type === 'delete') clearRecentSave('permissions', action.permission.uuid); else rememberSave('permissions', action.permission.uuid, message); setAction(null); setSuccess(message); setRevision((value) => value + 1) }} />}
  </section>
}
