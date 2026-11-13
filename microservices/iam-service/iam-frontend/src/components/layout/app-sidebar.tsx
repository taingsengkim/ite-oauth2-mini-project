import {
    KeyRound,
    LayoutDashboard,
    Shield,
    Users,
} from 'lucide-react'

import { Button } from '@/components/ui/button'

const navigation = [
    { name: 'Overview', icon: LayoutDashboard },
    { name: 'Users', icon: Users },
    { name: 'Roles', icon: Shield },
    { name: 'Permissions', icon: KeyRound },
] as const

export type DashboardPage =
    (typeof navigation)[number]['name']

type AppSidebarProps = {
    activePage: DashboardPage
    onNavigate: (page: DashboardPage) => void
}

export function AppSidebar({
                               activePage,
                               onNavigate,
                           }: AppSidebarProps) {
    return (
        <aside className="border-b bg-muted/30 p-5 md:w-64 md:shrink-0 md:border-r md:border-b-0">
            <div className="mb-8 flex items-center gap-3">
                <div className="rounded-lg bg-primary p-2 text-primary-foreground">
                    <Shield aria-hidden="true" className="size-5" />
                </div>

                <div>
                    <p className="font-semibold">ISTAD IAM</p>
                    <p className="text-xs text-muted-foreground">
                        Identity & access
                    </p>
                </div>
            </div>

            <nav
                aria-label="Main navigation"
                className="space-y-1"
            >
                {navigation.map(({ name, icon: Icon }) => (
                    <Button
                        key={name}
                        variant={activePage === name ? 'secondary' : 'ghost'}
                        className="w-full justify-start gap-3"
                        aria-current={activePage === name ? 'page' : undefined}
                        onClick={() => onNavigate(name)}
                    >
                        <Icon aria-hidden="true" className="size-4" />
                        {name}
                    </Button>
                ))}
            </nav>
        </aside>
    )
}