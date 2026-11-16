import { useEffect, useState } from 'react'
import { useWatch } from 'react-hook-form'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { FormField, FormError } from '@/components/shared/form-field'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { useSchemaForm } from '@/lib/forms'
import { permissionSchema, confirmationSchema } from '@/lib/form-schemas'
import { createPermission, updatePermission, deletePermission, changePermissionStatus, getPermission } from '../api/permissions-api'
import type { Permission, PermissionAction } from '../types'

export function PermissionFormDialog({ permission, onClose, onSaved }: { permission?: Permission; onClose: () => void; onSaved: (permission: Permission) => void }) {
  const form = useSchemaForm(permissionSchema, { resource: permission?.resource || '', action: permission?.action || '', description: permission?.description || '' })
  const [resource, action] = useWatch({ control: form.control, name: ['resource', 'action'] })
  const busy = form.formState.isSubmitting
  return <ManagementDialog title={permission ? 'Edit permission' : 'Create permission'} description="Combine a resource and an action, such as article:read." busy={busy} onClose={onClose}>
    <form noValidate className="space-y-4" onSubmit={form.submit(async (values) => onSaved(permission ? await updatePermission(permission.uuid, values) : await createPermission(values)))}>
      <FormError message={form.formState.errors.root?.server?.message} />
      <div className="grid gap-4 sm:grid-cols-2"><FormField form={form} name="resource" label="Resource *" disabled={busy} /><FormField form={form} name="action" label="Action *" disabled={busy} /></div>
      <p className="rounded-md bg-muted/40 p-3 text-sm">Permission name: <strong>{resource?.trim() || 'resource'}:{action?.trim() || 'action'}</strong></p>
      <FormField form={form} name="description" label="Description" as="textarea" disabled={busy} />
      <div className="flex justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button><Button type="submit" disabled={busy}>{busy ? 'Saving...' : permission ? 'Save changes' : 'Create permission'}</Button></div>
    </form>
  </ManagementDialog>
}
export function PermissionActionDialog({ permission, action, onClose, onDone }: { permission: Permission; action: PermissionAction; onClose: () => void; onDone: (message: string) => void }) {
  const form = useSchemaForm(confirmationSchema(action === 'delete' ? permission.name : ''), { confirmation: '' })
  const busy = form.formState.isSubmitting
  const label = action === 'delete' ? 'Delete' : action === 'activate' ? 'Activate' : 'Deactivate'
  return <ManagementDialog title={label + ' ' + permission.name + '?'} description="Changes apply to future sign-ins. Existing sessions keep their current permissions. Deletion retains the record." busy={busy} onClose={onClose}>
    <form noValidate className="space-y-4" onSubmit={form.submit(async () => {
      if (action === 'delete') await deletePermission(permission.uuid)
      else await changePermissionStatus(permission.uuid, action === 'activate')
      onDone('Permission ' + permission.name + ' updated successfully.')
    })}>
      <FormError message={form.formState.errors.root?.server?.message} />
      {action === 'delete' && <FormField form={form} name="confirmation" label={'Type ' + permission.name + ' to confirm.'} disabled={busy} autoComplete="off" />}
      <div className="flex justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button><Button type="submit" variant={action === 'activate' ? 'default' : 'destructive'} disabled={busy}>{busy ? 'Updating...' : label + ' permission'}</Button></div>
    </form>
  </ManagementDialog>
}

export function PermissionDetailsDialog({ uuid, onClose }: { uuid: string; onClose: () => void }) {
  const [permission, setPermission] = useState<Permission | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [retry, setRetry] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    getPermission(uuid, controller.signal).then((value) => { if (!controller.signal.aborted) setPermission(value) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setError(failure instanceof Error ? failure.message : 'Could not load permission.') })
    return () => controller.abort()
  }, [uuid, retry])
  return <ManagementDialog title={permission?.name || 'Permission details'} description="Resource-based access definition." onClose={onClose}>
    {error ? <div role="alert" className="space-y-2"><p className="text-sm text-destructive">{error}</p><Button variant="outline" onClick={() => { setError(null); setRetry((value) => value + 1) }}>Retry</Button></div>
      : !permission ? <p role="status">Loading permission...</p> : <div className="space-y-4">
        <div className="flex gap-2"><Badge variant={permission.active ? 'secondary' : 'outline'}>{permission.active ? 'Active' : 'Inactive'}</Badge><Badge variant="outline">{permission.system ? 'System' : 'Custom'}</Badge></div>
        <p className="text-sm text-muted-foreground">{permission.description || 'No description.'}</p>
        <dl className="grid gap-4 text-sm sm:grid-cols-2">{[
          ['Resource', permission.resource], ['Action', permission.action], ['Created', new Date(permission.createdAt).toLocaleString()], ['Updated', new Date(permission.updatedAt).toLocaleString()],
        ].map(([label, value]) => <div key={label}><dt className="text-muted-foreground">{label}</dt><dd>{value}</dd></div>)}</dl>
        {permission.system && <p className="rounded-md bg-muted/40 p-3 text-sm">System permissions are protected from changes and deletion.</p>}
      </div>}
  </ManagementDialog>
}
