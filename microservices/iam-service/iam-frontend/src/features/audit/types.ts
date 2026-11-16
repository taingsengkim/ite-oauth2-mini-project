export type AuditActionOption = { code: string; label: string; resource: string }
export type AuditEvent = {
  uuid: string; occurredAt: string; actor: string; actorUuid: string | null; action: string
  resource: string; targetId: string | null; outcome: 'SUCCESS' | 'FAILURE'
  ipAddress: string | null; userAgent: string | null; relatedIds: string[]; submittedFields: string[]
  expiresAt: string | null; effect: string | null; provider: string | null; reason: string | null
}
export type AuditPage = { content: AuditEvent[]; page: number; size: number; totalElements: number; totalPages: number }
