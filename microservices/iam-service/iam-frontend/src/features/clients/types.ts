export type ClientDefinition = {
  clientId: string; name: string; authenticationMethod: string; grantTypes: string[]
  redirectUris: string[]; postLogoutRedirectUris: string[]; scopes: string[]
  requireProofKey: boolean; requireConsent: boolean; accessTokenTtlSeconds: number; refreshTokenTtlSeconds: number
}
export type ManagedClient = Omit<ClientDefinition, 'authenticationMethod'> & {
  id: string; authenticationMethods: string[]; issuedAt: string | null; secretExpiresAt: string | null
  hasSecret: boolean; protectedClient: boolean; editable: boolean
}
export type IssuedClient = { client: ManagedClient; clientSecret: string | null }
export type ClientPage = { content: ManagedClient[]; page: number; size: number; totalElements: number; totalPages: number }
export type ClientAccess = { create: boolean; update: boolean; delete: boolean }
