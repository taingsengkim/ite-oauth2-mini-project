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
    currentUserUuid?: string | null
}

export function UsersTable({ users,onViewUser,onEditUser,onStatusAction,onDeleteUser,onResetPassword,currentUserUuid }: UsersTableProps) {
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
                                <TableCell className="font-medium">
                                    <button type="button"
                                        className="cursor-pointer rounded-sm text-left hover:underline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ring"
                                        aria-label={`View details for ${user.username}`}
                                        onClick={() => onViewUser(user.uuid)}>
                                        {user.name || user.username}
                                    </button>
                                </TableCell>
                                <TableCell>{user.username}</TableCell>
                                <TableCell>{user.email || '—'}</TableCell>
                                <TableCell>
                                    <div className="flex flex-wrap gap-1">
                                    <Badge
                                        variant={user.enabled ? 'secondary' : 'outline'}
                                    >
                                        {user.enabled ? 'Active' : 'Inactive'}
                                    </Badge>
                                    {!user.accountNonLocked && <Badge variant="destructive">Locked</Badge>}
                                    </div>
                                </TableCell>
                                <TableCell className="cursor-default text-right" data-user-actions>
                                    <DropdownMenu>
                                        <DropdownMenuTrigger render={<Button variant="outline" size="sm" aria-label={`Actions for ${user.username}`} />}>
                                            Actions
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent align="end" className="min-w-44">
                                            <DropdownMenuItem onClick={() => onViewUser(user.uuid)}>View details</DropdownMenuItem>
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
