import { useId } from 'react'
import type { HTMLInputTypeAttribute } from 'react'
import type { FieldValues, Path, UseFormReturn } from 'react-hook-form'
import { Input } from '@/components/ui/input'
import { Controller } from 'react-hook-form'
import { DateTimePicker } from '@/components/shared/date-time-picker'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

export function FormField<T extends FieldValues>({ form, name, label, type = 'text', as = 'input', options = [], hint, disabled, readOnly, placeholder, autoComplete, min, max, maxLength, deps }: {
  form: UseFormReturn<T>; name: Path<T>; label: string; type?: HTMLInputTypeAttribute
  as?: 'input' | 'textarea' | 'select'; options?: { value: string; label: string }[]
  hint?: string; disabled?: boolean; readOnly?: boolean; placeholder?: string; autoComplete?: string
  min?: number; max?: number; maxLength?: number
  deps?: Path<T>[]
}) {
  const id = useId()
  const error = form.getFieldState(name, form.formState).error
  const datePicker = as === 'input' && (type === 'date' || type === 'datetime-local')
  const registration = datePicker || as === 'select' ? undefined : form.register(name, type === 'number' ? { valueAsNumber: true, deps } : { deps })
  const shared = { id, disabled, 'aria-invalid': Boolean(error), 'aria-describedby': error ? `${id}-error` : hint ? `${id}-hint` : undefined }
  return <div className="space-y-2">
    <label htmlFor={id} className="block text-sm font-medium">{label}</label>
    {datePicker ? <Controller control={form.control} name={name} disabled={disabled} rules={{ deps }} render={({ field }) => <DateTimePicker id={id} value={String(field.value ?? '')} onChange={field.onChange} onBlur={field.onBlur} inputRef={field.ref} includeTime={type === 'datetime-local'} disabled={field.disabled} readOnly={readOnly} invalid={Boolean(error)} describedBy={shared['aria-describedby']} label={label} />} />
      : as === 'textarea' ? <textarea {...registration} {...shared} readOnly={readOnly} placeholder={placeholder} maxLength={maxLength} rows={3} className="w-full rounded-md border bg-background p-2 text-sm aria-invalid:border-destructive" />
      : as === 'select' ? <Controller control={form.control} name={name} disabled={disabled} rules={{ deps }} render={({ field }) => <Select items={options} value={String(field.value ?? '')} onValueChange={(value) => field.onChange(value ?? '')} disabled={field.disabled || readOnly}><SelectTrigger {...shared} disabled={field.disabled || readOnly} ref={field.ref} onBlur={field.onBlur} className="h-10 w-full bg-background px-3 text-sm"><SelectValue /></SelectTrigger><SelectContent align="start" alignItemWithTrigger={false}>{options.map(option => <SelectItem key={option.value} value={option.value}>{option.label}</SelectItem>)}</SelectContent></Select>} />
        : <Input {...registration} {...shared} type={type} readOnly={readOnly} placeholder={placeholder} autoComplete={autoComplete} min={min} max={max} maxLength={maxLength} />}
    {hint && !error && <p id={`${id}-hint`} className="text-xs text-muted-foreground">{hint}</p>}
    {error?.message && <p id={`${id}-error`} role="alert" className="text-xs text-destructive">{error.message}</p>}
  </div>
}

export function FormError({ message }: { message?: string }) {
  return message ? <p role="alert" className="text-sm text-destructive">{message}</p> : null
}
