import { useSyncExternalStore } from 'react'

export type ManagementResource = 'users' | 'roles' | 'permissions' | 'clients'
type RecentSave = { id: string; message: string; revision: number }
const records: Partial<Record<ManagementResource, RecentSave>> = {}
const listeners = new Set<() => void>()
let revision = 0
const subscribe = (listener: () => void) => { listeners.add(listener); return () => { listeners.delete(listener) } }
export function rememberSave(resource: ManagementResource, id: string, message: string) {
  records[resource] = { id, message, revision: ++revision }
  listeners.forEach(listener => listener())
}
export function clearRecentSave(resource: ManagementResource, id?: string) {
  if (id && records[resource]?.id !== id) return
  delete records[resource]
  listeners.forEach(listener => listener())
}
export function useRecentSave(resource: ManagementResource) {
  return useSyncExternalStore(subscribe, () => records[resource], () => undefined)
}
