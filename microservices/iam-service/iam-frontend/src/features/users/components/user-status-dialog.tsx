import { useRef, useState } from 'react'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { Button } from '@/components/ui/button'
import { changeUserStatus } from '../api/users-api'
import type { User, UserStatusAction } from '../types/types'

const actions = {
  enable: {
    label: 'Activate', result: 'activated',
    description: 'Allow this account to sign in again. An account that is also locked must be unlocked separately.',
  },
  disable: {
    label: 'Deactivate', result: 'deactivated',
    description: 'Block new sign-ins for this account. Existing browser sessions will be terminated.',
  },
  lock: {
    label: 'Lock', result: 'locked',
    description: 'Prevent new sign-ins until an administrator unlocks this account. Existing browser sessions will be terminated.',
  },
  unlock: {
    label: 'Unlock', result: 'unlocked',
    description: 'Remove the manual account lock and any temporary password lockout, and reset failed login attempts. A deactivated account must also be activated to sign in.',
  },
} as const

type UserStatusDialogProps = {
  user: User
  action: UserStatusAction
  onClose: () => void
  onChanged: (message: string) => void
}

export function UserStatusDialog({ user, action, onClose, onChanged }: UserStatusDialogProps) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const inFlight = useRef(false)
  const cancelButton = useRef<HTMLButtonElement>(null)
  const detail = actions[action]
  const restrictive = action === 'disable' || action === 'lock'

  async function confirm() {
    if (inFlight.current) return
    inFlight.current = true
    setBusy(true)
    setError(null)
    try {
      await changeUserStatus(user.uuid, action)
      onChanged(`Account ${user.username} was ${detail.result} successfully.`)
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Could not update account status.')
    } finally {
      inFlight.current = false
      setBusy(false)
    }
  }

  return (
    <ManagementDialog title={`${detail.label} account?`} description={detail.description} busy={busy} onClose={onClose}>
          <div className="my-5 rounded-lg bg-muted/40 p-3 text-sm">
            <p className="font-medium">{user.name || user.username}</p>
            <p className="break-words text-muted-foreground">{user.username} · {user.email}</p>
          </div>
          {error && <p role="alert" className="mb-4 text-sm text-destructive">{error}</p>}
          <div className="flex justify-end gap-2" aria-busy={busy}>
            <Button ref={cancelButton} type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button>
            <Button type="button" disabled={busy} variant={restrictive ? 'destructive' : 'default'}
              onClick={() => void confirm()}>{busy ? 'Updating...' : `${detail.label} account`}</Button>
          </div>
    </ManagementDialog>
  )
}
