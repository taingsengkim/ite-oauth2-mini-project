import { useId } from 'react'
import type { HTMLInputTypeAttribute } from 'react'
import type { FieldValues, Path, UseFormReturn } from 'react-hook-form'
import { Input } from '@/components/ui/input'

export function FormField<T extends FieldValues>({ form, name, label, type = 'text', as = 'input', options = [], hint, disabled, readOnly, placeholder, autoComplete, min, max, maxLength, deps }: {
  form: UseFormReturn<T>; name: Path<T>; label: string; type?: HTMLInputTypeAttribute
  as?: 'input' | 'textarea' | 'select'; options?: { value: string; label: string }[]
  hint?: string; disabled?: boolean; readOnly?: boolean; placeholder?: string; autoComplete?: string
  min?: number; max?: number; maxLength?: number
  deps?: Path<T>[]
}) {
  const id = useId()
  const error = form.getFieldState(name, form.formState).error
  const registration = form.register(name, type === 'number' ? { valueAsNumber: true, deps } : { deps })
  const shared = { id, disabled, 'aria-invalid': Boolean(error), 'aria-describedby': error ? `${id}-error` : hint ? `${id}-hint` : undefined }
  return <div className="space-y-2">
    <label htmlFor={id} className="block text-sm font-medium">{label}</label>
    {as === 'textarea' ? <textarea {...registration} {...shared} readOnly={readOnly} placeholder={placeholder} maxLength={maxLength} rows={3} className="w-full rounded-md border bg-background p-2 text-sm aria-invalid:border-destructive" />
      : as === 'select' ? <select {...registration} {...shared} className="h-8 w-full rounded-md border bg-background px-2 text-sm aria-invalid:border-destructive">{options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select>
        : <Input {...registration} {...shared} type={type} readOnly={readOnly} placeholder={placeholder} autoComplete={autoComplete} min={min} max={max} maxLength={maxLength} />}
    {hint && !error && <p id={`${id}-hint`} className="text-xs text-muted-foreground">{hint}</p>}
    {error?.message && <p id={`${id}-error`} role="alert" className="text-xs text-destructive">{error.message}</p>}
  </div>
}

export function FormError({ message }: { message?: string }) {
  return message ? <p role="alert" className="text-sm text-destructive">{message}</p> : null
}
