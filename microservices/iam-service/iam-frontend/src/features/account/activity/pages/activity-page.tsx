import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { FormField, FormError } from '@/components/shared/form-field'
import { useAccountLocale } from '@/lib/account-locale'
import { useSchemaForm } from '@/lib/forms'
import { ApiError, requestJson } from '@/lib/api-client'
import { deviceDetails } from '../../sessions/session-details'
import { activityMessages, activityActions } from '../i18n'
import { activitySchema, activityParams, emptyActivityFilters } from '../schemas'
import type { ActivityFilters } from '../schemas'

type Activity = { uuid: string; occurredAt: string; action: string; performedByYou: boolean; ipAddress: string | null; userAgent: string | null }
type ActivityPageData = { content: Activity[]; totalElements: number; totalPages: number; page: number; size: number }
export function ActivityPage() {
  const { locale } = useAccountLocale()
  const t = activityMessages[locale]
  const form = useSchemaForm(activitySchema(locale), emptyActivityFilters)
  const [filters, setFilters] = useState<ActivityFilters>(emptyActivityFilters)
  const [page, setPage] = useState(0)
  const [revision, setRevision] = useState(0)
  const url = `/dashboard-api/account/activity?${activityParams(filters, page)}`
  const key = `${url}#${revision}`
  const [result, setResult] = useState<{ key: string; data?: ActivityPageData; error?: 'failed' | 'expired' } | null>(null)
  const loading = result?.key !== key
  const data = loading ? undefined : result.data
  useEffect(() => {
    const controller = new AbortController()
    requestJson<ActivityPageData>(url, { signal: controller.signal })
      .then((data) => { if (!controller.signal.aborted) setResult({ key, data }) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setResult({ key, error: failure instanceof ApiError && failure.status === 401 ? 'expired' : 'failed' }) })
    return () => controller.abort()
  }, [url, key])
  return <section className="space-y-5" aria-label={t.title}>
    <form noValidate className="space-y-4 rounded-xl border p-5" onSubmit={form.submit((values) => { setFilters(values); setPage(0); setRevision((value) => value + 1) })}>
      <div className="grid gap-4 md:grid-cols-3"><FormField form={form} name="action" label={t.action} as="select" options={[{ value: '', label: t.all }, ...Object.entries(activityActions).map(([value, text]) => ({ value, label: text[locale] }))]} /><FormField form={form} name="from" label={t.from} type="datetime-local" /><FormField form={form} name="to" label={t.to} type="datetime-local" /></div>
      <div className="flex flex-wrap gap-2"><Button type="submit">{t.apply}</Button><Button type="button" variant="outline" onClick={() => { form.reset(emptyActivityFilters); setFilters(emptyActivityFilters); setPage(0); setRevision((value) => value + 1) }}>{t.reset}</Button><Button type="button" variant="outline" disabled={loading} onClick={() => setRevision((value) => value + 1)}>{t.refresh}</Button></div>
    </form>
    {loading ? <p role="status">{t.loading}</p> : result.error ? <div className="space-y-3"><FormError message={t[result.error]} />{result.error === 'expired' && <a className="underline" href="/dashboard/account/activity">{t.signIn}</a>}</div> : data && <>
      <p aria-live="polite" className="text-sm text-muted-foreground">{t.total.replace('{count}', data.totalElements.toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB'))}</p>
      {!data.content.length ? <p>{t.empty}</p> : <div className="grid gap-4 lg:grid-cols-2">{data.content.map((event) => {
        const details = deviceDetails(event.userAgent || '')
        return <article key={event.uuid} className="space-y-4 rounded-xl border p-5"><div className="flex flex-wrap items-center justify-between gap-3"><h2 className="font-medium">{activityActions[event.action]?.[locale] || t.generic}</h2><Badge variant="outline">{event.performedByYou ? t.you : t.other}</Badge></div><time className="text-sm text-muted-foreground" dateTime={event.occurredAt}>{new Date(event.occurredAt).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB')}</time>
          {event.performedByYou && <><dl className="grid gap-3 text-sm sm:grid-cols-2"><div><dt className="text-muted-foreground">{t.ip}</dt><dd className="break-words">{event.ipAddress || t.unknown}</dd></div><div><dt className="text-muted-foreground">{t.device}</dt><dd>{details.browser || t.unknown} · {details.os || t.unknown}</dd></div></dl><details className="text-xs text-muted-foreground"><summary className="cursor-pointer">{t.rawAgent}</summary><p className="mt-2 break-words">{event.userAgent || t.unknown}</p></details></>}
        </article>
      })}</div>}
      <div className="flex flex-wrap items-center justify-between gap-3"><Button variant="outline" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>{t.previous}</Button><span className="text-sm">{t.page.replace('{page}', String(page + 1)).replace('{pages}', String(Math.max(1, data.totalPages)))}</span><Button variant="outline" disabled={page + 1 >= data.totalPages || page >= 100000} onClick={() => setPage((value) => value + 1)}>{t.next}</Button></div>
    </>}
    <p className="text-sm text-muted-foreground">{t.hint}</p><p className="text-sm text-muted-foreground">{t.privacy}</p>
  </section>
}
