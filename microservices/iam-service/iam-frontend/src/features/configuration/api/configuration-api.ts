import { mutateJson, requestJson } from '@/lib/api-client'
import type { Configuration, SecurityPolicy, ClientDefaults } from '../schemas'

export function getConfiguration(signal: AbortSignal) { return requestJson<Configuration>('/dashboard-api/configuration', { signal }) }
export function updateConfiguration(version: number, policy: SecurityPolicy) { return mutateJson<Configuration>('/dashboard-api/configuration', 'PUT', { version, policy }) }
export function getClientDefaults(signal: AbortSignal) { return requestJson<ClientDefaults>('/dashboard-api/client-defaults', { signal }) }
