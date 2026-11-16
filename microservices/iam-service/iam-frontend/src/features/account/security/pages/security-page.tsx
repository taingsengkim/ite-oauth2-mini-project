import { KeyRound, ShieldCheck, Clock3, ArrowRight, Info, Eye, EyeOff, CheckCircle2 } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { FormField, FormError } from '@/components/shared/form-field'
import { ApiError, mutateJson, requestJson } from '@/lib/api-client'
import { getPasswordRequirements } from '@/lib/password-policy'
import type { PasswordRequirements } from '@/lib/password-policy'
import { useAccountLocale } from '@/lib/account-locale'
import { useSchemaForm } from '@/lib/forms'
import { securityMessages } from '../i18n'
import { securitySchema } from '../schemas'

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
  if (state.error) return <div className="space-y-3"><FormError message={state.error === 'expired' ? t.expired : t.loadFailed} /><Button onClick={() => setRevision((value) => value + 1)}>{t.retry}</Button>{state.error === 'expired' && <a className="ml-4 underline" href="/dashboard/account/security">{t.signIn}</a>}</div>
  return state.status && state.policy && <section className="space-y-6" aria-label={t.title}>
    <div className="flex flex-wrap items-center justify-between gap-4 rounded-xl border bg-card p-5"><div className="flex items-center gap-3"><span className="flex size-11 items-center justify-center rounded-xl bg-muted"><ShieldCheck className="size-5" aria-hidden="true" /></span><div><p className="font-medium">{state.status.hasPassword ? t.local : t.provider}</p><p className="mt-1 text-sm text-muted-foreground">{t.description}</p></div></div>{state.status.passwordChangedAt && <div className="flex items-center gap-2 text-xs text-muted-foreground"><Clock3 className="size-4" aria-hidden="true" /><div><p>{t.changed}</p><time dateTime={state.status.passwordChangedAt}>{new Date(state.status.passwordChangedAt).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB')}</time></div></div>}</div>
    <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
      {state.status.hasPassword ? <PasswordForm policy={state.policy} /> : <div className="space-y-4 rounded-xl border bg-card p-6"><KeyRound className="size-6 text-muted-foreground" aria-hidden="true" /><p className="text-sm leading-7 text-muted-foreground">{t.social}</p><Button variant="outline" render={<Link to="/account/linked-accounts" />}>{t.manageProviders}<ArrowRight className="size-4" aria-hidden="true" /></Button></div>}
      <aside className="space-y-4">
        {state.status.hasPassword && <div className="space-y-4 rounded-xl border bg-card p-5"><h3 className="text-sm font-semibold">{t.requirements}</h3><ul className="space-y-3">{[t.length.replace('{min}', String(state.policy.minLength)).replace('{max}', String(state.policy.maxLength)), t.bytes, state.policy.requireUppercase && t.uppercase, state.policy.requireLowercase && t.lowercase, state.policy.requireDigit && t.digit, state.policy.requireSpecial && t.special].filter(Boolean).map(requirement => <li key={String(requirement)} className="flex gap-2 text-xs leading-relaxed text-muted-foreground"><span className="mt-1.5 size-1 shrink-0 rounded-full bg-muted-foreground" aria-hidden="true" />{requirement}</li>)}</ul></div>}
        <div className="space-y-3 rounded-xl border bg-card p-5"><h3 className="text-sm font-semibold">{t.recovery}</h3><p className="text-xs leading-6 text-muted-foreground">{t.recoveryHelp}</p><a className="inline-flex items-center gap-2 text-sm font-medium underline underline-offset-4" href={`/dashboard/recovery.html?lang=${locale}`}>{t.recovery}<ArrowRight className="size-3.5" aria-hidden="true" /></a></div>
      </aside>
    </div>
  </section>
}
function PasswordForm({ policy }: { policy: PasswordRequirements }) {
  const { locale } = useAccountLocale()
  const t = securityMessages[locale]
  const form = useSchemaForm(securitySchema(policy, locale), { currentPassword: '', newPassword: '', confirmPassword: '' })
  const [show, setShow] = useState(false)
  const [saved, setSaved] = useState(false)
  if (saved) return <div role="status" className="space-y-3 rounded-xl border bg-card p-6"><CheckCircle2 className="size-6 text-emerald-600 dark:text-emerald-400" aria-hidden="true" /><p>{t.success}</p><a className="font-medium underline" href="/dashboard/account/security">{t.signIn}</a></div>
  return <form noValidate className="space-y-5 rounded-xl border bg-card p-5 sm:p-6" onSubmit={form.submit(async (values) => {
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
    <div className="flex flex-wrap items-center justify-between gap-3 border-b pb-5"><h3 className="flex items-center gap-2 font-semibold"><KeyRound className="size-4 text-muted-foreground" aria-hidden="true" />{t.passwordTitle}</h3><Button type="button" variant="ghost" size="sm" aria-pressed={show} disabled={form.formState.isSubmitting} onClick={() => setShow(value => !value)}>{show ? <EyeOff className="size-4" aria-hidden="true" /> : <Eye className="size-4" aria-hidden="true" />}{show ? t.hide : t.show}</Button></div>
    <FormError message={form.formState.errors.root?.server?.message} />
    <fieldset disabled={form.formState.isSubmitting} className="space-y-4">
      <FormField form={form} name="currentPassword" label={t.current} type={show ? 'text' : 'password'} autoComplete="current-password" />
      <FormField form={form} name="newPassword" label={t.next} type={show ? 'text' : 'password'} autoComplete="new-password" />
      <FormField form={form} name="confirmPassword" label={t.confirm} type={show ? 'text' : 'password'} autoComplete="new-password" />
      <p className="flex gap-2 rounded-lg bg-muted/50 p-3 text-xs leading-relaxed text-muted-foreground"><Info className="mt-0.5 size-4 shrink-0" aria-hidden="true" />{t.warning}</p>
    </fieldset>
    <div className="flex justify-end border-t pt-5"><Button type="submit" className="w-full sm:w-auto" disabled={form.formState.isSubmitting}>{form.formState.isSubmitting ? t.saving : t.save}<ArrowRight className="size-4" aria-hidden="true" /></Button></div>
  </form>
}
