import { useRef } from 'react'
import type { BaseSyntheticEvent } from 'react'
import { useForm } from 'react-hook-form'
import type { DefaultValues, FieldValues, Path, UseFormReturn } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import type { z } from 'zod'
import { ApiError } from './api-client'

export function useSchemaForm<T extends FieldValues>(schema: z.ZodType<T, T>, defaultValues: DefaultValues<NoInfer<T>>) {
  const form = useForm<T>({ resolver: zodResolver(schema), defaultValues, mode: 'onTouched', reValidateMode: 'onChange' })
  const pending = useRef(false)
  // Keep the synchronous guard: async resolvers can otherwise admit two rapid submissions.
  function submit(operation: (values: T) => Promise<void> | void) {
    return async (event?: BaseSyntheticEvent) => {
      event?.preventDefault()
      if (pending.current) return
      pending.current = true
      try {
        await form.handleSubmit(async (values) => {
          form.clearErrors('root')
          try { await operation(values) }
          catch (failure) { applyFormError(form, failure) }
        })(event)
      }
      finally { pending.current = false }
    }
  }
  return { ...form, submit }
}

export function applyFormError<T extends FieldValues>(form: UseFormReturn<T>, failure: unknown) {
  if (failure instanceof ApiError) {
    for (const [field, message] of Object.entries(failure.fieldErrors)) {
      if (Object.hasOwn(form.getValues(), field)) form.setError(field as Path<T>, { type: 'server', message }, { shouldFocus: true })
    }
  }
  form.setError('root.server', { type: 'server', message: failure instanceof Error ? failure.message : 'Could not save changes. Please try again.' })
}
