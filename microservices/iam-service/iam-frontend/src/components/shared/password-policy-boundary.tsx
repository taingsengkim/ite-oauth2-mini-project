import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { ManagementDialog } from './management-dialog'
import { FormError } from './form-field'
import { Button } from '@/components/ui/button'
import { getPasswordRequirements } from '@/lib/password-policy'
import type { PasswordRequirements } from '@/lib/password-policy'

export function PasswordPolicyBoundary({ title, onClose, children }: { title: string; onClose: () => void; children: (requirements: PasswordRequirements) => ReactNode }) {
  const [policy, setPolicy] = useState<PasswordRequirements | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    getPasswordRequirements(controller.signal).then((value) => { if (!controller.signal.aborted) setPolicy(value) })
      .catch((failure: unknown) => { if (!controller.signal.aborted) setError(failure instanceof Error ? failure.message : 'Could not load password requirements.') })
    return () => controller.abort()
  }, [revision])
  if (policy) return children(policy)
  return <ManagementDialog title={title} description="Loading the current password requirements." onClose={onClose}>
    {error ? <div className="space-y-3"><FormError message={error} /><Button variant="outline" onClick={() => { setError(null); setRevision((value) => value + 1) }}>Retry</Button></div> : <p role="status">Loading password policy...</p>}
  </ManagementDialog>
}
