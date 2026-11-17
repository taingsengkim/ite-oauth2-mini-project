import { rememberSave, clearRecentSave } from '@/lib/recent-save'
import { useEffect, useState } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { ManagementPageContext } from '@/lib/management-presentation'
import type { DashboardSession } from '@/lib/api-client'
import { userActionRoutes } from '@/features/users/user-action-routes'
import type { UserAction } from '@/features/users/user-action-routes'
import { getUser } from '@/features/users/api/users-api'
import type { UserDetails, UserStatusAction } from '@/features/users/types/types'
import { CreateUserDialog } from '@/features/users/components/create-user-dialog'
import { UserRolesDialog } from '@/features/users/components/user-roles-dialog'
import { UserPermissionsDialog } from '@/features/users/components/user-permissions-dialog'
import { UserSessionsDialog } from '@/features/users/components/user-sessions-dialog'
import { LoginHistoryDialog } from '@/features/users/components/login-history-dialog'
import { ResetPasswordDialog } from '@/features/users/components/reset-password-dialog'
import { UserStatusDialog } from '@/features/users/components/user-status-dialog'
import { DeleteUserDialog } from '@/features/users/components/delete-user-dialog'

export function UserActionPage({ action, session }: { action: UserAction; session: DashboardSession }) {
  const { id = '' } = useParams()
  const authority = action === 'new' ? 'user:create' : action === 'all-history' ? 'user:read' : userActionRoutes.find(route => route.action === action)!.authority
  if (!session.authorities.includes(authority)) return <Navigate replace to="/users" />
  return <UserActionContent key={id + action} action={action} session={session} id={id} />
}

function UserActionContent({ action, session, id }: { action: UserAction; session: DashboardSession; id: string }) {
  const navigate = useNavigate()
  const [retry, setRetry] = useState(0)
  const [result, setResult] = useState<{ user?: UserDetails; error?: string } | null>(null)
  const global = action === 'new' || action === 'all-history'
  const details = '/users/' + encodeURIComponent(id)
  const close = () => { void navigate(global ? '/users' : details) }
  const done = (message: string, deleted = false) => { if (deleted) clearRecentSave('users', id); else rememberSave('users', id, message); void navigate(deleted ? '/users' : details, { replace: true, state: { notice: message } }) }
  useEffect(() => {
    if (global) return
    const controller = new AbortController()
    getUser(id, controller.signal).then(user => { if (!controller.signal.aborted) setResult({ user }) }).catch((error: unknown) => { if (!controller.signal.aborted) setResult({ error: error instanceof Error ? error.message : 'Could not load user.' }) })
    return () => controller.abort()
  }, [global, id, retry])
  const user = result?.user
  const selfRestricted = user?.uuid === session.userUuid && ['disable', 'lock', 'delete'].includes(action)
  return <div className="space-y-5">
    <div className="flex flex-wrap items-center gap-3"><Button variant="outline" render={<Link to="/users" />}><ArrowLeft className="size-4" aria-hidden="true" />Back to users</Button>{!global && <Button variant="ghost" render={<Link to={details} />}>User details</Button>}</div>
    <ManagementPageContext value={true}>
      {action === 'new' ? <CreateUserDialog onClose={close} onCreated={created => { rememberSave('users', created.uuid, 'User created successfully.'); void navigate('/users/' + encodeURIComponent(created.uuid), { replace: true, state: { notice: 'User created successfully.' } }) }} />
        : action === 'all-history' ? <LoginHistoryDialog onClose={close} />
        : result?.error ? <div role="alert" className="space-y-3 rounded-xl border p-5"><p className="text-sm text-destructive">{result.error}</p><Button variant="outline" onClick={() => { setResult(null); setRetry(value => value + 1) }}>Retry</Button></div>
        : !user ? <p role="status">Loading user...</p>
        : selfRestricted ? <p className="rounded-xl border p-5 text-sm">You cannot deactivate, lock, or delete your own account.</p>
        : <>
          {action === 'roles' && <UserRolesDialog user={user} canBrowse={session.authorities.includes('role:read')} currentUserUuid={session.userUuid} onClose={close} onChanged={message => done(message)} />}
          {action === 'permissions' && <UserPermissionsDialog user={user} canBrowse={session.authorities.includes('permission:read')} currentUserUuid={session.userUuid} onClose={close} onChanged={message => done(message)} />}
          {action === 'sessions' && <UserSessionsDialog user={user} canTerminate={session.authorities.includes('user:update')} onClose={close} />}
          {action === 'login-history' && <LoginHistoryDialog user={user} onClose={close} />}
          {action === 'reset-password' && <ResetPasswordDialog user={user} onClose={close} onReset={() => done('Password reset successfully.')} />}
          {action === 'delete' && <DeleteUserDialog user={user} onClose={close} onDeleted={() => done('User deleted successfully.', true)} />}
          {(['enable','disable','lock','unlock'] as string[]).includes(action) && <UserStatusDialog user={user} action={action as UserStatusAction} onClose={close} onChanged={message => done(message)} />}
        </>}
    </ManagementPageContext>
  </div>
}
