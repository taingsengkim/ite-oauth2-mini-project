import { ManagementDialog } from '@/components/shared/management-dialog'
import { Badge } from '@/components/ui/badge'
import type { AuditEvent } from '../types'

export function AuditDetailsDialog({ event, label, onClose }: { event: AuditEvent; label: string; onClose: () => void }) {
  const relatedLabel = event.action.startsWith('user.role.') ? 'Role IDs' : event.action.includes('.permission.') ? 'Permission IDs' : event.resource === 'session' ? 'Session IDs' : 'Related IDs'
  return <ManagementDialog title={label} description="Audit event details. Sensitive request and response values are excluded." onClose={onClose}>
    <div className="space-y-5"><Badge variant={event.outcome === 'SUCCESS' ? 'secondary' : 'destructive'}>{event.outcome === 'SUCCESS' ? 'Successful' : 'Failed'}</Badge>
      <dl className="grid gap-4 text-sm sm:grid-cols-2">{[
        ['When', new Date(event.occurredAt).toLocaleString()], ['Actor', event.actor], ['Actor ID', event.actorUuid || 'Not authenticated / system'],
        ['Action', event.action], ['Resource', event.resource], [event.resource === 'session' ? 'Affected user ID' : 'Target ID', event.targetId || 'Unidentified account'],
        ['IP address', event.ipAddress || 'Unavailable'], ['Event ID', event.uuid],
      ].map(([name, value]) => <div key={name}><dt className="text-muted-foreground">{name}</dt><dd className="break-all">{value}</dd></div>)}</dl>
      {event.userAgent && <div className="text-sm"><p className="text-muted-foreground">Browser / device</p><p className="break-words">{event.userAgent}</p></div>}
      {event.relatedIds.length > 0 && <div className="text-sm"><p className="text-muted-foreground">{relatedLabel}</p><ul className="space-y-1">{event.relatedIds.map((id) => <li className="break-all" key={id}>{id}</li>)}</ul></div>}
      {event.submittedFields.length > 0 && <div className="text-sm"><p className="text-muted-foreground">Fields submitted</p><p>{event.submittedFields.join(', ')}</p></div>}
      {(event.action.includes('.assign') || event.action === 'user.role.update') && <div className="text-sm"><p className="text-muted-foreground">Assignment expiration</p><p>{event.expiresAt ? new Date(event.expiresAt).toLocaleString() : 'Permanent'}</p></div>}
      {event.effect && <p className="text-sm">Permission effect: <strong>{event.effect}</strong></p>}
      {event.provider && <p className="text-sm">Sign-in provider: <strong>{event.provider === 'LOCAL' ? 'Password' : event.provider}</strong></p>}
      {event.reason && <p className="text-sm">Result: {event.reason.replaceAll('_', ' ')}</p>}
      {event.action === 'login.failure' && <p className="text-xs text-muted-foreground">For failed sign-ins, the actor is the claimed login or Anonymous; it does not identify an authenticated person.</p>}
    </div>
  </ManagementDialog>
}
