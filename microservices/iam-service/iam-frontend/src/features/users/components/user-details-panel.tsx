import { useEffect, useState } from 'react'
import { Dialog } from '@base-ui/react/dialog'

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
    const [user, setUser] = useState<UserDetails | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [retryKey, setRetryKey] = useState(0)

    useEffect(() => {
        const controller = new AbortController()

        setLoading(true)
        setError(null)
        setUser(null)

        async function loadUser() {
            try {
                const result = await getUser(uuid, controller.signal)

                if (!controller.signal.aborted) {
                    setUser(result)
                }
            } catch (error) {
                if (!controller.signal.aborted) {
                    setError(
                        error instanceof Error
                            ? error.message
                            : 'Could not load user details.',
                    )
                }
            } finally {
                if (!controller.signal.aborted) {
                    setLoading(false)
                }
            }
        }

        void loadUser()

        return () => controller.abort()
    }, [uuid, retryKey])

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
        <Dialog.Root open onOpenChange={(open) => { if (!open) onClose() }}>
          <Dialog.Portal>
            <Dialog.Backdrop className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" />
            <Dialog.Popup className="fixed top-1/2 left-1/2 z-50 max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] max-w-2xl -translate-x-1/2 -translate-y-1/2 overflow-y-auto rounded-xl border bg-background p-6 text-foreground shadow-xl">
            <div className="mb-6 space-y-2">
                <div className="flex items-center justify-between gap-4">
                    <Dialog.Title className="text-lg font-semibold">User details</Dialog.Title>

                    <Dialog.Close render={<Button variant="outline" />}>
                        Close details
                    </Dialog.Close>
                </div>
                <Dialog.Description className="text-sm text-muted-foreground">
                    Profile information, account status, and assigned roles.
                </Dialog.Description>
            </div>

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
            </Dialog.Popup>
          </Dialog.Portal>
        </Dialog.Root>
    )
}
