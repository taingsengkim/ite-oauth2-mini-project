import { useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Dialog } from '@base-ui/react/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { deleteUser } from '../api/users-api'
import type { User } from '../types/types'

type DeleteUserDialogProps = {
  user: User
  onClose: () => void
  onDeleted: (user: User) => void
}

export function DeleteUserDialog({ user, onClose, onDeleted }: DeleteUserDialogProps) {
  const [confirmation, setConfirmation] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const inFlight = useRef(false)
  const cancelButton = useRef<HTMLButtonElement>(null)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (confirmation !== user.username || inFlight.current) return
    inFlight.current = true
    setBusy(true)
    setError(null)
    try {
      await deleteUser(user.uuid)
      onDeleted(user)
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : 'Could not delete user.')
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
          <Dialog.Title className="text-lg font-semibold">Delete user?</Dialog.Title>
          <Dialog.Description className="mt-2 text-sm text-muted-foreground">
            This account will be removed from the user list and blocked from new sign-ins.
            Its record will be retained. Existing sessions are not terminated by this action.
          </Dialog.Description>
          <div className="my-5 rounded-lg bg-muted/40 p-3 text-sm">
            <p className="font-medium">{user.name || user.username}</p>
            <p className="break-words text-muted-foreground">{user.username} · {user.email}</p>
          </div>
          <form onSubmit={(event) => void submit(event)} className="space-y-4" aria-busy={busy}>
            {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
            <div className="space-y-2">
              <label htmlFor="delete-user-confirmation" className="text-sm">
                Type <strong>{user.username}</strong> to confirm.
              </label>
              <Input id="delete-user-confirmation" value={confirmation} disabled={busy} required
                autoComplete="off" spellCheck={false} autoCapitalize="none"
                onChange={(event) => setConfirmation(event.target.value)} />
            </div>
            <div className="flex justify-end gap-2 border-t pt-4">
              <Dialog.Close disabled={busy} render={<Button ref={cancelButton} type="button" variant="outline" />}>Cancel</Dialog.Close>
              <Button type="submit" variant="destructive" disabled={busy || confirmation !== user.username}>
                {busy ? 'Deleting...' : 'Delete user'}
              </Button>
            </div>
          </form>
        </Dialog.Popup>
      </Dialog.Portal>
    </Dialog.Root>
  )
}
