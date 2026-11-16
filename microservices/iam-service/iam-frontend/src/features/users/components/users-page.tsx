import { useNavigate, useLocation } from 'react-router-dom'
import { UserFiltersForm } from './user-filters'
import { defaultUserFilters } from '../user-filter-schema'
import type { UserFilters } from '../user-filter-schema'
import { Plus, History, RefreshCw } from 'lucide-react'

import { useEffect, useState } from 'react'

import { Button } from '@/components/ui/button'

import { listUsers } from '../api/users-api'
import { UsersTable } from '../components/users-table'
import type { UserPageResponse } from '../types/types'
import { getDashboardSession } from '@/lib/api-client'


export function UsersPage() {
    const navigate = useNavigate()
    const [canRead, setCanRead] = useState(false)
    const [canCreate, setCanCreate] = useState(false)
    const [canUpdate, setCanUpdate] = useState(false)
    const [canDelete, setCanDelete] = useState(false)
    const [canAssignRoles, setCanAssignRoles] = useState(false)
    const [currentUserUuid, setCurrentUserUuid] = useState<string | null>(null)
    const [sessionError, setSessionError] = useState<string | null>(null)
    const success = useLocation().state?.notice as string | undefined

    useEffect(() => {
        const controller = new AbortController()
        getDashboardSession(controller.signal)
            .then((session) => {
                if (!controller.signal.aborted) {
                    setCanRead(session.authorities.includes('user:read'))
                    setCanCreate(session.authorities.includes('user:create'))
                    setCanUpdate(session.authorities.includes('user:update'))
                    setCanDelete(session.authorities.includes('user:delete'))
                    setCanAssignRoles(session.authorities.includes('user:assign'))
                    setCurrentUserUuid(session.userUuid)
                }
            })
            .catch((failure: unknown) => {
                if (!controller.signal.aborted) {
                    setSessionError(failure instanceof Error ? failure.message : 'Could not load account permissions.')
                }
            })
        return () => controller.abort()
    }, [])

    const [search, setSearch] = useState('')
    const [filters, setFilters] = useState<UserFilters>(defaultUserFilters)
    const [page, setPage] = useState(0)

    const [retryKey, setRetryKey] = useState(0)
    const queryKey = JSON.stringify([page, search, filters, retryKey])
    const [result, setResult] = useState<{ key: string; data?: UserPageResponse; error?: string } | null>(null)
    const loading = result?.key !== queryKey
    const data = !loading ? result?.data : undefined
    const error = !loading ? result?.error : undefined

    useEffect(() => {
        const controller = new AbortController()



        async function loadUsers() {
            try {
                const result = await listUsers({
                    page,
                    size: Number(filters.size),
                    enabled: filters.enabled, locked: filters.locked, emailVerified: filters.emailVerified, sort: filters.sort,
                    search,
                    signal: controller.signal,
                })

                if (!controller.signal.aborted) {
                    setResult({ key: queryKey, data: result })
                }
            } catch (error) {
                if (!controller.signal.aborted) {
                    setResult({ key: queryKey, error: error instanceof Error ? error.message : 'Could not load users.' })
                }
            }
        }

        void loadUsers()

        return () => controller.abort()
    }, [page, search, filters, queryKey])


    return (
        <section className="space-y-4" aria-label="User management">
            {success && <p role="status" className="rounded-lg border bg-muted/40 p-3 text-sm">{success}</p>}
            {sessionError && <p role="alert" className="text-sm text-destructive">{sessionError}</p>}
            <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border bg-card p-4"><p className="text-sm text-muted-foreground">Manage accounts, access and security.</p><div className="flex flex-wrap gap-2"><Button variant="outline" disabled={loading} onClick={() => setRetryKey(value => value + 1)}><RefreshCw className="size-4" aria-hidden="true" />Refresh</Button>{canRead && <Button variant="outline" onClick={() => { void navigate('/users/login-history') }}><History className="size-4" aria-hidden="true" />Login history</Button>}{canCreate && <Button onClick={() => { void navigate('/users/new') }}><Plus className="size-4" aria-hidden="true" />Create user</Button>}</div></div>
            <UserFiltersForm onApply={(values) => { setFilters(values); setSearch(values.search); setPage(0); setRetryKey(current => current + 1) }} />

            {loading && (
                <p role="status" className="py-8 text-muted-foreground">
                    Loading users...
                </p>
            )}

            {!loading && error && (
                <div role="alert" className="space-y-3 rounded-lg border p-5">
                    <p className="text-destructive">{error}</p>

                    <Button
                        variant="outline"
                        onClick={() => setRetryKey((value) => value + 1)}
                    >
                        Retry
                    </Button>

                    <a
                        href="/dashboard/index.html"
                        className="ml-4 text-sm underline"
                    >
                        Open dashboard sign-in
                    </a>
                </div>
            )}

            {!loading && !error && data && (
                <>
                    <p className="text-sm text-muted-foreground">
                        {data.page.totalElements} users found
                    </p>

                    <UsersTable
                        users={data.content}
                        onViewUser={uuid => { void navigate('/users/' + encodeURIComponent(uuid)) }}
                        onEditUser={canUpdate ? (uuid) => { void navigate('/users/' + encodeURIComponent(uuid) + '/edit') } : undefined}
                        currentUserUuid={currentUserUuid}
                        onDeleteUser={canDelete ? (user) => { void navigate('/users/' + encodeURIComponent(user.uuid) + '/delete') } : undefined}
                        onResetPassword={canUpdate ? (user) => { void navigate('/users/' + encodeURIComponent(user.uuid) + '/reset-password') } : undefined}
                        onManageRoles={canAssignRoles ? (user) => { void navigate('/users/' + encodeURIComponent(user.uuid) + '/roles') } : undefined}
                        onLoginHistory={canRead ? (user) => void navigate('/users/' + encodeURIComponent(user.uuid) + '/login-history') : undefined}
                        onManageSessions={canRead ? (user) => void navigate('/users/' + encodeURIComponent(user.uuid) + '/sessions') : undefined}
                        onManagePermissions={canAssignRoles ? (user) => { void navigate('/users/' + encodeURIComponent(user.uuid) + '/permissions') } : undefined}
                        onStatusAction={canUpdate ? (user, action) => { void navigate('/users/' + encodeURIComponent(user.uuid) + '/' + action) } : undefined}
                    />
                    <div className="flex flex-wrap items-center justify-between gap-3">
                        <p className="text-sm text-muted-foreground">
                            {data.page.totalPages === 0
                                ? 'No pages'
                                : `Page ${data.page.number + 1} of ${data.page.totalPages}`}
                        </p>

                        <div className="flex gap-2">
                            <Button
                                variant="outline"
                                disabled={page === 0}
                                onClick={() => setPage((value) => value - 1)}
                            >
                                Previous
                            </Button>

                            <Button
                                variant="outline"
                                disabled={page + 1 >= data.page.totalPages}
                                onClick={() => setPage((value) => value + 1)}
                            >
                                Next
                            </Button>
                        </div>
                    </div>
                </>
            )}
        </section>
    )
}
