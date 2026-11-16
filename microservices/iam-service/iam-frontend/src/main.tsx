import { AccountLocaleProvider } from '@/components/shared/account-locale-provider'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { BrowserRouter } from 'react-router-dom'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter basename="/dashboard"><AccountLocaleProvider><App /></AccountLocaleProvider></BrowserRouter>
  </StrictMode>,
)
