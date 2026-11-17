import { RecentSavedRecord } from '@/components/shared/recent-saved-record'
import { useRecentSave, rememberSave, clearRecentSave } from '@/lib/recent-save'
import { useNavigate } from 'react-router-dom'
import { SearchForm } from '@/components/shared/search-form'
import { useEffect, useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { getDashboardSession } from '@/lib/api-client'
import { listRoles } from '../api/roles-api'
import { RoleFormDialog } from '../components/role-form-dialog'
import { RoleActionDialog } from '../components/role-action-dialog'
import type { RoleAction } from '../components/role-action-dialog'
import type { Page, Role, RoleAccess, RoleSummary } from '../types'

const noAccess: RoleAccess = { create: false, update: false, delete: false, assign: false, browsePermissions: false }
export function RolesPage() {
  const recent = useRecentSave('roles')
  const navigate = useNavigate()
  const [access, setAccess] = useState(noAccess)
  const [sessionError, setSessionError] = useState<string | null>(null)
  const [query, setQuery] = useState({ page: 0, search: '', active: '' })
  const [revision, setRevision] = useState(0)
  const [result, setResult] = useState<{ key: string; data?: Page<RoleSummary>; error?: string } | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [editor, setEditor] = useState<{ role?: RoleSummary } | null>(null)
  const [action, setAction] = useState<{ role: RoleSummary; type: RoleAction } | null>(null)
  const key = JSON.stringify([query, revision])

  useEffect(() => {
    const controller = new AbortController()
    getDashboardSession(controller.signal).then((session) => {
      if (!controller.signal.aborted) setAccess({
        create: session.authorities.includes('role:create'), update: session.authorities.includes('role:update'),
        delete: session.authorities.includes('role:delete'), assign: session.authorities.includes('role:assign'),
        browsePermissions: session.authorities.includes('permission:read'),
      })
    }).catch((failure: unknown) => {
      if (!controller.signal.aborted) setSessionError(failure instanceof Error ? failure.message : 'Could not load permissions.')
    })
    return () => controller.abort()
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    listRoles(query, controller.signal).then((data) => {
      if (controller.signal.aborted) return
      if (query.page > 0 && query.page >= data.page.totalPages) {
        setQuery((current) => ({ ...current, page: Math.max(0, data.page.totalPages - 1) }))
        return
      }
      setResult({ key, data })
    }).catch((failure: unknown) => {
      if (!controller.signal.aborted) setResult({ key, error: failure instanceof Error ? failure.message : 'Could not load roles.' })
    })
    return () => controller.abort()
  }, [query, key])

  function saved(role: Role) {
    setSuccess(`Role ${role.name} was ${editor?.role ? 'updated' : 'created'} successfully.`)
    setEditor(null)
    setRevision((value) => value + 1)
    rememberSave('roles', role.uuid, `Role ${role.name} saved successfully.`)
  }
  const loading = result?.key !== key
  const data = !loading ? result?.data : undefined

  return <section className="space-y-4" aria-label="Role management"><RecentSavedRecord resource="roles" />
    {success && <p role="status" className="rounded-lg border bg-muted/40 p-3 text-sm">{success}</p>}
    {sessionError && <p role="alert" className="text-sm text-destructive">{sessionError}</p>}
    <div className="flex flex-wrap items-end justify-between gap-4">
      <SearchForm label="Search roles" onSearch={(value) => { setQuery((current) => ({ ...current, page: 0, search: value })) }}><div className="space-y-2"><label htmlFor="role-status" className="block text-sm font-medium">Status</label>
          <select id="role-status" className="h-8 rounded-md border bg-background px-2 text-sm" value={query.active}
            onChange={(event) => setQuery((current) => ({ ...current, page: 0, active: event.target.value }))}>
            <option value="">All roles</option><option value="true">Active</option><option value="false">Inactive</option>
          </select></div></SearchForm>
      {access.create && <Button onClick={() => { setSuccess(null); setEditor({}) }}>Create role</Button>}
    </div>
    {loading ? <p role="status" className="py-8 text-sm text-muted-foreground">Loading roles...</p>
      : result?.error ? <div role="alert" className="space-y-3 rounded-lg border p-4"><p className="text-sm text-destructive">{result.error}</p>
        <Button variant="outline" onClick={() => setRevision((value) => value + 1)}>Retry</Button></div>
      : data && <>
        <p className="text-sm text-muted-foreground">{data.page.totalElements} roles found</p>
        <div className="overflow-hidden rounded-lg border"><Table>
          <caption className="sr-only">Roles and their management actions</caption>
          <TableHeader><TableRow><TableHead>Name</TableHead><TableHead>Description</TableHead><TableHead>Type</TableHead><TableHead>Status</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader>
          <TableBody>{!data.content.length ? <TableRow><TableCell colSpan={5} className="h-28 text-center text-muted-foreground">No roles match your search.</TableCell></TableRow>
            : data.content.map((role) => <TableRow key={role.uuid} className={`cursor-pointer ${recent?.id === role.uuid ? 'bg-emerald-500/5 ring-1 ring-inset ring-emerald-500/20' : ''}`} onClick={(event) => {
              const target = event.target
              if (target instanceof Element && event.currentTarget.contains(target) && !target.closest('button,[role="menuitem"],[data-role-actions]') && !window.getSelection()?.toString()) void navigate('/roles/' + encodeURIComponent(role.uuid))
            }}>
              <TableCell><button type="button" className="rounded-sm text-left font-medium hover:underline focus-visible:outline-2" onClick={() => void navigate('/roles/' + encodeURIComponent(role.uuid))} aria-label={`View ${role.name}`}>{role.name}</button></TableCell>
              <TableCell className="max-w-xs truncate" title={role.description || ''}>{role.description || 'No description'}</TableCell>
              <TableCell><Badge variant="outline">{role.system ? 'System' : 'Custom'}</Badge></TableCell>
              <TableCell><Badge variant={role.active ? 'secondary' : 'outline'}>{role.active ? 'Active' : 'Inactive'}</Badge></TableCell>
              <TableCell className="cursor-default text-right" data-role-actions><DropdownMenu>
                <DropdownMenuTrigger render={<Button variant="outline" size="sm" aria-label={`Actions for ${role.name}`} />}>Actions</DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="min-w-44">
                  <DropdownMenuItem onClick={() => void navigate('/roles/' + encodeURIComponent(role.uuid))}>Details & permissions</DropdownMenuItem>
                  {access.update && <DropdownMenuItem disabled={role.system} onClick={() => { setSuccess(null); void navigate('/roles/' + encodeURIComponent(role.uuid) + '/edit') }}>Edit role</DropdownMenuItem>}
                  {access.update && <DropdownMenuItem disabled={role.system} onClick={() => { setSuccess(null); setAction({ role, type: role.active ? 'deactivate' : 'activate' }) }}>{role.active ? 'Deactivate' : 'Activate'}</DropdownMenuItem>}
                  {access.delete && <><DropdownMenuSeparator /><DropdownMenuItem variant="destructive" disabled={role.system} onClick={() => { setSuccess(null); setAction({ role, type: 'delete' }) }}>Delete role</DropdownMenuItem></>}
                  {role.system && <p className="px-2 py-1 text-xs text-muted-foreground">System role: changes are protected.</p>}
                </DropdownMenuContent>
              </DropdownMenu></TableCell>
            </TableRow>)}</TableBody>
        </Table></div>
        <div className="flex items-center justify-between gap-3 text-sm text-muted-foreground"><p>{data.page.totalPages ? `Page ${data.page.number + 1} of ${data.page.totalPages}` : 'No pages'}</p>
          <div className="flex gap-2"><Button variant="outline" disabled={!query.page} onClick={() => setQuery((current) => ({ ...current, page: current.page - 1 }))}>Previous</Button>
            <Button variant="outline" disabled={query.page + 1 >= data.page.totalPages} onClick={() => setQuery((current) => ({ ...current, page: current.page + 1 }))}>Next</Button></div></div>
      </>}
    {editor && <RoleFormDialog key={editor.role?.uuid || 'create'} role={editor.role} onClose={() => setEditor(null)} onSaved={saved} />}
    {action && <RoleActionDialog role={action.role} action={action.type} onClose={() => setAction(null)} onDone={(message) => { if (action.type === 'delete') clearRecentSave('roles', action.role.uuid); else rememberSave('roles', action.role.uuid, message); setAction(null); setSuccess(message); setRevision((value) => value + 1) }} />}
  </section>
}
