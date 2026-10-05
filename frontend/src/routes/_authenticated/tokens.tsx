import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Check, Coins, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { EmptyState, ErrorState, LoadingState } from "@/components/states";
import { api } from "@/lib/api/client";
import { formatDate } from "@/lib/format";
import { useWallet } from "./route";

export const Route = createFileRoute("/_authenticated/tokens")({
  head: () => ({
    meta: [
      { title: "Tokens — Datasphere" },
      { name: "description", content: "Check your token balance, buy packages and review transactions." },
      { property: "og:title", content: "Tokens — Datasphere" },
      { property: "og:description", content: "Manage your Datasphere tokens." },
    ],
  }),
  component: TokensPage,
});

function TokensPage() {
  const wallet = useWallet();
  const packages = useQuery({ queryKey: ["packages"], queryFn: api.packages });
  const transactions = useQuery({ queryKey: ["transactions", 0], queryFn: () => api.transactions(0) });
  const [pending, setPending] = useState<string | null>(null);

  const purchase = useMutation({
    mutationFn: (packageId: string) => api.purchase(packageId),
    onMutate: (id) => setPending(id),
    onSuccess: async (purchase) => {
      toast.message(`Purchase ${purchase.purchaseId} is pending payment confirmation.`);
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : "Purchase failed"),
    onSettled: () => setPending(null),
  });

  return (
    <div className="space-y-8">
      <div>
        <h1 className="flex items-center gap-2 text-2xl font-semibold"><Coins className="size-5 text-primary" /> Tokens</h1>
        <p className="text-sm text-muted-foreground">Top up your balance and review every movement.</p>
      </div>

      <Card className="shadow-[var(--shadow-card)]">
        <CardContent className="flex flex-wrap items-center justify-between gap-4 pt-6">
          <div>
            <p className="text-sm text-muted-foreground">Current balance</p>
            <p className="flex items-center gap-2 text-4xl font-semibold">
              <Coins className="size-7 text-primary" />
              {wallet.isLoading ? "—" : (wallet.data?.balance ?? 0).toLocaleString()}
            </p>
          </div>
          <div className="flex gap-8 text-sm">
            <div>
              <p className="text-muted-foreground">Purchased</p>
              <p className="text-lg font-medium">{wallet.data?.totalPurchased ?? 0}</p>
            </div>
            <div>
              <p className="text-muted-foreground">Spent</p>
              <p className="text-lg font-medium">{wallet.data?.totalSpent ?? 0}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <section>
        <h2 className="mb-4 text-lg font-semibold">Token packages</h2>
        {packages.isLoading ? (
          <LoadingState />
        ) : packages.isError ? (
          <ErrorState error={packages.error} onRetry={() => void packages.refetch()} />
        ) : (
          <div className="grid gap-5 md:grid-cols-3">
            {packages.data?.map((p) => (
              <Card
                key={p.id}
                className={p.badge
                  ? "relative border-2 border-primary bg-primary/5 shadow-[var(--shadow-lift)]"
                  : "shadow-[var(--shadow-card)]"}
              >
                {p.badge && <Badge className="absolute -top-3 left-5">{p.badge}</Badge>}
                <CardHeader className={p.badge ? "pt-8" : undefined}>
                  <CardTitle className="text-base font-medium text-muted-foreground">{p.name}</CardTitle>
                  <p className="text-3xl font-semibold">${p.price.toLocaleString()}</p>
                  <CardDescription>{p.tokens.toLocaleString("en-US")} tokens</CardDescription>
                </CardHeader>
                <CardContent className="space-y-5">
                  {p.features.length > 0 && (
                    <ul className="space-y-2 text-sm">
                      {p.features.map((feature) => (
                        <li key={feature} className="flex items-start gap-2">
                          <Check className="mt-0.5 size-4 shrink-0 text-success" />
                          <span>{feature}</span>
                        </li>
                      ))}
                    </ul>
                  )}
                  <Button
                    className="w-full"
                    variant={p.badge ? "default" : "outline"}
                    disabled={purchase.isPending}
                    onClick={() => purchase.mutate(p.id)}
                  >
                    {pending === p.id && <Loader2 className="size-4 animate-spin" />}
                    Buy tokens
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </section>

      <section>
        <h2 className="mb-4 text-lg font-semibold">Transaction history</h2>
        <Card className="shadow-[var(--shadow-card)]">
          <CardContent className="p-0">
            {transactions.isLoading ? (
              <LoadingState />
            ) : transactions.isError ? (
              <ErrorState error={transactions.error} onRetry={() => void transactions.refetch()} />
            ) : !transactions.data?.content.length ? (
              <div className="p-6">
                <EmptyState title="No transactions yet" description="Buy a token package to get started." />
              </div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Type</TableHead>
                      <TableHead>Description</TableHead>
                      <TableHead className="text-right">Amount</TableHead>
                      <TableHead>Date</TableHead>
                      <TableHead className="text-right">Balance after</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {transactions.data.content.map((t) => (
                      <TableRow key={t.id}>
                        <TableCell>
                          <Badge variant={t.amount > 0 ? "secondary" : "outline"}>{t.type}</Badge>
                        </TableCell>
                        <TableCell className="text-muted-foreground">{t.description}</TableCell>
                        <TableCell
                          className={`text-right font-medium ${t.amount > 0 ? "text-success" : "text-destructive"}`}
                        >
                          {t.amount > 0 ? "+" : ""}
                          {t.amount}
                        </TableCell>
                        <TableCell className="text-muted-foreground">{formatDate(t.createdAt)}</TableCell>
                        <TableCell className="text-right">{t.balanceAfter}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
