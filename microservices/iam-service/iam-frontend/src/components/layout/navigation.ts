import { accountMessages } from '@/features/account/i18n'
import { securityMessages } from '@/features/account/security/i18n'
import { accountsMessages } from '@/features/account/accounts/i18n'
import { sessionsMessages } from '@/features/account/sessions/i18n'
import { historyMessages } from '@/features/account/login-history/i18n'
import { activityMessages } from '@/features/account/activity/i18n'
import { deactivationMessages } from '@/features/account/deactivation/i18n'
import type { AccountLocale } from '@/features/account/i18n'
import { aboutMessages } from '@/features/about/i18n'

export const dashboardNavigation = [
  { page: 'About the Developer', path: 'about', view: 'about', authority: null },
  { page: 'My Profile', path: 'account/profile', view: 'profile', authority: null },
  { page: 'Security & Password', path: 'account/security', view: 'security', authority: null },
  { page: 'Linked Accounts', path: 'account/linked-accounts', view: 'accounts', authority: null },
  { page: 'Active Sessions', path: 'account/sessions', view: 'sessions', authority: null },
  { page: 'Login History', path: 'account/login-history', view: 'history', authority: null },
  { page: 'Account Activity', path: 'account/activity', view: 'activity', authority: null },
  { page: 'Account Deactivation', path: 'account/deactivation', view: 'deactivation', authority: null },
  { page: 'Overview', path: 'overview', view: 'overview', authority: 'overview:read' },
  { page: 'Users', path: 'users', view: 'users', authority: 'user:read' },
  { page: 'Roles', path: 'roles', view: 'roles', authority: 'role:read' },
  { page: 'Permissions', path: 'permissions', view: 'permissions', authority: 'permission:read' },
  { page: 'Clients', path: 'clients', view: 'clients', authority: 'client:read' },
  { page: 'Audit Logs', path: 'audit-logs', view: 'audit', authority: 'audit:read' },
  { page: 'System Configuration', path: 'configuration', view: 'configuration', authority: 'configuration:read' },
] as const
export type DashboardPage = typeof dashboardNavigation[number]['page']
export function pagePath(page: DashboardPage) { return '/' + dashboardNavigation.find((item) => item.page === page)!.path }
export function pageForPath(pathname: string) {
  const path = pathname.replace(/^\/+|\/+$/g, '').toLowerCase()
  return dashboardNavigation.find((item) => item.path === path || (['users', 'roles', 'clients'].includes(item.path) && path.startsWith(item.path + '/')))?.page
}
export function allowedPages(authorities: readonly string[]) {
  return dashboardNavigation.filter((item) => item.authority === null || authorities.includes(item.authority))
}
export function initialPage(authorities: readonly string[], search: string) {
  const view = new URLSearchParams(search).get('view')
  const allowed = allowedPages(authorities)
  return allowed.find((item) => item.view === view)?.page || (view ? 'My Profile' : allowed.find((item) => item.page === 'Overview')?.page || 'My Profile')
}
export function pageText(page: DashboardPage, locale: AccountLocale) {
  if (page === 'About the Developer') return aboutMessages[locale]
  const account = { 'My Profile': accountMessages[locale], 'Security & Password': securityMessages[locale], 'Linked Accounts': accountsMessages[locale], 'Active Sessions': sessionsMessages[locale], 'Login History': historyMessages[locale], 'Account Activity': activityMessages[locale], 'Account Deactivation': deactivationMessages[locale] }
  if (Object.hasOwn(account, page)) return account[page as keyof typeof account]
  const labels: Record<string, string> = locale === 'km' ? { Overview: 'ទិដ្ឋភាពទូទៅ', Users: 'អ្នកប្រើ', Roles: 'តួនាទី', Permissions: 'សិទ្ធិ', Clients: 'កម្មវិធីអតិថិជន', 'Audit Logs': 'កំណត់ត្រាសវនកម្ម', 'System Configuration': 'ការកំណត់ប្រព័ន្ធ' } : {}
  return { title: labels[page] || page, description: locale === 'km' ? 'គ្រប់គ្រងអត្តសញ្ញាណ និងការចូលប្រើកម្មវិធីរបស់អ្នក។' : 'Manage identities and access across your applications.' }
}
const en = { navigation: 'Navigation', account: 'My account', management: 'Management', workspace: 'Workspace', identity: 'Identity & access', language: 'Language', loading: 'Loading your workspace…', failed: 'Could not load your workspace. Refresh the page to try again.', signIn: 'Sign in again', skip: 'Skip to main content', logout: 'Sign out', signingOut: 'Signing out…', logoutFailed: 'Could not sign out. Please try again.', notFound: 'Page not found', returnHome: 'Return to your dashboard' }
export const shellMessages: Record<AccountLocale, typeof en> = { en, km: {
  notFound: 'រកមិនឃើញទំព័រ', returnHome: 'ត្រឡប់ទៅផ្ទាំងរបស់អ្នក',
  logout: 'ចាកចេញ', signingOut: 'កំពុងចាកចេញ…', logoutFailed: 'មិនអាចចាកចេញបានទេ។ សូមព្យាយាមម្ដងទៀត។',
  navigation: 'ការរុករក', account: 'គណនីរបស់ខ្ញុំ', management: 'ការគ្រប់គ្រង', workspace: 'កន្លែងធ្វើការ', identity: 'អត្តសញ្ញាណ និងការចូលប្រើ', language: 'ភាសា', loading: 'កំពុងផ្ទុកកន្លែងធ្វើការរបស់អ្នក…', failed: 'មិនអាចផ្ទុកកន្លែងធ្វើការបានទេ។ សូមផ្ទុកទំព័រឡើងវិញ។', signIn: 'ចូលម្ដងទៀត', skip: 'រំលងទៅមាតិកាចម្បង',
} }
