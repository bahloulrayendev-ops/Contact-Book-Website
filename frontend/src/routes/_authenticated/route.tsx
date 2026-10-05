import { createFileRoute, Link, Outlet, useNavigate, useRouterState } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Building2,
  Bell,
  Coins,
  Database,
  History,
  LayoutDashboard,
  Loader2,
  LogOut,
  Menu,
  UserRound,
  Users,
  X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { api } from "@/lib/api/client";
import { useAuth } from "@/lib/auth";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/_authenticated")({
  component: AppLayout,
});

const nav = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { to: "/search", label: "Company Hub", icon: Building2, core: true },
  { to: "/people", label: "People Hub", icon: Users, core: true },
  { to: "/tokens", label: "Tokens", icon: Coins },
  { to: "/history", label: "History", icon: History },
  { to: "/notifications", label: "Notifications", icon: Bell },
  { to: "/account", label: "Account", icon: UserRound },
] as const;

export function useWallet() {
  const { token } = useAuth();
  return useQuery({ queryKey: ["wallet"], queryFn: api.wallet, enabled: !!token });
}

function AppLayout() {
  const { token, loading, logout, user } = useAuth();
  const navigate = useNavigate();
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  const [open, setOpen] = useState(false);
  const wallet = useWallet();
  const unreadNotifications = useQuery({
    queryKey: ["notifications", "unread-count"],
    queryFn: api.unreadNotificationCount,
    enabled: !!token,
    staleTime: 30_000,
    refetchOnWindowFocus: true,
  });

  useEffect(() => {
    if (!loading && !token) void navigate({ to: "/login" });
  }, [loading, token, navigate]);

  useEffect(() => setOpen(false), [pathname]);

  if (loading || !token) {
    return (
      <div className="grid min-h-screen place-items-center bg-surface">
        <Loader2 className="size-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-surface">
      <header className="sticky top-0 z-40 border-b border-border bg-background/90 backdrop-blur lg:hidden">
        <div className="flex h-14 items-center justify-between px-4">
          <Link to="/dashboard" className="flex items-center gap-2 font-semibold">
            <span className="grid size-7 place-items-center rounded-lg bg-primary text-primary-foreground">
              <Database className="size-3.5" />
            </span>
            Datasphere
          </Link>
          <div className="flex items-center gap-2">
            <TokenPill balance={wallet.data?.balance} loading={wallet.isLoading} />
            <Button variant="ghost" size="icon" onClick={() => setOpen((v) => !v)} aria-label="Toggle menu">
              {open ? <X className="size-5" /> : <Menu className="size-5" />}
            </Button>
          </div>
        </div>
      </header>

      <div className="mx-auto flex max-w-[1400px]">
        <aside
          className={cn(
            "fixed inset-x-0 top-14 z-30 border-b border-border bg-background px-4 py-4 lg:sticky lg:top-0 lg:block lg:h-screen lg:w-64 lg:shrink-0 lg:border-r lg:border-b-0 lg:py-6",
            open ? "block" : "hidden",
          )}
        >
          <Link to="/dashboard" className="mb-8 hidden items-center gap-2 px-2 font-semibold lg:flex">
            <span className="grid size-8 place-items-center rounded-lg bg-primary text-primary-foreground">
              <Database className="size-4" />
            </span>
            Datasphere
          </Link>

          <div className="mb-6 hidden rounded-xl border border-border bg-surface p-4 lg:block">
            <p className="text-xs font-medium tracking-wide text-muted-foreground uppercase">Token balance</p>
            <p className="mt-1 flex items-center gap-2 text-2xl font-semibold">
              <Coins className="size-5 text-primary" />
              {wallet.isLoading ? "—" : (wallet.data?.balance ?? 0).toLocaleString()}
            </p>
            <Button asChild size="sm" variant="outline" className="mt-3 w-full">
              <Link to="/tokens">Buy tokens</Link>
            </Button>
          </div>

          <nav className="space-y-1">
            {nav.map((item) => (
              <Link
                key={item.to}
                to={item.to}
                className={cn(
                  "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground",
                  "core" in item && item.core && "font-semibold text-foreground",
                )}
                activeProps={{ className: "bg-accent text-accent-foreground" }}
              >
                <item.icon className={cn("size-4", "core" in item && item.core && "text-primary")} />
                <span className="flex-1">{item.label}</span>
                {item.to === "/notifications" && (unreadNotifications.data ?? 0) > 0 && (
                  <span
                    aria-label={`${unreadNotifications.data} unread notifications`}
                    className="inline-flex min-w-5 items-center justify-center rounded-full bg-primary px-1.5 py-0.5 text-[10px] font-semibold leading-none text-primary-foreground"
                  >
                    {(unreadNotifications.data ?? 0) > 99 ? "99+" : unreadNotifications.data}
                  </span>
                )}
                {"core" in item && item.core && (
                  <span >
            
                  </span>
                )}
              </Link>
            ))}
          </nav>

          <div className="mt-6 border-t border-border pt-4 lg:absolute lg:inset-x-4 lg:bottom-6 lg:mt-0">
            <div className="flex min-w-0 items-center gap-2 px-3">
              <span className="grid size-7 shrink-0 place-items-center rounded-full bg-accent text-accent-foreground">
                <UserRound className="size-4" aria-hidden="true" />
              </span>
              <p className="truncate text-sm font-medium">
                {user?.fullName ?? "Account"}
              </p>
            </div>
            <Button
              variant="ghost"
              className="mt-2 w-full justify-start gap-3 text-muted-foreground"
              onClick={() => {
                logout();
                void navigate({ to: "/" });
              }}
            >
              <LogOut className="size-4" /> Log out
            </Button>
          </div>
        </aside>

        <main className="min-w-0 flex-1 px-4 py-6 sm:px-6 lg:px-10 lg:py-10">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

function TokenPill({ balance, loading }: { balance?: number | undefined; loading: boolean }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border border-border bg-background px-3 py-1 text-sm font-medium">
      <Coins className="size-3.5 text-primary" />
      {loading ? "—" : (balance ?? 0).toLocaleString()}
    </span>
  );
}
