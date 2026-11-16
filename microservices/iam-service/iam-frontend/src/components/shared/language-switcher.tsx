import type { AccountLocale } from '@/features/account/i18n'

const languages = [
  { value: 'en', label: 'English', flag: '/assets/flags/united-kingdom.png' },
  { value: 'km', label: 'ខ្មែរ', flag: '/assets/flags/cambodia.webp' },
] as const

export function LanguageSwitcher({ value, onChange, disabled = false, label = 'Language / ភាសា' }: {
  value: AccountLocale; onChange: (value: AccountLocale) => void; disabled?: boolean; label?: string
}) {
  return <div role="group" aria-label={label} className="inline-flex shrink-0 items-center gap-1 rounded-lg border bg-background p-1">
    {languages.map((language) => <button key={language.value} type="button" aria-label={language.label} title={language.label} aria-pressed={value === language.value} disabled={disabled}
      onClick={() => onChange(language.value)} className={`flex size-10 items-center justify-center rounded-md border outline-none transition-colors focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50 ${value === language.value ? 'border-primary bg-primary/10' : 'border-transparent hover:bg-muted'}`}>
      <img src={language.flag} alt="" aria-hidden="true" className="h-5 w-8 rounded-sm object-contain" width="32" height="20" />
    </button>)}
  </div>
}
