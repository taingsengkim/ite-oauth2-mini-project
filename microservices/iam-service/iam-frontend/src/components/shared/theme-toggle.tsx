import { useState } from 'react'
import { Moon, Sun } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useAccountLocale } from '@/lib/account-locale'

export function ThemeToggle() {
  const { locale } = useAccountLocale()
  const [dark, setDark] = useState(() => document.documentElement.classList.contains('dark'))
  const label = locale === 'km' ? (dark ? 'ប្ដូរទៅរបៀបភ្លឺ' : 'ប្ដូរទៅរបៀបងងឹត') : (dark ? 'Switch to light mode' : 'Switch to dark mode')
  return <Button type="button" variant="outline" size="icon" aria-label={label} title={label} onClick={() => {
    const next = !dark
    document.documentElement.classList.toggle('dark', next)
    document.documentElement.style.colorScheme = next ? 'dark' : 'light'
    try { localStorage.setItem('iam-theme', next ? 'dark' : 'light') } catch { /* Storage may be unavailable. */ }
    setDark(next)
  }}>{dark ? <Sun aria-hidden="true" className="size-4" /> : <Moon aria-hidden="true" className="size-4" />}</Button>
}
