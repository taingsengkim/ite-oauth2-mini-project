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

export function ProfilePage() {
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
    {saved && <div role="status" className="space-y-3 rounded-lg border bg-muted/40 p-4 text-sm"><p>{saved.signInRequired ? t.signedOut : t.saved}</p>{saved.signInRequired && <a className="font-medium underline" href="/dashboard/index.html?view=profile">{t.signIn}</a>}</div>}
    {saved?.signInRequired ? <p className="break-words text-sm">{t.username}: <strong>{saved.profile.username}</strong> · {t.email}: <strong>{saved.profile.email}</strong></p>
      : result?.key !== revision ? <p role="status">{t.loading}</p> : result.error ? <div className="space-y-3"><FormError message={result.error === 'unauthenticated' ? t.signInRequired : t.loadFailed} /><Button variant="outline" onClick={() => setRevision((value) => value + 1)}>{t.retry}</Button>{result.error === 'unauthenticated' && <a className="ml-3 text-sm underline" href="/dashboard/index.html?view=profile">{t.signIn}</a>}</div>
        : result.profile && <ProfileForm key={`${revision}-${result.profile.updatedAt}`} profile={result.profile} onReload={() => { setSaved(null); setRevision((value) => value + 1) }} onSaved={(value) => { setSaved(value); setResult({ key: revision, profile: value.profile }) }} />}
  </section>
}

function ProfileForm({ profile, onReload, onSaved }: { profile: AccountProfile; onReload: () => void; onSaved: (value: ProfileSaved) => void }) {
  const { locale, setLocale } = useAccountLocale()
  const t = accountMessages[locale]
  const form = useSchemaForm(profileSchema(locale, profile.username), { username: profile.username, email: profile.email || '', givenName: profile.givenName, familyName: profile.familyName,
    nickname: profile.nickname || '', phoneNumber: profile.phoneNumber || '', pictureUrl: profile.pictureUrl || '', locale: profile.locale === 'km' ? 'km' : 'en' })
  const [username, email, phone, picture] = useWatch({ control: form.control, name: ['username', 'email', 'phoneNumber', 'pictureUrl'] })
  const [failedPicture, setFailedPicture] = useState<string | null>(null)
  const busy = form.formState.isSubmitting
  const identifiersChanged = username.trim() !== profile.username || email.trim().toLowerCase() !== profile.email?.toLowerCase()
  const localeRegistration = form.register('locale')
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
      <div className="max-w-sm space-y-2"><label htmlFor="account-locale" className="text-sm font-medium">{t.locale}</label><select {...localeRegistration} id="account-locale" className="h-9 w-full rounded-md border bg-background px-3 text-sm" onChange={(event) => { void localeRegistration.onChange(event); setLocale(event.target.value === 'km' ? 'km' : 'en'); form.clearErrors() }}><option value="en">English</option><option value="km">ខ្មែរ</option></select></div>
      {identifiersChanged && <p className="rounded-lg border bg-muted/40 p-3 text-sm">{t.identifiersWarning}</p>}
    </fieldset>
    <div className="flex flex-wrap justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy || !form.formState.isDirty} onClick={() => { form.reset(); setLocale(profile.locale) }}>{t.discard}</Button><Button type="submit" disabled={busy || !form.formState.isDirty}>{busy ? t.saving : t.save}</Button></div>
    <div className="flex flex-wrap gap-x-6 gap-y-2 text-xs text-muted-foreground"><p>{t.created}: {new Date(profile.createdAt).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB')}</p><p>{t.updated}: {new Date(profile.updatedAt).toLocaleString(locale === 'km' ? 'km-KH' : 'en-GB')}</p></div>
  </form>
}
