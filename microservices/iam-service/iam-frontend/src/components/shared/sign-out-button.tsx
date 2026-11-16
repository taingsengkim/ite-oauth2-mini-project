import { useRef, useState } from 'react'
import { LogOut } from 'lucide-react'
import { Dialog } from '@base-ui/react/dialog'
import { Button } from '@/components/ui/button'
import { useAccountLocale } from '@/lib/account-locale'
import { shellMessages } from '@/components/layout/navigation'
import { ApiError, getDashboardSession } from '@/lib/api-client'

export function SignOutButton() {
  const { locale } = useAccountLocale()
  const t = shellMessages[locale]
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(false)
  const [open, setOpen] = useState(false)
  const cancelRef = useRef<HTMLButtonElement>(null)
  const confirmation = locale === 'km'
    ? { title: 'តើអ្នកចង់ចាកចេញមែនទេ?', description: 'អ្នកនឹងចាកចេញពីវគ្គបច្ចុប្បន្ន ហើយត្រឡប់ទៅទំព័រចូល។', cancel: 'បោះបង់' }
    : { title: 'Sign out of your account?', description: 'You will end your current session and return to the sign-in page.', cancel: 'Cancel' }
  const pending = useRef(false)
  async function signOut() {
    if (pending.current) return
    pending.current = true; setBusy(true); setError(false)
    try {
      const session = await getDashboardSession()
      const form = document.createElement('form'); form.method = 'POST'; form.action = '/logout'
      for (const [name, value] of Object.entries({ _csrf: session.csrfToken, lang: locale })) {
        const input = document.createElement('input'); input.type = 'hidden'; input.name = name; input.value = value; form.append(input)
      }
      document.body.append(form)
      try { form.submit() } finally { form.remove() }
    } catch (failure) {
      if (failure instanceof ApiError && failure.status === 401) { window.location.replace('/login'); return }
      pending.current = false; setBusy(false); setError(true)
    }
  }
  return <Dialog.Root open={open} onOpenChange={(next) => { if (!pending.current) { setOpen(next); setError(false) } }}>
    <Dialog.Trigger render={<Button type="button" variant="ghost" className="h-11 w-full justify-start gap-3 rounded-lg border border-sidebar-border px-3 text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground" disabled={busy} />}><LogOut className="size-4" aria-hidden="true" />{t.logout}</Dialog.Trigger>
    <Dialog.Portal>
      <Dialog.Backdrop className="fixed inset-0 z-50 bg-black/50" />
      <Dialog.Popup initialFocus={cancelRef} className="fixed top-1/2 left-1/2 z-50 w-[calc(100%-2rem)] max-w-sm -translate-x-1/2 -translate-y-1/2 space-y-4 rounded-xl border bg-popover p-6 text-popover-foreground shadow-xl" aria-busy={busy}>
        <div className="flex size-10 items-center justify-center rounded-full bg-muted"><LogOut className="size-5" aria-hidden="true" /></div>
        <Dialog.Title className="text-lg font-semibold">{confirmation.title}</Dialog.Title>
        <Dialog.Description className="text-sm leading-relaxed text-muted-foreground">{confirmation.description}</Dialog.Description>
        {error && <p role="alert" className="text-sm text-destructive">{t.logoutFailed}</p>}
        <div className="flex justify-end gap-2 pt-2"><Button ref={cancelRef} type="button" variant="outline" disabled={busy} onClick={() => setOpen(false)}>{confirmation.cancel}</Button><Button type="button" disabled={busy} onClick={() => { void signOut() }}>{busy ? t.signingOut : t.logout}</Button></div>
      </Dialog.Popup>
    </Dialog.Portal>
  </Dialog.Root>
}
