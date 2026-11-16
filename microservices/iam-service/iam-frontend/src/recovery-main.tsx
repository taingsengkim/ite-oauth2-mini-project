import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { AccountLocaleProvider } from '@/components/shared/account-locale-provider'
import { RecoveryPage } from '@/features/account/recovery/pages/recovery-page'
import './index.css'

// Keep the secret in memory only. Remove it before the application makes requests.
const token = new URLSearchParams(window.location.hash.slice(1)).get('token') || ''
window.history.replaceState(null, '', window.location.pathname + window.location.search)
createRoot(document.getElementById('root')!).render(<StrictMode><AccountLocaleProvider><RecoveryPage token={token} /></AccountLocaleProvider></StrictMode>)
