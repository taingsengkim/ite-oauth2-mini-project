import { useEffect, useState } from 'react'
import { ManagementDialog } from '@/components/shared/management-dialog'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'

import { getUser } from '../api/users-api'
import type { UserDetails } from '../types/types'

const defaultAvatar =
    'https://static.vecteezy.com/system/resources/thumbnails/009/292/244/small/default-avatar-icon-of-social-media-user-vector.jpg'

type UserDetailsPanelProps = {
    uuid: string
    onClose: () => void
}

function formatDate(value: string | null) {
    return value ? new Date(value).toLocaleString() : 'Not available'
}

export function UserDetailsPanel({
                                     uuid,
                                     onClose,
                                 }: UserDetailsPanelProps) {
    const [retryKey, setRetryKey] = useState(0)
    const key = uuid + ':' + retryKey
    const [result, setResult] = useState<{ key: string; user?: UserDetails; error?: string } | null>(null)
    const loading = result?.key !== key
    const user = !loading ? result?.user : undefined
    const error = !loading ? result?.error : undefined

    useEffect(() => {
        const controller = new AbortController()



        async function loadUser() {
            try {
                const result = await getUser(uuid, controller.signal)

                if (!controller.signal.aborted) {
                    setResult({ key, user: result })
                }
            } catch (error) {
                if (!controller.signal.aborted) {
                    setResult({ key, error: error instanceof Error ? error.message : 'Could not load user details.' })
                }
            }
        }

        void loadUser()

        return () => controller.abort()
    }, [uuid, key])

    const fields = user
        ? [
            ['Username', user.username],
            ['Email', user.email || 'Not provided'],
            ['Nickname', user.nickname || 'Not provided'],
            ['Phone number', user.phoneNumber || 'Not provided'],
            ['Given name', user.givenName],
            ['Family name', user.familyName],
            ['Locale', user.locale || 'Not provided'],
            ['Email verified', user.emailVerified ? 'Yes' : 'No'],
            ['Created', formatDate(user.createdAt)],
            ['Updated', formatDate(user.updatedAt)],
            ['User ID', user.uuid],
        ]
        : []

    return (
        <ManagementDialog title="User details" description="Profile information, account status, and assigned roles." onClose={onClose}>
            <div className="space-y-6" aria-busy={loading}>
                {loading && <p role="status">Loading user details...</p>}

                {error && (
                    <div role="alert" className="space-y-3">
                        <p className="text-destructive">{error}</p>
                        <Button
                            variant="outline"
                            onClick={() => setRetryKey((value) => value + 1)}
                        >
                            Retry
                        </Button>
                    </div>
                )}

                {user && (
                    <>
                        <div className="flex items-center gap-4">
                            <img
                                src={user.pictureUrl || defaultAvatar}
                                onError={(event) => {
                                    event.currentTarget.onerror = null
                                    if (event.currentTarget.src !== defaultAvatar) {
                                        event.currentTarget.src = defaultAvatar
                                    }
                                }}
                                alt=""
                                className="size-16 rounded-full border object-cover"
                            />

                            <div className="space-y-2">
                                <h2 className="text-lg font-semibold">
                                    {user.name || user.username}
                                </h2>

                                <Badge
                                    variant={user.enabled ? 'secondary' : 'outline'}
                                >
                                    {user.enabled ? 'Enabled' : 'Disabled'}
                                </Badge>
                                <Badge className="ml-2" variant={user.accountNonLocked ? 'outline' : 'destructive'}>
                                    {user.accountNonLocked ? 'Unlocked' : 'Locked'}
                                </Badge>
                            </div>
                        </div>

                        <dl className="grid gap-4 sm:grid-cols-2">
                            {fields.map(([label, value]) => (
                                <div key={label}>
                                    <dt className="text-sm text-muted-foreground">
                                        {label}
                                    </dt>
                                    <dd className="mt-1 break-words text-sm font-medium">
                                        {value}
                                    </dd>
                                </div>
                            ))}
                        </dl>

                        <div className="space-y-2">
                            <h3 className="text-sm font-medium">Roles</h3>

                            <div className="flex flex-wrap gap-2">
                                {user.roles.length > 0 ? (
                                    user.roles.map((role) => (
                                        <Badge key={role.uuid} variant="secondary">
                                            {role.name}
                                        </Badge>
                                    ))
                                ) : (
                                    <p className="text-sm text-muted-foreground">
                                        No roles assigned.
                                    </p>
                                )}
                            </div>
                        </div>
                    </>
                )}
            </div>
        </ManagementDialog>
    )
}
