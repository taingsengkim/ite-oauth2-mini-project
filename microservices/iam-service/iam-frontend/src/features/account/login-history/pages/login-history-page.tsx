import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { FormField, FormError } from '@/components/shared/form-field'
import { useAccountLocale } from '@/lib/account-locale'
import { useSchemaForm } from '@/lib/forms'
import { ApiError, requestJson } from '@/lib/api-client'
import type { LoginHistoryPage } from '@/features/users/api/login-history-api'
import { deviceDetails } from '../../sessions/session-details'
import { historyMessages, historyReason } from '../i18n'
import { emptyHistoryFilters, historyFilterSchema, historyParams } from '../schemas'
import type { HistoryFilters } from '../schemas'

export function AccountLoginHistoryPage() {
  const { locale } = useAccountLocale()
  const t = historyMessages[locale]
  const form = useSchemaForm(historyFilterSchema(locale), emptyHistoryFilters)
  const [filters, setFilters] = useState<HistoryFilters>(emptyHistoryFilters)
  const [page, setPage] = useState(0)
  const [revision, setRevision] = useState(0)
  const url = `/dashboard-api/account/login-history?${historyParams(filters, page)}`
  const key = `${url}#${revision}`
  const [result, setResult] = useState<{ key: string; data?: LoginHistoryPage; error?: 'failed' | 'expired' } | null>(null)
  const loading = result?.key !== key
  const data = loading ? undefined : result.data
  useEffect(() => {
    const controller = new AbortController()
    requestJson<LoginHistoryPage>(url, { signal: controller.signal })
      .then((data) => { if (!controller.signal.aborted) setResult({ key, data }) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setResult({ key, error: failure instanceof ApiError && failure.status === 401 ? 'expired' : 'failed' }) })
    return () => controller.abort()
  }, [url, key])
  return <section className="space-y-5" aria-label={t.title}>
    <form noValidate className="space-y-4 rounded-xl border p-5" onSubmit={form.submit((values) => { setFilters(values); setPage(0); setRevision((value) => value + 1) })}>
      <div className="grid gap-4 md:grid-cols-3"><FormField form={form} name="outcome" label={t.result} as="select" options={[{ value: '', label: t.all }, { value: 'SUCCESS', label: t.success }, { value: 'FAILURE', label: t.failure }]} /><FormField form={form} name="from" label={t.from} type="datetime-local" /><FormField form={form} name="to" label={t.to} type="datetime-local" /></div>
      <label className="flex items-center gap-2 text-sm"><input type="checkbox" {...form.register('suspiciousOnly')} />{t.suspiciousOnly}</label>
      <div className="flex flex-wrap gap-2"><Button type="submit">{t.apply}</Button><Button type="button" variant="outline" onClick={() => { form.reset(emptyHistoryFilters); setFilters(emptyHistoryFilters); setPage(0); setRevision((value) => value + 1) }}>{t.reset}</Button><Button type="button" variant="outline" disabled={loading} onClick={() => setRevision((value) => value + 1)}>{t.refresh}</Button></div>
    </form>
    {loading ? <p role="status">{t.loading}</p> : result.error ? <div className="space-y-3"><FormError message={t[result.error]} />{result.error === 'expired' && <a className="underline" href="/dashboard/index.html?view=history">{t.signIn}</a>}</div> : data && <>
      <p aria-live="polite" className="text-sm text-muted-foreground">{t.total.replace('{count}', data.totalElements.toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB'))}</p>
      {!data.content.length ? <p>{t.empty}</p> : <div className="grid gap-4 lg:grid-cols-2">{data.content.map((entry) => {
        const details = deviceDetails(entry.userAgent || '')
        return <article key={entry.uuid} className="space-y-4 rounded-xl border p-5"><div className="flex flex-wrap items-center justify-between gap-3"><time className="text-sm font-medium" dateTime={entry.occurredAt}>{new Date(entry.occurredAt).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB')}</time><div className="flex flex-wrap gap-2"><Badge variant={entry.outcome === 'SUCCESS' ? 'secondary' : 'destructive'}>{entry.outcome === 'SUCCESS' ? t.success : t.failure}</Badge>{entry.suspicious && <Badge variant="outline">{t.suspicious}</Badge>}</div></div>
          <p className="text-sm">{entry.provider === 'LOCAL' ? t.password : entry.provider} · {historyReason(entry.reason, locale, entry.outcome === 'SUCCESS')}</p>
          <dl className="grid gap-3 text-sm sm:grid-cols-2"><div><dt className="text-muted-foreground">{t.ip}</dt><dd className="break-words">{entry.ipAddress || t.unknown}</dd></div><div><dt className="text-muted-foreground">{t.browser}</dt><dd>{details.browser || t.unknown} · {details.os || t.unknown}</dd></div></dl>
          <details className="text-xs text-muted-foreground"><summary className="cursor-pointer">{t.rawAgent}</summary><p className="mt-2 break-words">{entry.userAgent || t.unknown}</p></details>
        </article>
      })}</div>}
      <div className="flex flex-wrap items-center justify-between gap-3"><Button variant="outline" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>{t.previous}</Button><span className="text-sm">{t.page.replace('{page}', String(page + 1)).replace('{pages}', String(Math.max(1, data.totalPages)))}</span><Button variant="outline" disabled={page + 1 >= data.totalPages || page >= 100000} onClick={() => setPage((value) => value + 1)}>{t.next}</Button></div>
    </>}
    <div className="space-y-2 text-sm text-muted-foreground"><p>{t.historyHint}</p><p>{t.suspiciousHint}</p><p>{t.deviceHint}</p></div>
  </section>
}
