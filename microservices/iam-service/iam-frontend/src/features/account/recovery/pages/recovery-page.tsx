import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { FormField, FormError } from '@/components/shared/form-field'
import { useAccountLocale } from '@/lib/account-locale'
import { useSchemaForm } from '@/lib/forms'
import { ApiError } from '@/lib/api-client'
import { securityRequirements } from '../../security/schemas'
import { recoveryMessages } from '../i18n'
import { recoveryEmailSchema, recoveryResetSchema } from '../schemas'
import { recoveryContext, recoveryMutation } from '../api'
import type { RecoveryContext } from '../api'
import { LanguageSwitcher } from '@/components/shared/language-switcher'

export function RecoveryPage({ token }: { token: string }) {
  const { locale, setLocale } = useAccountLocale()
  const t = recoveryMessages[locale]
  const [revision, setRevision] = useState(0)
  const [state, setState] = useState<{ revision: number; context?: RecoveryContext; error?: boolean } | null>(null)
  useEffect(() => {
    const controller = new AbortController()
    recoveryContext(controller.signal).then((context) => { if (!controller.signal.aborted) setState({ revision, context }) })
      .catch(() => { if (!controller.signal.aborted) setState({ revision, error: true }) })
    return () => controller.abort()
  }, [revision])
  return <main className="flex min-h-dvh items-center justify-center bg-muted/30 px-4 py-10"><section className="w-full max-w-lg space-y-6 rounded-2xl border bg-background p-6 shadow-sm sm:p-8">
    <div className="flex flex-wrap items-center justify-between gap-3"><p className="font-semibold">ISTAD IAM</p><LanguageSwitcher value={locale} onChange={setLocale} /></div>
    <div className="space-y-2"><h1 className="text-2xl font-semibold">{token ? t.resetTitle : t.title}</h1><p className="text-sm text-muted-foreground">{t.description}</p></div>
    {state?.revision !== revision ? <p role="status">{t.loading}</p> : state.error ? <div className="space-y-3"><FormError message={t.failed} /><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>{t.retry}</Button></div>
      : !state.context?.enabled ? <p role="status" className="text-sm">{t.unavailable}</p> : token ? /^[A-Za-z0-9_-]{43}$/.test(token) ? <ResetForm context={state.context} token={token} /> : <FormError message={t.invalid} /> : <EmailForm />}
    <div className="flex flex-wrap gap-4 border-t pt-4 text-sm"><a className="underline" href="/login">{t.signIn}</a>{token && <a className="underline" href="/dashboard/recovery.html">{t.newLink}</a>}</div>
  </section></main>
}
function EmailForm() {
  const { locale } = useAccountLocale()
  const t = recoveryMessages[locale]
  const form = useSchemaForm(recoveryEmailSchema(locale), { email: '' })
  const [sent, setSent] = useState(false)
  if (sent) return <div role="status" className="space-y-3 rounded-lg border p-4 text-sm"><p>{t.sent}</p><p className="text-muted-foreground">{t.help}</p></div>
  return <form noValidate className="space-y-4" onSubmit={form.submit(async (values) => {
    try { await recoveryMutation('request', values); form.reset(); setSent(true) }
    catch (error) { throw new Error(error instanceof ApiError && error.status === 503 ? t.unavailable : error instanceof ApiError && error.status === 403 ? t.csrf : t.requestFailed, { cause: error }) }
  })}><FormError message={form.formState.errors.root?.server?.message} /><fieldset disabled={form.formState.isSubmitting}><FormField form={form} name="email" label={t.email} type="email" autoComplete="email" /></fieldset><p className="text-sm text-muted-foreground">{t.help}</p><Button className="w-full" type="submit" disabled={form.formState.isSubmitting}>{form.formState.isSubmitting ? t.sending : t.send}</Button></form>
}
function ResetForm({ token, context }: { token: string; context: RecoveryContext }) {
  const { locale } = useAccountLocale()
  const t = recoveryMessages[locale]
  const form = useSchemaForm(recoveryResetSchema(context.policy, locale), { newPassword: '', confirmPassword: '' })
  const [show, setShow] = useState(false)
  const [done, setDone] = useState(false)
  if (done) return <p role="status" className="rounded-lg border p-4 text-sm">{t.success}</p>
  return <form noValidate className="space-y-4" onSubmit={form.submit(async (values) => {
    try { await recoveryMutation<void>('reset', { ...values, token }); form.reset(); setShow(false); setDone(true) }
    catch (error) {
      const message = error instanceof ApiError && error.message === 'Reset link is invalid or expired' ? t.invalid : error instanceof ApiError && error.message === 'New password must differ from the current password' ? t.different : error instanceof ApiError && error.status === 503 ? t.unavailable : error instanceof ApiError && error.status === 403 ? t.csrf : t.resetFailed
      throw new Error(message, { cause: error })
    }
  })}><FormError message={form.formState.errors.root?.server?.message} /><fieldset disabled={form.formState.isSubmitting} className="space-y-4"><FormField form={form} name="newPassword" label={t.next} type={show ? 'text' : 'password'} autoComplete="new-password" hint={securityRequirements(context.policy, locale)} /><FormField form={form} name="confirmPassword" label={t.confirm} type={show ? 'text' : 'password'} autoComplete="new-password" /><label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={show} onChange={(event) => setShow(event.target.checked)} />{t.show}</label></fieldset><p className="text-sm text-muted-foreground">{t.security}</p><Button className="w-full" type="submit" disabled={form.formState.isSubmitting}>{form.formState.isSubmitting ? t.resetting : t.reset}</Button></form>
}
