import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { FormField, FormError } from '@/components/shared/form-field'
import { ApiError, mutateJson, requestJson } from '@/lib/api-client'
import { getPasswordRequirements } from '@/lib/password-policy'
import type { PasswordRequirements } from '@/lib/password-policy'
import { useAccountLocale } from '@/lib/account-locale'
import { useSchemaForm } from '@/lib/forms'
import { securityMessages } from '../i18n'
import { securitySchema, securityRequirements } from '../schemas'

type SecurityStatus = { hasPassword: boolean; passwordChangedAt: string | null }
export function SecurityPage() {
  const { locale } = useAccountLocale()
  const t = securityMessages[locale]
  const [revision, setRevision] = useState(0)
  const [state, setState] = useState<{ revision: number; status?: SecurityStatus; policy?: PasswordRequirements; error?: 'expired' | 'failed' } | null>(null)
  useEffect(() => {
    const controller = new AbortController()
    Promise.all([requestJson<SecurityStatus>('/dashboard-api/account/security', { signal: controller.signal }), getPasswordRequirements(controller.signal)])
      .then(([status, policy]) => { if (!controller.signal.aborted) setState({ revision, status, policy }) })
      .catch((error: unknown) => { if (!controller.signal.aborted) setState({ revision, error: error instanceof ApiError && error.status === 401 ? 'expired' : 'failed' }) })
    return () => controller.abort()
  }, [revision])
  if (state?.revision !== revision) return <p role="status">{t.loading}</p>
  if (state.error) return <div className="space-y-3"><FormError message={state.error === 'expired' ? t.expired : t.loadFailed} /><Button onClick={() => setRevision((value) => value + 1)}>{t.retry}</Button>{state.error === 'expired' && <a className="ml-4 underline" href="/dashboard/index.html?view=security">{t.signIn}</a>}</div>
  return state.status && state.policy && <section className="max-w-2xl space-y-5" aria-label={t.title}>
    {state.status.hasPassword ? <PasswordForm policy={state.policy} /> : <p className="rounded-xl border p-5">{t.social}</p>}
    {state.status.passwordChangedAt && <p className="text-sm text-muted-foreground">{t.changed}: {new Date(state.status.passwordChangedAt).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB')}</p>}
    <details className="rounded-xl border p-5 text-sm"><summary className="cursor-pointer font-medium">{t.recovery}</summary><p className="mt-3 text-muted-foreground">{t.recoveryHelp}</p><a className="mt-3 inline-block underline" href={`/dashboard/recovery.html?lang=${locale}`}>{t.recovery}</a></details>
  </section>
}
function PasswordForm({ policy }: { policy: PasswordRequirements }) {
  const { locale } = useAccountLocale()
  const t = securityMessages[locale]
  const form = useSchemaForm(securitySchema(policy, locale), { currentPassword: '', newPassword: '', confirmPassword: '' })
  const [show, setShow] = useState(false)
  const [saved, setSaved] = useState(false)
  if (saved) return <div role="status" className="space-y-3 rounded-xl border p-5"><p>{t.success}</p><a className="font-medium underline" href="/dashboard/index.html?view=security">{t.signIn}</a></div>
  return <form noValidate className="space-y-5 rounded-xl border p-5 sm:p-6" onSubmit={form.submit(async (values) => {
    try {
      await mutateJson<void>('/dashboard-api/account/security/password', 'PUT', values)
      form.reset(); setShow(false); setSaved(true)
    } catch (error) {
      if (!(error instanceof ApiError)) throw new Error(t.failed, { cause: error })
      const incorrect = error.message === 'Current password is incorrect'
      const different = error.message === 'New password must differ from the current password'
      const message = error.status === 401 ? t.expired : error.status === 403 ? t.forbidden : incorrect ? t.incorrect : different ? t.different : locale === 'en' ? error.message : t.failed
      throw new ApiError(message, error.status, incorrect ? { currentPassword: message } : different ? { newPassword: message } : {})
    }
  })}>
    <FormError message={form.formState.errors.root?.server?.message} />
    <fieldset disabled={form.formState.isSubmitting} className="space-y-4">
      <FormField form={form} name="currentPassword" label={t.current} type={show ? 'text' : 'password'} autoComplete="current-password" />
      <FormField form={form} name="newPassword" label={t.next} type={show ? 'text' : 'password'} autoComplete="new-password" hint={securityRequirements(policy, locale)} />
      <FormField form={form} name="confirmPassword" label={t.confirm} type={show ? 'text' : 'password'} autoComplete="new-password" />
      <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={show} onChange={(event) => setShow(event.target.checked)} />{t.show}</label>
      <p className="rounded-lg bg-muted/40 p-3 text-sm">{t.warning}</p>
    </fieldset>
    <Button type="submit" disabled={form.formState.isSubmitting}>{form.formState.isSubmitting ? t.saving : t.save}</Button>
  </form>
}
