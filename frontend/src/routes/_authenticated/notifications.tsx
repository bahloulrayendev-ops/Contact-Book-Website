import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Bell, Check, CircleDollarSign, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { EmptyState, ErrorState, LoadingState } from "@/components/states";
import { api } from "@/lib/api/client";
import { formatDateTime } from "@/lib/format";

export const Route = createFileRoute("/_authenticated/notifications")({
  head: () => ({
    meta: [
      { title: "Notifications — Datasphere" },
      { name: "description", content: "Contact updates for details you have unlocked." },
    ],
  }),
  component: NotificationsPage,
});

function NotificationsPage() {
  const queryClient = useQueryClient();
  const notifications = useQuery({
    queryKey: ["notifications", 0],
    queryFn: () => api.notifications(0),
  });

  const markRead = useMutation({
    mutationFn: api.markNotificationRead,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["notifications"] });
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Could not update notification."),
  });

  const repurchase = useMutation({
    mutationFn: api.repurchaseUpdatedContact,
    onSuccess: async () => {
      toast.success("Updated contact details unlocked");
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["notifications"] }),
        queryClient.invalidateQueries({ queryKey: ["wallet"] }),
        queryClient.invalidateQueries({ queryKey: ["transactions"] }),
        queryClient.invalidateQueries({ queryKey: ["history", "unlocks"] }),
        queryClient.invalidateQueries({ queryKey: ["employees"] }),
        queryClient.invalidateQueries({ queryKey: ["people"] }),
      ]);
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Could not unlock updated contact."),
  });

  return (
    <div className="space-y-6">
      <header>
        <h1 className="flex items-center gap-2 text-2xl font-semibold">
          <Bell className="size-5 text-primary" /> Notifications
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">Updates to contact details you have unlocked.</p>
      </header>

      {notifications.isLoading ? (
        <LoadingState />
      ) : notifications.isError ? (
        <ErrorState error={notifications.error} onRetry={() => void notifications.refetch()} />
      ) : !notifications.data?.content.length ? (
        <EmptyState title="You’re all caught up" description="Updates to your unlocked contacts will appear here." />
      ) : (
        <ul className="space-y-3">
          {notifications.data.content.map((notification) => (
            <li key={notification.id}>
              <Card className={notification.read ? "shadow-[var(--shadow-card)]" : "border-primary/35 bg-primary/[0.025] shadow-[var(--shadow-card)]"}>
                <CardContent className="flex flex-col gap-4 p-4 sm:flex-row sm:items-start sm:justify-between sm:p-5">
                  <div className="min-w-0 space-y-2">
                    <div className="flex flex-wrap items-center gap-2">
                      <h2 className="font-semibold">{notification.title}</h2>
                      {!notification.read && <Badge variant="secondary">New</Badge>}
                    </div>
                    <p className="text-sm text-muted-foreground">{notification.message}</p>
                    <p className="text-xs text-muted-foreground">
                      {contactTypeLabel(notification.contactType)} · {formatDateTime(notification.createdAt)}
                    </p>

                    {notification.newValue && (
                      <div className="rounded-md border border-success/30 bg-success/5 px-3 py-2">
                        <p className="text-xs font-medium text-muted-foreground">Updated contact detail</p>
                        <p className="mt-1 break-all font-mono text-sm">{notification.newValue}</p>
                      </div>
                    )}

                    {notification.discountedTokenCost !== null && !notification.repurchaseCompleted && (
                      <Button
                        size="sm"
                        disabled={repurchase.isPending}
                        onClick={() => repurchase.mutate(notification.id)}
                      >
                        {repurchase.isPending ? <Loader2 className="size-4 animate-spin" /> : <CircleDollarSign className="size-4" />}
                        Unlock updated detail · {notification.discountedTokenCost} tokens
                      </Button>
                    )}

                    {notification.repurchaseCompleted && (
                      <p className="flex items-center gap-1.5 text-sm font-medium text-success">
                        <Check className="size-4" /> Updated detail unlocked
                      </p>
                    )}
                  </div>

                  {!notification.read && (
                    <Button
                      variant="outline"
                      size="sm"
                      className="shrink-0"
                      disabled={markRead.isPending}
                      onClick={() => markRead.mutate(notification.id)}
                    >
                      Mark as read
                    </Button>
                  )}
                </CardContent>
              </Card>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function contactTypeLabel(contactType: string) {
  return contactType.replaceAll("_", " ").toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
}