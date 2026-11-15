import { useRef, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { changeRoleStatus, deleteRole } from '../api/roles-api'
import type { RoleSummary } from '../types'
import { RoleModal } from './role-modal'

export type RoleAction = 'delete' | 'activate' | 'deactivate'
export function RoleActionDialog({ role, action, onClose, onDone }: {
  role: RoleSummary; action: RoleAction; onClose: () => void; onDone: (message: string) => void
}) {
  const [confirmation, setConfirmation] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const pending = useRef(false)
  const label = action === 'delete' ? 'Delete' : action === 'activate' ? 'Activate' : 'Deactivate'
  const description = action === 'delete'
    ? 'Remove this role from active management. Its record is retained. Users will no longer receive permissions through this role on their next sign-in.'
    : action === 'deactivate' ? 'Stop this role from granting permissions on future sign-ins. Existing sessions are not refreshed automatically.'
    : 'Allow this role and its active permission assignments to grant access on future sign-ins.'

  async function confirm() {
    if (pending.current || (action === 'delete' && confirmation !== role.name)) return
    pending.current = true
    setBusy(true)
    setError(null)
    try {
      if (action === 'delete') await deleteRole(role.uuid)
      else await changeRoleStatus(role.uuid, action === 'activate')
      onDone(`Role ${role.name} was ${action === 'delete' ? 'deleted' : action === 'activate' ? 'activated' : 'deactivated'} successfully.`)
    } catch (failure) { setError(failure instanceof Error ? failure.message : 'Could not update role.') }
    finally { pending.current = false; setBusy(false) }
  }

  return <RoleModal title={`${label} ${role.name}?`} description={description} busy={busy} onClose={onClose}>
    <div className="space-y-4">
      {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
      {action === 'delete' && <div className="space-y-2">
        <label htmlFor="delete-role-confirm" className="text-sm">Type <strong>{role.name}</strong> to confirm.</label>
        <Input id="delete-role-confirm" value={confirmation} disabled={busy} autoComplete="off" onChange={(event) => setConfirmation(event.target.value)} />
      </div>}
      <div className="flex justify-end gap-2 border-t pt-4">
        <Button variant="outline" disabled={busy} onClick={onClose}>Cancel</Button>
        <Button variant={action === 'activate' ? 'default' : 'destructive'} disabled={busy || (action === 'delete' && confirmation !== role.name)}
          onClick={() => void confirm()}>{busy ? 'Updating...' : `${label} role`}</Button>
      </div>
    </div>
  </RoleModal>
}
