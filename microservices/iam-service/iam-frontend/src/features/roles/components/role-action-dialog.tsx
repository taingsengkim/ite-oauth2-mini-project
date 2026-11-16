import { FormField, FormError } from '@/components/shared/form-field'
import { Button } from '@/components/ui/button'
import { useSchemaForm } from '@/lib/forms'
import { confirmationSchema } from '@/lib/form-schemas'
import { changeRoleStatus, deleteRole } from '../api/roles-api'
import type { RoleSummary } from '../types'
import { RoleModal } from './role-modal'
export type RoleAction = 'delete' | 'activate' | 'deactivate'
export function RoleActionDialog({ role, action, onClose, onDone }: { role: RoleSummary; action: RoleAction; onClose: () => void; onDone: (message: string) => void }) {
  const form = useSchemaForm(confirmationSchema(action === 'delete' ? role.name : ''), { confirmation: '' })
  const busy = form.formState.isSubmitting
  const label = action === 'delete' ? 'Delete' : action === 'activate' ? 'Activate' : 'Deactivate'
  return <RoleModal title={label + ' ' + role.name + '?'} description="Changes apply to future sign-ins. Existing sessions are not refreshed automatically. Deletion retains the record." busy={busy} onClose={onClose}>
    <form noValidate className="space-y-4" onSubmit={form.submit(async () => {
      if (action === 'delete') await deleteRole(role.uuid)
      else await changeRoleStatus(role.uuid, action === 'activate')
      onDone('Role ' + role.name + ' updated successfully.')
    })}>
      <FormError message={form.formState.errors.root?.server?.message} />
      {action === 'delete' && <FormField form={form} name="confirmation" label={'Type ' + role.name + ' to confirm.'} disabled={busy} autoComplete="off" />}
      <div className="flex justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button><Button type="submit" variant={action === 'activate' ? 'default' : 'destructive'} disabled={busy}>{busy ? 'Updating...' : label + ' role'}</Button></div>
    </form>
  </RoleModal>
}
