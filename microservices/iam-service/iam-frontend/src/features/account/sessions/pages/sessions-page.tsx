import { useEffect, useRef, useState } from 'react'
import { Dialog } from '@base-ui/react/dialog'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { FormError } from '@/components/shared/form-field'
import { ApiError, mutateJson, requestJson } from '@/lib/api-client'
import { useAccountLocale } from '@/lib/account-locale'
import { sessionsMessages } from '../i18n'
import { deviceDetails, isLoopback } from '../session-details'
import { locationText } from '@/lib/session-location'
import type { SessionLocation } from '@/lib/session-location'

type Session = { id: string; signedInAt: string; lastActiveAt: string; expiresAt: string | null; ipAddress: string; userAgent: string; current: boolean; location?: SessionLocation }
const url = '/dashboard-api/account/sessions'
export function SessionsPage() {
  const { locale } = useAccountLocale()
  const t = sessionsMessages[locale]
  const [revision, setRevision] = useState(0)
  const [result, setResult] = useState<{ revision: number; sessions?: Session[]; error?: 'failed' | 'expired' } | null>(null)
  const [pending, setPending] = useState<Session | 'all' | null>(null)
  const [notice, setNotice] = useState<'ended' | 'signedOut' | null>(null)
  useEffect(() => {
    const controller = new AbortController()
    requestJson<Session[]>(url, { signal: controller.signal })
      .then((sessions) => { if (!controller.signal.aborted) setResult({ revision, sessions }) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setResult({ revision, error: failure instanceof ApiError && failure.status === 401 ? 'expired' : 'failed' }) })
    return () => controller.abort()
  }, [revision])
  if (notice === 'signedOut') return <div role="status" className="space-y-3 rounded-xl border p-5"><p>{t.signedOut}</p><a className="font-medium underline" href="/dashboard/index.html?view=sessions">{t.signIn}</a></div>
  const loading = result?.revision !== revision
  const sessions = loading ? [] : result.sessions || []
  const date = (value: string | null) => value ? new Date(value).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB') : t.noTimeout
  return <section className="space-y-5" aria-label={t.title}>
    {notice && <p role="status" className="rounded-lg border p-4 text-sm">{t[notice]}</p>}
    <div className="flex flex-wrap gap-3"><Button variant="outline" disabled={loading || Boolean(pending)} onClick={() => setRevision((value) => value + 1)}>{t.refresh}</Button><Button variant="destructive" disabled={loading || Boolean(result?.error) || !sessions.length || Boolean(pending)} onClick={() => setPending('all')}>{t.endAll}</Button></div>
    {loading ? <p role="status">{t.loading}</p> : result.error ? <div className="space-y-3"><FormError message={t[result.error]} />{result.error === 'expired' && <a className="underline" href="/dashboard/index.html?view=sessions">{t.signIn}</a>}</div> : !sessions.length ? <p>{t.empty}</p>
      : <div className="grid gap-4 lg:grid-cols-2">{sessions.map((session) => {
        const details = deviceDetails(session.userAgent)
        const location = locationText(session.location, locale) || (isLoopback(session.ipAddress) ? t.local : session.location?.status === 'PRIVATE' ? t.privateNetwork : t.unavailable)
        return <article key={session.id} className="space-y-4 rounded-xl border p-5"><div className="flex flex-wrap items-center justify-between gap-3"><h2 className="font-medium">{details.browser || t.unknown} · {details.os || t.unknown}</h2>{session.current && <Badge>{t.current}</Badge>}</div>
          <dl className="grid gap-4 text-sm sm:grid-cols-2">{[[t.device, t[details.type]], [t.browser, details.browser || t.unknown], [t.ip, session.ipAddress || t.unknown], [t.location, location], [t.signedIn, date(session.signedInAt)], [t.activity, date(session.lastActiveAt)], [t.expires, date(session.expiresAt)]].map(([label, value]) => <div key={label}><dt className="text-muted-foreground">{label}</dt><dd className="break-words">{value}</dd></div>)}</dl>
          <details className="text-xs text-muted-foreground"><summary className="cursor-pointer">{t.rawAgent}</summary><p className="mt-2 break-words">{session.userAgent}</p></details><Button variant="outline" disabled={Boolean(pending)} onClick={() => setPending(session)}>{t.end}</Button>
        </article>
      })}</div>}
    <p className="text-sm text-muted-foreground">{t.hint}</p><p className="text-sm text-muted-foreground">{t.tokenHint}</p>
    <a className="text-xs text-muted-foreground underline" href="https://db-ip.com" target="_blank" rel="noopener noreferrer">IP Geolocation by DB-IP</a>
    {pending && <EndSessionDialog pending={pending} onClose={() => setPending(null)} onEnded={() => {
      const signsOut = pending === 'all' || pending.current
      setPending(null); setNotice(signsOut ? 'signedOut' : 'ended')
      if (!signsOut) setRevision((value) => value + 1)
    }} />}
  </section>
}
function EndSessionDialog({ pending, onClose, onEnded }: { pending: Session | 'all'; onClose: () => void; onEnded: () => void }) {
  const { locale } = useAccountLocale()
  const t = sessionsMessages[locale]
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const guard = useRef(false)
  async function end() {
    if (guard.current) return
    guard.current = true; setBusy(true); setError(null)
    try { await mutateJson<void>(pending === 'all' ? url : `${url}/${pending.id}`, 'DELETE'); onEnded() }
    catch (failure) { setError(failure instanceof ApiError && failure.status === 401 ? t.expired : failure instanceof ApiError && failure.status === 403 ? t.forbidden : failure instanceof ApiError && failure.status === 404 ? t.missing : t.endFailed) }
    finally { guard.current = false; setBusy(false) }
  }
  return <Dialog.Root open onOpenChange={(open) => { if (!open && !busy) onClose() }}><Dialog.Portal><Dialog.Backdrop className="fixed inset-0 z-50 bg-black/50" /><Dialog.Popup className="fixed top-1/2 left-1/2 z-50 w-[calc(100%-2rem)] max-w-md -translate-x-1/2 -translate-y-1/2 space-y-5 rounded-xl border bg-background p-6 shadow-xl"><Dialog.Title className="text-lg font-semibold">{pending === 'all' ? t.confirmAllTitle : t.confirmTitle}</Dialog.Title><Dialog.Description className="text-sm text-muted-foreground">{pending === 'all' ? t.confirmAll : pending.current ? t.confirmCurrent : t.confirm}</Dialog.Description><FormError message={error || undefined} /><div className="flex justify-end gap-2"><Button variant="outline" disabled={busy} onClick={onClose}>{t.cancel}</Button><Button variant="destructive" disabled={busy} onClick={() => { void end() }}>{busy ? t.ending : t.confirmButton}</Button></div></Dialog.Popup></Dialog.Portal></Dialog.Root>
}
