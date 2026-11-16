import { useEffect, useRef, useState } from 'react'
import { Dialog } from '@base-ui/react/dialog'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { FormError } from '@/components/shared/form-field'
import { ApiError, getDashboardSession, mutateJson, requestJson } from '@/lib/api-client'
import { useAccountLocale } from '@/lib/account-locale'
import { accountsMessages } from '../i18n'

type Identity = { uuid: string; provider: string; email: string | null; displayName: string | null; username: string | null; linkedAt: string; lastLoginAt: string | null }
type Accounts = { hasPassword: boolean; identities: Identity[] }
const providers = ['google', 'github', 'facebook', 'telegram'] as const
const names: Record<string, string> = { google: 'Google', github: 'GitHub', facebook: 'Facebook', telegram: 'Telegram' }
const getAccounts = (signal?: AbortSignal) => requestJson<Accounts>('/dashboard-api/account/identities', { signal })

export function AccountsPage() {
  const { locale } = useAccountLocale()
  const t = accountsMessages[locale]
  const [data, setData] = useState<Accounts | null>(null)
  const [error, setError] = useState<'failed' | 'expired' | null>(null)
  const [notice, setNotice] = useState<'blocked' | 'linked' | 'unlinked' | 'incomplete' | null>(null)
  const [revision, setRevision] = useState(0)
  const [pending, setPending] = useState<string | null>(null)
  const [selected, setSelected] = useState<Identity | null>(null)
  const popup = useRef<Window | null>(null)
  const starting = useRef(false)
  useEffect(() => {
    const controller = new AbortController()
    getAccounts(controller.signal).then((value) => { if (!controller.signal.aborted) { setData(value); setError(null) } })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setError(failure instanceof ApiError && failure.status === 401 ? 'expired' : 'failed') })
    return () => controller.abort()
  }, [revision])
  useEffect(() => () => { popup.current?.close() }, [])
  useEffect(() => {
    if (!pending) return
    const controller = new AbortController()
    const started = Date.now()
    let running = false
    const interval = window.setInterval(async () => {
      if (running) return
      running = true
      try {
        const value = await getAccounts(controller.signal)
        if (controller.signal.aborted) return
        setData(value)
        if (value.identities.some((identity) => identity.provider.toLowerCase() === pending)) {
          setNotice('linked'); setPending(null); popup.current?.close()
        } else if (Date.now() - started > 300000 || popup.current?.closed) {
          setNotice('incomplete'); setPending(null)
        }
      } catch (failure) {
        if (!controller.signal.aborted) { setError(failure instanceof ApiError && failure.status === 401 ? 'expired' : 'failed'); setPending(null) }
      } finally { running = false }
    }, 1500)
    return () => { window.clearInterval(interval); controller.abort() }
  }, [pending])
  async function start(provider: string) {
    if (starting.current || pending) return
    starting.current = true
    setNotice(null)
    // Open synchronously from the click so browser popup blockers can recognize the gesture.
    const target = `iam-link-${crypto.randomUUID()}`
    const child = window.open('about:blank', target, 'popup,width=560,height=720')
    if (!child) { setNotice('blocked'); starting.current = false; return }
    child.opener = null
    popup.current = child
    try {
      const session = await getDashboardSession()
      if (child.closed) { setNotice('incomplete'); return }
      const form = document.createElement('form')
      form.method = 'POST'; form.action = `/account/identities/link/${provider}`; form.target = target
      for (const [name, value] of Object.entries({ _csrf: session.csrfToken, popup: 'true' })) {
        const input = document.createElement('input'); input.type = 'hidden'; input.name = name; input.value = value; form.append(input)
      }
      document.body.append(form)
      try { form.submit() } finally { form.remove() }
      setPending(provider)
    } catch (failure) {
      child.close(); setError(failure instanceof ApiError && failure.status === 401 ? 'expired' : 'failed')
    } finally { starting.current = false }
  }
  if (!data && !error) return <p role="status">{t.loading}</p>
  return <section className="space-y-5" aria-label={t.title}>
    {error && <div className="space-y-2"><FormError message={t[error === 'expired' ? 'expired' : 'failed']} />{error === 'expired' && <a className="underline" href="/dashboard/index.html?view=accounts">{t.signIn}</a>}</div>}
    {notice && <p role="status" className="rounded-lg border p-4 text-sm">{t[notice]}</p>}
    <div className="flex flex-wrap items-center justify-between gap-3"><p className="text-sm text-muted-foreground">{data?.hasPassword ? t.password : t.social}</p><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>{t.reload}</Button></div>
    {pending && <div role="status" className="space-y-3 rounded-lg border p-4 text-sm"><p>{names[pending]}: {t.linking}</p><Button variant="outline" onClick={() => { popup.current?.close(); setPending(null); setNotice('incomplete'); setRevision((value) => value + 1) }}>{t.stop}</Button></div>}
    {data && <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{providers.map((provider) => {
      const identity = data.identities.find((value) => value.provider.toLowerCase() === provider)
      const last = !data.hasPassword && data.identities.length <= 1
      const date = (value: string | null) => value ? new Date(value).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB') : t.never
      return <article key={provider} className="flex min-w-0 flex-col gap-4 rounded-xl border p-5"><div className="flex flex-wrap items-center justify-between gap-2"><h2 className="font-semibold">{names[provider]}</h2><Badge variant="outline">{identity ? t.connected : t.notConnected}</Badge></div>
        {identity ? <><p className="break-words text-sm">{identity.email || identity.username || identity.displayName || names[provider]}</p><dl className="space-y-3 text-sm text-muted-foreground"><div><dt>{t.linkedAt}</dt><dd>{date(identity.linkedAt)}</dd></div><div><dt>{t.lastLogin}</dt><dd>{date(identity.lastLoginAt)}</dd></div></dl>{last && <p className="text-sm">{t.lastMethod}</p>}<Button variant="outline" className="mt-auto" disabled={last || Boolean(pending)} onClick={() => setSelected(identity)}>{t.unlink}</Button></>
          : <Button variant="outline" className="mt-auto" disabled={Boolean(pending) || Boolean(error)} onClick={() => { void start(provider) }}>{t.link}</Button>}
      </article>
    })}</div>}
    {selected && <UnlinkDialog identity={selected} onClose={() => setSelected(null)} onRemoved={() => { setSelected(null); setNotice('unlinked'); setRevision((value) => value + 1) }} />}
  </section>
}
function UnlinkDialog({ identity, onClose, onRemoved }: { identity: Identity; onClose: () => void; onRemoved: () => void }) {
  const { locale } = useAccountLocale()
  const t = accountsMessages[locale]
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const guard = useRef(false)
  async function remove() {
    if (guard.current) return
    guard.current = true; setBusy(true); setError(null)
    try { await mutateJson<void>(`/dashboard-api/account/identities/${identity.uuid}`, 'DELETE'); onRemoved() }
    catch (failure) { setError(failure instanceof ApiError && failure.message === 'Cannot unlink the last sign-in method' ? t.lastMethod : failure instanceof ApiError && failure.status === 401 ? t.expired : failure instanceof ApiError && failure.status === 403 ? t.forbidden : t.unlinkFailed) }
    finally { guard.current = false; setBusy(false) }
  }
  return <Dialog.Root open onOpenChange={(open) => { if (!open && !busy) onClose() }}><Dialog.Portal><Dialog.Backdrop className="fixed inset-0 z-50 bg-black/50" /><Dialog.Popup className="fixed top-1/2 left-1/2 z-50 w-[calc(100%-2rem)] max-w-md -translate-x-1/2 -translate-y-1/2 space-y-5 rounded-xl border bg-background p-6 shadow-xl"><Dialog.Title className="text-lg font-semibold">{t.confirmTitle}</Dialog.Title><Dialog.Description className="text-sm text-muted-foreground">{t.confirm.replace('{provider}', names[identity.provider.toLowerCase()] || identity.provider)}</Dialog.Description><FormError message={error || undefined} /><div className="flex justify-end gap-2"><Button variant="outline" disabled={busy} onClick={onClose}>{t.cancel}</Button><Button variant="destructive" disabled={busy} onClick={() => { void remove() }}>{busy ? t.removing : t.unlink}</Button></div></Dialog.Popup></Dialog.Portal></Dialog.Root>
}
