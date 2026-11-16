export type SessionLocation = { status: 'RESOLVED' | 'LOCAL' | 'PRIVATE' | 'UNAVAILABLE'; city: string | null; countryCode: string | null }
export function locationText(location: SessionLocation | null | undefined, locale: 'en' | 'km') {
  if (location?.status !== 'RESOLVED' || !location.countryCode) return null
  let country = location.countryCode
  try { country = new Intl.DisplayNames([locale], { type: 'region' }).of(country) || country } catch { /* Fall back to the ISO code. */ }
  return [location.city, country].filter(Boolean).join(', ')
}
