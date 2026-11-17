import { rememberSave } from '@/lib/recent-save'
import { userActionRoutes } from '@/features/users/user-action-routes'
import { useEffect, useState } from 'react'
import { Link, Navigate, useLocation, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Pencil } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { ManagementPageContext } from '@/lib/management-presentation'
import type { DashboardSession } from '@/lib/api-client'
import { EditUserDialog } from '@/features/users/components/edit-user-dialog'
import { UserDetailsPanel } from '@/features/users/components/user-details-panel'
import { RoleFormDialog } from '@/features/roles/components/role-form-dialog'
import { RoleDetailsDialog } from '@/features/roles/components/role-details-dialog'
import { getRole } from '@/features/roles/api/roles-api'
import type { Role } from '@/features/roles/types'
import { ClientFormDialog, ClientDetailsDialog, ClientSecretDialog } from '@/features/clients/components/client-dialogs'
import type { IssuedClient } from '@/features/clients/types'

type Resource = 'users' | 'roles' | 'clients'
export function ManagementResourcePage({ resource, edit = false, session }: { resource: Resource; edit?: boolean; session: DashboardSession }) {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const [notice, setNotice] = useState<string | null>(null)
  const [issued, setIssued] = useState<IssuedClient | null>(null)
  const root = '/' + resource
  const details = root + '/' + encodeURIComponent(id)
  const permission = resource === 'users' ? 'user' : resource === 'roles' ? 'role' : 'client'
  const canEdit = session.authorities.includes(permission + ':update')
  const close = () => { void navigate(edit ? details : root) }
  const saved = () => { rememberSave(resource, id, 'Changes saved successfully.'); void navigate(resource === 'roles' ? root : details, { replace: true, state: { saved: true } }) }
  if (edit && !canEdit) return <Navigate replace to={details} />
  return <div className="space-y-5">
    <div className="flex flex-wrap items-center justify-between gap-3"><Button variant="outline" render={<Link to={root} />}><ArrowLeft className="size-4" aria-hidden="true" />Back to {resource}</Button>{!edit && canEdit && <Button variant="outline" render={<Link to={details + '/edit'} />}><Pencil className="size-4" aria-hidden="true" />Edit {permission}</Button>}</div>
    {(notice || location.state?.notice || location.state?.saved) && <p role="status" className="rounded-lg border bg-muted/40 p-3 text-sm">{notice || location.state?.notice || 'Changes saved successfully.'}</p>}
    {resource === 'clients' && !edit && <nav aria-label="Client actions" className="flex flex-wrap gap-2">{canEdit && <Button variant="outline" render={<Link to={details + '/rotate'} />}>Rotate secret</Button>}{session.authorities.includes('client:delete') && <Button variant="destructive" render={<Link to={details + '/delete'} />}>Delete client</Button>}</nav>}
    {resource === 'users' && !edit && <nav aria-label="User management" className="flex flex-wrap gap-2">{userActionRoutes.filter(route => ['roles','permissions','sessions','login-history','reset-password'].includes(route.action) && session.authorities.includes(route.authority)).map(route => <Button key={route.action} variant="outline" size="sm" render={<Link to={details + '/' + route.action} />}>{route.label}</Button>)}</nav>}
    <ManagementPageContext value={true}>
      {resource === 'users' && (edit ? <EditUserDialog key={id} uuid={id} onClose={close} onSaved={saved} /> : <UserDetailsPanel key={id} uuid={id} onClose={close} />)}
      {resource === 'roles' && (edit ? <RoleEditPage key={id} id={id} onClose={close} onSaved={saved} /> : <RoleDetailsDialog key={id} uuid={id} onClose={close} onChanged={message => { rememberSave('roles', id, message); setNotice(message); void navigate(root) }} access={{ create: session.authorities.includes('role:create'), update: canEdit, delete: session.authorities.includes('role:delete'), assign: session.authorities.includes('role:assign'), browsePermissions: session.authorities.includes('permission:read') }} />)}
      {resource === 'clients' && !issued && (edit ? <ClientFormDialog key={id} id={id} onClose={close} onSaved={value => { if (value.clientSecret) setIssued(value); else saved() }} /> : <ClientDetailsDialog key={id} id={id} onClose={close} />)}
    </ManagementPageContext>
    {issued && <ManagementPageContext value={true}><ClientSecretDialog issued={issued} onClose={() => { setIssued(null); saved() }} /></ManagementPageContext>}
  </div>
}

function RoleEditPage({ id, onClose, onSaved }: { id: string; onClose: () => void; onSaved: () => void }) {
  const [retry, setRetry] = useState(0)
  const [result, setResult] = useState<{ role?: Role; error?: string } | null>(null)
  useEffect(() => {
    const controller = new AbortController()
    getRole(id, controller.signal).then(role => { if (!controller.signal.aborted) setResult({ role }) }).catch((error: unknown) => { if (!controller.signal.aborted) setResult({ error: error instanceof Error ? error.message : 'Could not load role.' }) })
    return () => controller.abort()
  }, [id, retry])
  if (result?.error) return <div role="alert" className="space-y-3 rounded-xl border p-5"><p className="text-sm text-destructive">{result.error}</p><Button variant="outline" onClick={() => { setResult(null); setRetry(value => value + 1) }}>Retry</Button></div>
  if (!result?.role) return <p role="status">Loading role...</p>
  if (result.role.system) return <p className="rounded-xl border p-5 text-sm">System roles are protected and cannot be edited.</p>
  return <RoleFormDialog key={result.role.uuid} role={result.role} onClose={onClose} onSaved={onSaved} />
}
