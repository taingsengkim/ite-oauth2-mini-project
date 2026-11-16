import { z } from 'zod'
import { requiredText } from '@/lib/form-schemas'

export const splitLines = (value: string) => [...new Set(value.split(/\r?\n/).map((entry) => entry.trim()).filter(Boolean))]
export const splitScopes = (value: string) => [...new Set(value.trim().split(/\s+/).filter(Boolean))]
function validRedirect(value: string) {
  try {
    if (!/^https?:\/\//i.test(value) || /[\s*,#]/.test(value) || /%(?![\da-f]{2})/i.test(value)) return false
    const url = new URL(value)
    const authority = value.match(/^https?:\/\/([^/?#]+)/i)?.[1] ?? ''
    if (authority.includes('@') || url.username || url.password || url.port === '0' || url.hostname.includes('_')) return false
    const loopback = /^(localhost|127\.0\.0\.1|\[::1\])(?::\d+)?$/i.test(authority)
    return url.protocol === 'https:' || (url.protocol === 'http:' && loopback)
  } catch { return false }
}
const redirectList = z.string().superRefine((value, context) => {
  const uris = splitLines(value)
  if (uris.length > 10) context.addIssue({ code: 'custom', message: 'Provide at most ten redirect URIs.' })
  if (uris.join(',').length > 1000) context.addIssue({ code: 'custom', message: 'Combined URIs must not exceed 1000 characters.' })
  if (uris.some((uri) => !validRedirect(uri))) context.addIssue({ code: 'custom', message: 'Use exact HTTPS URLs, or HTTP on localhost/loopback, without credentials, fragments, commas, or wildcards.' })
})
export const clientFormSchema = z.object({
  clientId: requiredText('Client ID', 100).regex(/^[A-Za-z0-9._~-]+$/, 'Use letters, digits, dots, underscores, tildes, or hyphens.'),
  name: requiredText('Application name', 200).refine((value) => [...value].every((character) => { const code = character.charCodeAt(0); return code >= 32 && (code < 127 || code > 159) }), 'Use printable characters.'),
  authenticationMethod: z.enum(['none', 'client_secret_basic', 'client_secret_post']),
  grantTypes: z.array(z.enum(['authorization_code', 'refresh_token', 'client_credentials'])).min(1, 'Choose at least one grant type.'),
  redirectUris: redirectList, postLogoutRedirectUris: redirectList,
  scopes: z.string().superRefine((value, context) => {
    const scopes = splitScopes(value)
    if (!scopes.length || scopes.length > 30 || scopes.join(',').length > 1000 || scopes.some((scope) => !/^[A-Za-z0-9._:/-]{1,100}$/.test(scope)))
      context.addIssue({ code: 'custom', message: 'Provide 1–30 scopes using letters, digits, dots, underscores, colons, slashes, or hyphens (1000 characters combined).' })
  }),
  requireProofKey: z.boolean(), requireConsent: z.boolean(),
  accessTokenTtlSeconds: z.number({ error: 'Enter an access token lifetime.' }).int().min(60).max(86400),
  refreshTokenTtlSeconds: z.number({ error: 'Enter a refresh token lifetime.' }).int().min(300).max(2592000),
}).superRefine((value, context) => {
  const issue = (path: string, message: string) => context.addIssue({ code: 'custom', path: [path], message })
  const code = value.grantTypes.includes('authorization_code')
  if (code && !splitLines(value.redirectUris).length) issue('redirectUris', 'Authorization code requires at least one redirect URI.')
  if (!code) {
    if (splitLines(value.redirectUris).length) issue('redirectUris', 'Leave redirects empty for machine-only clients.')
    if (splitLines(value.postLogoutRedirectUris).length) issue('postLogoutRedirectUris', 'Logout redirects require authorization code.')
    if (value.grantTypes.includes('refresh_token')) issue('grantTypes', 'Refresh tokens require authorization code.')
    if (splitScopes(value.scopes).includes('openid')) issue('scopes', 'OpenID Connect requires authorization code.')
  }
  if (value.authenticationMethod === 'none') {
    if (!code || value.grantTypes.includes('client_credentials')) issue('grantTypes', 'Public clients require authorization code and cannot use client credentials.')
    if (!value.requireProofKey) issue('requireProofKey', 'Public clients must require PKCE.')
  }
  if (value.grantTypes.includes('refresh_token') && value.refreshTokenTtlSeconds < value.accessTokenTtlSeconds)
    issue('refreshTokenTtlSeconds', 'Refresh tokens must last at least as long as access tokens.')
})
