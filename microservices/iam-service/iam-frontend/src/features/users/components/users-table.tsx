import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar'
import { MoreHorizontal } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table'

import type { User, UserStatusAction } from '../types/types'
import {
    DropdownMenu, DropdownMenuTrigger, DropdownMenuContent,
    DropdownMenuItem, DropdownMenuSeparator,
} from '@/components/ui/dropdown-menu'
import {Button} from "@/components/ui/button.tsx";

type UsersTableProps = {
    users: User[]
    onViewUser: (uuid: string) => void
    onEditUser?: (uuid: string) => void
    onStatusAction?: (user: User, action: UserStatusAction) => void
    onDeleteUser?: (user: User) => void
    onResetPassword?: (user: User) => void
    onManageRoles?: (user: User) => void
    onLoginHistory?: (user: User) => void
    onManageSessions?: (user: User) => void
    onManagePermissions?: (user: User) => void
    currentUserUuid?: string | null
}

export function UsersTable({ users,onViewUser,onEditUser,onStatusAction,onDeleteUser,onResetPassword,onManageRoles,onManagePermissions,onManageSessions,onLoginHistory,currentUserUuid }: UsersTableProps) {
    return (
        <div className="overflow-hidden rounded-lg border">
            <Table>
                <caption className="sr-only">
                    User accounts
                </caption>

                <TableHeader>
                    <TableRow>
                        <TableHead>Name</TableHead>
                        <TableHead>Username</TableHead>
                        <TableHead>Email</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                </TableHeader>

                <TableBody>
                    {users.length === 0 ? (
                        <TableRow>
                            <TableCell
                                colSpan={5}
                                className="h-32 text-center text-muted-foreground"
                            >
                                No users found.
                            </TableCell>
                        </TableRow>
                    ) : (
                        users.map((user) => (
                            <TableRow key={user.uuid} className="cursor-pointer"
                                onClick={(event) => {
                                    const target = event.target
                                    if (!(target instanceof Element) || !event.currentTarget.contains(target)) return
                                    if (target.closest('button, a, input, select, textarea, [role="menuitem"], [data-user-actions]')) return
                                    if (window.getSelection()?.toString()) return
                                    onViewUser(user.uuid)
                                }}>
                                <TableCell className="font-medium"><div className="flex items-center gap-3"><Avatar className="size-9" aria-hidden="true">{user.pictureUrl && <AvatarImage src={user.pictureUrl} alt="" loading="lazy" referrerPolicy="no-referrer" />}<AvatarFallback className="text-xs font-semibold">{(user.name || user.username).trim().split(/\s+/).map(part => part[0]).slice(0, 2).join('').toUpperCase()}</AvatarFallback></Avatar>
                                    <button type="button"
                                        className="cursor-pointer rounded-sm text-left hover:underline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ring"
                                        aria-label={`View details for ${user.username}`}
                                        onClick={() => onViewUser(user.uuid)}>
                                        {user.name || user.username}
                                    </button></div>
                                </TableCell>
                                <TableCell>{user.username}</TableCell>
                                <TableCell>{user.email || '—'}</TableCell>
                                <TableCell>
                                    <div className="flex flex-wrap gap-1">
                                    <Badge
                                        variant={user.enabled ? 'secondary' : 'outline'}
                                    >
                                        {user.enabled ? 'Enabled' : 'Disabled'}
                                    </Badge>
                                    {!user.accountNonLocked && <Badge variant="destructive">Locked</Badge>}
                                    </div>
                                </TableCell>
                                <TableCell className="cursor-default text-right" data-user-actions>
                                    <DropdownMenu>
                                        <DropdownMenuTrigger render={<Button variant="outline" size="icon" aria-label={`Actions for ${user.username}`} />}>
                                            <MoreHorizontal className="size-4" aria-hidden="true" />
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent align="end" className="min-w-56">
                                            <DropdownMenuItem onClick={() => onViewUser(user.uuid)}>View details</DropdownMenuItem>
                                            {onLoginHistory && <DropdownMenuItem onClick={() => onLoginHistory(user)}>Login history</DropdownMenuItem>}
                                            {onManageSessions && <DropdownMenuItem onClick={() => onManageSessions(user)}>View sessions</DropdownMenuItem>}
                                            {onManageRoles && <DropdownMenuItem onClick={() => onManageRoles(user)}>Manage roles</DropdownMenuItem>}
                                            {onManagePermissions && <DropdownMenuItem onClick={() => onManagePermissions(user)}>Manage permissions</DropdownMenuItem>}
                                            {onEditUser && <DropdownMenuItem onClick={() => onEditUser(user.uuid)}>Edit profile</DropdownMenuItem>}
                                            {onResetPassword && <DropdownMenuItem onClick={() => onResetPassword(user)}>Reset password</DropdownMenuItem>}
                                            {onStatusAction && <>
                                                <DropdownMenuSeparator />
                                                <DropdownMenuItem variant={user.enabled ? 'destructive' : 'default'}
                                                    disabled={user.enabled && user.uuid === currentUserUuid}
                                                    onClick={() => onStatusAction(user, user.enabled ? 'disable' : 'enable')}>
                                                    {user.enabled ? 'Deactivate' : 'Activate'}
                                                </DropdownMenuItem>
                                                <DropdownMenuItem variant={user.accountNonLocked ? 'destructive' : 'default'}
                                                    disabled={user.accountNonLocked && user.uuid === currentUserUuid}
                                                    onClick={() => onStatusAction(user, user.accountNonLocked ? 'lock' : 'unlock')}>
                                                    {user.accountNonLocked ? 'Lock account' : 'Unlock account'}
                                                </DropdownMenuItem>
                                                {user.accountNonLocked && <DropdownMenuItem onClick={() => onStatusAction(user, 'unlock')}>Clear password lockout</DropdownMenuItem>}
                                                {user.uuid === currentUserUuid && <p className="px-2 py-1 text-xs text-muted-foreground">You cannot restrict your own account.</p>}
                                            </>}
                                            {onDeleteUser && <>
                                                <DropdownMenuSeparator />
                                                <DropdownMenuItem variant="destructive" disabled={user.uuid === currentUserUuid}
                                                    onClick={() => onDeleteUser(user)}>Delete user</DropdownMenuItem>
                                            </>}
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                </TableCell>
                            </TableRow>
                        ))
                    )}
                </TableBody>
            </Table>
        </div>
    )
}
