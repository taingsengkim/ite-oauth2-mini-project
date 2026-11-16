import { useEffect, useRef, useState } from 'react'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { mutateJson, requestJson } from '@/lib/api-client'
import type { User } from '../types/types'
import { locationText } from '@/lib/session-location'
import type { SessionLocation } from '@/lib/session-location'

type UserSession = {
  id: string; signedInAt: string; lastActiveAt: string; expiresAt: string | null
  ipAddress: string; userAgent: string; current: boolean
  location?: SessionLocation
}
const formatTime = (value: string | null) => value ? new Date(value).toLocaleString() : 'No timeout'

export function UserSessionsDialog({ user, canTerminate, onClose }: {
  user: User; canTerminate: boolean; onClose: () => void
}) {
  const [sessions, setSessions] = useState<UserSession[]>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const busyRef = useRef(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [revision, setRevision] = useState(0)
  const [pending, setPending] = useState<UserSession | 'all' | null>(null)
  const url = `/dashboard-api/users/${user.uuid}/sessions`

  useEffect(() => {
    const controller = new AbortController()
    requestJson<UserSession[]>(url, { signal: controller.signal })
      .then((data) => { if (!controller.signal.aborted) { setSessions(data); setLoading(false) } })
      .catch((failure: unknown) => {
        if (!controller.signal.aborted) { setError(failure instanceof Error ? failure.message : 'Could not load sessions.'); setLoading(false) }
      })
    return () => controller.abort()
  }, [url, revision])

  function refresh() { setError(null); setLoading(true); setRevision((value) => value + 1) }
  async function terminate() {
    if (!pending || busyRef.current) return
    busyRef.current = true; setBusy(true); setError(null); setSuccess(null)
    const signsOut = pending === 'all' ? sessions.some((session) => session.current) : pending.current
    try {
      await mutateJson<void>(pending === 'all' ? url : `${url}/${pending.id}`, 'DELETE')
      if (signsOut) { window.location.assign('/login'); return }
      setPending(null); setSuccess('Sessions terminated successfully.'); refresh()
    } catch (failure: unknown) {
      setError(failure instanceof Error ? failure.message : 'Could not terminate sessions.')
    } finally { busyRef.current = false; setBusy(false) }
  }

  return <ManagementDialog title={`Sessions: ${user.username}`} description="View active browser sessions and sign out devices. Device information is reported by the browser." busy={busy} onClose={onClose}>
    {error && <p role="alert" className="mb-4 text-sm text-destructive">{error}</p>}
    {success && <p role="status" className="mb-4 text-sm">{success}</p>}
    <div className="mb-4 flex justify-between gap-2">
      <Button variant="outline" disabled={busy || loading} onClick={refresh}>Refresh</Button>
      {canTerminate && <Button variant="destructive" disabled={busy || loading || sessions.length === 0} onClick={() => setPending('all')}>Terminate all sessions</Button>}
    </div>
    {pending && <div className="mb-4 space-y-3 rounded-lg border p-4" role="alert">
      <p className="text-sm">{pending === 'all' ? 'Sign this user out of every browser session?' : 'Sign this device out?'} {(pending === 'all' ? sessions.some((session) => session.current) : pending.current) && 'This will also sign you out.'}</p>
      <div className="flex gap-2"><Button variant="destructive" disabled={busy} onClick={() => void terminate()}>{busy ? 'Terminating…' : 'Confirm termination'}</Button><Button variant="outline" disabled={busy} onClick={() => setPending(null)}>Cancel</Button></div>
    </div>}
    {loading ? <p role="status">Loading sessions…</p> : error ? <p className="text-sm text-muted-foreground">Use Refresh to try again.</p> : sessions.length === 0 ? <p className="text-sm text-muted-foreground">No active browser sessions.</p> : <div className="space-y-3">{sessions.map((session) => <article key={session.id} className="space-y-3 rounded-lg border p-4">
      <div className="flex items-start justify-between gap-3"><p className="min-w-0 break-words text-sm font-medium">{session.userAgent}</p>{session.current && <Badge>Current</Badge>}</div>
      <dl className="grid gap-2 text-sm sm:grid-cols-2"><div><dt className="text-muted-foreground">IP address</dt><dd>{session.ipAddress}</dd></div><div><dt className="text-muted-foreground">Signed in</dt><dd>{formatTime(session.signedInAt)}</dd></div><div><dt className="text-muted-foreground">Last activity</dt><dd>{formatTime(session.lastActiveAt)}</dd></div><div><dt className="text-muted-foreground">Expires</dt><dd>{formatTime(session.expiresAt)}</dd></div></dl>
      {canTerminate && <Button variant="outline" size="sm" disabled={busy} onClick={() => setPending(session)}>Terminate session</Button>}
    </article>)}</div>}
    <p className="mt-4 text-xs text-muted-foreground">Session termination signs out browser sessions. Issued API access tokens remain valid until they expire.</p>
    <div className="mt-3 space-y-2 text-xs text-muted-foreground">{sessions.map((session) => <p key={session.id}>{session.ipAddress}: {locationText(session.location, 'en') || (session.location?.status === 'LOCAL' ? 'Local device' : session.location?.status === 'PRIVATE' ? 'Private or reserved network' : 'Location unavailable')}</p>)}<p>IP locations are approximate. <a className="underline" href="https://db-ip.com" target="_blank" rel="noopener noreferrer">IP Geolocation by DB-IP</a></p></div>
  </ManagementDialog>
}
