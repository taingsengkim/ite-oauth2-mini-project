import { FormField, FormError } from '@/components/shared/form-field'
import { Button } from '@/components/ui/button'
import { useSchemaForm } from '@/lib/forms'
import { roleSchema } from '@/lib/form-schemas'
import { createRole, updateRole } from '../api/roles-api'
import type { Role, RoleSummary } from '../types'
import { RoleModal } from './role-modal'
export function RoleFormDialog({ role, onClose, onSaved }: { role?: RoleSummary; onClose: () => void; onSaved: (role: Role) => void }) {
  const form = useSchemaForm(roleSchema, { name: role?.name || '', description: role?.description || '' })
  const busy = form.formState.isSubmitting
  return <RoleModal title={role ? 'Edit role' : 'Create role'} description="Give this role a name and description. Permissions can be assigned from its details." busy={busy} onClose={onClose}>
    <form noValidate className="space-y-4" onSubmit={form.submit(async (values) => onSaved(role ? await updateRole(role.uuid, values) : await createRole(values)))}>
      <FormError message={form.formState.errors.root?.server?.message} /><FormField form={form} name="name" label="Name *" disabled={busy} /><FormField form={form} name="description" label="Description" as="textarea" disabled={busy} />
      <div className="flex justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button><Button type="submit" disabled={busy}>{busy ? 'Saving...' : role ? 'Save changes' : 'Create role'}</Button></div>
    </form>
  </RoleModal>
}
