import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { ArrowRight, Coins, Search as SearchIcon, TrendingDown, TrendingUp,LayoutDashboard } from "lucide-react";
import { CONTACT_FIELD_META } from "@/lib/contact-fields";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { EmptyState, ErrorState, LoadingState } from "@/components/states";
import { api } from "@/lib/api/client";
import { formatDateTime } from "@/lib/format";
import { useWallet } from "./route";

export const Route = createFileRoute("/_authenticated/dashboard")({
  head: () => ({
    meta: [
      { title: "Dashboard — Datasphere" },
      { name: "description", content: "Your token balance and recently unlocked contacts." },
      { property: "og:title", content: "Dashboard — Datasphere" },
      { property: "og:description", content: "Your Datasphere activity at a glance." },
    ],
  }),
  component: DashboardPage,
});

function DashboardPage() {
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState("");
  const wallet = useWallet();
  const unlocks = useQuery({ queryKey: ["history", "unlocks", 0], queryFn: () => api.unlockHistory(0) });
  const searches = useQuery({ queryKey: ["history", "searches", 0], queryFn: () => api.searchHistory(0) });

  return (
    <div className="space-y-8">
      <div>
      
        <h1 className="flex items-center gap-2 text-2xl font-semibold"><LayoutDashboard className="size-5 text-primary" />
           Dashboard</h1>
        <p className="text-sm text-muted-foreground">Track your balance and pick up where you left off.</p>
      </div>

      <div className="grid gap-5 lg:grid-cols-3">
        <Card className="shadow-[var(--shadow-card)]">
          <CardHeader className="pb-2">
            <CardDescription>Token balance</CardDescription>
            <CardTitle className="flex items-center gap-2 text-3xl">
              <Coins className="size-6 text-primary" />
              {wallet.isLoading ? "—" : (wallet.data?.balance ?? 0).toLocaleString()}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex gap-4 text-sm text-muted-foreground">
              <span className="flex items-center gap-1">
                <TrendingUp className="size-4 text-success" /> {wallet.data?.totalPurchased ?? 0} purchased
              </span>
              <span className="flex items-center gap-1">
                <TrendingDown className="size-4 text-destructive" /> {wallet.data?.totalSpent ?? 0} spent
              </span>
            </div>
            <Button asChild size="sm" className="w-full">
              <Link to="/tokens">Buy more tokens</Link>
            </Button>
          </CardContent>
        </Card>

        <Card className="shadow-[var(--shadow-card)] lg:col-span-2">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-base">
              <SearchIcon className="size-4 text-primary" /> Quick search
            </CardTitle>
            <CardDescription>
              Search the{" "}
              <Link to="/search" className="font-medium text-foreground underline-offset-2 hover:underline">
                Company Hub
              </Link>{" "}
              for companies, or the{" "}
              <Link to="/people" className="font-medium text-foreground underline-offset-2 hover:underline">
                People Hub
              </Link>{" "}
              for individual decision-makers.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form
              className="flex flex-col gap-3 sm:flex-row"
              onSubmit={(e) => {
                e.preventDefault();
                void navigate({ to: "/search", search: { keyword, run: true } });
              }}
            >
              <div className="relative flex-1">
                <SearchIcon className="absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  className="pl-9"
                  placeholder="e.g. textiles, Germany, Atlas Trade"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                />
              </div>
              <Button type="submit" className="shrink-0">
                Companies <ArrowRight className="size-4" />
              </Button>
              <Button
                type="button"
                variant="outline"
                className="shrink-0"
                onClick={() => void navigate({ to: "/people", search: { keyword, run: true } })}
              >
                People <ArrowRight className="size-4" />
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-5 lg:grid-cols-2">
        <Card className="shadow-[var(--shadow-card)]">
          <CardHeader className="flex-row items-center justify-between space-y-0">
            <CardTitle className="text-base">Recent unlocks</CardTitle>
            <Button asChild variant="ghost" size="sm">
              <Link to="/history">View all</Link>
            </Button>
          </CardHeader>
          <CardContent>
            {unlocks.isLoading ? (
              <LoadingState />
            ) : unlocks.isError ? (
              <ErrorState error={unlocks.error} onRetry={() => void unlocks.refetch()} />
            ) : !unlocks.data?.content.length ? (
              <EmptyState
                title="No unlocks yet"
                description="Unlock a contact from either hub and it will appear here."
                action={
                  <Button asChild size="sm">
                    <Link to="/search">Open Company Hub</Link>
                  </Button>
                }
              />
            ) : (
              <ul className="divide-y divide-border">
                {unlocks.data.content.slice(0, 5).map((u) => (
                  <li key={u.id} className="flex items-center justify-between gap-3 py-3">
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium">{u.employeeName}</p>
                      <p className="truncate text-xs text-muted-foreground">{u.companyName}</p>
                    </div>
                    <Badge variant="secondary" className="gap-1 shrink-0">
                      {(() => { const Icon = CONTACT_FIELD_META[u.fieldType].icon; return <Icon className="size-3" />; })()}
                      {formatDateTime(u.unlockedAt)}
                    </Badge>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>
        <Card className="shadow-[var(--shadow-card)]">
          <CardHeader className="flex-row items-center justify-between space-y-0">
            <CardTitle className="text-base">Recent searches</CardTitle>
            <Button asChild variant="ghost" size="sm">
              <Link to="/history">View all</Link>
            </Button>
          </CardHeader>
          <CardContent>
            {searches.isLoading ? (
              <LoadingState />
            ) : searches.isError ? (
              <ErrorState error={searches.error} onRetry={() => void searches.refetch()} />
            ) : !searches.data?.content.length ? (
              <EmptyState title="No searches yet" description="Run a company or people search and it will appear here." />
            ) : (
              <ul className="divide-y divide-border">
                {searches.data.content.slice(0, 5).map((search) => (
                  <li key={search.id} className="flex items-center justify-between gap-3 py-3">
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium">{search.query}</p>
                      <p className="truncate text-xs text-muted-foreground">{search.filters}</p>
                    </div>
                    <span className="shrink-0 text-xs text-muted-foreground">
                      {search.resultCount} results
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
