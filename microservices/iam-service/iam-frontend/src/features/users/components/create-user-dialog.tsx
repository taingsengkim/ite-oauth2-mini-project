import { useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Dialog } from '@base-ui/react/dialog'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { ApiError } from '@/lib/api-client'
import { createUser } from '../api/users-api'
import type { UserDetails } from '../types/types'

type CreateUserDialogProps = {
  onClose: () => void
  onCreated: (user: UserDetails) => void
}

const initialValues = {
  username: '', email: '', givenName: '', familyName: '',
  password: '', confirmation: '', phoneNumber: '', locale: 'en',
}

const fields = [
  { name: 'givenName', label: 'Given name', type: 'text', autoComplete: 'given-name', max: 100 },
  { name: 'familyName', label: 'Family name', type: 'text', autoComplete: 'family-name', max: 100 },
  { name: 'username', label: 'Username', type: 'text', autoComplete: 'off', max: 50 },
  { name: 'email', label: 'Email', type: 'email', autoComplete: 'off', max: 255 },
  { name: 'password', label: 'Password', type: 'password', autoComplete: 'new-password', max: 72 },
  { name: 'confirmation', label: 'Confirm password', type: 'password', autoComplete: 'new-password', max: 72 },
] as const

export function CreateUserDialog({ onClose, onCreated }: CreateUserDialogProps) {
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const inFlight = useRef(false)

  function change(name: keyof typeof initialValues, value: string) {
    setValues((current) => ({ ...current, [name]: value }))
    setErrors((current) => ({ ...current, [name]: '', ...(name === 'password' ? { confirmation: '' } : {}) }))
    setError(null)
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (inFlight.current) return
    const invalid: Record<string, string> = {}
    for (const field of fields) {
      if (!values[field.name].trim()) invalid[field.name] = `${field.label} is required.`
    }
    if (values.password !== values.confirmation) invalid.confirmation = 'Passwords do not match.'
    if (new TextEncoder().encode(values.password).length > 72) {
      invalid.password = 'Password must not exceed 72 UTF-8 bytes.'
    }
    if (Object.keys(invalid).length) {
      setErrors(invalid)
      setError('Please correct the highlighted fields.')
      return
    }

    inFlight.current = true
    setSubmitting(true)
    setErrors({})
    setError(null)
    try {
      const user = await createUser({
        username: values.username.trim(), email: values.email.trim().toLowerCase(),
        givenName: values.givenName.trim(), familyName: values.familyName.trim(),
        password: values.password, locale: values.locale === 'km' ? 'km' : 'en',
        phoneNumber: values.phoneNumber.trim() || null,
      })
      onCreated(user)
    } catch (failure) {
      if (failure instanceof ApiError) setErrors(failure.fieldErrors)
      setError(failure instanceof Error ? failure.message : 'Could not create user. Please try again.')
    } finally {
      inFlight.current = false
      setSubmitting(false)
    }
  }

  return (
    <Dialog.Root open onOpenChange={(open) => { if (!open && !inFlight.current) onClose() }}>
      <Dialog.Portal>
        <Dialog.Backdrop className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" />
        <Dialog.Popup className="fixed top-1/2 left-1/2 z-50 max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] max-w-xl -translate-x-1/2 -translate-y-1/2 overflow-y-auto rounded-xl border bg-background p-6 text-foreground shadow-xl">
          <div className="mb-6 space-y-2">
            <Dialog.Title className="text-lg font-semibold">Create user</Dialog.Title>
            <Dialog.Description className="text-sm text-muted-foreground">
              Create an account with the default USER role. Required fields are marked with *.
            </Dialog.Description>
          </div>

          <form onSubmit={(event) => void submit(event)} className="space-y-5" aria-busy={submitting}>
            {error && <p role="alert" className="rounded-md border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{error}</p>}
            <fieldset disabled={submitting} className="grid gap-4 sm:grid-cols-2">
              <legend className="sr-only">New user information</legend>
              {fields.map((field) => (
                <div key={field.name} className="space-y-2">
                  <label htmlFor={`create-${field.name}`} className="text-sm font-medium">{field.label} *</label>
                  <Input
                    id={`create-${field.name}`} name={field.name} type={field.type}
                    autoComplete={field.autoComplete} maxLength={field.max} required
                    value={values[field.name]} onChange={(event) => change(field.name, event.target.value)}
                    aria-invalid={Boolean(errors[field.name])}
                    aria-describedby={errors[field.name] ? `error-${field.name}` : undefined}
                  />
                  {errors[field.name] && <p id={`error-${field.name}`} className="text-xs text-destructive">{errors[field.name]}</p>}
                </div>
              ))}
              <div className="space-y-2">
                <label htmlFor="create-locale" className="text-sm font-medium">Language *</label>
                <select id="create-locale" name="locale" value={values.locale}
                  onChange={(event) => change('locale', event.target.value)}
                  className="h-8 w-full rounded-md border bg-background px-2 text-sm">
                  <option value="en">English</option>
                  <option value="km">Khmer</option>
                </select>
              </div>
              <div className="space-y-2">
                <label htmlFor="create-phone" className="text-sm font-medium">Phone number</label>
                <Input id="create-phone" name="phoneNumber" type="tel" autoComplete="off"
                  placeholder="+85512345678" pattern={'\\+[1-9][0-9]{1,14}'} maxLength={16}
                  value={values.phoneNumber} onChange={(event) => change('phoneNumber', event.target.value)}
                  aria-invalid={Boolean(errors.phoneNumber)} aria-describedby="create-phone-help" />
                <p id="create-phone-help" className={`text-xs ${errors.phoneNumber ? 'text-destructive' : 'text-muted-foreground'}`}>
                  {errors.phoneNumber || 'Optional. Use international format, starting with +.'}
                </p>
              </div>
            </fieldset>
            <div className="flex justify-end gap-2 border-t pt-4">
              <Dialog.Close disabled={submitting} render={<Button type="button" variant="outline" />}>Cancel</Dialog.Close>
              <Button type="submit" disabled={submitting}>{submitting ? 'Creating...' : 'Create user'}</Button>
            </div>
          </form>
        </Dialog.Popup>
      </Dialog.Portal>
    </Dialog.Root>
  )
}
