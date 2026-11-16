import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { FormField, FormError } from '@/components/shared/form-field'
import { useSchemaForm } from '@/lib/forms'
import { getDashboardSession } from '@/lib/api-client'
import { getConfiguration, updateConfiguration } from '../api/configuration-api'
import { securityPolicySchema } from '../schemas'
import type { Configuration } from '../schemas'

export function ConfigurationPage() {
  const [revision, setRevision] = useState(0)
  const [result, setResult] = useState<{ key: number; configuration?: Configuration; canUpdate?: boolean; error?: string } | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  useEffect(() => {
    const controller = new AbortController()
    Promise.all([getConfiguration(controller.signal), getDashboardSession(controller.signal)])
      .then(([configuration, session]) => { if (!controller.signal.aborted) setResult({ key: revision, configuration, canUpdate: session.authorities.includes('configuration:update') }) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setResult({ key: revision, error: failure instanceof Error ? failure.message : 'Could not load system configuration.' }) })
    return () => controller.abort()
  }, [revision])
  return <section aria-label="System configuration" className="space-y-5">
    {success && <p role="status" className="rounded-lg border bg-muted/40 p-3 text-sm">{success}</p>}
    {result?.key !== revision ? <p role="status">Loading system configuration...</p> : result.error ? <div className="space-y-3"><FormError message={result.error} /><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>Retry</Button></div> : result.configuration && <ConfigurationForm key={`${revision}-${result.configuration.version}`} configuration={result.configuration} canUpdate={Boolean(result.canUpdate)} onReload={() => { setSuccess(null); setRevision((value) => value + 1) }} onSaved={(configuration) => {
      setResult({ key: revision, configuration, canUpdate: result.canUpdate }); setSuccess('Security policies saved successfully. The change was recorded in Audit Logs.')
    }} />}
  </section>
}

function ConfigurationForm({ configuration, canUpdate, onReload, onSaved }: { configuration: Configuration; canUpdate: boolean; onReload: () => void; onSaved: (value: Configuration) => void }) {
  const form = useSchemaForm(securityPolicySchema, configuration.policy)
  const busy = form.formState.isSubmitting
  return <form noValidate className="space-y-5" onSubmit={form.submit(async (policy) => onSaved(await updateConfiguration(configuration.version, policy)))}>
    <div className="flex flex-wrap items-center justify-between gap-3"><p className="text-sm text-muted-foreground">Updated {new Date(configuration.updatedAt).toLocaleString()} by {configuration.updatedBy} · Version {configuration.version}</p><Button type="button" variant="outline" disabled={busy} onClick={onReload}>Reload saved settings</Button></div>
    {!canUpdate && <p className="rounded-lg border p-3 text-sm text-muted-foreground">Read-only access. Editing requires configuration:update.</p>}
    <FormError message={form.formState.errors.root?.server?.message} />
    <fieldset disabled={busy || !canUpdate} className="space-y-5">
      <section className="space-y-4 rounded-lg border p-5"><h2 className="font-semibold">Password sign-in protection</h2><p className="text-sm text-muted-foreground">Consecutive incorrect password attempts trigger a temporary lockout. Social sign-ins and manual administrator locks are separate. Successful password sign-in, administrator unlock, or password reset clears the automatic lockout. Changing the duration affects new lockouts.</p>
        <div className="grid gap-4 sm:grid-cols-2"><FormField form={form} name="maxFailedLoginAttempts" label="Failed attempts before lockout" type="number" hint="3–20 attempts." /><FormField form={form} name="lockoutDurationSeconds" label="Lockout duration (seconds)" type="number" hint="60–86400 seconds." /></div>
      </section>
      <section className="space-y-4 rounded-lg border p-5"><h2 className="font-semibold">Password policy</h2><p className="text-sm text-muted-foreground">Applies when passwords are created, changed, or reset. Existing passwords continue to work. Passwords always have a maximum of 72 UTF-8 bytes for bcrypt, regardless of character count.</p>
        <div className="grid gap-4 sm:grid-cols-2"><FormField form={form} name="passwordMinLength" label="Minimum characters" type="number" hint="8–64 characters." /><FormField form={form} name="passwordMaxLength" label="Maximum characters" type="number" hint="At most 72, and no less than the minimum." /></div>
        <div className="grid gap-3 text-sm sm:grid-cols-2">{[
          ['passwordRequireUppercase', 'Require an uppercase letter'], ['passwordRequireLowercase', 'Require a lowercase letter'], ['passwordRequireDigit', 'Require a digit'], ['passwordRequireSpecial', 'Require a special character'],
        ].map(([name, label]) => <label key={name} className="flex items-center gap-2"><input type="checkbox" {...form.register(name as 'passwordRequireUppercase' | 'passwordRequireLowercase' | 'passwordRequireDigit' | 'passwordRequireSpecial')} />{label}</label>)}</div>
      </section>
      <section className="space-y-4 rounded-lg border p-5"><h2 className="font-semibold">Browser sessions</h2><p className="text-sm text-muted-foreground">The new idle timeout is applied on the next browser request. A session that has already been idle longer than the new limit will be signed out on that request.</p><FormField form={form} name="sessionIdleTimeoutSeconds" label="Idle timeout (seconds)" type="number" hint="60–86400 seconds. Applies to browser sessions, not API access tokens." /></section>
      <section className="space-y-4 rounded-lg border p-5"><h2 className="font-semibold">Default token lifetimes</h2><p className="text-sm text-muted-foreground">Starting values for new clients registered through this dashboard. Administrators can override each client's lifetimes. Existing clients, external dynamic registrations, and already-issued tokens keep their own lifetimes.</p><div className="grid gap-4 sm:grid-cols-2"><FormField form={form} name="defaultAccessTokenTtlSeconds" label="Default access token lifetime (seconds)" type="number" /><FormField form={form} name="defaultRefreshTokenTtlSeconds" label="Default refresh token lifetime (seconds)" type="number" /></div></section>
    </fieldset>
    {canUpdate && <div className="flex justify-end gap-2"><Button type="button" variant="outline" disabled={busy || !form.formState.isDirty} onClick={() => form.reset(configuration.policy)}>Discard changes</Button><Button type="submit" disabled={busy || !form.formState.isDirty}>{busy ? 'Saving...' : 'Save configuration'}</Button></div>}
  </form>
}
