import type { ClientDefaults } from '@/features/configuration/schemas'
import { useWatch } from 'react-hook-form'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { FormField, FormError } from '@/components/shared/form-field'
import { Button } from '@/components/ui/button'
import { useSchemaForm } from '@/lib/forms'
import { clientFormSchema, splitLines, splitScopes } from '../schemas'
import { createClient, updateClient } from '../api/clients-api'
import type { IssuedClient, ManagedClient } from '../types'

export function ClientForm({ client, defaults, onClose, onSaved }: { client?: ManagedClient; defaults?: ClientDefaults; onClose: () => void; onSaved: (value: IssuedClient) => void }) {
  const form = useSchemaForm(clientFormSchema, {
    clientId: client?.clientId ?? '', name: client?.name ?? '', authenticationMethod: (client?.authenticationMethods[0] ?? 'none') as 'none' | 'client_secret_basic' | 'client_secret_post',
    grantTypes: (client?.grantTypes ?? ['authorization_code']) as ('authorization_code' | 'refresh_token' | 'client_credentials')[],
    redirectUris: client?.redirectUris.join('\n') ?? '', postLogoutRedirectUris: client?.postLogoutRedirectUris.join('\n') ?? '', scopes: client?.scopes.join(' ') ?? 'openid profile email',
    requireProofKey: client?.requireProofKey ?? true, requireConsent: client?.requireConsent ?? true,
    accessTokenTtlSeconds: client?.accessTokenTtlSeconds ?? defaults?.accessTokenTtlSeconds ?? 3600, refreshTokenTtlSeconds: client?.refreshTokenTtlSeconds ?? defaults?.refreshTokenTtlSeconds ?? 604800,
  })
  const method = useWatch({ control: form.control, name: 'authenticationMethod' })
  const proofKey = useWatch({ control: form.control, name: 'requireProofKey' })
  const publicClient = method === 'none'
  const busy = form.formState.isSubmitting
  const methodRegistration = form.register('authenticationMethod')
  return <ManagementDialog title={client ? `Edit ${client.name}` : 'Register OAuth client'} description="Configure an application that uses this IAM server for sign-in and access tokens." busy={busy} onClose={onClose}>
    <form noValidate className="space-y-4" onSubmit={form.submit(async (values) => {
      const definition = { ...values, redirectUris: splitLines(values.redirectUris), postLogoutRedirectUris: splitLines(values.postLogoutRedirectUris), scopes: splitScopes(values.scopes) }
      onSaved(client ? { client: await updateClient(client.id, definition), clientSecret: null } : await createClient(definition))
    })}>
      <FormError message={form.formState.errors.root?.server?.message} />
      <fieldset disabled={busy} className="space-y-4">
        <div className="grid gap-4 sm:grid-cols-2"><FormField form={form} name="clientId" label="Client ID *" readOnly={Boolean(client)} /><FormField form={form} name="name" label="Application name *" /></div>
        <div className="space-y-2"><label htmlFor="client-method" className="text-sm font-medium">Authentication method</label>
          {client && <input type="hidden" {...methodRegistration} />}
          <select {...(client ? {} : methodRegistration)} defaultValue={client?.authenticationMethods[0]} id="client-method" disabled={Boolean(client)} className="h-9 w-full rounded-md border bg-background px-3" onChange={(event) => {
            void methodRegistration.onChange(event)
            if (event.target.value === 'none') {
              form.setValue('requireProofKey', true, { shouldDirty: true, shouldValidate: true })
              form.setValue('grantTypes', [...new Set(['authorization_code' as const, ...form.getValues('grantTypes').filter((grant) => grant !== 'client_credentials')])], { shouldDirty: true, shouldValidate: true })
            }
          }}><option value="none">Public (PKCE, no secret)</option><option value="client_secret_basic">Confidential (HTTP Basic)</option><option value="client_secret_post">Confidential (secret in POST body)</option></select>
          <FormError message={form.formState.errors.authenticationMethod?.message} />
          <p className="text-xs text-muted-foreground">Client ID and authentication method are fixed after registration.</p>
        </div>
        <fieldset className="space-y-2"><legend className="mb-2 text-sm font-medium">Grant types *</legend>{[
          ['authorization_code', 'Authorization code (user sign-in)'], ['refresh_token', 'Refresh token'], ['client_credentials', 'Client credentials (machine-to-machine)'],
        ].map(([grant, label]) => <label key={grant} className="flex items-center gap-2 text-sm"><input type="checkbox" value={grant} {...form.register('grantTypes')} disabled={publicClient && grant === 'client_credentials'} aria-invalid={Boolean(form.formState.errors.grantTypes)} aria-describedby="client-grants-error" />{label}</label>)}
          <div id="client-grants-error"><FormError message={form.formState.errors.grantTypes?.message} /></div>
        </fieldset>
        <FormField form={form} name="redirectUris" label="Redirect URIs (one per line)" as="textarea" placeholder="http://localhost:5173/callback" hint="Use exact HTTPS URLs, or HTTP on localhost/loopback. Required for authorization code; leave empty for machine-only clients." />
        <FormField form={form} name="postLogoutRedirectUris" label="Post-logout redirect URIs (one per line)" as="textarea" />
        <FormField form={form} name="scopes" label="Scopes (separated by spaces) *" hint="Use openid for OpenID Connect. Machine-only clients should use API scopes such as reports.read." />
        <div className="grid gap-4 sm:grid-cols-2"><FormField form={form} name="accessTokenTtlSeconds" label="Access token lifetime (seconds) *" type="number" /><FormField form={form} name="refreshTokenTtlSeconds" label="Refresh token lifetime (seconds) *" type="number" /></div>
        <div><label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={proofKey} disabled={publicClient} onChange={(event) => form.setValue('requireProofKey', event.target.checked, { shouldDirty: true, shouldValidate: true })} />Require PKCE</label><FormError message={form.formState.errors.requireProofKey?.message} /></div>
        <label className="flex items-center gap-2 text-sm"><input type="checkbox" {...form.register('requireConsent')} />Require user consent</label>
        {publicClient && <p className="text-xs text-muted-foreground">Public clients have no secret. The current server does not issue refresh tokens to public clients.</p>}
      </fieldset>
      <div className="flex justify-end gap-2"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button><Button type="submit" disabled={busy}>{busy ? 'Saving...' : client ? 'Save changes' : 'Register client'}</Button></div>
    </form>
  </ManagementDialog>
}
