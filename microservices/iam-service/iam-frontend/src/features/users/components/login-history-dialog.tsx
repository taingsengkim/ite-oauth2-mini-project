import { useEffect, useState } from 'react'
import { useSchemaForm } from '@/lib/forms'
import { loginHistorySchema } from '@/lib/form-schemas'
import { FormField } from '@/components/shared/form-field'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { listLoginHistory } from '../api/login-history-api'
import type { LoginHistoryPage } from '../api/login-history-api'
import type { User } from '../types/types'

type Filters = { login: string; outcome: '' | 'SUCCESS' | 'FAILURE'; from: string; to: string; suspiciousOnly: boolean }
const emptyFilters: Filters = { login: '', outcome: '', from: '', to: '', suspiciousOnly: false }
const reasons: Record<string, string> = {
  authenticated: 'Signed in successfully', invalid_credentials: 'Invalid username or password',
  account_locked: 'Account locked', account_disabled: 'Account deactivated', account_expired: 'Account expired',
  credentials_expired: 'Password expired', authentication_failed: 'Authentication failed',
  account_link_required: 'Existing account must link this provider', access_denied: 'Provider denied access',
  social_login_failed: 'Social sign-in failed', invalid_state_parameter: 'Invalid sign-in state',
  authorization_request_not_found: 'Sign-in request expired or missing', invalid_id_token: 'Invalid provider ID token',
  invalid_token_response: 'Invalid provider token response', invalid_user_info_response: 'Invalid provider profile',
  invalid_grant: 'Provider rejected authorization', invalid_client: 'Provider client configuration error',
  invalid_request: 'Invalid provider request', server_error: 'Provider server error', temporarily_unavailable: 'Provider temporarily unavailable',
}

export function LoginHistoryDialog({ user, onClose }: { user?: User; onClose: () => void }) {
  const form = useSchemaForm(loginHistorySchema, emptyFilters)
  const [filters, setFilters] = useState<Filters>(emptyFilters)
  const [page, setPage] = useState(0)
  const [revision, setRevision] = useState(0)
  const params = new URLSearchParams({ page: String(page), size: '20' })
  if (filters.login.trim()) params.set('login', filters.login.trim())
  if (filters.outcome) params.set('outcome', filters.outcome)
  if (filters.from) params.set('from', filters.from)
  if (filters.to) params.set('to', filters.to)
  if (filters.suspiciousOnly) params.set('suspiciousOnly', 'true')
  const url = `${user ? `/dashboard-api/users/${user.uuid}/login-history` : '/dashboard-api/login-history'}?${params}`
  const key = `${url}#${revision}`
  const [result, setResult] = useState<{ key: string; data: LoginHistoryPage | null; error: string | null }>({ key: '', data: null, error: null })
  const loading = result.key !== key
  const data = loading ? null : result.data
  const error = loading ? null : result.error

  useEffect(() => {
    const controller = new AbortController()
    listLoginHistory(url, controller.signal)
      .then((data) => { if (!controller.signal.aborted) setResult({ key, data, error: null }) })
      .catch((failure: unknown) => {
        if (!controller.signal.aborted) setResult({ key, data: null, error: failure instanceof Error ? failure.message : 'Could not load login history.' })
      })
    return () => controller.abort()
  }, [url, key])

  return <ManagementDialog title={user ? `Login history: ${user.username}` : 'Login history'} description="Successful and failed sign-ins, newest first. Five or more failures within ten minutes for the same account, login, or IP are flagged for review." onClose={onClose}>
    <form noValidate onSubmit={form.submit((values) => {
      setFilters({ ...values, from: values.from ? new Date(values.from).toISOString() : '', to: values.to ? new Date(values.to).toISOString() : '' })
      setPage(0); setRevision((value) => value + 1)
    })} className="mb-5 space-y-3">
      <div className="grid gap-3 sm:grid-cols-2">
        {!user && <FormField form={form} name="login" label="Username or email (exact)" placeholder="All accounts" />}
        <FormField form={form} name="outcome" label="Result" as="select" options={[{ value: '', label: 'All results' }, { value: 'SUCCESS', label: 'Successful' }, { value: 'FAILURE', label: 'Failed' }]} />
        <FormField form={form} name="from" label="From (local time)" type="datetime-local" /><FormField form={form} name="to" label="To (local time)" type="datetime-local" />
      </div>
      <label className="flex items-center gap-2 text-sm"><input type="checkbox" {...form.register('suspiciousOnly')} />Suspicious activity only</label>
      <div className="flex flex-wrap gap-2"><Button type="submit">Apply filters</Button><Button type="button" variant="outline" onClick={() => { form.reset(emptyFilters); setFilters(emptyFilters); setPage(0); setRevision((value) => value + 1) }}>Reset</Button><Button type="button" variant="outline" disabled={loading} onClick={() => setRevision((value) => value + 1)}>Refresh</Button></div>
    </form>
    {loading && <p role="status" className="py-5 text-sm text-muted-foreground">Loading login history…</p>}
    {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
    {data && <>
      <p className="mb-3 text-sm text-muted-foreground">{data.totalElements} matching events</p>
      {data.content.length === 0 ? <p className="py-5 text-sm">No login events match these filters.</p> : <div className="space-y-3">{data.content.map((entry) => <article key={entry.uuid} className="space-y-3 rounded-lg border p-4">
        <div className="flex flex-wrap items-center justify-between gap-2"><time dateTime={entry.occurredAt} className="text-sm font-medium">{new Date(entry.occurredAt).toLocaleString()}</time><div className="flex gap-2"><Badge variant={entry.outcome === 'SUCCESS' ? 'secondary' : 'destructive'}>{entry.outcome === 'SUCCESS' ? 'Successful' : 'Failed'}</Badge>{entry.suspicious && <Badge variant="outline">Suspicious</Badge>}</div></div>
        <p className="break-words text-sm">{entry.attemptedLogin || 'Unidentified social account'} · {entry.provider === 'LOCAL' ? 'Password' : entry.provider}</p>
        <p className="text-sm">{reasons[entry.reason] ?? 'Authentication failed'}</p>
        <dl className="space-y-2 text-sm"><div><dt className="text-muted-foreground">IP address</dt><dd>{entry.ipAddress || 'Unavailable'}</dd></div><div><dt className="text-muted-foreground">Browser / device</dt><dd className="break-words">{entry.userAgent || 'Unavailable'}</dd></div></dl>
      </article>)}</div>}
      <div className="mt-5 flex items-center justify-between gap-2"><Button variant="outline" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>Previous</Button><span className="text-sm">Page {page + 1} of {Math.max(data.totalPages, 1)}</span><Button variant="outline" disabled={page + 1 >= data.totalPages} onClick={() => setPage((value) => value + 1)}>Next</Button></div>
    </>}
    <p className="mt-4 text-xs text-muted-foreground">History starts when this feature is enabled. IP and browser information are signals for review; shared networks can produce false positives.</p>
  </ManagementDialog>
}
