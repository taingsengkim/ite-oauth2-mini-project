import { useState } from 'react'

import { DashboardLayout } from '@/components/layout/dashboard-layout'
import type { DashboardPage } from '@/components/layout/app-sidebar'
import { Button } from '@/components/ui/button'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {UsersPage} from "@/features/users/components/users-page.tsx";

const sections = [
  {
    name: 'Users',
    description: 'Manage accounts and account status.',
  },
  {
    name: 'Roles',
    description: 'Organize access by responsibility.',
  },
  {
    name: 'Permissions',
    description: 'Define actions users can perform.',
  },
] as const

export default function App() {
  const [page, setPage] = useState<DashboardPage>('Overview')

  return (
      <DashboardLayout
          activePage={page}
          onNavigate={setPage}
      >
        <div>
          <h1 className="text-3xl font-semibold tracking-tight">
            {page}
          </h1>

          <p className="mt-2 text-muted-foreground">
            Manage identities and access across your applications.
          </p>
        </div>

        {page === 'Overview' ? (
            <div className="grid gap-4 md:grid-cols-3">
              {sections.map(({ name, description }) => (
                  <Card key={name}>
                    <CardHeader>
                      <CardTitle>{name}</CardTitle>
                      <CardDescription>{description}</CardDescription>
                    </CardHeader>

                    <CardContent>
                      <Button
                          variant="outline"
                          onClick={() => setPage(name)}
                      >
                        Manage {name.toLowerCase()}
                      </Button>
                    </CardContent>
                  </Card>
              ))}
            </div>
        )  : page === 'Users' ? (
                    <UsersPage />
        ) : (
            <Card>
              <CardHeader>
                <CardTitle>{page} management</CardTitle>
                <CardDescription>
                  This page is ready for its management interface.
                </CardDescription>
              </CardHeader>
            </Card>
        )}
      </DashboardLayout>
  )
}
