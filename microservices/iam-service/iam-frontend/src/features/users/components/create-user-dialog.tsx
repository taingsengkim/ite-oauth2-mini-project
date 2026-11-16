import { PasswordPolicyBoundary } from '@/components/shared/password-policy-boundary'
import { passwordWithRequirements, passwordRequirementsText } from '@/lib/password-policy'
import type { PasswordRequirements } from '@/lib/password-policy'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { FormField, FormError } from '@/components/shared/form-field'
import { Button } from '@/components/ui/button'
import { useSchemaForm } from '@/lib/forms'
import { createUserSchema } from '@/lib/form-schemas'
import { createUser } from '../api/users-api'
import type { UserDetails } from '../types/types'

type Props = { onClose: () => void; onCreated: (user: UserDetails) => void }
export function CreateUserDialog(props: Props) {
  return <PasswordPolicyBoundary title="Create user" onClose={props.onClose}>{(requirements) => <CreateUserForm {...props} requirements={requirements} />}</PasswordPolicyBoundary>
}
function CreateUserForm({ onClose, onCreated, requirements }: Props & { requirements: PasswordRequirements }) {
  const form = useSchemaForm(createUserSchema.safeExtend({ password: passwordWithRequirements(requirements) }), { username: '', email: '', givenName: '', familyName: '', password: '', confirmation: '', phoneNumber: '', locale: 'en' })
  const busy = form.formState.isSubmitting
  return <ManagementDialog title="Create user" description="Create an account with the default USER role. Required fields are marked with *." busy={busy} onClose={onClose}>
    <form noValidate className="space-y-5" onSubmit={form.submit(async (values) => {
      const user = await createUser({ username: values.username, email: values.email.toLowerCase(), password: values.password, givenName: values.givenName, familyName: values.familyName, locale: values.locale, phoneNumber: values.phoneNumber || null })
      form.reset(); onCreated(user)
    })} aria-busy={busy}>
      <FormError message={form.formState.errors.root?.server?.message} />
      <fieldset disabled={busy} className="grid gap-4 sm:grid-cols-2">
        <FormField form={form} name="givenName" label="Given name *" autoComplete="given-name" />
        <FormField form={form} name="familyName" label="Family name *" autoComplete="family-name" />
        <FormField form={form} name="username" label="Username *" autoComplete="off" />
        <FormField form={form} name="email" label="Email *" type="email" autoComplete="off" />
        <FormField form={form} name="password" label="Password *" type="password" autoComplete="new-password" hint={passwordRequirementsText(requirements)} deps={['confirmation']} />
        <FormField form={form} name="confirmation" label="Confirm password *" type="password" autoComplete="new-password" />
        <FormField form={form} name="locale" label="Language *" as="select" options={[{ value: 'en', label: 'English' }, { value: 'km', label: 'Khmer' }]} />
        <FormField form={form} name="phoneNumber" label="Phone number" type="tel" placeholder="+85512345678" hint="Optional. Use international format, starting with +." />
      </fieldset>
      <div className="flex justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button><Button type="submit" disabled={busy}>{busy ? 'Creating...' : 'Create user'}</Button></div>
    </form>
  </ManagementDialog>
}
