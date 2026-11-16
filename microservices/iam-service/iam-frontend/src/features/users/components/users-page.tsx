import { SearchForm } from '@/components/shared/search-form'
import { UserSessionsDialog } from './user-sessions-dialog'

import { lazy, Suspense, useEffect, useState } from 'react'

import { Button } from '@/components/ui/button'

import { listUsers } from '../api/users-api'
import { UsersTable } from '../components/users-table'
import type { UserPageResponse } from '../types/types'
import {UserDetailsPanel} from "@/features/users/components/user-details-panel.tsx";
import { CreateUserDialog } from './create-user-dialog'
import { EditUserDialog } from './edit-user-dialog'
import { getDashboardSession } from '@/lib/api-client'
import type { UserDetails, User, UserStatusAction } from '../types/types'
import { UserStatusDialog } from './user-status-dialog'
import { DeleteUserDialog } from './delete-user-dialog'
import { ResetPasswordDialog } from './reset-password-dialog'
import { UserRolesDialog } from './user-roles-dialog'
import { UserPermissionsDialog } from './user-permissions-dialog'

const LoginHistoryDialog = lazy(() => import('./login-history-dialog').then((module) => ({ default: module.LoginHistoryDialog })))

export function UsersPage() {
    const [historyUser, setHistoryUser] = useState<User | 'all' | null>(null)
    const [sessionUser, setSessionUser] = useState<User | null>(null)
    const [canRead, setCanRead] = useState(false)
    const [searchRevision, setSearchRevision] = useState(0)
    const [createOpen, setCreateOpen] = useState(false)
    const [canCreate, setCanCreate] = useState(false)
    const [canUpdate, setCanUpdate] = useState(false)
    const [canDelete, setCanDelete] = useState(false)
    const [canAssignRoles, setCanAssignRoles] = useState(false)
    const [canBrowseRoles, setCanBrowseRoles] = useState(false)
    const [roleUser, setRoleUser] = useState<User | null>(null)
    const [permissionUser, setPermissionUser] = useState<User | null>(null)
    const [canBrowsePermissions, setCanBrowsePermissions] = useState(false)
    const [deletingUser, setDeletingUser] = useState<User | null>(null)
    const [resettingUser, setResettingUser] = useState<User | null>(null)
    const [editingUuid, setEditingUuid] = useState<string | null>(null)
    const [statusChange, setStatusChange] = useState<{ user: User; action: UserStatusAction } | null>(null)
    const [currentUserUuid, setCurrentUserUuid] = useState<string | null>(null)
    const [sessionError, setSessionError] = useState<string | null>(null)
    const [success, setSuccess] = useState<string | null>(null)

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
                    setCanBrowseRoles(session.authorities.includes('role:read'))
                    setCanBrowsePermissions(session.authorities.includes('permission:read'))
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

    const [selectedUserUuid, setSelectedUserUuid] =
        useState<string | null>(null)
    const [search, setSearch] = useState('')
    const [page, setPage] = useState(0)

    const [data, setData] = useState<UserPageResponse | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [retryKey, setRetryKey] = useState(0)

    useEffect(() => {
        const controller = new AbortController()

        setLoading(true)
        setError(null)
        setData(null)

        async function loadUsers() {
            try {
                const result = await listUsers({
                    page,
                    size: 20,
                    search,
                    signal: controller.signal,
                })

                if (!controller.signal.aborted) {
                    setData(result)
                }
            } catch (error) {
                if (!controller.signal.aborted) {
                    setError(
                        error instanceof Error
                            ? error.message : 'Could not load users.',)
                }
            } finally {
                if (!controller.signal.aborted) {
                    setLoading(false)
                }
            }
        }

        void loadUsers()

        return () => controller.abort()
    }, [page, search, retryKey])


    function handleCreated(user: UserDetails) {
        setCreateOpen(false)
        setSuccess(`User ${user.username} was created successfully.`)
        setSearchRevision((value) => value + 1)
        setSearch('')
        setPage(0)
        setRetryKey((value) => value + 1)
        setSelectedUserUuid(user.uuid)
    }

    function handleSaved(user: UserDetails) {
        setEditingUuid(null)
        setSuccess(`User ${user.username} was updated successfully.`)
        setRetryKey((value) => value + 1)
        setSelectedUserUuid(user.uuid)
    }

    function handleDeleted(user: User) {
        setDeletingUser(null)
        if (selectedUserUuid === user.uuid) setSelectedUserUuid(null)
        setSuccess(`User ${user.username} was deleted successfully.`)
        if (data?.content.length === 1 && page > 0) setPage((value) => value - 1)
        setRetryKey((value) => value + 1)
    }

    return (
        <section className="space-y-4" aria-label="User management">
            {success && <p role="status" className="rounded-lg border bg-muted/40 p-3 text-sm">{success}</p>}
            {sessionError && <p role="alert" className="text-sm text-destructive">{sessionError}</p>}
            {canCreate && (
                <div className="flex justify-end">
                    <Button onClick={() => { setSuccess(null); setCreateOpen(true) }}>Create user</Button>
                </div>
            )}
            {createOpen && <CreateUserDialog onClose={() => setCreateOpen(false)} onCreated={handleCreated} />}
            {canRead && <div className="flex justify-end"><Button variant="outline" onClick={() => setHistoryUser('all')}>All login history</Button></div>}
            {historyUser && <Suspense fallback={<p role="status">Loading login history?</p>}><LoginHistoryDialog key={historyUser === 'all' ? 'all' : historyUser.uuid} user={historyUser === 'all' ? undefined : historyUser} onClose={() => setHistoryUser(null)} /></Suspense>}
            {sessionUser && <UserSessionsDialog key={sessionUser.uuid} user={sessionUser} canTerminate={canUpdate} onClose={() => setSessionUser(null)} />}
            {permissionUser && <UserPermissionsDialog key={permissionUser.uuid} user={permissionUser} canBrowse={canBrowsePermissions} currentUserUuid={currentUserUuid}
                onClose={() => setPermissionUser(null)} onChanged={(message) => { setSuccess(message); setRetryKey((value) => value + 1) }} />}
            {roleUser && <UserRolesDialog key={roleUser.uuid} user={roleUser} canBrowse={canBrowseRoles} currentUserUuid={currentUserUuid}
                onClose={() => setRoleUser(null)} onChanged={(message) => { setSuccess(message); setRetryKey((value) => value + 1) }} />}
            {editingUuid && <EditUserDialog key={editingUuid} uuid={editingUuid}
                onClose={() => setEditingUuid(null)} onSaved={handleSaved} />}
            {deletingUser && <DeleteUserDialog key={deletingUser.uuid} user={deletingUser}
                onClose={() => setDeletingUser(null)} onDeleted={handleDeleted} />}
            {resettingUser && <ResetPasswordDialog key={resettingUser.uuid} user={resettingUser}
                onClose={() => setResettingUser(null)} onReset={(user) => {
                    setResettingUser(null)
                    setSuccess(`Password for ${user.username} was reset successfully.`)
                    setRetryKey((value) => value + 1)
                }} />}
            {statusChange && <UserStatusDialog user={statusChange.user} action={statusChange.action}
                onClose={() => setStatusChange(null)} onChanged={(message) => {
                    setStatusChange(null)
                    setSuccess(message)
                    setRetryKey((value) => value + 1)
                }} />}
            {selectedUserUuid && (
                <UserDetailsPanel
                    key={selectedUserUuid}
                    uuid={selectedUserUuid}
                    onClose={() => setSelectedUserUuid(null)}
                />
            )}
            <SearchForm key={searchRevision} label="Search users" onSearch={(value) => { setPage(0); setSearch(value); setRetryKey((current) => current + 1) }} />

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
                        onViewUser={setSelectedUserUuid}
                        onEditUser={canUpdate ? (uuid) => { setSuccess(null); setEditingUuid(uuid) } : undefined}
                        currentUserUuid={currentUserUuid}
                        onDeleteUser={canDelete ? (user) => { setSuccess(null); setDeletingUser(user) } : undefined}
                        onResetPassword={canUpdate ? (user) => { setSuccess(null); setResettingUser(user) } : undefined}
                        onManageRoles={canAssignRoles ? (user) => { setSuccess(null); setRoleUser(user) } : undefined}
                        onLoginHistory={canRead ? (user) => setHistoryUser(user) : undefined}
                        onManageSessions={canRead ? (user) => setSessionUser(user) : undefined}
                        onManagePermissions={canAssignRoles ? (user) => { setSuccess(null); setPermissionUser(user) } : undefined}
                        onStatusAction={canUpdate ? (user, action) => { setSuccess(null); setStatusChange({ user, action }) } : undefined}
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
