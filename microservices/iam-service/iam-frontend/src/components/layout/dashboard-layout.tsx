import type { ReactNode } from 'react'

import { AppHeader } from './app-header'
import { AppSidebar } from './app-sidebar'
import type { DashboardPage } from './app-sidebar'

type DashboardLayoutProps = {
    activePage: DashboardPage
    onNavigate: (page: DashboardPage) => void
    children: ReactNode
}

export function DashboardLayout({
                                    activePage,
                                    onNavigate,
                                    children,
                                }: DashboardLayoutProps) {
    return (
        <div className="min-h-screen bg-background text-foreground md:flex">
            <AppSidebar
                activePage={activePage}
                onNavigate={onNavigate}
            />

            <div className="min-w-0 flex-1">
                <AppHeader title={activePage} />

                <main
                    id="main-content"
                    className="mx-auto max-w-6xl space-y-6 p-6 md:p-10"
                >
                    {children}
                </main>
            </div>
        </div>
    )
}
