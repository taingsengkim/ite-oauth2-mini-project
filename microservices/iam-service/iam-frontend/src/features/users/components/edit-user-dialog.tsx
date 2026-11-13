import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Dialog } from '@base-ui/react/dialog'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { ApiError } from '@/lib/api-client'
import { getUser, updateUser } from '../api/users-api'
import type { UpdateUserInput, UserDetails } from '../types/types'

type EditUserDialogProps = {
  uuid: string
  onClose: () => void
  onSaved: (user: UserDetails) => void
}

const fields = [
  { name: 'givenName', label: 'Given name', required: true, max: 100, type: 'text' },
  { name: 'familyName', label: 'Family name', required: true, max: 100, type: 'text' },
  { name: 'nickname', label: 'Nickname', required: false, max: 100, type: 'text' },
  { name: 'phoneNumber', label: 'Phone number', required: false, max: 16, type: 'tel' },
] as const

export function EditUserDialog({ uuid, onClose, onSaved }: EditUserDialogProps) {
  const [user, setUser] = useState<UserDetails | null>(null)
  const [values, setValues] = useState<UpdateUserInput | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [retryKey, setRetryKey] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [saving, setSaving] = useState(false)
  const inFlight = useRef(false)

  useEffect(() => {
    const controller = new AbortController()
    getUser(uuid, controller.signal).then((result) => {
      if (controller.signal.aborted) return
      setUser(result)
      setValues({
        givenName: result.givenName, familyName: result.familyName,
        nickname: result.nickname || '', phoneNumber: result.phoneNumber || '',
        locale: result.locale === 'km' ? 'km' : 'en',
      })
    }).catch((failure: unknown) => {
      if (!controller.signal.aborted) {
        setLoadError(failure instanceof Error ? failure.message : 'Could not load user.')
      }
    })
    return () => controller.abort()
  }, [uuid, retryKey])

  function change(name: keyof UpdateUserInput, value: string) {
    setValues((current) => current ? { ...current, [name]: value } : current)
    setErrors((current) => ({ ...current, [name]: '' }))
    setError(null)
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!values || inFlight.current) return
    const invalid: Record<string, string> = {}
    if (!values.givenName.trim()) invalid.givenName = 'Given name is required.'
    if (!values.familyName.trim()) invalid.familyName = 'Family name is required.'
    if (Object.keys(invalid).length) {
      setErrors(invalid)
      setError('Please correct the highlighted fields.')
      return
    }

    inFlight.current = true
    setSaving(true)
    setError(null)
    setErrors({})
    try {
      const updated = await updateUser(uuid, {
        ...values, givenName: values.givenName.trim(), familyName: values.familyName.trim(),
        nickname: values.nickname.trim(), phoneNumber: values.phoneNumber.trim(),
      })
      onSaved(updated)
    } catch (failure) {
      if (failure instanceof ApiError) setErrors(failure.fieldErrors)
      setError(failure instanceof Error ? failure.message : 'Could not save user.')
    } finally {
      inFlight.current = false
      setSaving(false)
    }
  }

  return (
    <Dialog.Root open onOpenChange={(open) => { if (!open && !inFlight.current) onClose() }}>
      <Dialog.Portal>
        <Dialog.Backdrop className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" />
        <Dialog.Popup className="fixed top-1/2 left-1/2 z-50 max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] max-w-xl -translate-x-1/2 -translate-y-1/2 overflow-y-auto rounded-xl border bg-background p-6 text-foreground shadow-xl">
          <div className="mb-6 flex items-start justify-between gap-4">
            <div className="space-y-2">
              <Dialog.Title className="text-lg font-semibold">Edit user</Dialog.Title>
              <Dialog.Description className="text-sm text-muted-foreground">
                Update profile and contact details. Required fields are marked with *.
              </Dialog.Description>
            </div>
            <Dialog.Close disabled={saving} render={<Button type="button" variant="outline" />}>Close</Dialog.Close>
          </div>

          {loadError ? (
            <div role="alert" className="space-y-3">
              <p className="text-sm text-destructive">{loadError}</p>
              <Button variant="outline" onClick={() => { setLoadError(null); setRetryKey((key) => key + 1) }}>Retry</Button>
            </div>
          ) : !values || !user ? (
            <p role="status" className="py-8 text-sm text-muted-foreground">Loading user...</p>
          ) : (
            <form onSubmit={(event) => void submit(event)} className="space-y-5" aria-busy={saving}>
              <div className="rounded-lg bg-muted/40 p-3 text-sm">
                <p className="font-medium">{user.username}</p>
                <p className="break-words text-muted-foreground">{user.email}</p>
              </div>
              {error && <p role="alert" className="rounded-md border border-destructive/30 p-3 text-sm text-destructive">{error}</p>}

              <fieldset disabled={saving} className="grid gap-4 sm:grid-cols-2">
                <legend className="sr-only">User profile</legend>
                {fields.map((field) => (
                  <div key={field.name} className="space-y-2">
                    <label htmlFor={`edit-${field.name}`} className="text-sm font-medium">{field.label}{field.required ? ' *' : ''}</label>
                    <Input id={`edit-${field.name}`} name={field.name} type={field.type}
                      value={values[field.name]} required={field.required} maxLength={field.max} autoComplete="off"
                      pattern={field.name === 'phoneNumber' ? '\\+[1-9][0-9]{1,14}' : undefined}
                      placeholder={field.name === 'phoneNumber' ? '+85512345678' : undefined}
                      onChange={(event) => change(field.name, event.target.value)}
                      aria-invalid={Boolean(errors[field.name])}
                      aria-describedby={errors[field.name] ? `edit-error-${field.name}` : undefined} />
                    {errors[field.name] && <p id={`edit-error-${field.name}`} className="text-xs text-destructive">{errors[field.name]}</p>}
                  </div>
                ))}
                <div className="space-y-2">
                  <label htmlFor="edit-locale" className="text-sm font-medium">Language *</label>
                  <select id="edit-locale" name="locale" value={values.locale}
                    onChange={(event) => change('locale', event.target.value)}
                    className="h-8 w-full rounded-md border bg-background px-2 text-sm">
                    <option value="en">English</option><option value="km">Khmer</option>
                  </select>
                </div>
              </fieldset>
              <div className="flex justify-end gap-2 border-t pt-4">
                <Dialog.Close disabled={saving} render={<Button type="button" variant="outline" />}>Cancel</Dialog.Close>
                <Button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save changes'}</Button>
              </div>
            </form>
          )}
        </Dialog.Popup>
      </Dialog.Portal>
    </Dialog.Root>
  )
}
