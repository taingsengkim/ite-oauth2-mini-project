import { useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { ApiError } from '@/lib/api-client'
import { createRole, updateRole } from '../api/roles-api'
import type { Role, RoleSummary } from '../types'
import { RoleModal } from './role-modal'

export function RoleFormDialog({ role, onClose, onSaved }: { role?: RoleSummary; onClose: () => void; onSaved: (role: Role) => void }) {
  const [name, setName] = useState(role?.name || '')
  const [description, setDescription] = useState(role?.description || '')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const pending = useRef(false)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (pending.current) return
    if (!name.trim()) { setErrors({ name: 'Role name is required.' }); return }
    pending.current = true
    setBusy(true)
    setError(null)
    setErrors({})
    try {
      const input = { name: name.trim(), description: description.trim() }
      onSaved(role ? await updateRole(role.uuid, input) : await createRole(input))
    } catch (failure) {
      if (failure instanceof ApiError) setErrors(failure.fieldErrors)
      setError(failure instanceof Error ? failure.message : 'Could not save role.')
    } finally { pending.current = false; setBusy(false) }
  }

  return <RoleModal title={role ? 'Edit role' : 'Create role'} description="Give this role a name and description. Permissions can be assigned from its details." busy={busy} onClose={onClose}>
    <form onSubmit={(event) => void submit(event)} className="space-y-4" aria-busy={busy}>
      {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
      <div className="space-y-2">
        <label htmlFor="role-name" className="text-sm font-medium">Name *</label>
        <Input id="role-name" required maxLength={50} value={name} disabled={busy}
          onChange={(event) => { setName(event.target.value); setErrors({}); setError(null) }}
          aria-invalid={Boolean(errors.name)} aria-describedby={errors.name ? 'role-name-error' : undefined} />
        {errors.name && <p id="role-name-error" className="text-xs text-destructive">{errors.name}</p>}
      </div>
      <div className="space-y-2">
        <label htmlFor="role-description" className="text-sm font-medium">Description</label>
        <textarea id="role-description" maxLength={255} rows={3} value={description} disabled={busy}
          onChange={(event) => setDescription(event.target.value)} className="w-full rounded-md border bg-background p-2 text-sm"
          aria-invalid={Boolean(errors.description)} aria-describedby={errors.description ? 'role-description-error' : undefined} />
        {errors.description && <p id="role-description-error" className="text-xs text-destructive">{errors.description}</p>}
      </div>
      <div className="flex justify-end gap-2 border-t pt-4">
        <Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button>
        <Button type="submit" disabled={busy}>{busy ? 'Saving...' : role ? 'Save changes' : 'Create role'}</Button>
      </div>
    </form>
  </RoleModal>
}
