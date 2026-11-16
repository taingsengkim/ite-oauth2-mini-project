import type { ReactNode } from 'react'

import { AppHeader } from './app-header'
import { AppSidebar } from './app-sidebar'
import type { DashboardPage } from './app-sidebar'
import { useAccountLocale } from '@/lib/account-locale'
import { shellMessages } from './navigation'

type DashboardLayoutProps = {
    activePage: DashboardPage
    onNavigate: (page: DashboardPage) => void
    children: ReactNode
    authorities: readonly string[]
}

export function DashboardLayout({
                                    activePage,
                                    onNavigate,
                                    children,
                                    authorities,
                                }: DashboardLayoutProps) {
    const { locale } = useAccountLocale()
    return (
        <div className="min-h-screen bg-background text-foreground md:flex">
            <a href="#main-content" className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-50 focus:rounded focus:bg-background focus:p-3">{shellMessages[locale].skip}</a>
            <AppSidebar
                activePage={activePage}
                onNavigate={onNavigate}
                authorities={authorities}
            />

            <div className="min-w-0 flex-1">
                <AppHeader title={activePage} />

                <main
                    id="main-content"
                    className="mx-auto max-w-6xl space-y-6 p-4 sm:p-6 md:p-10"
                >
                    {children}
                </main>
            </div>
        </div>
    )
}
