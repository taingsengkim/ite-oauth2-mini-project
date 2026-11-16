import { userFilterSchema, defaultUserFilters } from '../user-filter-schema'
import type { UserFilters } from '../user-filter-schema'
import { Filter, RotateCcw, Search } from 'lucide-react'
import { FormField } from '@/components/shared/form-field'
import { Button } from '@/components/ui/button'
import { useSchemaForm } from '@/lib/forms'

export function UserFiltersForm({ onApply }: { onApply: (filters: UserFilters) => void }) {
  const form = useSchemaForm(userFilterSchema, defaultUserFilters)
  return <form noValidate onSubmit={form.submit(onApply)} className="space-y-4 rounded-xl border bg-card p-5">
    <div className="flex items-center gap-2 text-sm font-medium"><Filter className="size-4 text-muted-foreground" aria-hidden="true" />Filter users</div>
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4"><div className="sm:col-span-2 xl:col-span-1"><FormField form={form} name="search" label="Search" type="search" placeholder="Name, username or email" /></div>
    <FormField form={form} name="enabled" label="Account status" as="select" options={[{value:'',label:'All accounts'},{value:'true',label:'Enabled'},{value:'false',label:'Disabled'}]} />
    <FormField form={form} name="locked" label="Account lock" as="select" options={[{value:'',label:'All lock states'},{value:'true',label:'Locked'},{value:'false',label:'Unlocked'}]} />
    <FormField form={form} name="emailVerified" label="Email verification" as="select" options={[{value:'',label:'All emails'},{value:'true',label:'Verified'},{value:'false',label:'Unverified'}]} /></div>
    <div className="flex flex-col gap-4 border-t pt-4 sm:flex-row sm:items-end sm:justify-between"><div className="grid gap-4 sm:grid-cols-2"><FormField form={form} name="sort" label="Sort by" as="select" options={[{value:'username,asc',label:'Username A?Z'},{value:'username,desc',label:'Username Z?A'},{value:'createdAt,desc',label:'Newest first'},{value:'createdAt,asc',label:'Oldest first'}]} /><FormField form={form} name="size" label="Rows per page" as="select" options={['10','20','50','100'].map(value=>({value,label:value}))} /></div><div className="flex gap-2"><Button type="button" variant="outline" onClick={()=>{form.reset(defaultUserFilters);onApply(defaultUserFilters)}}><RotateCcw className="size-4" aria-hidden="true" />Reset</Button><Button type="submit"><Search className="size-4" aria-hidden="true" />Apply filters</Button></div></div>
  </form>
}
