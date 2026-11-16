import type { ReactNode } from 'react'
import { Button } from '@/components/ui/button'
import { FormField } from './form-field'
import { useSchemaForm } from '@/lib/forms'
import { searchSchema, permissionSearchSchema } from '@/lib/form-schemas'

export function SearchForm({ onSearch, label = 'Search', placeholder, disabled = false, children }: {
  onSearch: (search: string) => void; label?: string; placeholder?: string; disabled?: boolean; children?: ReactNode
}) {
  const form = useSchemaForm(searchSchema, { search: '' })
  return <form noValidate className="flex flex-wrap items-end gap-3" onSubmit={form.submit(({ search }) => onSearch(search))}>
    <FormField form={form} name="search" label={label} type="search" placeholder={placeholder} disabled={disabled} />
    <Button type="submit" variant="outline" disabled={disabled}>Search</Button>{children}
  </form>
}

export function PermissionSearchForm({ onSearch, children }: { onSearch: (values: { search: string; resource: string; action: string }) => void; children?: ReactNode }) {
  const form = useSchemaForm(permissionSearchSchema, { search: '', resource: '', action: '' })
  return <form noValidate className="flex flex-wrap items-end gap-3" onSubmit={form.submit(onSearch)}>
    <FormField form={form} name="search" label="Search permissions" type="search" />
    <FormField form={form} name="resource" label="Resource (exact)" /><FormField form={form} name="action" label="Action (exact)" />
    <Button type="submit" variant="outline">Filter</Button>{children}
  </form>
}
