import { useEffect, useState } from 'react'
import { useAccountLocale } from '@/lib/account-locale'
import { shellMessages } from '@/components/layout/navigation'
import { getDashboardSession } from '@/lib/api-client'
import type { DashboardSession } from '@/lib/api-client'
import { DashboardRoutes } from '@/app/dashboard-routes'

export default function App() {
  const { locale } = useAccountLocale()
  const t = shellMessages[locale]
  const [result, setResult] = useState<{ session?: DashboardSession; error?: boolean } | null>(null)
  useEffect(() => {
    const controller = new AbortController()
    const onRestore = (event: PageTransitionEvent) => { if (event.persisted) window.location.reload() }
    window.addEventListener('pageshow', onRestore)
    getDashboardSession(controller.signal).then((session) => { if (!controller.signal.aborted) setResult({ session }) })
      .catch(() => { if (!controller.signal.aborted) setResult({ error: true }) })
    return () => { controller.abort(); window.removeEventListener('pageshow', onRestore) }
  }, [])
  if (!result) return <main className="p-6" role="status">{t.loading}</main>
  // A full navigation lets Spring Security start login and preserve the requested dashboard route.
  if (!result.session) return <main className="space-y-4 p-6"><p role="alert">{t.failed}</p><a className="underline" href={window.location.pathname + window.location.search}>{t.signIn}</a></main>
  return <DashboardRoutes session={result.session} />
}
