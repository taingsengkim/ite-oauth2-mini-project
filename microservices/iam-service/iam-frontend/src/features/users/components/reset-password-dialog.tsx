import { PasswordPolicyBoundary } from '@/components/shared/password-policy-boundary'
import { passwordWithRequirements, passwordRequirementsText } from '@/lib/password-policy'
import type { PasswordRequirements } from '@/lib/password-policy'
import { useState } from 'react'
import { ManagementDialog } from '@/components/shared/management-dialog'
import { FormField, FormError } from '@/components/shared/form-field'
import { Button } from '@/components/ui/button'
import { useSchemaForm } from '@/lib/forms'
import { resetPasswordSchema } from '@/lib/form-schemas'
import { resetUserPassword } from '../api/users-api'
import type { User } from '../types/types'
type Props = { user: User; onClose: () => void; onReset: (user: User) => void }
export function ResetPasswordDialog(props: Props) {
  return <PasswordPolicyBoundary title="Reset password" onClose={props.onClose}>{(requirements) => <ResetPasswordForm {...props} requirements={requirements} />}</PasswordPolicyBoundary>
}
function ResetPasswordForm({ user, onClose, onReset, requirements }: Props & { requirements: PasswordRequirements }) {
  const form = useSchemaForm(resetPasswordSchema.safeExtend({ newPassword: passwordWithRequirements(requirements) }), { newPassword: '', confirmPassword: '' })
  const [show, setShow] = useState(false)
  const busy = form.formState.isSubmitting
  return <ManagementDialog title="Reset password" description="Set a new IAM password. Linked social sign-ins remain available. Existing browser sessions will be terminated." busy={busy} onClose={onClose}>
    <p className="mb-5 text-sm font-medium">{user.username}</p>
    <form noValidate className="space-y-4" onSubmit={form.submit(async (values) => { await resetUserPassword(user.uuid, values.newPassword, values.confirmPassword); form.reset(); onReset(user) })}>
      <FormError message={form.formState.errors.root?.server?.message} />
      <fieldset disabled={busy} className="space-y-4"><FormField form={form} name="newPassword" label="New password *" type={show ? 'text' : 'password'} autoComplete="new-password" hint={passwordRequirementsText(requirements)} deps={['confirmPassword']} /><FormField form={form} name="confirmPassword" label="Confirm password *" type={show ? 'text' : 'password'} autoComplete="new-password" />
      <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={show} onChange={(event) => setShow(event.target.checked)} />Show passwords</label></fieldset>
      <div className="flex justify-end gap-2 border-t pt-4"><Button type="button" variant="outline" disabled={busy} onClick={onClose}>Cancel</Button><Button type="submit" disabled={busy}>{busy ? 'Resetting...' : 'Reset password'}</Button></div>
    </form>
  </ManagementDialog>
}
