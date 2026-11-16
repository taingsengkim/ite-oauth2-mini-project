import { ManagementDialog } from '@/components/shared/management-dialog'
import { FormField, FormError } from '@/components/shared/form-field'
import { Button } from '@/components/ui/button'
import { useSchemaForm } from '@/lib/forms'
import { confirmationSchema } from '@/lib/form-schemas'
import { deleteUser } from '../api/users-api'
import type { User } from '../types/types'
export function DeleteUserDialog({ user, onClose, onDeleted }: { user: User; onClose: () => void; onDeleted: (user: User) => void }) {
  const form = useSchemaForm(confirmationSchema(user.username), { confirmation: '' })
  const busy = form.formState.isSubmitting
  return <ManagementDialog title="Delete user?" description="This account will be removed from the user list and blocked from new sign-ins. Its record will be retained. Existing browser sessions will be terminated." busy={busy} onClose={onClose}>
    <form noValidate className="space-y-4" onSubmit={form.submit(async () => { await deleteUser(user.uuid); onDeleted(user) })}>
      <FormError message={form.formState.errors.root?.server?.message} /><FormField form={form} name="confirmation" label={'Type ' + user.username + ' to confirm.'} disabled={busy} autoComplete="off" />
      <div className="flex justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button><Button type="submit" variant="destructive" disabled={busy}>{busy ? 'Deleting...' : 'Delete user'}</Button></div>
    </form>
  </ManagementDialog>
}
