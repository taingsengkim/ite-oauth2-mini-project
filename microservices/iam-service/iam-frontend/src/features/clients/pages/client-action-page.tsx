import { useEffect, useState } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { ManagementPageContext } from '@/lib/management-presentation'
import type { DashboardSession } from '@/lib/api-client'
import { rememberSave, clearRecentSave } from '@/lib/recent-save'
import { getClient } from '../api/clients-api'
import { ClientActionDialog, ClientFormDialog, ClientSecretDialog } from '../components/client-dialogs'
import type { IssuedClient, ManagedClient } from '../types'

export function ClientActionPage({ action, session }: { action: 'new' | 'rotate' | 'delete'; session: DashboardSession }) {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const [client, setClient] = useState<ManagedClient | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [revision, setRevision] = useState(0)
  const [issued, setIssued] = useState<IssuedClient | null>(null)
  const details = '/clients/' + encodeURIComponent(id)
  const close = () => { void navigate(action === 'new' ? '/clients' : details) }
  const permission = action === 'new' ? 'client:create' : action === 'delete' ? 'client:delete' : 'client:update'
  const allowed = session.authorities.includes(permission)
  useEffect(() => {
    if(action === 'new' || !allowed) return
    const controller = new AbortController()
    getClient(id, controller.signal).then(value => { if(!controller.signal.aborted) setClient(value) }).catch((failure: unknown) => { if(!controller.signal.aborted) setError(failure instanceof Error ? failure.message : 'Could not load client.') })
    return () => controller.abort()
  }, [id, action, revision, allowed])
  if(!allowed) return <Navigate replace to="/clients" />
  function saved(value: IssuedClient) {
    rememberSave('clients',value.client.id,'Client saved successfully.')
    if(value.clientSecret) setIssued(value)
    else void navigate('/clients/' + encodeURIComponent(value.client.id), { replace: true, state: { saved: true } })
  }
  return <div className="space-y-5"><Button variant="outline" render={<Link to={action === 'new' ? '/clients' : details} />}>Back to {action === 'new' ? 'clients' : 'client'}</Button>
    <ManagementPageContext value={true}>
      {issued ? <ClientSecretDialog issued={issued} onClose={() => { void navigate('/clients/' + encodeURIComponent(issued.client.id), { replace: true, state: { saved: true } }) }} />
        : action === 'new' ? <ClientFormDialog onClose={close} onSaved={saved} />
          : error ? <div role="alert" className="space-y-3 rounded-xl border p-5"><p className="text-sm text-destructive">{error}</p><Button variant="outline" onClick={() => { setError(null); setRevision(value => value + 1) }}>Retry</Button></div>
            : !client || client.id !== id ? <p role="status">Loading client…</p>
              : action === 'delete' && client.protectedClient ? <p role="alert">Built-in clients cannot be deleted.</p>
                : action === 'rotate' && !client.authenticationMethods.some(method => ['client_secret_basic','client_secret_post'].includes(method)) ? <p role="alert">This client does not use a rotatable client secret.</p>
                  : <ClientActionDialog client={client} action={action} onClose={close} onDone={value => {
                    if(value) { rememberSave('clients',client.id,'Client secret rotated successfully.'); setIssued(value) }
                    else { clearRecentSave('clients',client.id); void navigate('/clients',{ replace: true, state: { notice: 'Client deleted successfully.' } }) }
                  }} />}
    </ManagementPageContext>
  </div>
}
