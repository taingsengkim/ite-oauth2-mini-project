import { Code2, GraduationCap, MapPin, Sparkles } from 'lucide-react'
import { useAccountLocale } from '@/lib/account-locale'
import { aboutMessages } from '../i18n'

export function AboutPage() {
  const { locale } = useAccountLocale()
  const t = aboutMessages[locale]
  const technologies = [
    { title: t.frontend, items: ['React.js', 'Next.js'] },
    { title: t.backend, items: ['Spring Boot', 'Spring Cloud Gateway', t.microservices, t.systemDesign] },
    { title: t.security, items: ['Keycloak', 'OAuth2 / OpenID Connect', 'Eureka'] },
    { title: t.messaging, items: ['Apache Kafka', 'Debezium'] },
    { title: t.devops, items: ['Jenkins', 'Docker', 'Docker Compose'] },
  ]
  return <section className="space-y-6" aria-label={t.title}>
    <div className="relative overflow-hidden rounded-2xl border bg-card p-6 text-card-foreground shadow-sm sm:p-9">
      <div className="absolute -right-12 -top-12 size-56 rounded-full border-[18px] border-muted" aria-hidden="true" />
      <div className="relative flex flex-col gap-6 sm:flex-row sm:items-center"><img src="/assets/branding/taing-sengkim.jpg" alt={t.name} width="1400" height="1120" className="h-56 w-full shrink-0 rounded-xl border object-cover object-[50%_20%] sm:h-52 sm:w-44" /><div className="max-w-2xl space-y-4"><span className="inline-flex rounded-full border bg-secondary px-3 py-1 text-xs font-medium text-muted-foreground">{t.badge}</span><h2 className="text-3xl font-bold leading-relaxed text-foreground sm:text-4xl">{t.name}</h2><p className="text-sm leading-8 text-muted-foreground">{t.bio}</p></div></div>
      <div className="relative mt-7 flex flex-wrap gap-5 border-t border-border pt-5 text-sm text-muted-foreground"><p className="flex items-center gap-2"><MapPin className="size-4 text-muted-foreground" aria-hidden="true" />{t.location}: {t.country}</p><p className="flex items-center gap-2"><Code2 className="size-4 text-muted-foreground" aria-hidden="true" />{t.focusValue}</p></div>
    </div>
    <div className="grid gap-5 lg:grid-cols-2"><article className="space-y-5 rounded-xl border bg-card p-6"><h3 className="flex items-center gap-3 text-lg font-semibold text-primary"><GraduationCap className="size-5" aria-hidden="true" />{t.education}</h3><ol className="ml-2 border-l border-border"><li className="relative pb-6 pl-6"><span className="absolute -left-1.5 top-1.5 size-3 rounded-full border-2 border-card bg-muted-foreground" aria-hidden="true" /><p className="mb-1 text-xs text-muted-foreground">{t.previousSchool}</p><p className="font-medium">{t.step}</p><p className="mt-3 text-sm text-muted-foreground">{t.transfer}</p></li><li className="relative pl-6"><span className="absolute -left-1.5 top-1.5 size-3 rounded-full border-2 border-card bg-primary" aria-hidden="true" /><span className="inline-flex rounded-full border bg-secondary px-2 py-0.5 text-xs font-medium">{t.currentSchool}</span><p className="mt-2 font-medium">{t.istad}</p><p className="mt-1 text-sm text-muted-foreground">{t.currentStudy}</p></li></ol></article>
      <article className="space-y-5 rounded-xl border bg-card p-6"><h3 className="flex items-center gap-3 text-lg font-semibold text-primary"><Sparkles className="size-5" aria-hidden="true" />{t.interests}</h3><div className="space-y-5">{technologies.map(group => <div key={group.title} className="space-y-2"><h4 className="text-xs font-medium text-muted-foreground">{group.title}</h4><ul className="flex flex-wrap gap-2">{group.items.map(technology => <li key={technology} className="rounded-lg border bg-secondary px-3 py-2 text-sm text-secondary-foreground">{technology}</li>)}</ul></div>)}</div></article></div>
    <article className="space-y-3 rounded-xl border bg-card p-6"><h3 className="font-semibold text-primary">{t.project}</h3><p className="text-sm leading-7 text-foreground">{t.projectText}</p></article>
  </section>
}
