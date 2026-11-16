import { useState } from 'react'
import type { CSSProperties, Ref } from 'react'
import { CalendarDays, Clock3 } from 'lucide-react'
import { enUS, km } from 'date-fns/locale'
import { Calendar } from '@/components/ui/calendar'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Popover, PopoverContent, PopoverTitle, PopoverTrigger } from '@/components/ui/popover'
import { useAccountLocale } from '@/lib/account-locale'
import { localDateValue, parseLocalDateTime, selectedDateValue } from '@/lib/local-date-time'

export function DateTimePicker({ id, value, onChange, onBlur, inputRef, includeTime = true, disabled, readOnly, invalid, describedBy, label }: {
  id: string; value: string; onChange: (value: string) => void; onBlur: () => void; inputRef?: Ref<HTMLButtonElement>
  includeTime?: boolean; disabled?: boolean; readOnly?: boolean; invalid?: boolean; describedBy?: string; label: string
}) {
  const { locale } = useAccountLocale()
  const [open, setOpen] = useState(false)
  const date = parseLocalDateTime(value)
  const t = locale === 'km' ? { choose: 'ជ្រើសរើសកាលបរិច្ឆេទ', time: 'ម៉ោងមូលដ្ឋាន', clear: 'សម្អាត', today: 'ថ្ងៃនេះ', done: 'រួចរាល់' }
    : { choose: 'Choose a date', time: 'Local time', clear: 'Clear', today: 'Today', done: 'Done' }
  const formatted = date ? new Intl.DateTimeFormat(locale === 'km' ? 'km-KH' : 'en-GB', { dateStyle: 'medium', ...(includeTime ? { timeStyle: 'short' as const } : {}) }).format(date) : t.choose
  const locked = disabled || readOnly
  function choose(day: Date | undefined) {
    onChange(day ? selectedDateValue(day, value, includeTime) : '')
    if (!includeTime) { setOpen(false); onBlur() }
  }
  return <Popover open={open} onOpenChange={(next) => { setOpen(next); if (!next) onBlur() }}>
    <PopoverTrigger id={id} ref={inputRef} disabled={locked} aria-invalid={invalid} aria-describedby={describedBy}
      render={<Button type="button" variant="outline" className="h-10 w-full justify-start gap-2 border-input bg-background px-3 text-left text-sm font-normal aria-invalid:border-destructive" />}>
      <CalendarDays className="size-4 shrink-0 text-primary" aria-hidden="true" /><span className={`min-w-0 truncate ${date ? '' : 'text-muted-foreground'}`}>{formatted}</span>
    </PopoverTrigger>
    <PopoverContent align="start" className="max-h-[calc(100dvh-2rem)] w-auto max-w-[calc(100vw-2rem)] gap-0 overflow-y-auto border border-border p-0 shadow-xl">
      <PopoverTitle className="border-b bg-primary/5 px-4 py-3 text-sm font-semibold text-primary">{label}</PopoverTitle>
      <Calendar mode="single" selected={date} defaultMonth={date} onSelect={choose} locale={locale === 'km' ? km : enUS}
        captionLayout="dropdown" startMonth={new Date(1900, 0)} endMonth={new Date(new Date().getFullYear() + 30, 11)}
        style={{ '--cell-size': '2.25rem' } as CSSProperties} classNames={{ today: 'rounded-md bg-accent text-accent-foreground' }} autoFocus disabled={locked} />
      {includeTime && <div className="space-y-2 border-t px-4 py-3"><label htmlFor={`${id}-time`} className="flex items-center gap-2 text-xs font-medium text-primary"><Clock3 className="size-3.5" aria-hidden="true" />{t.time}</label><Input id={`${id}-time`} type="time" step={60} disabled={locked || !date} className="h-10 bg-background" value={date ? value.slice(11, 16) || '00:00' : ''} onChange={(event) => {
        if (date && /^\d{2}:\d{2}$/.test(event.target.value)) onChange(`${localDateValue(date)}T${event.target.value}`)
      }} /></div>}
      <div className="flex flex-wrap items-center justify-between gap-2 border-t bg-muted/40 p-3"><Button type="button" variant="ghost" disabled={locked} onClick={() => choose(new Date())}>{t.today}</Button><div className="flex gap-2"><Button type="button" variant="ghost" disabled={locked || !value} onClick={() => { onChange(''); setOpen(false); onBlur() }}>{t.clear}</Button><Button type="button" disabled={locked} onClick={() => { setOpen(false); onBlur() }}>{t.done}</Button></div></div>
    </PopoverContent>
  </Popover>
}
