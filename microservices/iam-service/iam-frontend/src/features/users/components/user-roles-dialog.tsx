import { assignmentSchema, expirySchema, toExpiry } from '@/lib/form-schemas'
import { useWatch } from 'react-hook-form'
import { useSchemaForm } from '@/lib/forms'
import { FormField, FormError } from '@/components/shared/form-field'
import { SearchForm } from '@/components/shared/search-form'
import { useEffect, useRef, useState } from 'react'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { listRoles } from '@/features/roles/api/roles-api'
import type { Page, RoleSummary } from '@/features/roles/types'
import { getUserRoles, assignUserRoles, changeUserRoleExpiry, revokeUserRole } from '../api/user-roles-api'
import type { AssignedUserRole, User } from '../types/types'


function localTime(value: string | null) {
  if (!value) return ''
  const date = new Date(value)
  return new Date(date.getTime() - date.getTimezoneOffset() * 60_000).toISOString().slice(0, 16)
}

export function UserRolesDialog({ user, canBrowse, currentUserUuid, onClose, onChanged }: {
  user: User; canBrowse: boolean; currentUserUuid: string | null; onClose: () => void; onChanged: (message: string) => void
}) {
  const [assigned, setAssigned] = useState<AssignedUserRole[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [retry, setRetry] = useState(0)
  const [catalogue, setCatalogue] = useState<{ key: string; data?: Page<RoleSummary>; error?: string } | null>(null)
  const [query, setQuery] = useState({ page: 0, search: '', active: 'true' })
  const assignment = useSchemaForm(assignmentSchema, { selected: [], expiresAt: '', reason: '' })
  const selected = useWatch({ control: assignment.control, name: 'selected' })
  function setSelected(value: string[] | ((current: string[]) => string[])) { assignment.setValue('selected', typeof value === 'function' ? value(assignment.getValues('selected')) : value, { shouldDirty: true, shouldValidate: assignment.formState.isSubmitted }) }


  const [removing, setRemoving] = useState<AssignedUserRole | null>(null)
  const [editing, setEditing] = useState<AssignedUserRole | null>(null)
  const expiryForm = useSchemaForm(expirySchema, { expiresAt: '' })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const pending = useRef(false)
  const queryKey = JSON.stringify(query)
  const isSelf = user.uuid === currentUserUuid

  useEffect(() => {
    const controller = new AbortController()
    getUserRoles(user.uuid, controller.signal).then((result) => {
      if (!controller.signal.aborted) setAssigned(result)
    }).catch((failure: unknown) => {
      if (!controller.signal.aborted) setLoadError(failure instanceof Error ? failure.message : 'Could not load assigned roles.')
    })
    return () => controller.abort()
  }, [user.uuid, retry])

  useEffect(() => {
    if (!canBrowse) return
    const controller = new AbortController()
    listRoles(query, controller.signal).then((data) => {
      if (!controller.signal.aborted) setCatalogue({ key: queryKey, data })
    }).catch((failure: unknown) => {
      if (!controller.signal.aborted) setCatalogue({ key: queryKey, error: failure instanceof Error ? failure.message : 'Could not load roles.' })
    })
    return () => controller.abort()
  }, [canBrowse, query, queryKey, retry])

  async function perform(operation: () => Promise<unknown>, message: string) {
    if (pending.current) return
    pending.current = true
    setBusy(true)
    setError(null)
    let saved = false
    try {
      await operation()
      saved = true
      assignment.reset({ selected: [], expiresAt: '', reason: '' })
      setRemoving(null)
      setEditing(null)
      onChanged(message)
      setAssigned(await getUserRoles(user.uuid))
    } catch (failure) {
      if (saved) {
        setAssigned(null)
        setLoadError('Changes were saved, but refreshing failed. Retry loading the role assignments.')
      } else setError(failure instanceof Error ? failure.message : 'Could not update role assignments.')
    } finally { pending.current = false; setBusy(false) }
  }

  const assignedIds = new Set(assigned?.filter((role) => !role.expired).map((role) => role.uuid))
  return <ManagementDialog title={`Manage roles: ${user.username}`} description="Assign roles, change expiration, and revoke access. Changes apply to future sign-ins; existing sessions keep their current permissions." busy={busy} onClose={onClose}>
        {loadError ? <div role="alert" className="space-y-3"><p className="text-sm text-destructive">{loadError}</p>
          <Button variant="outline" onClick={() => { setLoadError(null); setRetry((value) => value + 1) }}>Retry</Button></div>
          : assigned === null ? <p role="status">Loading role assignments...</p> : <div className="space-y-6" aria-busy={busy}>
            {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
            {isSelf && <p className="rounded-md bg-muted/50 p-3 text-sm">Another administrator must change or revoke your existing role assignments.</p>}
            <section aria-label="Assigned user roles" className="space-y-3">
              <h3 className="font-medium">Assigned roles ({assigned.length})</h3>
              {!assigned.length && <p className="text-sm text-muted-foreground">No current role assignments.</p>}
              <ul className="max-h-72 space-y-2 overflow-y-auto">{assigned.map((role) => <li key={role.uuid} className="space-y-3 rounded-md border p-3">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div className="space-y-1"><div className="flex flex-wrap items-center gap-2"><span className="text-sm font-medium">{role.name}</span>
                    {role.system && <Badge variant="outline">System</Badge>}
                    {role.expired && <Badge variant="outline">Expired</Badge>}
                    {!role.active && <Badge variant="outline">Role inactive</Badge>}</div>
                    <p className="text-xs text-muted-foreground">{role.expiresAt ? `Expires ${new Date(role.expiresAt).toLocaleString()}` : 'Permanent assignment'}</p>
                    <p className="text-xs text-muted-foreground">Assigned by {role.assignedBy} · {new Date(role.assignedAt).toLocaleString()}</p>
                  </div>
                  <div className="flex gap-2"><Button size="sm" variant="outline" disabled={busy || isSelf || !role.active}
                    onClick={() => { setError(null); setRemoving(null); setEditing(role); expiryForm.reset({ expiresAt: role.expired ? '' : localTime(role.expiresAt) }) }}>Change expiry</Button>
                    <Button size="sm" variant="outline" disabled={busy || isSelf} onClick={() => { setError(null); setEditing(null); setRemoving(role) }}>Revoke</Button></div>
                </div>
                {editing?.uuid === role.uuid && <div className="space-y-3 border-t pt-3">
                  <FormField form={expiryForm} name="expiresAt" label="Expiration (local time; leave empty for permanent access)" type="datetime-local" disabled={busy} />
                  <div className="flex gap-2"><Button size="sm" variant="outline" disabled={busy} onClick={() => setEditing(null)}>Cancel</Button>
                    <Button size="sm" disabled={busy} onClick={(event) => void expiryForm.submit(async (values) => { await perform(() => changeUserRoleExpiry(user.uuid, role.uuid, toExpiry(values.expiresAt)), `Assignment for ${role.name} updated successfully.`) })(event)}>Save expiry</Button></div>
                </div>}
                {removing?.uuid === role.uuid && <div className="space-y-2 border-t pt-3"><p className="text-sm">Revoke {role.name} from {user.username}?</p>
                  <div className="flex gap-2"><Button size="sm" variant="outline" disabled={busy} onClick={() => setRemoving(null)}>Cancel</Button>
                    <Button size="sm" variant="destructive" disabled={busy} onClick={() => void perform(() => revokeUserRole(user.uuid, role.uuid), `Role ${role.name} revoked successfully.`)}>Confirm revocation</Button></div>
                </div>}
              </li>)}</ul>
            </section>
            {!canBrowse ? <p className="text-sm text-muted-foreground">Browsing additional roles requires role:read access.</p> : <section aria-label="Available user roles" className="space-y-3 border-t pt-4">
              <h3 className="font-medium">Assign additional roles</h3>
              <SearchForm label="Search available roles" disabled={busy} onSearch={(value) => { setQuery({ page: 0, search: value, active: 'true' }) }} />
              {catalogue?.key !== queryKey ? <p role="status" className="text-sm">Loading roles...</p>
                : catalogue.error ? <div role="alert" className="space-y-2 text-sm text-destructive">{catalogue.error}<Button size="sm" variant="outline" onClick={() => setRetry((value) => value + 1)}>Retry</Button></div>
                  : catalogue.data && <>
                    {!catalogue.data.content.length && <p className="text-sm text-muted-foreground">No matching active roles.</p>}
                    <ul className="max-h-48 space-y-2 overflow-y-auto">{catalogue.data.content.map((role) => <li key={role.uuid}><label className="flex items-start gap-3 rounded-md border p-2 text-sm">
                      <input type="checkbox" className="mt-1" checked={assignedIds.has(role.uuid) || selected.includes(role.uuid)}
                        disabled={busy || assignedIds.has(role.uuid) || (selected.length >= 100 && !selected.includes(role.uuid))}
                        onChange={(event) => setSelected((current) => event.target.checked ? [...current, role.uuid] : current.filter((id) => id !== role.uuid))} />
                      <span>{role.name}<span className="block text-xs text-muted-foreground">{assignedIds.has(role.uuid) ? 'Already assigned' : role.description || (role.system ? 'System role' : 'Custom role')}</span></span>
                    </label></li>)}</ul>
                    <div className="flex items-center justify-between gap-3 text-sm"><span>{catalogue.data.page.totalElements} active roles</span><div className="flex gap-2">
                      <Button size="sm" variant="outline" disabled={busy || !query.page} onClick={() => setQuery((current) => ({ ...current, page: current.page - 1 }))}>Previous</Button>
                      <Button size="sm" variant="outline" disabled={busy || query.page + 1 >= catalogue.data.page.totalPages} onClick={() => setQuery((current) => ({ ...current, page: current.page + 1 }))}>Next</Button></div></div>
                  </>}
              <form noValidate className="space-y-3" onSubmit={(event) => void assignment.submit(async (values) => { await perform(() => assignUserRoles(user.uuid, values.selected, toExpiry(values.expiresAt), values.reason || 'Assigned from dashboard'), 'User roles assigned successfully.') })(event)}>
                <div className="grid gap-3 sm:grid-cols-2"><FormField form={assignment} name="expiresAt" label="Expiration (optional, local time)" type="datetime-local" disabled={busy} /><FormField form={assignment} name="reason" label="Reason (optional)" disabled={busy} /></div>
                <FormError message={assignment.formState.errors.selected?.message} />
                <Button type="submit" disabled={busy || assignment.formState.isSubmitting}>{busy ? 'Updating...' : 'Assign selected (' + selected.length + ')'}</Button>
              </form>
            </section>}
          </div>}
  </ManagementDialog>
}
