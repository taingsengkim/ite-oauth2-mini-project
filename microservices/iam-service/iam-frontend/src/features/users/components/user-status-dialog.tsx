import { useRef, useState } from 'react'
import { Dialog } from '@base-ui/react/dialog'
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
    description: 'Block new sign-ins for this account. Existing sessions are not terminated by this action.',
  },
  lock: {
    label: 'Lock', result: 'locked',
    description: 'Prevent new sign-ins until an administrator unlocks this account. Existing sessions are not terminated by this action.',
  },
  unlock: {
    label: 'Unlock', result: 'unlocked',
    description: 'Remove the account lock and reset failed login attempts. A deactivated account must also be activated to sign in.',
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
    <Dialog.Root open onOpenChange={(open) => { if (!open && !inFlight.current) onClose() }}>
      <Dialog.Portal>
        <Dialog.Backdrop className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" />
        <Dialog.Popup initialFocus={cancelButton} className="fixed top-1/2 left-1/2 z-50 max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] max-w-md -translate-x-1/2 -translate-y-1/2 overflow-y-auto rounded-xl border bg-background p-6 text-foreground shadow-xl">
          <Dialog.Title className="text-lg font-semibold">{detail.label} account?</Dialog.Title>
          <Dialog.Description className="mt-2 text-sm text-muted-foreground">{detail.description}</Dialog.Description>
          <div className="my-5 rounded-lg bg-muted/40 p-3 text-sm">
            <p className="font-medium">{user.name || user.username}</p>
            <p className="break-words text-muted-foreground">{user.username} · {user.email}</p>
          </div>
          {error && <p role="alert" className="mb-4 text-sm text-destructive">{error}</p>}
          <div className="flex justify-end gap-2" aria-busy={busy}>
            <Dialog.Close disabled={busy} render={<Button ref={cancelButton} type="button" variant="outline" />}>Cancel</Dialog.Close>
            <Button type="button" disabled={busy} variant={restrictive ? 'destructive' : 'default'}
              onClick={() => void confirm()}>{busy ? 'Updating...' : `${detail.label} account`}</Button>
          </div>
        </Dialog.Popup>
      </Dialog.Portal>
    </Dialog.Root>
  )
}
