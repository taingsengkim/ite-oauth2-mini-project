import { permissionAssignmentSchema, toExpiry } from '@/lib/form-schemas'
import { useWatch } from 'react-hook-form'
import { useSchemaForm } from '@/lib/forms'
import { FormField, FormError } from '@/components/shared/form-field'
import { SearchForm } from '@/components/shared/search-form'
import { useEffect, useRef, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { listPermissions } from '@/features/permissions/api/permissions-api'
import type { Permission } from '@/features/permissions/types'
import type { Page } from '@/features/roles/types'
import { getUserPermissions, getUserAuthorities, grantUserPermissions, revokeUserPermission } from '../api/user-permissions-api'
import type { User, DirectUserPermission, PermissionEffect } from '../types/types'

export function UserPermissionsDialog({ user, canBrowse, currentUserUuid, onClose, onChanged }: {
  user: User; canBrowse: boolean; currentUserUuid: string | null; onClose: () => void; onChanged: (message: string) => void
}) {
  const [data, setData] = useState<{ direct: DirectUserPermission[]; effective: string[] } | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [retry, setRetry] = useState(0)
  const [query, setQuery] = useState({ page: 0, search: '', resource: '', action: '', active: 'true' })

  const [catalogue, setCatalogue] = useState<{ key: string; data?: Page<Permission>; error?: string } | null>(null)
  const assignment = useSchemaForm(permissionAssignmentSchema, { selected: [], expiresAt: '', reason: '', effect: 'GRANT' })
  const selected = useWatch({ control: assignment.control, name: 'selected' })
  function setSelected(value: string[] | ((current: string[]) => string[])) { assignment.setValue('selected', typeof value === 'function' ? value(assignment.getValues('selected')) : value, { shouldDirty: true, shouldValidate: assignment.formState.isSubmitted }) }
  const effect = useWatch({ control: assignment.control, name: 'effect' })
  function setEffect(value: PermissionEffect) { assignment.setValue('effect', value, { shouldDirty: true, shouldValidate: true }) }


  const [pendingAction, setPendingAction] = useState<{ permission: DirectUserPermission; effect?: PermissionEffect } | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const pending = useRef(false)
  const key = JSON.stringify(query)
  const isSelf = user.uuid === currentUserUuid

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([getUserPermissions(user.uuid, controller.signal), getUserAuthorities(user.uuid, controller.signal)])
      .then(([direct, authorities]) => { if (!controller.signal.aborted) setData({ direct, effective: authorities.effectivePermissions }) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setLoadError(failure instanceof Error ? failure.message : 'Could not load user permissions.') })
    return () => controller.abort()
  }, [user.uuid, retry])
  useEffect(() => {
    if (!canBrowse || isSelf) return
    const controller = new AbortController()
    listPermissions(query, controller.signal).then((value) => { if (!controller.signal.aborted) setCatalogue({ key, data: value }) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setCatalogue({ key, error: failure instanceof Error ? failure.message : 'Could not load permissions.' }) })
    return () => controller.abort()
  }, [canBrowse, isSelf, query, key, retry])

  async function perform(operation: () => Promise<unknown>) {
    if (pending.current) return
    pending.current = true; setBusy(true); setError(null)
    let saved = false
    try {
      await operation(); saved = true
      setPendingAction(null); assignment.reset({ selected: [], expiresAt: '', reason: '', effect: assignment.getValues('effect') })
      onChanged(`Direct permissions for ${user.username} updated successfully.`)
      const [direct, authorities] = await Promise.all([getUserPermissions(user.uuid), getUserAuthorities(user.uuid)])
      setData({ direct, effective: authorities.effectivePermissions })
    } catch (failure) {
      if (saved) { setData(null); setLoadError('Changes were saved, but refreshing failed. Retry loading permissions.') }
      else setError(failure instanceof Error ? failure.message : 'Could not update permissions.')
    } finally { pending.current = false; setBusy(false) }
  }
  const assigned = new Set(data?.direct.filter((permission) => !permission.expired && permission.effect === effect).map((permission) => permission.uuid))
  return <ManagementDialog title={`Permissions: ${user.username}`} description="Direct DENY overrides role-based and direct grants. Changes apply to future sign-ins; existing sessions keep their current permissions." busy={busy} onClose={onClose}>
    {loadError ? <div role="alert" className="space-y-2"><p className="text-sm text-destructive">{loadError}</p><Button variant="outline" onClick={() => { setLoadError(null); setRetry((value) => value + 1) }}>Retry</Button></div>
      : !data ? <p role="status">Loading permissions...</p> : <div className="space-y-6" aria-busy={busy}>
        {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
        {isSelf && <p className="rounded-md bg-muted/40 p-3 text-sm">Another administrator must change your own direct permissions.</p>}
        <section className="space-y-2" aria-label="Effective permissions"><h3 className="font-medium">Effective permissions ({data.effective.length})</h3>
          <div className="flex max-h-28 flex-wrap gap-1 overflow-y-auto">{data.effective.map((name) => <Badge key={name} variant="outline">{name}</Badge>)}
            {!data.effective.length && <p className="text-sm text-muted-foreground">No effective permissions.</p>}</div></section>
        <section className="space-y-3" aria-label="Direct permission assignments"><h3 className="font-medium">Direct assignments ({data.direct.length})</h3>
          {!data.direct.length && <p className="text-sm text-muted-foreground">No direct assignments. Permissions may still be inherited from roles.</p>}
          <ul className="max-h-64 space-y-2 overflow-y-auto">{data.direct.map((permission) => <li key={permission.uuid} className="space-y-3 rounded-md border p-3">
            <div className="flex flex-wrap items-center justify-between gap-2"><div className="space-y-1">
              <div className="flex flex-wrap gap-2"><span className="text-sm font-medium">{permission.name}</span><Badge variant={permission.effect === 'DENY' ? 'destructive' : 'secondary'}>{permission.effect}</Badge>
                {permission.expired && <Badge variant="outline">Expired</Badge>}{!permission.active && <Badge variant="outline">Inactive</Badge>}</div>
              <p className="text-xs text-muted-foreground">{permission.expiresAt ? `Expires ${new Date(permission.expiresAt).toLocaleString()}` : 'Permanent assignment'}</p>
              <p className="text-xs text-muted-foreground">Assigned by {permission.assignedBy}</p></div>
              <div className="flex gap-2"><Button size="sm" variant="outline" disabled={busy || isSelf || !permission.active} onClick={() => setPendingAction({ permission, effect: permission.effect === 'GRANT' ? 'DENY' : 'GRANT' })}>Change to {permission.effect === 'GRANT' ? 'DENY' : 'GRANT'}</Button>
                <Button size="sm" variant="outline" disabled={busy || isSelf} onClick={() => setPendingAction({ permission })}>Revoke</Button></div></div>
            {pendingAction?.permission.uuid === permission.uuid && <div className="space-y-2 border-t pt-3">
              <p className="text-sm">{pendingAction.effect ? `Change ${permission.name} to ${pendingAction.effect}?` : `Revoke the direct ${permission.effect} for ${permission.name}? Role-based access may still apply.`}</p>
              <div className="flex gap-2"><Button size="sm" variant="outline" disabled={busy} onClick={() => setPendingAction(null)}>Cancel</Button>
                <Button size="sm" variant="destructive" disabled={busy} onClick={() => void perform(() => pendingAction.effect
                  ? grantUserPermissions(user.uuid, [permission.uuid], pendingAction.effect, permission.expired ? null : permission.expiresAt, 'Direct permission effect changed from dashboard')
                  : revokeUserPermission(user.uuid, permission.uuid))}>Confirm</Button></div>
            </div>}
          </li>)}</ul></section>
        {!isSelf && (!canBrowse ? <p className="text-sm text-muted-foreground">Browsing permissions requires permission:read access.</p> : <section className="space-y-3 border-t pt-4" aria-label="Assign direct permissions">
          <h3 className="font-medium">Assign direct permissions</h3>
          <div className="space-y-2"><label htmlFor="direct-effect" className="text-sm">Effect</label>
            <select id="direct-effect" value={effect} disabled={busy} className="ml-3 rounded-md border bg-background p-2 text-sm" onChange={(event) => { setEffect(event.target.value as PermissionEffect); setSelected([]) }}>
              <option value="GRANT">GRANT — allow access</option><option value="DENY">DENY — override access</option></select></div>
          <SearchForm label="Search direct permissions" disabled={busy} onSearch={(value) => { setQuery((current) => ({ ...current, page: 0, search: value })) }} />
          {catalogue?.key !== key ? <p role="status" className="text-sm">Loading available permissions...</p>
            : catalogue.error ? <div role="alert" className="space-y-2 text-sm text-destructive">{catalogue.error}<Button variant="outline" onClick={() => setRetry((value) => value + 1)}>Retry</Button></div>
              : catalogue.data && <>
                {!catalogue.data.content.length && <p className="text-sm text-muted-foreground">No matching active permissions.</p>}
                <ul className="max-h-48 space-y-2 overflow-y-auto">{catalogue.data.content.map((permission) => <li key={permission.uuid}><label className="flex items-start gap-3 rounded-md border p-2 text-sm">
                  <input type="checkbox" className="mt-1" checked={assigned.has(permission.uuid) || selected.includes(permission.uuid)} disabled={busy || assigned.has(permission.uuid) || (selected.length >= 100 && !selected.includes(permission.uuid))}
                    onChange={(event) => setSelected((current) => event.target.checked ? [...current, permission.uuid] : current.filter((id) => id !== permission.uuid))} />
                  <span>{permission.name}<span className="block text-xs text-muted-foreground">{assigned.has(permission.uuid) ? `Already has direct ${effect}` : permission.description || `${permission.resource} / ${permission.action}`}</span></span></label></li>)}</ul>
                <div className="flex justify-between gap-2 text-sm"><span>{catalogue.data.page.totalElements} active permissions</span><div className="flex gap-2">
                  <Button size="sm" variant="outline" disabled={busy || !query.page} onClick={() => setQuery((current) => ({ ...current, page: current.page - 1 }))}>Previous</Button>
                  <Button size="sm" variant="outline" disabled={busy || query.page + 1 >= catalogue.data.page.totalPages} onClick={() => setQuery((current) => ({ ...current, page: current.page + 1 }))}>Next</Button></div></div>
              </>}
          <form noValidate className="space-y-3" onSubmit={(event) => void assignment.submit(async (values) => { await perform(() => grantUserPermissions(user.uuid, values.selected, values.effect, toExpiry(values.expiresAt), values.reason || 'Direct permissions assigned from dashboard')) })(event)}>
            <div className="grid gap-3 sm:grid-cols-2"><FormField form={assignment} name="expiresAt" label="Expiration (optional, local time)" type="datetime-local" disabled={busy} /><FormField form={assignment} name="reason" label="Reason (optional)" disabled={busy} /></div>
            <FormError message={assignment.formState.errors.selected?.message} />
            <Button type="submit" variant={effect === 'DENY' ? 'destructive' : 'default'} disabled={busy || assignment.formState.isSubmitting}>{busy ? 'Updating...' : 'Apply ' + effect + ' (' + selected.length + ')'}</Button>
          </form>
        </section>)}
      </div>}
  </ManagementDialog>
}
