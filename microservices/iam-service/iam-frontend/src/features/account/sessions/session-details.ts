export function deviceDetails(agent: string) {
  const browserPatterns: [string, RegExp][] = [
    ['Edge', /(?:EdgA|EdgiOS|Edg)\/([\d.]+)/], ['Opera', /(?:OPR|Opera)\/([\d.]+)/],
    ['Samsung Internet', /SamsungBrowser\/([\d.]+)/], ['Firefox', /(?:Firefox|FxiOS)\/([\d.]+)/],
    ['Chrome', /(?:Chrome|CriOS)\/([\d.]+)/], ['Safari', /Version\/([\d.]+).*Safari/],
  ]
  let browser: string | null = null
  for (const [name, pattern] of browserPatterns) {
    const match = agent.match(pattern)
    if (match) { browser = `${name} ${match[1].split('.')[0]}`; break }
  }
  const os = /Windows/.test(agent) ? 'Windows' : /Android/.test(agent) ? 'Android' : /iPhone|iPad|iPod/.test(agent) ? 'iOS' : /Macintosh|Mac OS X/.test(agent) ? 'macOS' : /Linux/.test(agent) ? 'Linux' : null
  const type = /iPad|Tablet/i.test(agent) || (/Android/.test(agent) && !/Mobile/.test(agent)) ? 'tablet' : /Mobi|iPhone|iPod|Android/i.test(agent) ? 'mobile' : os ? 'desktop' : 'unknown'
  return { browser, os, type } as const
}
export function isLoopback(ip: string) {
  const value = ip.toLowerCase()
  return value === '::1' || value === '0:0:0:0:0:0:0:1' || /^127\.(?:\d{1,3}\.){2}\d{1,3}$/.test(value) || value.startsWith('::ffff:127.')
}
