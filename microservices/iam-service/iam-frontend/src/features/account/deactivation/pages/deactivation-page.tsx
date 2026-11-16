import { useEffect, useState } from 'react'
import { Dialog } from '@base-ui/react/dialog'
import { Button } from '@/components/ui/button'
import { FormField, FormError } from '@/components/shared/form-field'
import { useAccountLocale } from '@/lib/account-locale'
import { useSchemaForm } from '@/lib/forms'
import { ApiError, mutateJson, requestJson } from '@/lib/api-client'
import { deactivationMessages } from '../i18n'
import { deactivationSchema } from '../schemas'

type Status = { username: string; hasPassword: boolean }
const url = '/dashboard-api/account/deactivation'
export function DeactivationPage() {
  const { locale } = useAccountLocale()
  const t = deactivationMessages[locale]
  const [revision, setRevision] = useState(0)
  const [result, setResult] = useState<{ revision: number; status?: Status; error?: 'failed' | 'expired' } | null>(null)
  const [open, setOpen] = useState(false)
  const [done, setDone] = useState(false)
  useEffect(() => {
    const controller = new AbortController()
    requestJson<Status>(url, { signal: controller.signal }).then((status) => { if (!controller.signal.aborted) setResult({ revision, status }) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setResult({ revision, error: failure instanceof ApiError && failure.status === 401 ? 'expired' : 'failed' }) })
    return () => controller.abort()
  }, [revision])
  if (done) return <div role="status" className="max-w-2xl space-y-3 rounded-xl border p-6"><p>{t.success}</p><p className="text-sm text-muted-foreground">{t.successHelp}</p></div>
  if (result?.revision !== revision) return <p role="status">{t.loading}</p>
  if (result.error) return <div className="space-y-3"><FormError message={t[result.error]} /><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>{t.retry}</Button>{result.error === 'expired' && <a className="ml-4 underline" href="/dashboard/account/deactivation">{t.signIn}</a>}</div>
  return <section className="max-w-2xl space-y-5" aria-label={t.title}><div className="space-y-4 rounded-xl border border-destructive/40 p-6"><h2 className="font-semibold">{t.heading}</h2><ul className="list-disc space-y-3 pl-5 text-sm"><li>{t.consequences}</li><li>{t.retained}</li><li>{t.reactivate}</li><li>{t.tokenHint}</li></ul><p className="text-sm text-muted-foreground">{t.adminHint}</p><Button variant="destructive" onClick={() => setOpen(true)}>{t.deactivate}</Button></div>
    {open && result.status && <DeactivationDialog status={result.status} onClose={() => setOpen(false)} onDone={() => { setOpen(false); setDone(true); setResult(null) }} />}
  </section>
}
function DeactivationDialog({ status, onClose, onDone }: { status: Status; onClose: () => void; onDone: () => void }) {
  const { locale } = useAccountLocale()
  const t = deactivationMessages[locale]
  const form = useSchemaForm(deactivationSchema(status.username, status.hasPassword, locale), { confirmation: '', currentPassword: '', acknowledged: false })
  const busy = form.formState.isSubmitting
  return <Dialog.Root open onOpenChange={(open) => { if (!open && !busy) onClose() }}><Dialog.Portal><Dialog.Backdrop className="fixed inset-0 z-50 bg-black/50" /><Dialog.Popup className="fixed top-1/2 left-1/2 z-50 max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] max-w-md -translate-x-1/2 -translate-y-1/2 space-y-5 overflow-y-auto rounded-xl border bg-background p-6 shadow-xl"><Dialog.Title className="text-lg font-semibold">{t.confirmTitle}</Dialog.Title><Dialog.Description className="text-sm text-muted-foreground">{t.confirmHelp}</Dialog.Description>
    <form noValidate className="space-y-4" onSubmit={form.submit(async (values) => {
      try { await mutateJson<void>(url, 'POST', { ...values, currentPassword: status.hasPassword ? values.currentPassword : null }); form.reset(); onDone() }
      catch (failure) {
        const incorrect = failure instanceof ApiError && failure.message === 'Current password is incorrect'
        const mismatch = failure instanceof ApiError && failure.message === 'Username confirmation does not match'
        const message = failure instanceof ApiError && failure.status === 401 ? t.expired : failure instanceof ApiError && failure.status === 403 ? t.forbidden : incorrect ? t.incorrect : mismatch ? t.match : t.saveFailed
        throw new ApiError(message, failure instanceof ApiError ? failure.status : 0, incorrect ? { currentPassword: message } : mismatch ? { confirmation: message } : {})
      }
    })}><FormError message={form.formState.errors.root?.server?.message} /><fieldset disabled={busy} className="space-y-4"><p className="break-words text-sm font-medium">{status.username}</p><FormField form={form} name="confirmation" label={t.username} autoComplete="off" />{status.hasPassword ? <FormField form={form} name="currentPassword" label={t.password} type="password" autoComplete="current-password" /> : <p className="text-sm text-muted-foreground">{t.social}</p>}<label className="flex items-start gap-2 text-sm"><input type="checkbox" className="mt-1" {...form.register('acknowledged')} aria-invalid={Boolean(form.formState.errors.acknowledged)} />{t.acknowledgement}</label>{form.formState.errors.acknowledged && <p role="alert" className="text-sm text-destructive">{form.formState.errors.acknowledged.message}</p>}</fieldset><div className="flex justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>{t.cancel}</Button><Button type="submit" variant="destructive" disabled={busy}>{busy ? t.busy : t.deactivate}</Button></div></form>
  </Dialog.Popup></Dialog.Portal></Dialog.Root>
}
