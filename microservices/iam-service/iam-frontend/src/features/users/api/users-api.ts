import type { UserPageResponse, UserDetails, CreateUserInput, UpdateUserInput, UserStatusAction } from '../types/types'
import { getDashboardSession, requestJson } from '@/lib/api-client'

export async function resetUserPassword(uuid: string, newPassword: string, confirmPassword: string): Promise<void> {
    const session = await getDashboardSession()
    return requestJson<void>(`/dashboard-api/users/${encodeURIComponent(uuid)}/password`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            [session.csrfHeaderName]: session.csrfToken,
        },
        body: JSON.stringify({ newPassword, confirmPassword }),
    })
}

export async function deleteUser(uuid: string): Promise<void> {
    const session = await getDashboardSession()
    return requestJson<void>(`/dashboard-api/users/${encodeURIComponent(uuid)}`, {
        method: 'DELETE',
        headers: { [session.csrfHeaderName]: session.csrfToken },
    })
}

export async function changeUserStatus(uuid: string, action: UserStatusAction): Promise<void> {
    const session = await getDashboardSession()
    return requestJson<void>(`/dashboard-api/users/${encodeURIComponent(uuid)}/${action}`, {
        method: 'POST',
        headers: { [session.csrfHeaderName]: session.csrfToken },
    })
}

export async function updateUser(uuid: string, input: UpdateUserInput): Promise<UserDetails> {
    const session = await getDashboardSession()
    return requestJson<UserDetails>(`/dashboard-api/users/${encodeURIComponent(uuid)}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            [session.csrfHeaderName]: session.csrfToken,
        },
        body: JSON.stringify(input),
    })
}

export async function createUser(input: CreateUserInput): Promise<UserDetails> {
    const session = await getDashboardSession()
    return requestJson<UserDetails>('/dashboard-api/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [session.csrfHeaderName]: session.csrfToken,
        },
        body: JSON.stringify(input),
    })
}

type ListUsersOptions = {
    page?: number
    size?: number
    search?: string
    signal?: AbortSignal
}

export async function listUsers({
                                    page = 0,
                                    size = 20,
                                    search = '',
                                    signal,
                                }: ListUsersOptions = {}): Promise<UserPageResponse> {
    const params = new URLSearchParams({
        page: String(page),
        size: String(size),
        sort: 'username,asc',
    })

    if (search.trim()) {
        params.set('search', search.trim())
    }

    const response = await fetch(`/dashboard-api/users?${params}`, {
        credentials: 'same-origin',
        headers: {
            Accept: 'application/json',
        },
        signal,
    })

    if (response.status === 401) {
        throw new Error('Please sign in to continue.')
    }

    if (response.status === 403) {
        throw new Error('You do not have permission to view users.')
    }

    if (!response.ok) {
        throw new Error(`Could not load users (${response.status}).`)
    }

    return response.json()
}
export async function getUser(
    uuid: string,
    signal?: AbortSignal,
): Promise<UserDetails> {
    const response = await fetch(
        `/dashboard-api/users/${encodeURIComponent(uuid)}`,
        {
            credentials: 'same-origin',
            headers: {
                Accept: 'application/json',
            },
            signal,
        },
    )

    if (response.status === 401) {
        throw new Error('Please sign in to continue.')
    }

    if (response.status === 403) {
        throw new Error('You do not have permission to view this user.')
    }

    if (response.status === 404) {
        throw new Error('This user could not be found.')
    }

    if (!response.ok) {
        throw new Error(`Could not load the user (${response.status}).`)
    }

    return response.json()
}
