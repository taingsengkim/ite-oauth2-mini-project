import { useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Dialog } from '@base-ui/react/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { ApiError } from '@/lib/api-client'
import { resetUserPassword } from '../api/users-api'
import type { User } from '../types/types'

type ResetPasswordDialogProps = {
  user: User
  onClose: () => void
  onReset: (user: User) => void
}

export function ResetPasswordDialog({ user, onClose, onReset }: ResetPasswordDialogProps) {
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPasswords, setShowPasswords] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const inFlight = useRef(false)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (inFlight.current) return
    const invalid: Record<string, string> = {}
    if (!newPassword.trim()) invalid.newPassword = 'New password is required.'
    if (!confirmPassword.trim()) invalid.confirmPassword = 'Please confirm the new password.'
    if (newPassword !== confirmPassword) invalid.confirmPassword = 'Passwords do not match.'
    if (new TextEncoder().encode(newPassword).length > 72) invalid.newPassword = 'Password must not exceed 72 UTF-8 bytes.'
    if (Object.keys(invalid).length) {
      setErrors(invalid)
      setError('Please correct the highlighted fields.')
      return
    }
    inFlight.current = true
    setBusy(true)
    setError(null)
    setErrors({})
    try {
      await resetUserPassword(user.uuid, newPassword, confirmPassword)
      setNewPassword('')
      setConfirmPassword('')
      onReset(user)
    } catch (failure) {
      if (failure instanceof ApiError) setErrors(failure.fieldErrors)
      setError(failure instanceof Error ? failure.message : 'Could not reset password.')
    } finally {
      inFlight.current = false
      setBusy(false)
    }
  }

  return (
    <Dialog.Root open onOpenChange={(open) => { if (!open && !inFlight.current) onClose() }}>
      <Dialog.Portal>
        <Dialog.Backdrop className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" />
        <Dialog.Popup className="fixed top-1/2 left-1/2 z-50 max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] max-w-md -translate-x-1/2 -translate-y-1/2 overflow-y-auto rounded-xl border bg-background p-6 text-foreground shadow-xl">
          <Dialog.Title className="text-lg font-semibold">Reset password</Dialog.Title>
          <Dialog.Description className="mt-2 text-sm text-muted-foreground">
            Set a new IAM password for this user. Linked social sign-ins remain available.
            Existing sessions are not terminated by this action.
          </Dialog.Description>
          <div className="my-5 rounded-lg bg-muted/40 p-3 text-sm">
            <p className="font-medium">{user.name || user.username}</p>
            <p className="break-words text-muted-foreground">{user.username} · {user.email}</p>
          </div>
          <form onSubmit={(event) => void submit(event)} className="space-y-4" aria-busy={busy}>
            {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
            <fieldset disabled={busy} className="space-y-4">
              <legend className="sr-only">New account password</legend>
              <div className="space-y-2">
                <label htmlFor="reset-new-password" className="text-sm font-medium">New password *</label>
                <Input id="reset-new-password" name="newPassword" type={showPasswords ? 'text' : 'password'}
                  autoComplete="new-password" required maxLength={72} value={newPassword}
                  onChange={(event) => { setNewPassword(event.target.value); setErrors({}); setError(null) }}
                  aria-invalid={Boolean(errors.newPassword)} aria-describedby="reset-password-help" />
                <p id="reset-password-help" className={`text-xs ${errors.newPassword ? 'text-destructive' : 'text-muted-foreground'}`}>
                  {errors.newPassword || 'Maximum 72 UTF-8 bytes.'}
                </p>
              </div>
              <div className="space-y-2">
                <label htmlFor="reset-confirm-password" className="text-sm font-medium">Confirm password *</label>
                <Input id="reset-confirm-password" name="confirmPassword" type={showPasswords ? 'text' : 'password'}
                  autoComplete="new-password" required maxLength={72} value={confirmPassword}
                  onChange={(event) => { setConfirmPassword(event.target.value); setErrors({}); setError(null) }}
                  aria-invalid={Boolean(errors.confirmPassword)}
                  aria-describedby={errors.confirmPassword ? 'reset-confirm-error' : undefined} />
                {errors.confirmPassword && <p id="reset-confirm-error" className="text-xs text-destructive">{errors.confirmPassword}</p>}
              </div>
              <label className="flex items-center gap-2 text-sm">
                <input type="checkbox" checked={showPasswords} onChange={(event) => setShowPasswords(event.target.checked)} />
                Show passwords
              </label>
            </fieldset>
            <div className="flex justify-end gap-2 border-t pt-4">
              <Dialog.Close disabled={busy} render={<Button type="button" variant="outline" />}>Cancel</Dialog.Close>
              <Button type="submit" disabled={busy}>{busy ? 'Resetting...' : 'Reset password'}</Button>
            </div>
          </form>
        </Dialog.Popup>
      </Dialog.Portal>
    </Dialog.Root>
  )
}
