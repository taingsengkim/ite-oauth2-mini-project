import { Filter, RefreshCw, RotateCcw, ShieldAlert, Monitor, CheckCircle2, XCircle } from 'lucide-react'
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
    <form noValidate className="space-y-4 rounded-xl border bg-card p-5" onSubmit={form.submit((values) => { setFilters(values); setPage(0); setRevision((value) => value + 1) })}>
      <p className="flex items-center gap-2 text-sm font-medium"><Filter className="size-4 text-muted-foreground" aria-hidden="true" />{t.apply}</p><div className="grid gap-4 md:grid-cols-3"><FormField form={form} name="outcome" label={t.result} as="select" options={[{ value: '', label: t.all }, { value: 'SUCCESS', label: t.success }, { value: 'FAILURE', label: t.failure }]} /><FormField form={form} name="from" label={t.from} type="datetime-local" /><FormField form={form} name="to" label={t.to} type="datetime-local" /></div>
      <label className="flex items-center gap-2 text-sm"><input type="checkbox" {...form.register('suspiciousOnly')} />{t.suspiciousOnly}</label>
      <div className="flex flex-wrap gap-2 border-t pt-4"><Button type="submit"><Filter className="size-4" aria-hidden="true" />{t.apply}</Button><Button type="button" variant="outline" onClick={() => { form.reset(emptyHistoryFilters); setFilters(emptyHistoryFilters); setPage(0); setRevision((value) => value + 1) }}><RotateCcw className="size-4" aria-hidden="true" />{t.reset}</Button><Button type="button" variant="outline" disabled={loading} onClick={() => setRevision((value) => value + 1)}><RefreshCw className="size-4" aria-hidden="true" />{t.refresh}</Button></div>
    </form>
    {loading ? <p role="status">{t.loading}</p> : result.error ? <div className="space-y-3"><FormError message={t[result.error]} />{result.error === 'expired' && <a className="underline" href="/dashboard/account/login-history">{t.signIn}</a>}</div> : data && <>
      <p aria-live="polite" className="text-sm text-muted-foreground">{t.total.replace('{count}', data.totalElements.toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB'))}</p>
      {!data.content.length ? <p className="rounded-xl border border-dashed p-10 text-center text-sm text-muted-foreground">{t.empty}</p> : <div className="overflow-hidden rounded-xl border bg-card divide-y">{data.content.map((entry) => {
        const details = deviceDetails(entry.userAgent || '')
        return <article key={entry.uuid} className="relative space-y-4 p-5 sm:pl-16"><span className="absolute top-5 left-5 hidden size-8 items-center justify-center rounded-full bg-muted sm:flex" aria-hidden="true">{entry.outcome === 'SUCCESS' ? <CheckCircle2 className="size-4 text-emerald-600 dark:text-emerald-400" /> : <XCircle className="size-4 text-destructive" />}</span><div className="flex flex-wrap items-center justify-between gap-3"><time className="text-sm font-medium" dateTime={entry.occurredAt}>{new Date(entry.occurredAt).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB')}</time><div className="flex flex-wrap gap-2"><Badge variant="outline" className={entry.outcome === 'SUCCESS' ? 'border-emerald-600/25 bg-emerald-500/10 text-emerald-700 dark:text-emerald-400' : 'border-destructive/25 bg-destructive/10 text-destructive'}>{entry.outcome === 'SUCCESS' ? t.success : t.failure}</Badge>{entry.suspicious && <Badge variant="outline" className="border-amber-500/30 bg-amber-500/10 text-amber-800 dark:text-amber-300"><ShieldAlert className="mr-1 size-3" aria-hidden="true" />{t.suspicious}</Badge>}</div></div>
          <p className="text-sm">{entry.provider === 'LOCAL' ? t.password : entry.provider} · {historyReason(entry.reason, locale, entry.outcome === 'SUCCESS')}</p>
          <dl className="grid gap-4 rounded-lg bg-muted/40 p-3 text-sm sm:grid-cols-2"><div><dt className="text-muted-foreground">{t.ip}</dt><dd className="break-words">{entry.ipAddress || t.unknown}</dd></div><div><dt className="flex items-center gap-1 text-muted-foreground"><Monitor className="size-3" aria-hidden="true" />{t.browser}</dt><dd>{details.browser || t.unknown} · {details.os || t.unknown}</dd></div></dl>
          <details className="text-xs text-muted-foreground"><summary className="cursor-pointer">{t.rawAgent}</summary><p className="mt-2 break-words">{entry.userAgent || t.unknown}</p></details>
        </article>
      })}</div>}
      <div className="flex flex-wrap items-center justify-between gap-3"><Button variant="outline" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>{t.previous}</Button><span className="text-sm">{t.page.replace('{page}', String(page + 1)).replace('{pages}', String(Math.max(1, data.totalPages)))}</span><Button variant="outline" disabled={page + 1 >= data.totalPages || page >= 100000} onClick={() => setPage((value) => value + 1)}>{t.next}</Button></div>
    </>}
    <div className="space-y-2 rounded-xl border bg-muted/30 p-4 text-xs leading-relaxed text-muted-foreground"><p>{t.historyHint}</p><p>{t.suspiciousHint}</p><p>{t.deviceHint}</p></div>
  </section>
}
