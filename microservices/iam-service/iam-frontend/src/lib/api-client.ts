export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(message: string, status: number, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

export async function requestJson<T>(url: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')
  const response = await fetch(url, { ...options, headers, credentials: 'same-origin', cache: 'no-store' })

  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    const fields: Record<string, string> = {}
    if (Array.isArray(problem?.errors)) {
      for (const error of problem.errors) {
        if (typeof error.field === 'string' && typeof error.message === 'string') {
          fields[error.field] = error.message
        }
      }
    }
    const message = response.status === 401
      ? 'Your session has expired. Sign in again to continue.'
      : response.status === 403
        ? 'Permission denied or security token expired. Refresh the page and try again.'
        : typeof problem?.detail === 'string'
          ? problem.detail
          : `Request failed (${response.status}).`
    if (response.status === 409) {
      if (message === 'Username is already registered') fields.username = message
      if (message === 'Email is already registered') fields.email = message
      if (message === 'Phone number is already registered') fields.phoneNumber = message
    }
    throw new ApiError(message, response.status, fields)
  }

  if (response.status === 204) return undefined as T
  if (!response.headers.get('content-type')?.includes('json')) {
    throw new ApiError('Your session may have expired. Sign in again to continue.', response.status)
  }
  return response.json() as Promise<T>
}

export type DashboardSession = {
  authorities: string[]
  csrfHeaderName: string
  csrfToken: string
  userUuid: string
}

export function getDashboardSession(signal?: AbortSignal) {
  return requestJson<DashboardSession>('/dashboard-api/session', { signal })
}
