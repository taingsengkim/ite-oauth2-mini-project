import { getClientDefaults } from '@/features/configuration/api/configuration-api'
import type { ClientDefaults } from '@/features/configuration/schemas'
import { ClientForm } from './client-form'
import { useSchemaForm } from '@/lib/forms'
import { confirmationSchema } from '@/lib/form-schemas'
import { FormField, FormError } from '@/components/shared/form-field'
import { useEffect, useState } from 'react'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { deleteClient, getClient, rotateSecret } from '../api/clients-api'
import type { IssuedClient, ManagedClient } from '../types'


const methods: Record<string, string> = { none: 'Public (PKCE, no secret)', client_secret_basic: 'Confidential (HTTP Basic)', client_secret_post: 'Confidential (secret in POST body)' }


export function ClientFormDialog({ id, onClose, onSaved }: { id?: string; onClose: () => void; onSaved: (value: IssuedClient) => void }) {
  const [defaults, setDefaults] = useState<ClientDefaults | null>(null)
  const [loaded, setLoaded] = useState<ManagedClient | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    if (!id) getClientDefaults(controller.signal).then((value) => { if (!controller.signal.aborted) setDefaults(value) }).catch((failure: unknown) => { if (!controller.signal.aborted) setError(failure instanceof Error ? failure.message : 'Could not load client defaults.') })
    else getClient(id, controller.signal).then((client) => { if (!controller.signal.aborted) setLoaded(client) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setError(failure instanceof Error ? failure.message : 'Could not load client.') })
    return () => controller.abort()
  }, [id, revision])
  if ((id && !loaded) || (!id && !defaults)) return <ManagementDialog title={id ? "Edit client" : "Register client"} description="Loading the current client configuration and defaults." onClose={onClose}>
    {error ? <div role="alert" className="space-y-3"><p className="text-sm text-destructive">{error}</p><Button variant="outline" onClick={() => { setError(null); setRevision((value) => value + 1) }}>Retry</Button></div> : <p role="status">Loading client…</p>}
  </ManagementDialog>
  if (loaded && !loaded.editable) return <ManagementDialog title="Client configuration" description="This client uses protected settings or authentication methods that cannot be edited here." onClose={onClose}><p className="text-sm">You can still view its details in the client list.</p></ManagementDialog>
  return <ClientForm defaults={defaults ?? undefined} client={loaded ?? undefined} onClose={onClose} onSaved={onSaved} />
}

export function ClientDetailsDialog({ id, onClose }: { id: string; onClose: () => void }) {
  const [client, setClient] = useState<ManagedClient | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    getClient(id, controller.signal).then((data) => { if (!controller.signal.aborted) setClient(data) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setError(failure instanceof Error ? failure.message : 'Could not load client.') })
    return () => controller.abort()
  }, [id, revision])
  return <ManagementDialog title={client?.name ?? 'Client details'} description="OAuth2 / OpenID Connect registration and token settings." onClose={onClose}>
    {error ? <div role="alert" className="space-y-3"><p className="text-sm text-destructive">{error}</p><Button variant="outline" onClick={() => { setError(null); setRevision((value) => value + 1) }}>Retry</Button></div> : !client ? <p role="status">Loading client…</p> : <div className="space-y-4">
      {client.protectedClient && <Badge variant="outline">Built-in client</Badge>}
      <dl className="grid gap-4 text-sm sm:grid-cols-2">{[
        ['Client ID', client.clientId], ['Authentication', client.authenticationMethods.map((method) => methods[method] ?? method).join(', ')],
        ['Grant types', client.grantTypes.join(', ')], ['Scopes', client.scopes.join(', ')], ['PKCE', client.requireProofKey ? 'Required' : 'Optional'],
        ['Consent', client.requireConsent ? 'Required' : 'Not required'], ['Access token lifetime', `${client.accessTokenTtlSeconds} seconds`], ['Refresh token lifetime', `${client.refreshTokenTtlSeconds} seconds`],
        ['Registered', client.issuedAt ? new Date(client.issuedAt).toLocaleString() : 'Unavailable'], ['Client secret', client.hasSecret ? 'Configured; cannot be retrieved' : 'None'],
        ['Secret expires', client.secretExpiresAt ? new Date(client.secretExpiresAt).toLocaleString() : 'No expiry configured'],
      ].map(([label, text]) => <div key={label}><dt className="text-muted-foreground">{label}</dt><dd className="break-words">{text}</dd></div>)}</dl>
      <div className="space-y-3 text-sm">{[['Redirect URIs', client.redirectUris], ['Post-logout redirect URIs', client.postLogoutRedirectUris]].map(([label, values]) => <div key={String(label)}><p className="mb-1 text-muted-foreground">{label}</p>{(values as string[]).length ? <ul className="space-y-1">{(values as string[]).map((uri) => <li className="break-all" key={uri}>{uri}</li>)}</ul> : <p>None</p>}</div>)}</div>
      {!client.editable && <p className="text-xs text-muted-foreground">This configuration is read-only here because it is protected or uses additional authentication methods or grant types.</p>}
    </div>}
  </ManagementDialog>
}

export function ClientActionDialog({ client, action, onClose, onDone }: { client: ManagedClient; action: 'delete' | 'rotate'; onClose: () => void; onDone: (issued?: IssuedClient) => void }) {
  const form = useSchemaForm(confirmationSchema(client.clientId), { confirmation: '' })
  const busy = form.formState.isSubmitting
  return <ManagementDialog title={action === 'delete' ? 'Delete ' + client.name + '?' : 'Rotate secret for ' + client.name + '?'} description={action === 'delete' ? 'Remove the client, saved authorizations, and consents. Issued JWTs may remain valid until expiry.' : 'The current secret stops working immediately. Update the application with the new secret. Existing tokens are not revoked.'} busy={busy} onClose={onClose}>
    <form noValidate className="space-y-4" onSubmit={form.submit(async () => { if (action === 'delete') { await deleteClient(client.id); onDone() } else onDone(await rotateSecret(client.id)) })}>
      <FormError message={form.formState.errors.root?.server?.message} /><FormField form={form} name="confirmation" label={'Type ' + client.clientId + ' to confirm.'} disabled={busy} autoComplete="off" />
      <div className="flex justify-end gap-2"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button><Button type="submit" variant="destructive" disabled={busy}>{busy ? 'Working...' : action === 'delete' ? 'Delete client' : 'Rotate secret'}</Button></div>
    </form>
  </ManagementDialog>
}

export function ClientSecretDialog({ issued, onClose }: { issued: IssuedClient; onClose: () => void }) {
  const [show, setShow] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  async function copy() {
    try { await navigator.clipboard.writeText(issued.clientSecret ?? ''); setMessage('Secret copied.') }
    catch { setShow(true); setMessage('Select the secret and copy it manually.') }
  }
  return <ManagementDialog title="Save the new client secret" description="This secret is shown only now. Store it securely before closing; it cannot be retrieved later." onClose={onClose}>
    <div className="space-y-4"><p className="break-words text-sm">Client ID: <strong>{issued.client.clientId}</strong></p>
      <label className="block space-y-2 text-sm">Client secret<Input type={show ? 'text' : 'password'} value={issued.clientSecret ?? ''} readOnly autoComplete="off" spellCheck={false} /></label>
      <div className="flex flex-wrap gap-2"><Button variant="outline" onClick={() => setShow(!show)}>{show ? 'Hide secret' : 'Show secret'}</Button><Button variant="outline" onClick={() => void copy()}>Copy secret</Button><Button onClick={onClose}>Done</Button></div>
      {message && <p role="status" className="text-sm">{message}</p>}
    </div>
  </ManagementDialog>
}
