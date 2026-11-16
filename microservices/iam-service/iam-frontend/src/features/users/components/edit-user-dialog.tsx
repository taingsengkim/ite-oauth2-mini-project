import { useEffect, useState } from 'react'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { FormField, FormError } from '@/components/shared/form-field'
import { Button } from '@/components/ui/button'
import { useSchemaForm } from '@/lib/forms'
import { editUserSchema } from '@/lib/form-schemas'
import { getUser, updateUser } from '../api/users-api'
import type { UserDetails } from '../types/types'

type Props = { uuid: string; onClose: () => void; onSaved: (user: UserDetails) => void }
export function EditUserDialog({ uuid, onClose, onSaved }: Props) {
  const [user, setUser] = useState<UserDetails | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [retry, setRetry] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    getUser(uuid, controller.signal).then((value) => { if (!controller.signal.aborted) setUser(value) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setError(failure instanceof Error ? failure.message : 'Could not load user.') })
    return () => controller.abort()
  }, [uuid, retry])
  if (!user) return <ManagementDialog title="Edit user" description="Load profile and contact details." onClose={onClose}>
    {error ? <div className="space-y-3"><FormError message={error} /><Button variant="outline" onClick={() => { setError(null); setRetry((value) => value + 1) }}>Retry</Button></div> : <p role="status">Loading user...</p>}
  </ManagementDialog>
  return <EditUserForm user={user} onClose={onClose} onSaved={onSaved} />
}
function EditUserForm({ user, onClose, onSaved }: { user: UserDetails; onClose: () => void; onSaved: Props['onSaved'] }) {
  const form = useSchemaForm(editUserSchema, { givenName: user.givenName, familyName: user.familyName, nickname: user.nickname || '', phoneNumber: user.phoneNumber || '', locale: user.locale === 'km' ? 'km' : 'en' })
  const busy = form.formState.isSubmitting
  return <ManagementDialog title="Edit user" description="Update profile and contact details. Required fields are marked with *." busy={busy} onClose={onClose}>
    <form noValidate className="space-y-5" onSubmit={form.submit(async (values) => onSaved(await updateUser(user.uuid, values)))} aria-busy={busy}>
      <div className="rounded-lg bg-muted/40 p-3 text-sm"><p className="font-medium">{user.username}</p><p className="break-words text-muted-foreground">{user.email}</p></div>
      <FormError message={form.formState.errors.root?.server?.message} />
      <fieldset disabled={busy} className="grid gap-4 sm:grid-cols-2">
        <FormField form={form} name="givenName" label="Given name *" /><FormField form={form} name="familyName" label="Family name *" />
        <FormField form={form} name="nickname" label="Nickname" /><FormField form={form} name="phoneNumber" label="Phone number" type="tel" placeholder="+85512345678" />
        <FormField form={form} name="locale" label="Language *" as="select" options={[{ value: 'en', label: 'English' }, { value: 'km', label: 'Khmer' }]} />
      </fieldset>
      <div className="flex justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button><Button type="submit" disabled={busy}>{busy ? 'Saving...' : 'Save changes'}</Button></div>
    </form>
  </ManagementDialog>
}
