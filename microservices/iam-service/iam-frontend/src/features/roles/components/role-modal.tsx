import type { ReactNode } from 'react'
import { Dialog } from '@base-ui/react/dialog'
import { Button } from '@/components/ui/button'

export function RoleModal({ title, description, busy = false, onClose, children }: {
  title: string; description: string; busy?: boolean; onClose: () => void; children: ReactNode
}) {
  return <Dialog.Root open onOpenChange={(open) => { if (!open && !busy) onClose() }}>
    <Dialog.Portal>
      <Dialog.Backdrop className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" />
      <Dialog.Popup className="fixed top-1/2 left-1/2 z-50 max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] max-w-2xl -translate-x-1/2 -translate-y-1/2 overflow-y-auto rounded-xl border bg-background p-6 text-foreground shadow-xl">
        <div className="mb-6 flex items-start justify-between gap-4">
          <div className="space-y-2">
            <Dialog.Title className="text-lg font-semibold">{title}</Dialog.Title>
            <Dialog.Description className="text-sm text-muted-foreground">{description}</Dialog.Description>
          </div>
          <Dialog.Close disabled={busy} render={<Button type="button" variant="outline" />}>Close</Dialog.Close>
        </div>
        {children}
      </Dialog.Popup>
    </Dialog.Portal>
  </Dialog.Root>
}
