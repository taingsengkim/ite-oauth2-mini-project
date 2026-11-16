import { Bar, BarChart, CartesianGrid, XAxis, YAxis } from 'recharts'
import { ChartContainer, ChartTooltip, ChartTooltipContent } from '@/components/ui/chart'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card'
import { useAccountLocale } from '@/lib/account-locale'
import type { Overview } from '../api/overview-api'

export function OverviewCharts({ statistics: s }: { statistics: Overview['statistics'] }) {
  const { locale } = useAccountLocale()
  const km = locale === 'km'
  const accounts = [{ name: km ? 'សកម្ម' : 'Active', count: s.activeUsers }, { name: km ? 'បិទដំណើរការ' : 'Disabled', count: s.disabledUsers }, { name: km ? 'ចាក់សោ' : 'Locked', count: s.lockedUsers }]
  const resources = [{ name: km ? 'តួនាទី' : 'Roles', count: s.roles }, { name: km ? 'សិទ្ធិ' : 'Permissions', count: s.permissions }, { name: km ? 'កម្មវិធី' : 'Clients', count: s.clients }]
  const charts = [{ title: km ? 'ស្ថានភាពគណនី' : 'Account status', description: km ? 'គណនីដែលបានបិទ និងចាក់សោអាចត្រួតគ្នា។' : 'Disabled and locked counts can overlap; these are not parts of a total.', data: accounts, color: 'var(--chart-1)' }, { title: km ? 'ធនធានគ្រប់គ្រងសិទ្ធិ' : 'Access resources', description: km ? 'ចំនួនបច្ចុប្បន្នពីប្រព័ន្ធ IAM។' : 'Current roles, permissions and registered OAuth clients.', data: resources, color: 'var(--chart-2)' }]
  return <div className="grid gap-6 lg:grid-cols-2">{charts.map(chart => <Card key={chart.title}><CardHeader><CardTitle>{chart.title}</CardTitle><CardDescription>{chart.description}</CardDescription></CardHeader><CardContent>
    <ChartContainer className="h-64 w-full" config={{ count: { label: km ? 'ចំនួន' : 'Count', color: chart.color } }}>
      <BarChart accessibilityLayer data={chart.data} margin={{ left: -12, right: 12, top: 12 }}><CartesianGrid vertical={false} /><XAxis dataKey="name" tickLine={false} axisLine={false} tickMargin={10} /><YAxis allowDecimals={false} tickLine={false} axisLine={false} /><ChartTooltip cursor={false} content={<ChartTooltipContent hideLabel />} /><Bar dataKey="count" fill="var(--color-count)" radius={[6, 6, 0, 0]} maxBarSize={56} /></BarChart>
    </ChartContainer>
    <ul className="sr-only">{chart.data.map(item => <li key={item.name}>{item.name}: {item.count}</li>)}</ul>
  </CardContent></Card>)}</div>
}
