import { mutateJson, requestJson } from '@/lib/api-client'
import type { ClientDefinition, ClientPage, IssuedClient, ManagedClient } from '../types'

const base = '/dashboard-api/clients'
export function listClients(search: string, page: number, signal: AbortSignal, priorityId?: string) {
  return requestJson<ClientPage>(`${base}?${new URLSearchParams({ search, page: String(page), size: '20', ...(priorityId ? { priorityId } : {}) })}`, { signal })
}
export function getClient(id: string, signal: AbortSignal) { return requestJson<ManagedClient>(`${base}/${encodeURIComponent(id)}`, { signal }) }
export function createClient(value: ClientDefinition) { return mutateJson<IssuedClient>(base, 'POST', value) }
export function updateClient(id: string, value: ClientDefinition) { return mutateJson<ManagedClient>(`${base}/${encodeURIComponent(id)}`, 'PUT', value) }
export function rotateSecret(id: string) { return mutateJson<IssuedClient>(`${base}/${encodeURIComponent(id)}/secret`, 'POST') }
export function deleteClient(id: string) { return mutateJson<void>(`${base}/${encodeURIComponent(id)}`, 'DELETE') }
