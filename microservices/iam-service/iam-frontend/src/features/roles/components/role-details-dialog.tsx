import { useEffect, useRef, useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { getRole, getRolePermissions, listPermissions, assignPermissions, revokePermission } from '../api/roles-api'
import type { Role, RoleAccess, AssignedPermission, Permission, Page } from '../types'
import { RoleModal } from './role-modal'

type Details = { role: Role; permissions: AssignedPermission[] }
export function RoleDetailsDialog({ uuid, access, onClose, onChanged }: {
  uuid: string; access: RoleAccess; onClose: () => void; onChanged: (message: string) => void
}) {
  const [data, setData] = useState<Details | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [retry, setRetry] = useState(0)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selected, setSelected] = useState<string[]>([])
  const [removing, setRemoving] = useState<string | null>(null)
  const [searchInput, setSearchInput] = useState('')
  const [query, setQuery] = useState({ page: 0, search: '' })
  const [catalogue, setCatalogue] = useState<{ key: string; data?: Page<Permission>; error?: string } | null>(null)
  const pending = useRef(false)
  const queryKey = JSON.stringify(query)
  const canAssign = Boolean(access.assign && data && !data.role.system && data.role.active)
  const canBrowse = canAssign && access.browsePermissions

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([getRole(uuid, controller.signal), getRolePermissions(uuid, controller.signal)])
      .then(([role, permissions]) => { if (!controller.signal.aborted) setData({ role, permissions }) })
      .catch((failure: unknown) => {
        if (!controller.signal.aborted) setLoadError(failure instanceof Error ? failure.message : 'Could not load role.')
      })
    return () => controller.abort()
  }, [uuid, retry])

  useEffect(() => {
    if (!canBrowse) return
    const controller = new AbortController()
    listPermissions(query.page, query.search, controller.signal).then((result) => {
      if (!controller.signal.aborted) setCatalogue({ key: queryKey, data: result })
    }).catch((failure: unknown) => {
      if (!controller.signal.aborted) setCatalogue({ key: queryKey, error: failure instanceof Error ? failure.message : 'Could not load permissions.' })
    })
    return () => controller.abort()
  }, [canBrowse, query, queryKey, retry])

  async function change(operation: () => Promise<unknown>) {
    if (pending.current) return
    pending.current = true
    setBusy(true)
    setError(null)
    let saved = false
    try {
      await operation()
      saved = true
      setSelected([])
      setRemoving(null)
      onChanged('Role permissions updated successfully. Changes apply to future sign-ins.')
      const [role, permissions] = await Promise.all([getRole(uuid), getRolePermissions(uuid)])
      setData({ role, permissions })
    } catch (failure) {
      if (saved) {
        setData(null)
        setLoadError('Changes were saved, but refreshing failed. Retry loading the details.')
      } else setError(failure instanceof Error ? failure.message : 'Could not update permissions.')
    } finally { pending.current = false; setBusy(false) }
  }

  const assigned = new Set(data?.permissions.map((permission) => permission.uuid))
  const catalogueLoading = catalogue?.key !== queryKey
  return <RoleModal title={data ? `Role: ${data.role.name}` : 'Role details'} description="View role information and manage its permission assignments." busy={busy} onClose={onClose}>
    {loadError ? <div role="alert" className="space-y-3">
      <p className="text-sm text-destructive">{loadError}</p>
      <Button variant="outline" onClick={() => { setLoadError(null); setRetry((value) => value + 1) }}>Retry</Button>
    </div> : !data ? <p role="status">Loading role details...</p> : <div className="space-y-6">
      <div className="space-y-3">
        <div className="flex gap-2"><Badge variant={data.role.active ? 'secondary' : 'outline'}>{data.role.active ? 'Active' : 'Inactive'}</Badge>
          <Badge variant="outline">{data.role.system ? 'System role' : 'Custom role'}</Badge></div>
        <p className="text-sm text-muted-foreground">{data.role.description || 'No description.'}</p>
        <dl className="grid gap-3 text-sm sm:grid-cols-2">
          <div><dt className="text-muted-foreground">Created</dt><dd>{new Date(data.role.createdAt).toLocaleString()}</dd></div>
          <div><dt className="text-muted-foreground">Updated</dt><dd>{new Date(data.role.updatedAt).toLocaleString()}</dd></div>
        </dl>
        {data.role.system && <p className="rounded-md bg-muted/50 p-3 text-sm">System roles and their permission assignments are protected from changes.</p>}
      </div>
      {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
      <section aria-label="Assigned permissions" className="space-y-3">
        <h3 className="font-medium">Assigned permissions ({data.permissions.length})</h3>
        {!data.permissions.length && <p className="text-sm text-muted-foreground">No current permission assignments.</p>}
        <ul className="max-h-64 space-y-2 overflow-y-auto">
          {data.permissions.map((permission) => <li key={permission.uuid} className="rounded-md border p-3">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div><p className="text-sm font-medium">{permission.name}</p>
                <p className="text-xs text-muted-foreground">{permission.resource} / {permission.action}{!permission.active ? ' · Inactive permission' : ''}</p>
                {permission.expiresAt && <p className="text-xs text-muted-foreground">Expires {new Date(permission.expiresAt).toLocaleString()}</p>}</div>
              {access.assign && !data.role.system && <Button variant="outline" size="sm" disabled={busy}
                aria-label={`Remove ${permission.name}`} onClick={() => { setError(null); setRemoving(permission.uuid) }}>Remove</Button>}
            </div>
            {removing === permission.uuid && <div className="mt-3 space-y-2 border-t pt-3">
              <p className="text-sm">Remove {permission.name} from this role?</p>
              <div className="flex gap-2"><Button size="sm" variant="outline" disabled={busy} onClick={() => setRemoving(null)}>Cancel</Button>
                <Button size="sm" variant="destructive" disabled={busy} onClick={() => void change(() => revokePermission(uuid, permission.uuid))}>Confirm removal</Button></div>
            </div>}
          </li>)}
        </ul>
      </section>
      {access.assign && !data.role.system && !data.role.active && <p className="text-sm text-muted-foreground">Activate this role before assigning additional permissions.</p>}
      {canAssign && !access.browsePermissions && <p className="text-sm text-muted-foreground">Permission browsing requires permission:read access.</p>}
      {canBrowse && <section aria-label="Available permissions" className="space-y-3 border-t pt-4">
        <h3 className="font-medium">Add permissions</h3>
        <form className="flex gap-2" onSubmit={(event) => { event.preventDefault(); setQuery({ page: 0, search: searchInput.trim() }) }}>
          <Input aria-label="Search available permissions" placeholder="Search permissions..." maxLength={255} value={searchInput}
            disabled={busy} onChange={(event) => setSearchInput(event.target.value)} />
          <Button type="submit" variant="outline" disabled={busy}>Search</Button>
        </form>
        {catalogueLoading ? <p role="status" className="text-sm">Loading permissions...</p>
          : catalogue?.error ? <p role="alert" className="text-sm text-destructive">{catalogue.error} <Button variant="outline" size="sm" onClick={() => setRetry((value) => value + 1)}>Retry</Button></p>
          : catalogue?.data && <>
            {!catalogue.data.content.length && <p className="text-sm text-muted-foreground">No matching active permissions.</p>}
            <ul className="max-h-52 space-y-2 overflow-y-auto">
              {catalogue.data.content.map((permission) => <li key={permission.uuid}>
                <label className="flex items-start gap-3 rounded-md border p-2 text-sm">
                  <input type="checkbox" className="mt-1" checked={assigned.has(permission.uuid) || selected.includes(permission.uuid)}
                    disabled={busy || assigned.has(permission.uuid) || (selected.length >= 100 && !selected.includes(permission.uuid))}
                    onChange={(event) => setSelected((current) => event.target.checked ? [...current, permission.uuid] : current.filter((id) => id !== permission.uuid))} />
                  <span>{permission.name}<span className="block text-xs text-muted-foreground">{assigned.has(permission.uuid) ? 'Already assigned' : permission.description || `${permission.resource} / ${permission.action}`}</span></span>
                </label>
              </li>)}
            </ul>
            <div className="flex items-center justify-between gap-2 text-sm">
              <span>{catalogue.data.page.totalElements} permissions</span>
              <div className="flex gap-2"><Button size="sm" variant="outline" disabled={busy || query.page === 0} onClick={() => setQuery((current) => ({ ...current, page: current.page - 1 }))}>Previous</Button>
                <Button size="sm" variant="outline" disabled={busy || query.page + 1 >= catalogue.data.page.totalPages} onClick={() => setQuery((current) => ({ ...current, page: current.page + 1 }))}>Next</Button></div>
            </div>
          </>}
        <Button disabled={busy || !selected.length} onClick={() => void change(() => assignPermissions(uuid, selected))}>
          {busy ? 'Updating...' : `Assign selected (${selected.length})`}
        </Button>
      </section>}
    </div>}
  </RoleModal>
}
