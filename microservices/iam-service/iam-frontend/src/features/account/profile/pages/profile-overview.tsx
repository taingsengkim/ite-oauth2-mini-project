import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Pencil, ShieldCheck, Link2, Monitor } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { buttonVariants } from '@/components/ui/button'
import { requestJson } from '@/lib/api-client'
import { useAccountLocale } from '@/lib/account-locale'
import { accountMessages } from '../../i18n'
import { validPictureUrl } from '../schemas'
import type { AccountProfile } from '../schemas'
import { profileDetails } from '../details-i18n'

type SessionSummary = { current: boolean; signedInAt: string }
type IdentitySummary = { identities: { provider: string }[]; hasPassword: boolean }

const copy = {
  en: { edit: 'Edit profile', personal: 'Personal information', details: 'Account details', security: 'Account security', missing: 'Not provided', signedIn: 'Signed in', name: 'Full name', nickname: 'Display name', sessions: 'Active sessions', providers: 'Linked accounts', password: 'Password & security', passwordHint: 'Manage your password and sign-in security.', manage: 'Manage', unavailable: 'Unavailable', loading: 'Loading…', currentSignIn: 'Current session started', profileHint: 'Your personal details and account security at a glance.' },
  km: { edit: 'កែប្រែប្រវត្តិរូប', personal: 'ព័ត៌មានផ្ទាល់ខ្លួន', details: 'ព័ត៌មានគណនី', security: 'សុវត្ថិភាពគណនី', missing: 'មិនបានផ្តល់', signedIn: 'បានចូលគណនី', name: 'ឈ្មោះពេញ', nickname: 'ឈ្មោះបង្ហាញ', sessions: 'វគ្គសកម្ម', providers: 'គណនីដែលបានភ្ជាប់', password: 'ពាក្យសម្ងាត់ និងសុវត្ថិភាព', passwordHint: 'គ្រប់គ្រងពាក្យសម្ងាត់ និងសុវត្ថិភាពនៃការចូលគណនី។', manage: 'គ្រប់គ្រង', unavailable: 'មិនអាចផ្ទុកបាន', loading: 'កំពុងផ្ទុក…', currentSignIn: 'វគ្គបច្ចុប្បន្នបានចាប់ផ្តើម', profileHint: 'ព័ត៌មានផ្ទាល់ខ្លួន និងសុវត្ថិភាពគណនីរបស់អ្នក។' },
}

export function ProfileOverview({ profile }: { profile: AccountProfile }) {
  const { locale } = useAccountLocale()
  const t = accountMessages[locale]
  const c = copy[locale]
  const d = profileDetails[locale]
  const [failedPicture, setFailedPicture] = useState<string | null>(null)
  const [summary, setSummary] = useState<{ sessions?: SessionSummary[]; identities?: IdentitySummary } | null>(null)
  useEffect(() => {
    const controller = new AbortController()
    void Promise.allSettled([
      requestJson<SessionSummary[]>('/dashboard-api/account/sessions', { signal: controller.signal }),
      requestJson<IdentitySummary>('/dashboard-api/account/identities', { signal: controller.signal }),
    ]).then(([sessions, identities]) => {
      if (!controller.signal.aborted) setSummary({ sessions: sessions.status === 'fulfilled' ? sessions.value : undefined, identities: identities.status === 'fulfilled' ? identities.value : undefined })
    })
    return () => controller.abort()
  }, [])
  const name = [profile.givenName, profile.middleName, profile.familyName].filter(Boolean).join(' ') || profile.username
  const initials = [profile.givenName || profile.username, profile.familyName].filter(Boolean).map(part => Array.from(part)[0]).join('').toUpperCase()
  const date = (value?: string) => value && Number.isFinite(Date.parse(value)) ? new Date(value).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB') : c.missing
  const current = summary?.sessions?.find(session => session.current)
  const verification = (verified: boolean) => <Badge variant="outline" className="mt-1 w-fit">{verified ? t.verified : t.unverified}</Badge>
  return <div className="space-y-6">
    <div className="flex flex-col gap-5 rounded-xl border bg-card p-5 text-card-foreground sm:flex-row sm:items-center sm:p-6">
      <div className="flex size-24 shrink-0 items-center justify-center overflow-hidden rounded-full border bg-muted text-3xl font-semibold">
        {profile.pictureUrl && validPictureUrl(profile.pictureUrl) && failedPicture !== profile.pictureUrl ? <img src={profile.pictureUrl} alt={name} className="size-full object-cover" referrerPolicy="no-referrer" onError={() => setFailedPicture(profile.pictureUrl)} /> : <span aria-label={name}>{initials}</span>}
      </div>
      <div className="min-w-0 flex-1 space-y-2"><h2 className="break-words text-2xl font-semibold tracking-tight">{name}</h2><p className="break-words text-sm text-muted-foreground">@{profile.username}</p><Badge variant="secondary">{c.signedIn}</Badge><p className="text-sm text-muted-foreground">{c.profileHint}</p></div>
      <Link to="/account/profile/edit" className={buttonVariants({ variant: 'default', size: 'lg', className: 'self-start sm:self-center' })}><Pencil />{c.edit}</Link>
    </div>
    <div className="grid gap-6 xl:grid-cols-3">
      <section className="rounded-xl border bg-card p-5 sm:p-6 xl:col-span-2"><h2 className="mb-5 font-semibold">{c.personal}</h2><dl className="grid gap-5 sm:grid-cols-2">
        <Detail label={c.name} value={name} /><Detail label={t.username} value={profile.username} />
        <Detail label={t.email} value={profile.email || c.missing}>{profile.email && verification(profile.emailVerified)}</Detail>
        <Detail label={t.phoneNumber} value={profile.phoneNumber || c.missing}>{profile.phoneNumber && verification(profile.phoneNumberVerified)}</Detail>
        <Detail label={c.nickname} value={profile.nickname || c.missing} /><Detail label={t.locale} value={profile.locale === 'km' ? 'ខ្មែរ' : 'English'} />
        <Detail label={d.gender} value={profile.gender ? d[profile.gender] : c.missing} /><Detail label={d.birthdate} value={profile.birthdate ? new Date(profile.birthdate + 'T00:00:00').toLocaleDateString(locale === 'km' ? 'km-KH' : 'en-GB') : c.missing} />
        <Detail label={d.zoneinfo} value={profile.zoneinfo || c.missing} /><Detail label={d.websiteUrl} value={profile.websiteUrl || c.missing} /><Detail label={d.profileUrl} value={profile.profileUrl || c.missing} />
      </dl></section>
      <section className="rounded-xl border bg-card p-5 sm:p-6"><h2 className="mb-5 font-semibold">{c.details}</h2><dl className="space-y-5"><Detail label={t.created} value={date(profile.createdAt)} /><Detail label={t.updated} value={date(profile.updatedAt)} /><Detail label={c.currentSignIn} value={!summary ? c.loading : !summary.sessions ? c.unavailable : date(current?.signedInAt)} /></dl></section>
    </div>
    <section className="rounded-xl border bg-card p-5 sm:p-6"><h2 className="mb-5 font-semibold">{d.address}</h2><dl className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">{(['streetAddress', 'locality', 'region', 'postalCode', 'country'] as const).map(field => <Detail key={field} label={d[field]} value={profile.address?.[field] || c.missing} />)}<Detail label={d.lastLogin} value={date(profile.lastLoginAt || undefined)} /></dl></section>
    <section aria-label={c.security} className="space-y-4"><h2 className="font-semibold">{c.security}</h2><div className="grid gap-4 md:grid-cols-3">
      {[
        { title: c.password, value: c.passwordHint, to: '/account/security', icon: ShieldCheck },
        { title: c.providers, value: !summary ? c.loading : !summary.identities ? c.unavailable : summary.identities.identities.map(identity => ({ google: 'Google', github: 'GitHub', facebook: 'Facebook', telegram: 'Telegram' })[identity.provider.toLowerCase()] || identity.provider).join(', ') || '0', to: '/account/security?tab=accounts', icon: Link2 },
        { title: c.sessions, value: !summary ? c.loading : summary.sessions ? String(summary.sessions.length) : c.unavailable, to: '/account/security?tab=sessions', icon: Monitor },
      ].map(item => <Link key={item.to} to={item.to} className="group flex flex-col gap-4 rounded-xl border bg-card p-5 transition-colors hover:bg-muted/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"><div className="flex items-center gap-2"><item.icon className="size-4 text-muted-foreground" /><h3 className="font-medium">{item.title}</h3></div><p className="flex-1 break-words text-sm text-muted-foreground">{item.value}</p><span className="flex items-center gap-2 text-sm font-medium">{c.manage}<ArrowRight className="size-4 transition-transform group-hover:translate-x-1" /></span></Link>)}
    </div></section>
  </div>
}

function Detail({ label, value, children }: { label: string; value: string; children?: React.ReactNode }) {
  return <div className="min-w-0 space-y-1"><dt className="text-sm text-muted-foreground">{label}</dt><dd className="flex flex-col items-start gap-1 break-words text-sm font-medium"><span className="max-w-full break-words">{value}</span>{children}</dd></div>
}
