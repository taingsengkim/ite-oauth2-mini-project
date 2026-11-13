import { Badge } from '@/components/ui/badge'

type AppHeaderProps = {
    title: string
}

export function AppHeader({ title }: AppHeaderProps) {
    return (
        <header className="flex h-16 items-center justify-between gap-4 border-b px-6">
            <p className="text-sm">
        <span className="text-muted-foreground">
          Workspace /{' '}
        </span>
                <span className="font-medium">{title}</span>
            </p>

            <Badge variant="outline">IAM Console</Badge>
        </header>
    )
}
