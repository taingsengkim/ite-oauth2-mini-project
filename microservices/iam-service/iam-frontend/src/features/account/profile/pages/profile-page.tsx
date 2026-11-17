import { useEffect, useState } from 'react'
import { useWatch } from 'react-hook-form'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { FormField, FormError } from '@/components/shared/form-field'
import { useSchemaForm } from '@/lib/forms'
import { useAccountLocale } from '@/lib/account-locale'
import { ApiError } from '@/lib/api-client'
import { accountMessages } from '../../i18n'
import { getAccountProfile, updateAccountProfile } from '../api/profile-api'
import { profileSchema, validPictureUrl } from '../schemas'
import type { AccountProfile, ProfileSaved } from '../schemas'
import { LanguageSwitcher } from '@/components/shared/language-switcher'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ProfileOverview } from './profile-overview'
import { profileDetails } from '../details-i18n'

export function ProfilePage({ edit = false }: { edit?: boolean }) {
  const navigate = useNavigate()
  const location = useLocation()
  const { locale, setLocale } = useAccountLocale()
  const t = accountMessages[locale]
  const [revision, setRevision] = useState(0)
  const [result, setResult] = useState<{ key: number; profile?: AccountProfile; error?: string } | null>(null)
  const [saved, setSaved] = useState<ProfileSaved | null>(null)
  useEffect(() => {
    const controller = new AbortController()
    getAccountProfile(controller.signal).then((profile) => {
      if (!controller.signal.aborted) { setResult({ key: revision, profile }); setLocale(profile.locale === 'km' ? 'km' : 'en') }
    }).catch((failure: unknown) => {
      if (!controller.signal.aborted) setResult({ key: revision, error: failure instanceof ApiError && failure.status === 401 ? 'unauthenticated' : 'failed' })
    })
    return () => controller.abort()
  }, [revision, setLocale])
  return <section aria-label={t.title} className="space-y-5">
    {edit && <Link to="/account/profile" className="text-sm font-medium underline underline-offset-4">{locale === 'km' ? 'ត្រឡប់ទៅប្រវត្តិរូប' : 'Back to profile'}</Link>}
    {!edit && location.state?.profileSaved && <p role="status" className="rounded-lg border bg-muted/40 p-4 text-sm">{t.saved}</p>}
    {saved && <div role="status" className="space-y-3 rounded-lg border bg-muted/40 p-4 text-sm"><p>{saved.signInRequired ? t.signedOut : t.saved}</p>{saved.signInRequired && <a className="font-medium underline" href="/dashboard/account/profile">{t.signIn}</a>}</div>}
    {saved?.signInRequired ? <p className="break-words text-sm">{t.username}: <strong>{saved.profile.username}</strong> · {t.email}: <strong>{saved.profile.email}</strong></p>
      : result?.key !== revision ? <p role="status">{t.loading}</p> : result.error ? <div className="space-y-3"><FormError message={result.error === 'unauthenticated' ? t.signInRequired : t.loadFailed} /><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>{t.retry}</Button>{result.error === 'unauthenticated' && <a className="ml-3 text-sm underline" href="/dashboard/account/profile">{t.signIn}</a>}</div>
        : result.profile && (edit ? <ProfileForm key={`${revision}-${result.profile.updatedAt}`} profile={result.profile} onReload={() => { setSaved(null); setRevision((value) => value + 1) }} onSaved={(value) => { if (value.signInRequired) { setSaved(value); setResult({ key: revision, profile: value.profile }) } else { void navigate('/account/profile', { replace: true, state: { profileSaved: true } }) } }} /> : <ProfileOverview profile={result.profile} />)}
  </section>
}

function ProfileForm({ profile, onReload, onSaved }: { profile: AccountProfile; onReload: () => void; onSaved: (value: ProfileSaved) => void }) {
  const { locale, setLocale } = useAccountLocale()
  const t = accountMessages[locale]
  const d = profileDetails[locale]
  const form = useSchemaForm(profileSchema(locale, profile.username), { username: profile.username, email: profile.email || '', givenName: profile.givenName, familyName: profile.familyName,
    nickname: profile.nickname || '', phoneNumber: profile.phoneNumber || '', pictureUrl: profile.pictureUrl || '', locale: profile.locale === 'km' ? 'km' : 'en',
    middleName: profile.middleName || '', gender: profile.gender || '', birthdate: profile.birthdate || '', zoneinfo: profile.zoneinfo || '', profileUrl: profile.profileUrl || '', websiteUrl: profile.websiteUrl || '',
    address: { streetAddress: profile.address?.streetAddress || '', locality: profile.address?.locality || '', region: profile.address?.region || '', postalCode: profile.address?.postalCode || '', country: profile.address?.country || '' } })
  const [username, email, phone, picture, profileLocale] = useWatch({ control: form.control, name: ['username', 'email', 'phoneNumber', 'pictureUrl', 'locale'] })
  const [failedPicture, setFailedPicture] = useState<string | null>(null)
  const busy = form.formState.isSubmitting
  const identifiersChanged = username.trim() !== profile.username || email.trim().toLowerCase() !== profile.email?.toLowerCase()
  function translateFailure(failure: unknown) {
    if (!(failure instanceof ApiError)) return new Error(t.saveFailed)
    const known: Record<string, string> = {
      'Username is already registered': t.usernameTaken, 'Username is reserved by another account': t.usernameReserved,
      'Email is already registered': t.emailTaken, 'Phone number is already registered': t.phoneTaken,
      'Profile changed in another session. Reload before saving.': t.conflict,
    }
    const message = failure.status === 401 ? t.signInRequired : failure.status === 403 ? t.forbidden : known[failure.message] ?? (locale === 'en' ? failure.message : t.saveFailed)
    return new ApiError(message, failure.status, Object.fromEntries(Object.keys(failure.fieldErrors).map((field) => [field, message])))
  }
  return <form noValidate className="space-y-6 rounded-xl border p-5 sm:p-6" onSubmit={form.submit(async (values) => {
    try { onSaved(await updateAccountProfile(values, profile.updatedAt)) } catch (failure) { throw translateFailure(failure) }
  })}>
    <div className="flex flex-wrap items-center justify-between gap-4"><p className="text-sm text-muted-foreground">{t.description}</p><Button type="button" variant="outline" disabled={busy} onClick={onReload}>{t.reload}</Button></div>
    <FormError message={form.formState.errors.root?.server?.message} />
    <fieldset disabled={busy} className="space-y-5">
      <div className="flex flex-wrap items-center gap-4"><div className="flex size-24 shrink-0 items-center justify-center overflow-hidden rounded-full border bg-muted text-2xl font-medium">
        {picture && validPictureUrl(picture.trim()) && failedPicture !== picture ? <img src={picture.trim()} alt={t.pictureAlt} className="size-full object-cover" referrerPolicy="no-referrer" onError={() => setFailedPicture(picture)} /> : <span aria-label={t.photoPreview}>{Array.from(profile.givenName || profile.username)[0]}{Array.from(profile.familyName || '')[0]}</span>}
      </div><div className="min-w-0 flex-1"><FormField form={form} name="pictureUrl" label={t.pictureUrl} hint={t.pictureHelp} type="url" /><Button type="button" variant="outline" size="sm" className="mt-2" disabled={!picture} onClick={() => form.setValue('pictureUrl', '', { shouldDirty: true, shouldValidate: true })}>{t.removePicture}</Button></div></div>
      <div className="grid gap-4 sm:grid-cols-2"><FormField form={form} name="givenName" label={`${t.givenName} *`} autoComplete="given-name" /><FormField form={form} name="familyName" label={`${t.familyName} *`} autoComplete="family-name" /><FormField form={form} name="nickname" label={t.nickname} autoComplete="nickname" /><FormField form={form} name="username" label={`${t.username} *`} autoComplete="username" hint={t.usernameRule} />
        <div className="space-y-2"><FormField form={form} name="email" label={`${t.email} *`} type="email" autoComplete="email" hint={t.emailHelp} /><Badge variant="outline">{email.trim().toLowerCase() !== profile.email?.toLowerCase() ? t.resetVerification : profile.emailVerified ? t.verified : t.unverified}</Badge></div>
        <div className="space-y-2"><FormField form={form} name="phoneNumber" label={t.phoneNumber} type="tel" autoComplete="tel" hint={t.phoneHelp} /><Badge variant="outline">{phone.trim() !== (profile.phoneNumber ?? '') ? t.resetVerification : profile.phoneNumberVerified ? t.verified : t.unverified}</Badge></div>
      </div>
      <div className="max-w-sm space-y-2"><p className="text-sm font-medium">{t.locale}</p><input type="hidden" {...form.register('locale')} /><LanguageSwitcher value={profileLocale} label={t.locale} disabled={busy} onChange={(value) => { form.setValue('locale', value, { shouldDirty: true, shouldValidate: true }); setLocale(value); form.clearErrors() }} /></div>
      <div className="grid gap-4 border-t pt-5 sm:grid-cols-2">
        <FormField form={form} name="middleName" label={d.middleName} autoComplete="additional-name" />
        <FormField form={form} name="gender" label={d.gender} as="select" options={['', 'male', 'female', 'other'].map(value => ({ value, label: value ? d[value as 'male' | 'female' | 'other'] : d.none }))} />
        <FormField form={form} name="birthdate" label={d.birthdate} type="date" />
        <FormField form={form} name="zoneinfo" label={d.zoneinfo} hint={d.timezoneHint} />
        <FormField form={form} name="profileUrl" label={d.profileUrl} type="url" /><FormField form={form} name="websiteUrl" label={d.websiteUrl} type="url" />
      </div>
      <div className="space-y-4 border-t pt-5"><h2 className="font-semibold">{d.address}</h2><FormField form={form} name="address.streetAddress" label={d.streetAddress} autoComplete="street-address" as="textarea" />
        <div className="grid gap-4 sm:grid-cols-2"><FormField form={form} name="address.locality" label={d.locality} autoComplete="address-level2" /><FormField form={form} name="address.region" label={d.region} autoComplete="address-level1" /><FormField form={form} name="address.postalCode" label={d.postalCode} autoComplete="postal-code" /><FormField form={form} name="address.country" label={d.country} hint={d.countryHint} autoComplete="country" /></div>
      </div>
      {identifiersChanged && <p className="rounded-lg border bg-muted/40 p-3 text-sm">{t.identifiersWarning}</p>}
    </fieldset>
    <div className="flex flex-wrap justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy || !form.formState.isDirty} onClick={() => { form.reset(); setLocale(profile.locale) }}>{t.discard}</Button><Button type="submit" disabled={busy || !form.formState.isDirty}>{busy ? t.saving : t.save}</Button></div>
    <div className="flex flex-wrap gap-x-6 gap-y-2 text-xs text-muted-foreground"><p>{t.created}: {new Date(profile.createdAt).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB')}</p><p>{t.updated}: {new Date(profile.updatedAt).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB')}</p></div>
  </form>
}
